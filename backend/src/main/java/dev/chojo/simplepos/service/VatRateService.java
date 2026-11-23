package dev.chojo.simplepos.service;

import dev.chojo.simplepos.entity.VatRate;
import dev.chojo.simplepos.entity.dto.VatRateRequest;
import dev.chojo.simplepos.exceptions.BadRequestException;
import dev.chojo.simplepos.exceptions.ConflictException;
import dev.chojo.simplepos.exceptions.ErrorCode;
import dev.chojo.simplepos.exceptions.NotFoundException;
import dev.chojo.simplepos.repository.VatRateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Manages the VAT rates. A rate is never deleted, so ingredients and bookings keep pointing at it; it is deactivated
 * instead. The default rate always stays active, so there is always one to fall back to.
 */
@Service
public class VatRateService {
    private final VatRateRepository repository;

    public VatRateService(VatRateRepository repository) {
        this.repository = repository;
    }

    /**
     * Adds an active rate that is not the default. The name is stored without surrounding spaces.
     *
     * @throws ConflictException if a rate with this name exists, in any case
     */
    @Transactional
    public VatRate create(VatRateRequest request) {
        String name = request.name().strip();
        if (repository.existsByNameIgnoreCase(name)) {
            throw new ConflictException(ErrorCode.VAT_RATE_NAME_TAKEN, name);
        }
        return repository.save(new VatRate(null, name, request.rate(), false));
    }

    /**
     * Renames a rate and sets its percentage. Money already booked keeps what it was booked with.
     *
     * @throws NotFoundException if the rate does not exist
     * @throws ConflictException if another rate has this name, in any case
     */
    @Transactional
    public VatRate update(int id, VatRateRequest request) {
        VatRate rate = repository.require(id);
        String name = request.name().strip();
        if (repository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new ConflictException(ErrorCode.VAT_RATE_NAME_TAKEN, name);
        }
        rate.setName(name);
        rate.setRate(request.rate());
        return repository.save(rate);
    }

    /**
     * Makes a rate the default, in place of the current one.
     *
     * @throws NotFoundException   if the rate does not exist
     * @throws BadRequestException if the rate is inactive
     */
    @Transactional
    public VatRate makeDefault(int id) {
        VatRate rate = repository.require(id);
        if (!rate.isActive()) throw new BadRequestException(ErrorCode.VAT_RATE_INACTIVE, rate.getName());
        if (rate.isDefaultRate()) return rate;
        repository.clearDefault();
        VatRate fresh = repository.require(id);
        fresh.setDefaultRate(true);
        return repository.save(fresh);
    }

    /**
     * Switches a rate between active and inactive.
     *
     * @throws NotFoundException if the rate does not exist
     * @throws ConflictException if the rate is the default
     */
    @Transactional
    public VatRate toggleActive(int id) {
        VatRate rate = repository.require(id);
        if (rate.isDefaultRate()) throw new ConflictException(ErrorCode.DEFAULT_VAT_RATE_ACTIVE, rate.getName());
        rate.setActive(!rate.isActive());
        return repository.save(rate);
    }

    /**
     * The rate an ingredient may be given: an active one, or the one it has already, even when that was deactivated
     * since. Null stands for the default rate.
     *
     * @throws BadRequestException if the rate does not exist, or is inactive and not the current one
     */
    public VatRate forIngredient(Integer id, VatRate current) {
        if (id == null) return null;
        VatRate rate = repository.requireReferenced(id);
        if (!rate.isActive() && (current == null || !Objects.equals(current.getId(), rate.getId()))) {
            throw new BadRequestException(ErrorCode.VAT_RATE_INACTIVE, rate.getName());
        }
        return rate;
    }
}

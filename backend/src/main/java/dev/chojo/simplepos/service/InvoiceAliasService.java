package dev.chojo.simplepos.service;

import dev.chojo.simplepos.entity.CountUnit;
import dev.chojo.simplepos.entity.Ingredient;
import dev.chojo.simplepos.entity.InvoiceAlias;
import dev.chojo.simplepos.repository.InvoiceAliasRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Remembers how suppliers name things on invoices and finds those names again. An alias is found by its GTIN when the
 * line has one, else by its text; a line with a GTIN whose text is known without one gives that alias the GTIN, so
 * one article does not end up as two. What the line says replaces what was remembered, so the latest booking wins.
 */
@Service
public class InvoiceAliasService {
    /**
     * The most aliases a search answers with.
     */
    public static final int SEARCH_LIMIT = 10;

    private final InvoiceAliasRepository repository;

    public InvoiceAliasService(InvoiceAliasRepository repository) {
        this.repository = repository;
    }

    /**
     * Remembers an invoice line under its GTIN or text. A line with neither is not remembered.
     *
     * @param ingredient the item's ingredient, null for pledge
     * @param unit       what the item counted, null for pledge
     */
    @Transactional
    public void remember(String text, String gtin, InvoiceAlias.Kind kind, Ingredient ingredient, CountUnit unit,
                         BigDecimal price, boolean net, Instant when) {
        InvoiceAlias named = new InvoiceAlias(text, gtin);
        if (named.getGtin() == null && named.getText() == null) return;
        InvoiceAlias alias = find(named).orElse(named);
        alias.assignGtin(named.getGtin());
        alias.retext(text);
        alias.use(kind, ingredient, unit, price, net, when);
        repository.save(alias);
    }

    private Optional<InvoiceAlias> find(InvoiceAlias named) {
        if (named.getGtin() == null) return repository.findByTextWithoutGtin(named.getText());
        Optional<InvoiceAlias> byGtin = repository.findByGtin(named.getGtin());
        if (byGtin.isPresent() || named.getText() == null) return byGtin;
        return repository.findByTextWithoutGtin(named.getText());
    }

    /**
     * The aliases whose GTIN is the query or whose text starts with it, the GTIN match first, then the most recently
     * used. A blank query finds nothing.
     */
    @Transactional(readOnly = true)
    public List<InvoiceAlias> search(String query) {
        String prefix = InvoiceAlias.normalize(query);
        if (prefix == null) return List.of();
        String escaped = prefix.replace("!", "!!").replace("%", "!%").replace("_", "!_");
        return repository.search(query.strip(), escaped, PageRequest.of(0, SEARCH_LIMIT));
    }
}

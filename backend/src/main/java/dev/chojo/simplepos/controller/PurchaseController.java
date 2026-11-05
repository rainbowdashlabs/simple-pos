package dev.chojo.simplepos.controller;

import dev.chojo.simplepos.configuration.BusinessTimeZone;
import dev.chojo.simplepos.controller.validation.PageNumber;
import dev.chojo.simplepos.controller.validation.PageSize;
import dev.chojo.simplepos.entity.Money;
import dev.chojo.simplepos.entity.User;
import dev.chojo.simplepos.entity.dto.CartPositionDto;
import dev.chojo.simplepos.entity.dto.PurchaseCancelRequest;
import dev.chojo.simplepos.entity.dto.PurchaseReceipt;
import dev.chojo.simplepos.entity.dto.SalesStatDto;
import dev.chojo.simplepos.entity.response.CashResponse;
import dev.chojo.simplepos.repository.ProductRepository;
import dev.chojo.simplepos.repository.PurchaseRepository;
import dev.chojo.simplepos.service.PurchaseService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/purchase")
public class PurchaseController {
    private final BusinessTimeZone businessTimeZone;
    private final PurchaseRepository purchaseRepository;
    private final ProductRepository productRepository;
    private final PurchaseService purchaseService;

    public PurchaseController(PurchaseRepository purchaseRepository, ProductRepository productRepository,
                              PurchaseService purchaseService, BusinessTimeZone businessTimeZone) {
        this.purchaseRepository = purchaseRepository;
        this.productRepository = productRepository;
        this.purchaseService = purchaseService;
        this.businessTimeZone = businessTimeZone;
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable int id) {
        purchaseService.cancel(id, currentUser());
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void cancel(@Valid @RequestBody PurchaseCancelRequest request) {
        purchaseService.cancelAll(request.ids(), currentUser());
    }

    @PostMapping("/guest")
    @ResponseStatus(HttpStatus.CREATED)
    PurchaseReceipt guest(@RequestBody @NotEmpty @Size(max = 100) List<@Valid @NotNull CartPositionDto> positions) {
        return new PurchaseReceipt(purchaseService.purchaseAsGuest(positions, currentUser()));
    }

    private static User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @GetMapping("stats/total")
    ResponseEntity<CashResponse> getTotalPurchases(@RequestParam(required = false) Instant after) {
        BigDecimal total = after == null ? purchaseRepository.totalPurchases() : purchaseRepository.totalPurchases(after);
        return ResponseEntity.ok(new CashResponse(Money.orZero(total)));
    }

    @GetMapping("stats/top")
    ResponseEntity<List<SalesStatDto>> getTopSales(@RequestParam() Instant after,
                                                   @RequestParam(defaultValue = "sales") SalesSorting sorting,
                                                   @RequestParam(defaultValue = "100") @PageSize int limit,
                                                   @RequestParam(defaultValue = "0") @PageNumber int page) {
        var sales = SalesStatDto.build(purchaseRepository.getTopSales(after, PageRequest.of(page, limit, Sort.by(Sort.Direction.DESC, sorting.name()))), productRepository);
        return ResponseEntity.ok(sales);
    }

    @GetMapping("stats/bottom")
    ResponseEntity<List<SalesStatDto>> getBottomSales(@RequestParam() Instant after,
                                                      @RequestParam(defaultValue = "10") @PageSize int limit,
                                                      @RequestParam(defaultValue = "0") @PageNumber int page) {
        var sales = SalesStatDto.build(purchaseRepository.getBottomSales(after, PageRequest.of(page, limit)), productRepository);
        return ResponseEntity.ok(sales);
    }

    @GetMapping("stats/daily")
    ResponseEntity<List<Map<String, Object>>> getDailyStats(@RequestParam() Instant after) {
        return ResponseEntity.ok(purchaseRepository.getDailyStats(after, businessTimeZone.id()).stream().map(t -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("day", BusinessTimeZone.dateOf(t.get("day")));
            m.put("sales", t.get("sales"));
            m.put("revenue", t.get("revenue"));
            m.put("profit", t.get("profit"));
            return m;
        }).toList());
    }

    @GetMapping("stats/categories")
    ResponseEntity<List<Map<String, Object>>> getSalesByCategory(@RequestParam() Instant after) {
        return ResponseEntity.ok(purchaseRepository.getSalesByCategory(after).stream().map(t -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("categoryId", t.get("category_id"));
            m.put("categoryName", t.get("category_name"));
            m.put("sales", t.get("sales"));
            m.put("revenue", t.get("revenue"));
            return m;
        }).toList());
    }

    /**
     * What the top sales can be ordered by. The names are the columns of the sales statistics query.
     */
    enum SalesSorting {
        sales, revenue, profit
    }
}

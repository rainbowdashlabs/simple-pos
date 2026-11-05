package dev.chojo.simplepos.controller;

import dev.chojo.simplepos.configuration.BusinessTimeZone;
import dev.chojo.simplepos.controller.validation.PageNumber;
import dev.chojo.simplepos.controller.validation.PageSize;
import dev.chojo.simplepos.entity.Money;
import dev.chojo.simplepos.entity.Product;
import dev.chojo.simplepos.entity.User;
import dev.chojo.simplepos.entity.dto.CartPositionDto;
import dev.chojo.simplepos.entity.dto.CategoryStat;
import dev.chojo.simplepos.entity.dto.DayStat;
import dev.chojo.simplepos.entity.dto.PurchaseCancelRequest;
import dev.chojo.simplepos.entity.dto.PurchaseReceipt;
import dev.chojo.simplepos.entity.dto.SalesStatDto;
import dev.chojo.simplepos.entity.response.CashResponse;
import dev.chojo.simplepos.entity.response.PageResponse;
import dev.chojo.simplepos.repository.ProductRepository;
import dev.chojo.simplepos.repository.PurchaseRepository;
import dev.chojo.simplepos.repository.projection.ProductSalesRow;
import dev.chojo.simplepos.service.PurchaseService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    ResponseEntity<Void> delete(@PathVariable int id, @CurrentUser User current) {
        purchaseService.cancel(id, current);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void cancel(@Valid @RequestBody PurchaseCancelRequest request, @CurrentUser User current) {
        purchaseService.cancelAll(request.ids(), current);
    }

    @PostMapping("/guest")
    @ResponseStatus(HttpStatus.CREATED)
    PurchaseReceipt guest(@RequestBody @NotEmpty @Size(max = 100) List<@Valid @NotNull CartPositionDto> positions,
                          @CurrentUser User current) {
        return new PurchaseReceipt(purchaseService.purchaseAsGuest(positions, current));
    }

    @GetMapping("stats/total")
    ResponseEntity<CashResponse> getTotalPurchases(@RequestParam(required = false) Instant after) {
        BigDecimal total = after == null ? purchaseRepository.totalPurchases() : purchaseRepository.totalPurchases(after);
        return ResponseEntity.ok(new CashResponse(Money.orZero(total)));
    }

    @GetMapping("stats/top")
    PageResponse<SalesStatDto> getTopSales(@RequestParam() Instant after,
                                           @RequestParam(defaultValue = "sales") SalesSorting sorting,
                                           @RequestParam(defaultValue = "100") @PageSize int limit,
                                           @RequestParam(defaultValue = "0") @PageNumber int page) {
        Sort order = Sort.by(Sort.Direction.DESC, sorting.name()).and(Sort.by("product_id"));
        return withProducts(purchaseRepository.getTopSales(after, PageRequest.of(page, limit, order)));
    }

    @GetMapping("stats/bottom")
    PageResponse<SalesStatDto> getBottomSales(@RequestParam() Instant after,
                                              @RequestParam(defaultValue = "10") @PageSize int limit,
                                              @RequestParam(defaultValue = "0") @PageNumber int page) {
        return withProducts(purchaseRepository.getBottomSales(after, PageRequest.of(page, limit)));
    }

    @GetMapping("stats/summary")
    ResponseEntity<DayStat> getSalesSummary(@RequestParam() Instant after) {
        return ResponseEntity.ok(DayStat.of(purchaseRepository.getSalesSummary(after)));
    }

    @GetMapping("stats/daily")
    ResponseEntity<List<DayStat>> getDailyStats(@RequestParam() Instant after) {
        return ResponseEntity.ok(purchaseRepository.getDailyStats(after, businessTimeZone.id()).stream().map(DayStat::of).toList());
    }

    @GetMapping("stats/categories")
    ResponseEntity<List<CategoryStat>> getSalesByCategory(@RequestParam() Instant after) {
        return ResponseEntity.ok(purchaseRepository.getSalesByCategory(after).stream().map(CategoryStat::of).toList());
    }

    private PageResponse<SalesStatDto> withProducts(Page<ProductSalesRow> rows) {
        Map<Integer, Product> products = productRepository.findAllByIdIn(rows.stream().map(ProductSalesRow::getProductId).toList())
                .stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
        List<SalesStatDto> stats = SalesStatDto.of(rows.getContent(), products);
        return new PageResponse<>(stats, rows.getNumber(), rows.getSize(), rows.getTotalElements(), rows.getTotalPages());
    }

    /**
     * What the top sales can be ordered by. The names are the columns of the sales statistics query.
     */
    enum SalesSorting {
        sales, revenue, profit
    }
}

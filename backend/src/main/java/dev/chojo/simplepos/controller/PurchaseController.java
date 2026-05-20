package dev.chojo.simplepos.controller;

import dev.chojo.simplepos.entity.dto.SalesStatDto;
import dev.chojo.simplepos.entity.response.CashResponse;
import dev.chojo.simplepos.repository.ProductRepository;
import dev.chojo.simplepos.repository.PurchaseRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.persistence.Tuple;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/purchase")
public class PurchaseController {
    private final PurchaseRepository purchaseRepository;
    private final ProductRepository productRepository;

    public PurchaseController(PurchaseRepository purchaseRepository, ProductRepository productRepository) {
        this.purchaseRepository = purchaseRepository;
        this.productRepository = productRepository;
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable int id) {
        if (!purchaseRepository.existsById(id)) return ResponseEntity.notFound().build();
        purchaseRepository.deleteById(id);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("stats/total")
    ResponseEntity<CashResponse> getTotalPurchases(@RequestParam(required = false) Instant after) {
        if (after == null) return ResponseEntity.ok(new CashResponse(purchaseRepository.totalPurchases()));
        return ResponseEntity.ok(new CashResponse(purchaseRepository.totalPurchases(after)));
    }

    @GetMapping("stats/top")
    ResponseEntity<List<SalesStatDto>> getTopSales(@RequestParam() Instant after,
                                                   @RequestParam(defaultValue = "sales") String sorting,
                                                   @RequestParam(defaultValue = "100") int limit,
                                                   @RequestParam(defaultValue = "0") int page) {
        var sales = purchaseRepository.getTopSales(after, PageRequest.of(page, limit, Sort.by(Sort.Direction.DESC, sorting)))
                .stream()
                .map(t -> SalesStatDto.build(t, productRepository))
                .toList();
        return ResponseEntity.ok(sales);
    }

    @GetMapping("stats/daily")
    ResponseEntity<List<Map<String, Object>>> getDailyStats(@RequestParam() Instant after) {
        return ResponseEntity.ok(purchaseRepository.getDailyStats(after).stream().map(t -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("day", t.get("day"));
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
            m.put("categoryName", t.get("category_name"));
            m.put("sales", t.get("sales"));
            m.put("revenue", t.get("revenue"));
            return m;
        }).toList());
    }
}

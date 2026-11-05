package dev.chojo.simplepos.repository;

import dev.chojo.simplepos.entity.Purchase;
import dev.chojo.simplepos.entity.PurchasePart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PurchasePartRepository extends JpaRepository<PurchasePart, Integer> {
    List<PurchasePart> findAllByPurchase(Purchase purchase);

    @Query("SELECT DISTINCT p.ingredient.id FROM PurchasePart p WHERE p.purchase.id = ?1")
    List<Integer> findIngredientIdsOf(int purchaseId);

}

package dev.chojo.simplepos.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class PurchasePart {
    @Id
    @GeneratedValue
    private Integer id;

    @ManyToOne
    private Purchase purchase;

    @ManyToOne
    private Storage storage;

    public PurchasePart(Integer id, Purchase purchase, Storage storage) {
        this.id = id;
        this.purchase = purchase;
        this.storage = storage;
    }

    public PurchasePart() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Purchase getPurchase() {
        return purchase;
    }

    public void setPurchase(Purchase purchase) {
        this.purchase = purchase;
    }

    public Storage getStorage() {
        return storage;
    }

    public void setStorage(Storage storage) {
        this.storage = storage;
    }
}

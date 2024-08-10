package dev.chojo.simplepos.entity;

import dev.chojo.simplepos.entity.dto.PurchaseDto;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

import java.time.Instant;
import java.util.List;

@Entity
public class Purchase {
    @Id
    @GeneratedValue
    private Integer id;

    @ManyToOne
    private Account account;

    @ManyToOne
    private Product product;

    private Instant purchased;

    private int amount;

    private double price;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_id")
    private List<PurchasePart> parts;

    public Purchase(Integer id, Account account, Product product, Instant purchased, int amount, double price, List<PurchasePart> parts) {
        this.id = id;
        this.account = account;
        this.product = product;
        this.purchased = purchased;
        this.amount = amount;
        this.price = price;
        this.parts = parts;
    }

    public Purchase() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Instant getPurchased() {
        return purchased;
    }

    public void setPurchased(Instant purchased) {
        this.purchased = purchased;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public List<PurchasePart> getParts() {
        return parts;
    }

    public void setParts(List<PurchasePart> parts) {
        this.parts = parts;
    }

    public PurchaseDto toDto(){
        return new PurchaseDto(id, product.getId(), purchased, amount, price);
    }
}

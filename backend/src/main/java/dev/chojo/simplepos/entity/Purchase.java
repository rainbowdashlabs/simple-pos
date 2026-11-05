package dev.chojo.simplepos.entity;

import dev.chojo.simplepos.entity.dto.PurchaseDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Entity
public class Purchase {
    @Id
    @GeneratedValue
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;

    private Instant purchased;

    private int amount;

    @Column(precision = 12, scale = Money.SCALE, nullable = false)
    private BigDecimal price;

    @Column(precision = 12, scale = Money.SCALE, nullable = false)
    private BigDecimal cost = Money.ZERO;

    @OneToMany(mappedBy = "purchase", fetch = FetchType.LAZY)
    private List<PurchasePart> parts;

    @ManyToOne(fetch = FetchType.LAZY)
    private User soldBy;

    /**
     * The same with the amounts as {@code double}, for test fixtures and demo data.
     */
    public Purchase(Integer id, Account account, Product product, Instant purchased, int amount, double price, List<PurchasePart> parts) {
        this(id, account, product, purchased, amount, Money.of(price), parts);
    }

    public Purchase(Integer id, Account account, Product product, Instant purchased, int amount, BigDecimal price, List<PurchasePart> parts) {
        this.id = id;
        this.account = account;
        this.product = product;
        this.purchased = purchased;
        this.amount = amount;
        this.price = Money.of(price);
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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = Money.of(price);
    }

    public List<PurchasePart> getParts() {
        return parts;
    }

    public void setParts(List<PurchasePart> parts) {
        this.parts = parts;
    }

    /**
     * What the units this purchase used cost, the sum of its parts. Written when the purchase is booked; the parts
     * never change afterwards.
     */
    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = Money.of(cost);
    }

    public User getSoldBy() {
        return soldBy;
    }

    public void setSoldBy(User soldBy) {
        this.soldBy = soldBy;
    }

    public PurchaseDto toDto(){
        return new PurchaseDto(id, product.getId(), purchased, amount, price, soldBy == null ? null : soldBy.getUsername());
    }
}

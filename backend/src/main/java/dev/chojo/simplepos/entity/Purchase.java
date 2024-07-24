package dev.chojo.simplepos.entity;

import jakarta.persistence.Entity;
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
    private int id;

    @ManyToOne
    private Account account;

    @ManyToOne
    private Product product;

    private Instant purchased;

    private int amount;

    private double price;

    @OneToMany
    @JoinColumn(name = "purchase_id")
    private List<PurchasePart> parts;


}

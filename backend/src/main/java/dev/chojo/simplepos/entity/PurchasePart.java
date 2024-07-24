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
    private int id;

    @ManyToOne
    private Purchase purchase;

    @ManyToOne
    private Storage storage;
}

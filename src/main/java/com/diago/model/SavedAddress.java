package com.diago.model;

import jakarta.persistence.*;

@Entity
public class SavedAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String label;
    private String address;

    @ManyToOne(optional = false)
    private User user;

    protected SavedAddress() {}

    public SavedAddress(User user, String label, String address) {
        this.user = user; this.label = label; this.address = address;
    }

    public Long getId() { return id; }
    public String getLabel() { return label; }
    public String getAddress() { return address; }
    public User getUser() { return user; }
}

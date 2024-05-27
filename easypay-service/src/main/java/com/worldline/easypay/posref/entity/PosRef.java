package com.worldline.easypay.posref.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "pos_ref")
public class PosRef {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "pos_id", unique = true)
    public String posId;

    @Column(name = "location")
    public String location;

    @Column(name = "active")
    public Boolean active;
}

package com.example.fonsion.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;


@Data
@Entity
@Table(name = "DiscountCode", schema = "FONsion")
public class DiscountCode {
    @Id
    @Column(name = "code", nullable = false, length = 30)
    private String code;

    @Column(name = "percent", nullable = false)
    private Integer percent;

    @Column(name = "is_used")
    private Boolean isUsed;

    @Column(name = "is_valid")
    private Boolean isValid;



}
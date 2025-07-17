package com.nevena.fonsion.entities;

import jakarta.persistence.*;
import lombok.Data;


@Data
@Entity
@Table(name = "DiscountCode", schema = "fonsion")
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generated_by_reservation_id")
    private Reservation generatedByReservation;


}
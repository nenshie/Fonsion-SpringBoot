package com.example.fonsion.entities;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "Guest", schema = "FONsion")
public class Guest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    @Column(name = "fullName", length = 30)
    private String fullName;


}
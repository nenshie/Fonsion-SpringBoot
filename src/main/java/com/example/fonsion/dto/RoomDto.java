package com.example.fonsion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomDto implements Serializable {

    private Long id;
    private String name;
    private Integer capacity;
    private String description;
    private BigDecimal pricePerNight;
    private String imageUrl;
}
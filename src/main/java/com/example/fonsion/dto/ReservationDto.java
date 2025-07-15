package com.example.fonsion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationDto implements Serializable {

    private Long id;
    private RoomDto room;
    private String email;
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private BigDecimal totalPrice;
    private String token;
    private DiscountCodeDto discountCode;
    private String status;
    private List<GuestDto> guests;
}
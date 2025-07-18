package com.nevena.fonsion.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationPreviewDto {
    private boolean roomAvailable;
    private BigDecimal totalPrice;
    private String discountCodeStatus;
    private String message;

    private String email;
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private List<String> guests;
    private String discountCode;
}
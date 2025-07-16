package com.nevena.fonsion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationRequestDto {

    private Long roomId;
    private String email;
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private List<String> guests;
    private String discountCode;
}

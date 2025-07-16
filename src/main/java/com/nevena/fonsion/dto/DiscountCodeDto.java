package com.nevena.fonsion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscountCodeDto implements Serializable {

    private String code;
    private Integer percent;
    private Boolean isUsed;
    private Boolean isValid;
}
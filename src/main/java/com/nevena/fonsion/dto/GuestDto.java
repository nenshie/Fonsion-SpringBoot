package com.nevena.fonsion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GuestDto implements Serializable {

    private Long id;
    private String fullName;
}
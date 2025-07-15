package com.example.fonsion.services;


import com.example.fonsion.dto.ReservationDto;
import com.example.fonsion.dto.ReservationRequestDto;

public interface ReservationService {

    ReservationDto makeReservation(ReservationRequestDto request);
    ReservationDto getReservationByToken(String token);
    void cancelReservation(String token);
    ReservationDto getReservationByTokenAndEmail(String token, String email);

}

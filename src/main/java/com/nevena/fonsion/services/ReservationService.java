package com.nevena.fonsion.services;


import com.nevena.fonsion.dto.ReservationDto;
import com.nevena.fonsion.dto.ReservationPreviewDto;
import com.nevena.fonsion.dto.ReservationRequestDto;

public interface ReservationService {

    ReservationDto makeReservation(ReservationRequestDto request);
    ReservationDto getReservationByToken(String token);
    void cancelReservation(String token);
    ReservationDto getReservationByTokenAndEmail(String token, String email);
    ReservationPreviewDto previewReservation(ReservationRequestDto request);

}

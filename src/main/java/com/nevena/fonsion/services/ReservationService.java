package com.nevena.fonsion.services;


import com.nevena.fonsion.dto.ReservationDto;
import com.nevena.fonsion.dto.ReservationPreviewDto;
import com.nevena.fonsion.dto.ReservationRequestDto;

import java.util.List;

public interface ReservationService {

    ReservationDto makeReservation(ReservationRequestDto request);

    void cancelReservation(Long id);

    ReservationPreviewDto previewReservation(ReservationRequestDto request);

    List<ReservationDto> findAllByEmailAndToken(String email, String token);
}

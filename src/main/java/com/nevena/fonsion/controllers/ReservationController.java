package com.nevena.fonsion.controllers;

import com.nevena.fonsion.dto.ReservationDto;
import com.nevena.fonsion.dto.ReservationPreviewDto;
import com.nevena.fonsion.dto.ReservationRequestDto;
import com.nevena.fonsion.services.ReservationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@CrossOrigin(origins = "http://localhost:5173")
@Slf4j
@RestController
@RequestMapping("/reservation")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/create")
    public ResponseEntity<ReservationDto> makeReservation(@RequestBody ReservationRequestDto req) {
        return ResponseEntity.ok(reservationService.makeReservation(req));
    }

    @PostMapping("/preview")
    public ResponseEntity<ReservationPreviewDto> previewReservation(@RequestBody ReservationRequestDto request) {
        ReservationPreviewDto preview = reservationService.previewReservation(request);
        if (!preview.isRoomAvailable()) {
            return ResponseEntity.badRequest().body(preview);
        }
        return ResponseEntity.ok(preview);
    }

    @GetMapping("/verify-access")
    public List<ReservationDto> verifyAccess(@RequestParam String email, @RequestParam String token) {
        return reservationService.findAllByEmailAndToken(email, token);
    }

    @PutMapping("/cancel/{id}")
    public ResponseEntity<Void> cancelReservation(@PathVariable Long id) {
        reservationService.cancelReservation(id);
        return ResponseEntity.noContent().build();
    }





}

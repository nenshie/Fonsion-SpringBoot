package com.nevena.fonsion.controllers;

import com.nevena.fonsion.dto.ReservationDto;
import com.nevena.fonsion.dto.ReservationRequestDto;
import com.nevena.fonsion.services.ReservationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


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

    @GetMapping("/{token}")
    public ResponseEntity<ReservationDto> getReservationDetails(@PathVariable String token) {
        return ResponseEntity.ok(reservationService.getReservationByToken(token));
    }

    @DeleteMapping("/{token}")
    public ResponseEntity<Void> cancelReservation(@PathVariable String token) {
        reservationService.cancelReservation(token);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/by-token-email")
    public ResponseEntity<ReservationDto> getReservationByTokenAndEmail(
            @RequestParam String token,
            @RequestParam String email
    ) {
        return ResponseEntity.ok(reservationService.getReservationByTokenAndEmail(token, email));
    }
}

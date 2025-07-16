package com.nevena.fonsion.services.impl;

import com.nevena.fonsion.dto.*;
import com.nevena.fonsion.entities.DiscountCode;
import com.nevena.fonsion.entities.Guest;
import com.nevena.fonsion.entities.Reservation;
import com.nevena.fonsion.entities.Room;
import com.nevena.fonsion.enums.ReservationStatus;
import com.nevena.fonsion.repositroy.DiscountCodeRepository;
import com.nevena.fonsion.repositroy.ReservationRepository;
import com.nevena.fonsion.repositroy.RoomRepository;
import com.nevena.fonsion.services.ReservationService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;
    private final DiscountCodeRepository discountCodeRepository;

    public ReservationServiceImpl(ReservationRepository reservationRepository, RoomRepository roomRepository, DiscountCodeRepository discountCodeRepository) {
        this.reservationRepository = reservationRepository;
        this.roomRepository = roomRepository;
        this.discountCodeRepository = discountCodeRepository;
    }

    @Override
    @Transactional
    public ReservationDto makeReservation(ReservationRequestDto request) {
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (request.getGuests() == null || request.getGuests().isEmpty()) {
            throw new RuntimeException("At least one guest is required.");
        }

        if (request.getGuests().size() > room.getCapacity()) {
            throw new RuntimeException("Number of guests exceeds room capacity.");
        }

        if (!isRoomAvailable(request.getRoomId(), request.getDateFrom(), request.getDateTo())) {
            throw new RuntimeException("Room is not available for selected dates.");
        }

        Reservation reservation = new Reservation();
        reservation.setRoom(room);
        reservation.setEmail(request.getEmail());
        reservation.setDateFrom(request.getDateFrom());
        reservation.setDateTo(request.getDateTo());
        reservation.setToken(UUID.randomUUID().toString());
        reservation.setStatus(ReservationStatus.ACTIVE);

        Integer discountPercent = 0;

        // Discount code ako postoji
        if (request.getDiscountCode() != null) {
            DiscountCode code = discountCodeRepository.findByCode(request.getDiscountCode())
                    .orElseThrow(() -> new RuntimeException("Promo code not found"));
            reservation.setDiscountCode(code);

            reservation.setDiscountCode(code);
            discountPercent = code.getPercent();
            code.setIsUsed(true);
            code.setIsValid(false);
        }

        // Guests
        List<Guest> guests = request.getGuests().stream()
                .map(name -> {
                    Guest g = new Guest();
                    g.setFullName(name);
                    g.setReservation(reservation);
                    return g;
                }).collect(Collectors.toList());

        reservation.setGuests(guests);

        // Ukupna cena
        long nights = ChronoUnit.DAYS.between(request.getDateFrom(), request.getDateTo());
        BigDecimal basePrice = room.getPricePerNight().multiply(BigDecimal.valueOf(nights));

        if (discountPercent > 0) {
            BigDecimal discount = basePrice.multiply(BigDecimal.valueOf(discountPercent)).divide(BigDecimal.valueOf(100));
            basePrice = basePrice.subtract(discount);
        }

        reservation.setTotalPrice(basePrice);

        // Novi promo kod generiši
        DiscountCode newPromo = generatePromoCode();
        discountCodeRepository.save(newPromo); // ovaj kod možeš dati korisniku kao nagradu

        Reservation saved = reservationRepository.save(reservation);

        return mapToDto(saved);
    }

    private DiscountCode generatePromoCode() {
        int[] options = {5, 10, 15, 20};
        int percent = options[new Random().nextInt(options.length)];

        DiscountCode code = new DiscountCode();
        code.setCode(UUID.randomUUID().toString().substring(0, 8));
        code.setPercent(percent);
        code.setIsValid(true);
        code.setIsUsed(false);
        return code;
    }

    @Override
    public ReservationDto getReservationByToken(String token) {
        Reservation reservation = reservationRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));
        return mapToDto(reservation);
    }

    @Override
    @Transactional
    public void cancelReservation(String token) {
        Reservation reservation = reservationRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        if (reservation.getDateFrom().minusDays(5).isBefore(LocalDate.now())) {
            throw new RuntimeException("Cannot cancel reservation less than 5 days before start date.");
        }

        if (reservation.getDiscountCode() != null) {
            DiscountCode dc = reservation.getDiscountCode();
            dc.setIsValid(false);
        }

        reservation.setStatus(ReservationStatus.CANCELED);
        reservationRepository.save(reservation);
    }

    private ReservationDto mapToDto(Reservation reservation) {
        ReservationDto dto = new ReservationDto();
        dto.setId(reservation.getId());
        dto.setEmail(reservation.getEmail());
        dto.setDateFrom(reservation.getDateFrom());
        dto.setDateTo(reservation.getDateTo());
        dto.setToken(reservation.getToken());
        dto.setStatus(reservation.getStatus().name());
        dto.setTotalPrice(reservation.getTotalPrice());

        Room room = reservation.getRoom();
        RoomDto roomDto = new RoomDto(
                room.getId(), room.getName(), room.getCapacity(),
                room.getDescription(), room.getPricePerNight(), room.getImageUrl()
        );
        dto.setRoom(roomDto);

        if (reservation.getDiscountCode() != null) {
            DiscountCode dc = reservation.getDiscountCode();
            dto.setDiscountCode(new DiscountCodeDto(dc.getCode(), dc.getPercent(), dc.getIsUsed(), dc.getIsValid()));
        }

        List<GuestDto> guestDtos = reservation.getGuests().stream()
                .map(g -> new GuestDto(g.getId(), g.getFullName()))
                .collect(Collectors.toList());

        dto.setGuests(guestDtos);

        return dto;
    }

    public boolean isRoomAvailable(Long roomId, LocalDate dateFrom, LocalDate dateTo) {
        List<Reservation> overlapping = reservationRepository
                .findOverlappingReservations(roomId, dateFrom, dateTo);

        return overlapping.isEmpty();
    }

    @Override
    public ReservationDto getReservationByTokenAndEmail(String token, String email) {
        Reservation reservation = reservationRepository.findByTokenAndEmail(token, email)
                .orElseThrow(() -> new RuntimeException("Reservation not found."));
        return mapToDto(reservation);
    }

}
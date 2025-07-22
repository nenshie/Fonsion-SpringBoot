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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
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
            throw new RuntimeException("Room is not available for the selected dates.");
        }

        Reservation reservation = new Reservation();
        reservation.setRoom(room);
        reservation.setEmail(request.getEmail());
        reservation.setDateFrom(request.getDateFrom());
        reservation.setDateTo(request.getDateTo());
        reservation.setToken(UUID.randomUUID().toString());
        reservation.setStatus(ReservationStatus.ACTIVE);

        // Guests
        List<Guest> guests = request.getGuests().stream()
                .map(name -> {
                    Guest g = new Guest();
                    g.setFullName(name);
                    g.setReservation(reservation);
                    return g;
                }).collect(Collectors.toList());
        reservation.setGuests(guests);

        // Discount code
        int discountPercent = 0;
        if (request.getDiscountCode() != null && !request.getDiscountCode().isBlank()) {
            DiscountCode code = discountCodeRepository.findByCode(request.getDiscountCode())
                    .orElseThrow(() -> new RuntimeException("Promo code not found."));

            if (Boolean.FALSE.equals(code.getIsValid()) || Boolean.TRUE.equals(code.getIsUsed())) {
                throw new RuntimeException("Promo code is not valid or already used.");
            }

            reservation.setDiscountCode(code);
            discountPercent = code.getPercent();

            code.setIsUsed(true);
            code.setIsValid(false);
            discountCodeRepository.save(code);
        }

        // Price calculation
        long nights = ChronoUnit.DAYS.between(request.getDateFrom(), request.getDateTo());
        BigDecimal total = room.getPricePerNight().multiply(BigDecimal.valueOf(nights));

        if (discountPercent > 0) {
            BigDecimal discount = total.multiply(BigDecimal.valueOf(discountPercent)).divide(BigDecimal.valueOf(100));
            total = total.subtract(discount);
        }

        reservation.setTotalPrice(total);

        Reservation saved = reservationRepository.save(reservation);

        DiscountCode newCode = generatePromoCode();
        newCode.setGeneratedByReservation(saved);
        discountCodeRepository.save(newCode);

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
    @Transactional
    public void cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Rezervacija nije pronađena."));

        if (reservation.getStatus() == ReservationStatus.CANCELED) {
            return;
        }

        LocalDate today = LocalDate.now();
        LocalDate startDate = reservation.getDateFrom();

        if (!today.isBefore(startDate.minusDays(5))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rezervaciju je moguće otkazati najkasnije 5 dana pre početnog datuma.");
        }

        reservation.setStatus(ReservationStatus.CANCELED);

        Optional<DiscountCode> generated = discountCodeRepository.findByGeneratedByReservation(reservation);
        generated.ifPresent(code -> {
            code.setIsValid(false);
            discountCodeRepository.save(code);
        });

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
        dto.setRoom(new RoomDto(
                room.getId(),
                room.getName(),
                room.getCapacity(),
                room.getDescription(),
                room.getPricePerNight(),
                room.getImageUrl()
        ));

        if (reservation.getDiscountCode() != null) {
            DiscountCode dc = reservation.getDiscountCode();
            dto.setDiscountCode(new DiscountCodeDto(
                    dc.getCode(),
                    dc.getPercent(),
                    dc.getIsUsed(),
                    dc.getIsValid()
            ));
        }

        List<GuestDto> guestDtos = reservation.getGuests().stream()
                .map(g -> new GuestDto(g.getId(), g.getFullName()))
                .collect(Collectors.toList());
        dto.setGuests(guestDtos);

        discountCodeRepository.findByGeneratedByReservation(reservation)
                .ifPresent(generatedCode -> dto.setGeneratedPromoCode(generatedCode.getCode()));

        return dto;
    }


    public boolean isRoomAvailable(Long roomId, LocalDate dateFrom, LocalDate dateTo) {
        List<Reservation> overlapping = reservationRepository
                .findOverlappingReservations(roomId, dateFrom, dateTo);

        return overlapping.isEmpty();
    }

    @Override
    public ReservationPreviewDto previewReservation(ReservationRequestDto request) {
        ReservationPreviewDto preview = new ReservationPreviewDto();

        Room room = roomRepository.findById(request.getRoomId())
                .orElse(null);

        if (room == null) {
            preview.setRoomAvailable(false);
            preview.setMessage("Room not found.");
            return preview;
        }

        if (request.getGuests() == null || request.getGuests().isEmpty()) {
            preview.setRoomAvailable(false);
            preview.setMessage("Morate uneti makar jednog gosta.");
            return preview;
        }

        if (request.getGuests().size() > room.getCapacity()) {
            preview.setRoomAvailable(false);
            preview.setMessage("Soba ne podržava unet broj gostiju.");
            return preview;
        }

        if (!isRoomAvailable(request.getRoomId(), request.getDateFrom(), request.getDateTo())) {
            preview.setRoomAvailable(false);
            preview.setMessage("Soba nije dostupna za izabrane datume");
            return preview;
        }

        if (request.getDateFrom().isBefore(LocalDate.now())) {
            preview.setRoomAvailable(false);
            preview.setMessage("Datum dolaska ne može biti u prošlosti.");
            return preview;
        }

        if (request.getDateTo().isBefore(request.getDateFrom()) || request.getDateTo().isEqual(request.getDateFrom())) {
            preview.setRoomAvailable(false);
            preview.setMessage("Datum odlaska mora biti posle datuma dolaska.");
            return preview;
        }

        long nights = ChronoUnit.DAYS.between(request.getDateFrom(), request.getDateTo());
        BigDecimal basePrice = room.getPricePerNight().multiply(BigDecimal.valueOf(nights));
        BigDecimal finalPrice = basePrice;
        Integer discountPercent = 0;

        String discountCodeStatus = "NOT_PROVIDED";

        if (request.getDiscountCode() != null && !request.getDiscountCode().isBlank()) {
            Optional<DiscountCode> optionalCode = discountCodeRepository.findByCode(request.getDiscountCode());

            if (optionalCode.isPresent()) {
                DiscountCode code = optionalCode.get();
                if (Boolean.TRUE.equals(code.getIsValid()) && Boolean.FALSE.equals(code.getIsUsed())) {
                    discountPercent = code.getPercent();
                    BigDecimal discount = basePrice.multiply(BigDecimal.valueOf(discountPercent))
                            .divide(BigDecimal.valueOf(100));
                    finalPrice = basePrice.subtract(discount);
                    discountCodeStatus = "VALID";
                } else {
                    discountCodeStatus = "INVALID";
                }
            } else {
                discountCodeStatus = "INVALID";
            }
        }

        preview.setRoomAvailable(true);
        preview.setTotalPrice(finalPrice);
        preview.setDiscountCodeStatus(discountCodeStatus);
        preview.setMessage("Soba je dostupna.Ukupna cena: " + basePrice + " EUR" +
                (discountPercent > 0 ? ". Umanjena cena: "+finalPrice+ ", sa primenjenih "+ discountPercent + "% popusta." : "."));
        preview.setEmail(request.getEmail());
        preview.setDateFrom(request.getDateFrom());
        preview.setDateTo(request.getDateTo());
        preview.setGuests(request.getGuests());
        preview.setDiscountCode(request.getDiscountCode());

        return preview;
    }

    @Override
    public List<ReservationDto> findAllByEmailAndToken(String email, String token) {
        return reservationRepository
                .findAllByEmailAndTokenAndStatus(email, token, ReservationStatus.ACTIVE)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }


}
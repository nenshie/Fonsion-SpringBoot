package com.example.fonsion.repositroy;

import com.example.fonsion.entities.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Optional<Reservation> findByToken(String token);

    @Query("SELECT r FROM Reservation r " +
            "WHERE r.room.id = :roomId " +
            "AND r.status = 'ACTIVE' " +
            "AND r.dateFrom < :dateTo AND r.dateTo > :dateFrom")
    List<Reservation> findOverlappingReservations(
            @Param("roomId") Long roomId,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo);


    Optional<Reservation> findByTokenAndEmail(String token, String email);

}

package com.nevena.fonsion.repositroy;

import com.nevena.fonsion.entities.Reservation;
import com.nevena.fonsion.enums.ReservationStatus;
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


    @Query("""
            SELECT r FROM Reservation r
            LEFT JOIN FETCH r.discountCode
            LEFT JOIN FETCH r.room
            LEFT JOIN FETCH r.guests
            WHERE r.email = :email AND r.token = :token AND r.status = :status
        """)
    List<Reservation> findAllByEmailAndTokenAndStatus(
            @Param("email") String email,
            @Param("token") String token,
            @Param("status") ReservationStatus status
    );


}

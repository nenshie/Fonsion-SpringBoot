package com.nevena.fonsion.repositroy;

import com.nevena.fonsion.entities.DiscountCode;
import com.nevena.fonsion.entities.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DiscountCodeRepository extends JpaRepository<DiscountCode, Long> {

    Optional<DiscountCode> findByCode(String code);

    Optional<DiscountCode> findByGeneratedByReservation(Reservation reservation);

}

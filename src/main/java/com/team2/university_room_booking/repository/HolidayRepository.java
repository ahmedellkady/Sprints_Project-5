package com.team2.university_room_booking.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.team2.university_room_booking.model.Holiday;

import jakarta.transaction.Transactional;

public interface HolidayRepository extends JpaRepository<Holiday, Long> {
    
    @Query("SELECT h FROM Holiday h WHERE h.endDate > :startDate AND h.startDate < :endDate")
    List<Holiday> findOverlappingHolidays(LocalDateTime startDate, LocalDateTime endDate);

    boolean existsByName(String name);

    @Modifying
    @Transactional
    @Query("DELETE FROM Holiday h WHERE h.name = :name")
    void deleteByName(@Param("name") String name);

    Optional<Holiday> findByName(String name);
}

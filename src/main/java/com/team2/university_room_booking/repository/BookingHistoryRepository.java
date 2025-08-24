package com.team2.university_room_booking.repository;

import com.team2.university_room_booking.enums.BookingStatus;
import com.team2.university_room_booking.model.BookingHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingHistoryRepository extends JpaRepository<BookingHistory, Long> {

    @Query("SELECT bh FROM BookingHistory bh WHERE " +
            "(:userId IS NULL OR bh.user.id = :userId) AND " +
            "(:bookingId IS NULL OR bh.booking.id = :bookingId) AND " +
            "(:status IS NULL OR bh.status = :status) AND " +
            "(:dateFrom IS NULL OR bh.timestamp >= :dateFrom) AND " +
            "(:dateTo IS NULL OR bh.timestamp <= :dateTo) " +
            "ORDER BY bh.timestamp DESC")
    List<BookingHistory> findWithFilters(
            @Param("userId") Long userId,
            @Param("bookingId") Long bookingId,
            @Param("status") BookingStatus status,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo);
}

package com.team2.university_room_booking.repository;

import com.team2.university_room_booking.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    boolean existsByRoomId(Long roomId);
}

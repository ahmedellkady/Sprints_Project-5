package com.team2.university_room_booking.repository;

import com.team2.university_room_booking.model.Room;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    boolean existsByNameAndBuildingId(String name, Long id);
}

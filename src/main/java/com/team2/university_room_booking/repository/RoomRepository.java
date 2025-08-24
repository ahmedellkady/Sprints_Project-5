package com.team2.university_room_booking.repository;

import com.team2.university_room_booking.model.Room;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    boolean existsByNameAndBuildingId(String name, Long id);

    Optional<Room> findByName(String roomName);
    @Query("SELECT r FROM Room r LEFT JOIN FETCH r.features WHERE r.name = :name")
    Optional<Room> findByNameWithFeatures(@Param("name") String name);

}

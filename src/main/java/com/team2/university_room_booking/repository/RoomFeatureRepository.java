package com.team2.university_room_booking.repository;

import com.team2.university_room_booking.model.RoomFeature;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomFeatureRepository extends JpaRepository<RoomFeature, Long> {


    @Query("SELECT COUNT(r) FROM Room r JOIN r.features f WHERE f.id = :id")
    int countRoomsWithFeature(Long id);
}

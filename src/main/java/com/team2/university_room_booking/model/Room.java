package com.team2.university_room_booking.model;


import java.util.Set;

import com.team2.university_room_booking.enums.RoomType;

import jakarta.persistence.*;
import lombok.*;

@Data
@AllArgsConstructor
@Entity
@Table(name = "rooms")
public class Room {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private RoomType type;
    private int capacity;
    private boolean available = true;

    @ManyToOne
    @JoinColumn(name = "building_id")
    private Building building;

    @ManyToMany
    @JoinTable(
        name = "room_features",
        joinColumns = @JoinColumn(name = "room_id"),
        inverseJoinColumns = @JoinColumn(name = "feature_id")
    )
    private Set<RoomFeature> features;
}

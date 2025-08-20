package com.team2.university_room_booking.model;

import com.team2.university_room_booking.enums.Role;

import jakarta.persistence.*;
import lombok.*;

@Data
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String username;

    private String email;
    
    private String password;
    
    @Enumerated(EnumType.STRING)
    private Role role;

}

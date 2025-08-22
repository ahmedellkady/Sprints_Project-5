package com.team2.university_room_booking.service;


import com.team2.university_room_booking.dto.request.AvailabilityRequestDto;

public interface AvailabilityService {
    boolean isRoomAvailable(String roomName, AvailabilityRequestDto dto);
}

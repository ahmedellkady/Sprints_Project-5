package com.team2.university_room_booking.service;

import com.team2.university_room_booking.dto.request.RoomRequestDto;
import com.team2.university_room_booking.dto.response.RoomDto;
import com.team2.university_room_booking.exceptions.BadRequestException;
import com.team2.university_room_booking.exceptions.ResourceNotFoundException;
import com.team2.university_room_booking.mapper.DtoMapper;
import com.team2.university_room_booking.model.Building;
import com.team2.university_room_booking.model.Room;
import com.team2.university_room_booking.model.RoomFeature;
import com.team2.university_room_booking.repository.BuildingRepository;
import com.team2.university_room_booking.repository.RoomFeatureRepository;
import com.team2.university_room_booking.repository.RoomRepository;
import com.team2.university_room_booking.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final DtoMapper dtoMapper;
    private final RoomRepository roomRepository;
    private final RoomFeatureRepository roomFeatureRepository;
    private final BuildingRepository buildingRepository;
    private final BookingRepository bookingRepository;

    // create room
    @Transactional
    public RoomDto createRoom(RoomRequestDto roomRequestDto) {
        // Validate building
        Building building = buildingRepository.findById(roomRequestDto.getBuildingId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Building not found with id: " + roomRequestDto.getBuildingId()
                ));

        // Validate room name uniqueness
        if (roomRepository.existsByNameAndBuildingId(roomRequestDto.getName(), building.getId())) {
            throw new BadRequestException("Room with name '" + roomRequestDto.getName() + "' already exists in this building.");
        }
        // Validate features in one go
        Set<RoomFeature> features = fetchAndValidateFeatures(roomRequestDto.getFeatureIds());

        // Map DTO → entity
        Room room = dtoMapper.toRoomEntity(roomRequestDto);
        room.setId(null); // Ensure new entity
        room.setBuilding(building);
        room.setFeatures(features);

        System.out.println("Received featureIds: " + roomRequestDto.getFeatureIds());
        System.out.println("Fetched features: " + features);

        Room savedRoom = roomRepository.save(room);
        // Map entity → DTO
        RoomDto roomDto = dtoMapper.toRoomDto(savedRoom);
        roomDto.setFeaturesFromEntities(room.getFeatures());
        return roomDto;
    }

    // Get by ID
    public RoomDto getRoomById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));
        // Map entity → DTO
        RoomDto roomDto = dtoMapper.toRoomDto(room);
        roomDto.setFeaturesFromEntities(room.getFeatures());
        return roomDto;
    }

    // Get all
    public List<RoomDto> getAllRooms() {
        List<Room> rooms = roomRepository.findAll();
        return rooms.stream()
                .map(room -> {
                    RoomDto roomDto = dtoMapper.toRoomDto(room);
                    roomDto.setFeaturesFromEntities(room.getFeatures());
                    return roomDto;
                })
                .collect(Collectors.toList());
    }

    // delete
    @Transactional
    public void deleteRoom(Long id) {
        if (!roomRepository.existsById(id)) {
            throw new ResourceNotFoundException("Room not found with id: " + id);
        }
        // Prevent deleting booked room
        if (bookingRepository.existsByRoomId(id)) {
            throw new BadRequestException("Cannot delete room with active bookings.");
        }

        roomRepository.deleteById(id);
    }

    // update
    @Transactional
    public RoomDto updateRoom(Long id, RoomRequestDto roomRequestDto) {
        Room existingRoom = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));

        // Update fields
        existingRoom.setName(roomRequestDto.getName());
        existingRoom.setType(roomRequestDto.getType());
        existingRoom.setCapacity(roomRequestDto.getCapacity());
        existingRoom.setAvailable(roomRequestDto.isAvailable());

        // Validate and update building
        var building = buildingRepository.findById(roomRequestDto.getBuildingId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Building not found with id: " + roomRequestDto.getBuildingId()
                ));
        existingRoom.setBuilding(building);

        // Validate and update features
        Set<RoomFeature> features = fetchAndValidateFeatures(roomRequestDto.getFeatureIds());
        existingRoom.setFeatures(features);

        // Save updated room
        Room updatedRoom = roomRepository.save(existingRoom);
        // Map entity → DTO
        RoomDto roomDto = dtoMapper.toRoomDto(updatedRoom);
        roomDto.setFeaturesFromEntities(updatedRoom.getFeatures());
        return roomDto;
    }

    // Helper method to validate & fetch features
    private Set<RoomFeature> fetchAndValidateFeatures(Set<Long> featureIds) {
        if (featureIds == null || featureIds.isEmpty()) return Set.of();

        Set<RoomFeature> features = new HashSet<>(roomFeatureRepository.findAllById(featureIds));
        if (features.size() != featureIds.size()) {
            throw new ResourceNotFoundException("Some room features were not found.");
        }
        return features;
    }

}

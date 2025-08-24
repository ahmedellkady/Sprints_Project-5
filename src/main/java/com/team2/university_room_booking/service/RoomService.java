package com.team2.university_room_booking.service;

import com.team2.university_room_booking.dto.request.AvailabilityRequestDto;
import com.team2.university_room_booking.dto.request.RoomRequestDto;
import com.team2.university_room_booking.dto.response.AvailableRoomTimesDto;
import com.team2.university_room_booking.dto.response.RoomDto;
import com.team2.university_room_booking.enums.BookingStatus;
import com.team2.university_room_booking.exceptions.BadRequestException;
import com.team2.university_room_booking.exceptions.ResourceConflictException;
import com.team2.university_room_booking.exceptions.ResourceNotFoundException;
import com.team2.university_room_booking.mapper.DtoMapper;
import com.team2.university_room_booking.model.Booking;
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

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomService {
    private final DtoMapper dtoMapper;
    private final RoomRepository roomRepository;
    private final RoomFeatureRepository roomFeatureRepository;
    private final BuildingRepository buildingRepository;
    private final BookingRepository bookingRepository;

    // create room
    @Transactional
    public RoomDto createRoom(RoomRequestDto roomRequestDto) {
        log.info("Creating room with name={} in buildingId={}",
                roomRequestDto.getName(), roomRequestDto.getBuildingId());

        Building building = buildingRepository.findById(roomRequestDto.getBuildingId())
                .orElseThrow(() -> {
                    log.error("Building not found with id={}", roomRequestDto.getBuildingId());
                    return new ResourceNotFoundException("Building not found with id: " + roomRequestDto.getBuildingId());
                });

        // Validate room name uniqueness
        if (roomRepository.existsByNameAndBuildingId(roomRequestDto.getName(), building.getId())) {
            log.warn("Room '{}' already exists in building {}", roomRequestDto.getName(), building.getId());
            throw new BadRequestException("Room with name '" + roomRequestDto.getName() + "' already exists in this building.");
        }

        Set<RoomFeature> features = fetchAndValidateFeatures(roomRequestDto.getFeatureIds());

        // Map DTO → entity
        Room room = dtoMapper.toRoomEntity(roomRequestDto);
        room.setId(null);
        room.setBuilding(building);
        room.setFeatures(features);
        Room savedRoom = roomRepository.save(room);
        log.info("Room created successfully with id={}", savedRoom.getId());

        RoomDto roomDto = dtoMapper.toRoomDto(savedRoom);
        roomDto.setFeaturesFromEntities(room.getFeatures());
        return roomDto;
    }

    // Get by ID
    public RoomDto getRoomById(Long id) {
        log.debug("Fetching room with id={}", id);
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Room not found with id={}", id);
                    return new ResourceNotFoundException("Room not found with id: " + id);
                });

        RoomDto roomDto = dtoMapper.toRoomDto(room);
        roomDto.setFeaturesFromEntities(room.getFeatures());
        return roomDto;
    }

    // Get all
    public List<RoomDto> getAllRooms() {
        log.debug("Fetching all rooms");
        return roomRepository.findAll()
                .stream()
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
        log.info("Deleting room with id={}", id);
        if (!roomRepository.existsById(id)) {
            log.error("Room not found with id={}", id);
            throw new ResourceNotFoundException("Room not found with id: " + id);
        }
        // Prevent deleting booked room
        if (bookingRepository.existsByRoomId(id)) {
            log.warn("Attempt to delete room {} failed: active bookings exist", id);
            throw new ResourceConflictException("Cannot delete room with active bookings.");
        }

        roomRepository.deleteById(id);
        log.info("Room deleted successfully with id={}", id);
    }

    // update
    @Transactional
    public RoomDto updateRoom(Long id, RoomRequestDto roomRequestDto) {
        log.info("Updating room with id={}", id);
        Room existingRoom = roomRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Room not found with id={}", id);
                    return new ResourceNotFoundException("Room not found with id: " + id);
                });

        // Update fields
        existingRoom.setName(roomRequestDto.getName());
        existingRoom.setType(roomRequestDto.getType());
        existingRoom.setCapacity(roomRequestDto.getCapacity());
        existingRoom.setAvailable(roomRequestDto.isAvailable());

        Building building = buildingRepository.findById(roomRequestDto.getBuildingId())
                .orElseThrow(() -> {
                    log.error("Building not found with id={}", roomRequestDto.getBuildingId());
                    return new ResourceNotFoundException("Building not found with id: " + roomRequestDto.getBuildingId());
                });
        existingRoom.setBuilding(building);

        // Validate and update features
        Set<RoomFeature> features = fetchAndValidateFeatures(roomRequestDto.getFeatureIds());
        existingRoom.setFeatures(features);

        // Save updated room
        Room updatedRoom = roomRepository.save(existingRoom);
        log.info("Room updated successfully with id={}", updatedRoom.getId());

        RoomDto roomDto = dtoMapper.toRoomDto(updatedRoom);
        roomDto.setFeaturesFromEntities(updatedRoom.getFeatures());
        return roomDto;
    }

    // Helper method to validate & fetch features
    private Set<RoomFeature> fetchAndValidateFeatures(Set<Long> featureIds) {
        log.debug("Validating features: {}", featureIds);
        if (featureIds == null || featureIds.isEmpty()) return Set.of();

        Set<RoomFeature> features = new HashSet<>(roomFeatureRepository.findAllById(featureIds));
        if (features.size() != featureIds.size()) {
            log.error("Some features not found for ids={}", featureIds);
            throw new ResourceNotFoundException("Some room features were not found.");
        }
        return features;
    }

    public List<AvailableRoomTimesDto> getRoomAvailability(Long roomId, AvailabilityRequestDto request) {
        log.info("Checking availability for roomId={} from {} to {}", roomId, request.getStart(), request.getEnd());
        if (request == null || request.getStart() == null || request.getEnd() == null) {
            throw new BadRequestException("Start and end times must be provided");
        }

        LocalDateTime start = request.getStart();
        LocalDateTime end = request.getEnd();

        validateBookingTimes(start, end);

        List<Booking> bookings = bookingRepository.findAllOverlappingBookings(
                roomId,
                List.of(BookingStatus.APPROVED, BookingStatus.PENDING),
                start,
                end
        );

        bookings.sort(Comparator.comparing(Booking::getStartTime));

        List<AvailableRoomTimesDto> freeSlots = new ArrayList<>();
        LocalDateTime current = start;

        for (Booking b : bookings) {
            if (current.isBefore(b.getStartTime())) {
                freeSlots.add(new AvailableRoomTimesDto(current, b.getStartTime()));
            }
            if (b.getEndTime().isAfter(current)) {
                current = b.getEndTime();
            }
        }

        if (current.isBefore(end)) {
            freeSlots.add(new AvailableRoomTimesDto(current, end));
        }

        log.info("Found {} available time slots for roomId={}", freeSlots.size(), roomId);
        return freeSlots;
    }

    private void validateBookingTimes(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            log.error("Start or end time is null: start={} end={}", start, end);
            throw new BadRequestException("Start and end times must not be null");
        }

        if (!end.isAfter(start)) {
            log.error("Invalid booking times: start={} end={}", start, end);
            throw new BadRequestException("End time must be after start time");
        }

        // optional: block past times (policy-based)
        if (start.isBefore(LocalDateTime.now())) {
            log.warn("Booking time starts in the past: start={}", start);
            throw new BadRequestException("Cannot request availability in the past");
        }
    }
}

package com.team2.university_room_booking.service;

import com.team2.university_room_booking.dto.request.CreateBookingRequestDto;
import com.team2.university_room_booking.dto.request.RejectBookingDto;
import com.team2.university_room_booking.dto.response.BookingDto;
import com.team2.university_room_booking.dto.response.TopRecurringRoomDto;
import com.team2.university_room_booking.enums.BookingStatus;
import com.team2.university_room_booking.exceptions.*;
import com.team2.university_room_booking.mapper.DtoMapper;
import com.team2.university_room_booking.model.Booking;
import com.team2.university_room_booking.model.Holiday;
import com.team2.university_room_booking.model.Room;
import com.team2.university_room_booking.model.RoomFeature;
import com.team2.university_room_booking.model.User;
import com.team2.university_room_booking.repository.BookingRepository;
import com.team2.university_room_booking.repository.HolidayRepository;
import com.team2.university_room_booking.repository.RoomRepository;
import com.team2.university_room_booking.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final HolidayRepository holidayRepository;
    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final DtoMapper dtoMapper;

    @Transactional
    public Booking createBooking(CreateBookingRequestDto request) {
        // Always validate against holidays first
        checkForHolidayConflicts(request.getStartTime(), request.getEndTime());

        // Resolve the room based on the provided parameters
        Room room;
        if (request.getRoomId() != null) {
            // Explicit room booking
            room = roomRepository.findById(request.getRoomId())
                    .orElseThrow(() -> new NotFoundException("Room not found"));
            validateRequiredFeatures(request, room);
            checkForBookingConflicts(room.getId(), request.getStartTime(), request.getEndTime());
        } else {
            // No explicit room: find a suitable available room by type and/or features
            room = selectAvailableRoomForRequest(request);
            if (room == null) {
                throw new ResourceNotFoundException("No available rooms match the requested criteria and time period");
            }
            validateRequiredFeatures(request, room);
            checkForBookingConflicts(room.getId(), request.getStartTime(), request.getEndTime());
        }

        User currentUser = resolveCurrentUser();

        Booking booking = new Booking();
        booking.setRoom(room);
        booking.setStartTime(request.getStartTime());
        booking.setEndTime(request.getEndTime());
        booking.setPurpose(request.getPurpose());
        booking.setStatus(BookingStatus.PENDING);
        booking.setUser(currentUser);

        return bookingRepository.save(booking);
    }

    @Transactional
    public List<BookingDto> getAllBookingsByStatus(BookingStatus status) {
        List<Booking> bookings = bookingRepository.findByStatus(status);
        return bookings.stream()
                .map(dtoMapper::toBookingDto)
                .toList();
    }


    @Transactional
    public BookingDto cancelBooking(Long bookingId){
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id " + bookingId));

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        if (!booking.getUser().getUsername().equals(currentUsername)) {
            throw new AccessDeniedException("You don't have permission to cancel this booking");
        }

        LocalDateTime now = LocalDateTime.now();

        //cancellation only allowed before start time
        if (!now.isBefore(booking.getStartTime())) {
            throw new AccessDeniedException("Booking cannot be cancelled after start time");
        }

        //cancellation only allowed if status is PENDING or APPROVED
        if (!(booking.getStatus() == BookingStatus.PENDING || booking.getStatus() == BookingStatus.APPROVED)) {
            throw new AccessDeniedException("Booking cannot be cancelled in status: " + booking.getStatus());
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        //the cancellation action should be recorded in book history.

        return dtoMapper.toBookingDto(booking);
    }

    @Transactional
    public BookingDto rejectBooking(Long bookingId, RejectBookingDto rejectBookingDto){
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BadRequestException("Booking not found with id " + bookingId));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BadRequestException("Only pending bookings can be rejected");
        }

        booking.setStatus(BookingStatus.REJECTED);
        bookingRepository.save(booking);

        //the rejection and rejection message should be logged in booking history.

        return dtoMapper.toBookingDto(booking);
    }

    @Transactional
    public BookingDto approveBooking(Long bookingId){
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id " + bookingId));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BadRequestException("Only PENDING bookings can be approved");
        }

        //check if a holiday have been added after booking request
        checkForHolidayConflicts(booking.getStartTime(), booking.getEndTime());

        booking.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(booking);

        //the acceptance should be logged in booking history

        return dtoMapper.toBookingDto(booking);
    }
    /**
     * Finds the first available room that matches the given criteria and has no booking conflicts within the time range.
     * Criteria considered:
     * - room.available must be true
     * - if request has roomType, room.type must match
     * - if request has requiredFeatureIds, room must contain all those features
     */
    private Room selectAvailableRoomForRequest(CreateBookingRequestDto request) {
        List<Room> allRooms = roomRepository.findAll();

        List<Room> candidates = allRooms.stream()
                .filter(Room::isAvailable)
                .filter(r -> {
                    try {
                        var method = request.getClass().getMethod("getRoomType");
                        Object roomType = method.invoke(request);
                        return roomType == null || roomType.equals(r.getType());
                    } catch (Exception e) {
                        // If request doesn't expose getRoomType(), ignore type filtering
                        return true;
                    }
                })
                .filter(r -> hasRequiredFeatures(request.getRequiredFeatureIds(), r))
                .toList();


        if (candidates.isEmpty()) {
            return null;
        }

        List<BookingStatus> conflictingStatuses = List.of(BookingStatus.PENDING, BookingStatus.APPROVED);
        for (Room candidate : candidates) {
            boolean overlaps = bookingRepository.existsByRoomIdAndStatusInAndStartTimeLessThanAndEndTimeGreaterThan(
                    candidate.getId(),
                    conflictingStatuses,
                    request.getEndTime(),
                    request.getStartTime()
            );
            if (!overlaps) {
                return candidate;
            }
        }
        return null;
    }

    private void checkForHolidayConflicts(LocalDateTime startTime, LocalDateTime endTime) {
        List<Holiday> overlappingHolidays = holidayRepository.findOverlappingHolidays(startTime, endTime);
        if (!overlappingHolidays.isEmpty()) {
            String holidayNames = overlappingHolidays.stream()
                    .map(Holiday::getName)
                    .collect(Collectors.joining(", "));
            throw new ResourceConflictException("Booking falls on a holiday: " + holidayNames);
        }
    }

    private void checkForBookingConflicts(Long roomId, LocalDateTime startTime, LocalDateTime endTime) {
        List<BookingStatus> conflictingStatuses = List.of(BookingStatus.PENDING, BookingStatus.APPROVED);
        boolean overlaps = bookingRepository.existsByRoomIdAndStatusInAndStartTimeLessThanAndEndTimeGreaterThan(
                roomId,
                conflictingStatuses,
                endTime,
                startTime
        );

        if (overlaps) {
            throw new ResourceConflictException("Requested time overlaps with an existing booking");
        }
    }

    private boolean hasRequiredFeatures(Set<Long> requiredFeatureIds, Room room) {
        if (requiredFeatureIds == null || requiredFeatureIds.isEmpty()) {
            return true;
        }
        Set<Long> roomFeatureIds = room.getFeatures() == null
                ? Set.of()
                : room.getFeatures().stream()
                .map(RoomFeature::getId)
                .collect(Collectors.toSet());
        return roomFeatureIds.containsAll(requiredFeatureIds);
    }

    private void validateRequiredFeatures(CreateBookingRequestDto request, Room room) {
        if (request.getRequiredFeatureIds() != null && !request.getRequiredFeatureIds().isEmpty()) {
            if (!hasRequiredFeatures(request.getRequiredFeatureIds(), room)) {
                Set<Long> roomFeatureIds = room.getFeatures() == null
                        ? Set.of()
                        : room.getFeatures().stream()
                        .map(RoomFeature::getId)
                        .collect(Collectors.toSet());

                Set<Long> missingFeatures = new java.util.HashSet<>(request.getRequiredFeatureIds());
                missingFeatures.removeAll(roomFeatureIds);
                throw new BadRequestException("Room is missing required features: " + missingFeatures);
            }
        }
    }


    public List<TopRecurringRoomDto> getTopRecurringRoomsForUser(Long userId, int limit) {
        if (limit <= 0) {
            limit = 3;
        }
        Pageable pageable = PageRequest.of(0, limit);
        List<BookingRepository.RoomBookingCount> rows = bookingRepository.findTopRoomsByUser(userId, pageable);
        List<TopRecurringRoomDto> result = new ArrayList<>(rows.size());
        for (BookingRepository.RoomBookingCount r : rows) {
            result.add(new TopRecurringRoomDto(r.getRoomId(), r.getCount()));
        }
        return result;
    }

    private User resolveCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new InvalidLoginException("You must be logged in");
        }

        Object principal = auth.getPrincipal();

        if (principal instanceof User user) {
            return user;
        }

        String username = null;
        if (principal instanceof UserDetails ud) {
            username = ud.getUsername();
        } else if (principal instanceof String s) {
            username = s;
        }

        if (username == null) {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest req = attrs.getRequest();
                String authHeader = req.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7);
                    try {
                        username = jwtUtil.extractUsername(token);
                    } catch (Exception ignored) {
                        // fall through to error below
                    }
                }
            }
        }

        if (username == null) {
            throw new InvalidLoginException("Unable to resolve current user from authentication");
        }

        UserDetails loaded = userDetailsService.loadUserByUsername(username);
        if (loaded instanceof User u) {
            return u;
        }

        throw new InvalidLoginException("Authenticated principal is not a domain User");
    }
}
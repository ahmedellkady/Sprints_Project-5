package com.team2.university_room_booking.service;

import com.team2.university_room_booking.dto.request.CreateBookingRequestDto;
import com.team2.university_room_booking.dto.response.TopRecurringRoomDto;
import com.team2.university_room_booking.enums.BookingStatus;
import com.team2.university_room_booking.exceptions.BadRequestException;
import com.team2.university_room_booking.exceptions.InvalidLoginException;
import com.team2.university_room_booking.exceptions.NotFoundException;
import com.team2.university_room_booking.exceptions.ResourceConflictException;
import com.team2.university_room_booking.model.*;
import com.team2.university_room_booking.repository.BookingRepository;
import com.team2.university_room_booking.repository.HolidayRepository;
import com.team2.university_room_booking.repository.RoomRepository;
import com.team2.university_room_booking.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    @Transactional
    public Booking createBooking(CreateBookingRequestDto request) {
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new NotFoundException("Room not found"));

        validateRequiredFeatures(request, room);

        checkForHolidayConflicts(request.getStartTime(), request.getEndTime());
        checkForBookingConflicts(room.getId(), request.getStartTime(), request.getEndTime());

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

    private void validateRequiredFeatures(CreateBookingRequestDto request, Room room) {
        if (request.getRequiredFeatureIds() != null && !request.getRequiredFeatureIds().isEmpty()) {
            Set<Long> roomFeatureIds = room.getFeatures() == null
                    ? Set.of()
                    : room.getFeatures().stream()
                    .map(RoomFeature::getId)
                    .collect(Collectors.toSet());

            boolean hasAllFeatures = roomFeatureIds.containsAll(request.getRequiredFeatureIds());
            if (!hasAllFeatures) {
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
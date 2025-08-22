package com.team2.university_room_booking.service;

import com.team2.university_room_booking.dto.request.CreateBookingRequestDto;
import com.team2.university_room_booking.dto.response.TopRecurringRoomDto;
import com.team2.university_room_booking.enums.BookingStatus;
import com.team2.university_room_booking.model.Booking;
import com.team2.university_room_booking.model.Room;
import com.team2.university_room_booking.model.RoomFeature;
import com.team2.university_room_booking.model.User;
import com.team2.university_room_booking.repository.BookingRepository;
import com.team2.university_room_booking.repository.RoomRepository;
import com.team2.university_room_booking.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    @Transactional
    public Booking createBooking(CreateBookingRequestDto request) {
        // Ensure the room exists
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));

        // Validate required features if provided
        if (request.getRequiredFeatureIds() != null && !request.getRequiredFeatureIds().isEmpty()) {
            Set<Long> roomFeatureIds = room.getFeatures() == null
                    ? Set.of()
                    : room.getFeatures().stream()
                    .map(RoomFeature::getId)
                    .collect(Collectors.toSet());

            if (!roomFeatureIds.containsAll(request.getRequiredFeatureIds())) {
                Set<Long> missing = new java.util.HashSet<>(request.getRequiredFeatureIds());
                missing.removeAll(roomFeatureIds);
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Room is missing required features: " + missing
                );
            }
        }

        // Check for overlap with PENDING or APPROVED bookings
        boolean overlaps = bookingRepository.existsByRoomIdAndStatusInAndStartTimeLessThanAndEndTimeGreaterThan(
                room.getId(),
                List.of(BookingStatus.PENDING, BookingStatus.APPROVED),
                request.getEndTime(),
                request.getStartTime()
        );

        if (overlaps) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Requested time overlaps with an existing booking");
        }

        // Resolve the current authenticated user
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

    public List<TopRecurringRoomDto> getTopRecurringRoomsForUser(Long userId, int limit) {
        if (limit <= 0) {
            limit = 3;
        }
        Pageable pageable = PageRequest.of(0, limit);
        List<com.team2.university_room_booking.repository.BookingRepository.RoomBookingCount> rows =
                bookingRepository.findTopRoomsByUser(userId, pageable);
        List<TopRecurringRoomDto> result =
                new ArrayList<>(rows.size());
        for (BookingRepository.RoomBookingCount r : rows) {
            result.add(new TopRecurringRoomDto(r.getRoomId(), r.getCount()));
        }
        return result;
    }

    private User resolveCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You must be logged in");
        }

        Object principal = auth.getPrincipal();

        // 1) If principal IS the domain User, use it directly
        if (principal instanceof User user) {
            return user;
        }

        // 2) Otherwise, try to get a username from the principal
        String username = null;
        if (principal instanceof UserDetails ud) {
            username = ud.getUsername();
        } else if (principal instanceof String s) {
            username = s;
        }

        // 3) As a fallback, read the JWT from the request and extract the username
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
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unable to resolve current user from authentication");
        }

        // 4) Load domain User via user details service (it should return domain User)
        UserDetails loaded = userDetailsService.loadUserByUsername(username);
        if (loaded instanceof User u) {
            return u;
        }

        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated principal is not a domain User");
    }
}
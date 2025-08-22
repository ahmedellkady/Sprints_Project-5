package com.team2.university_room_booking.service;

import com.team2.university_room_booking.dto.request.CreateBookingRequestDto;
import com.team2.university_room_booking.dto.response.TopRecurringRoomDto;
import com.team2.university_room_booking.enums.BookingStatus;
import com.team2.university_room_booking.exceptions.BadRequestException;
import com.team2.university_room_booking.exceptions.NotFoundException;
import com.team2.university_room_booking.exceptions.ResourceConflictException;
import com.team2.university_room_booking.model.Booking;
import com.team2.university_room_booking.model.Holiday;
import com.team2.university_room_booking.model.Room;
import com.team2.university_room_booking.model.User;
import com.team2.university_room_booking.repository.BookingRepository;
import com.team2.university_room_booking.repository.HolidayRepository;
import com.team2.university_room_booking.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private HolidayRepository holidayRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private BookingService bookingService;

    private User testUser;
    private Room testRoom;
    private CreateBookingRequestDto createBookingRequestDto;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        testRoom = new Room();
        testRoom.setId(1L);
        testRoom.setName("Room 101");

        createBookingRequestDto = new CreateBookingRequestDto();
        createBookingRequestDto.setRoomId(1L);
        createBookingRequestDto.setStartTime(LocalDateTime.now().plusHours(1));
        createBookingRequestDto.setEndTime(LocalDateTime.now().plusHours(2));
        createBookingRequestDto.setPurpose("Test Meeting");
    }

    private void setupSecurityContext() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(authentication.isAuthenticated()).thenReturn(true);
    }

    @Test
    void createBooking_Success() {
        setupSecurityContext();
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(holidayRepository.findOverlappingHolidays(any(), any())).thenReturn(Collections.emptyList());
        when(bookingRepository.existsByRoomIdAndStatusInAndStartTimeLessThanAndEndTimeGreaterThan(any(), any(), any(), any())).thenReturn(false);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Booking booking = bookingService.createBooking(createBookingRequestDto);

        assertNotNull(booking);
        assertEquals(testRoom, booking.getRoom());
        assertEquals(testUser, booking.getUser());
        assertEquals(BookingStatus.PENDING, booking.getStatus());
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    void createBooking_RoomNotFound_ThrowsNotFoundException() {
        when(roomRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.createBooking(createBookingRequestDto));
    }

    @Test
    void createBooking_HolidayConflict_ThrowsResourceConflictException() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        Holiday holiday = new Holiday(1L, "Test Holiday", createBookingRequestDto.getStartTime(), createBookingRequestDto.getEndTime());
        when(holidayRepository.findOverlappingHolidays(any(), any())).thenReturn(List.of(holiday));

        assertThrows(ResourceConflictException.class, () -> bookingService.createBooking(createBookingRequestDto));
    }

    @Test
    void createBooking_BookingConflict_ThrowsResourceConflictException() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(holidayRepository.findOverlappingHolidays(any(), any())).thenReturn(Collections.emptyList());
        when(bookingRepository.existsByRoomIdAndStatusInAndStartTimeLessThanAndEndTimeGreaterThan(any(), any(), any(), any())).thenReturn(true);

        assertThrows(ResourceConflictException.class, () -> bookingService.createBooking(createBookingRequestDto));
    }

    @Test
    void createBooking_MissingFeatures_ThrowsBadRequestException() {
        createBookingRequestDto.setRequiredFeatureIds(Collections.singleton(100L));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));

        assertThrows(BadRequestException.class, () -> bookingService.createBooking(createBookingRequestDto));
    }

    @Test
    void getTopRecurringRoomsForUser_Success() {
        Long userId = 1L;
        BookingRepository.RoomBookingCount bookingCount = new BookingRepository.RoomBookingCount() {
            public Long getRoomId() { return 1L; }
            public long getCount() { return 5L; }
        };
        when(bookingRepository.findTopRoomsByUser(eq(userId), any(Pageable.class))).thenReturn(List.of(bookingCount));

        List<TopRecurringRoomDto> result = bookingService.getTopRecurringRoomsForUser(userId, 3);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getRoomId());
        assertEquals(5L, result.get(0).getBookingCount());
    }

    @Test
    void getTopRecurringRoomsForUser_DefaultLimit() {
        Long userId = 1L;
        bookingService.getTopRecurringRoomsForUser(userId, 0);
        verify(bookingRepository).findTopRoomsByUser(eq(userId), eq(PageRequest.of(0, 3)));

        bookingService.getTopRecurringRoomsForUser(userId, -1);
        verify(bookingRepository, times(2)).findTopRoomsByUser(eq(userId), eq(PageRequest.of(0, 3)));
    }
}
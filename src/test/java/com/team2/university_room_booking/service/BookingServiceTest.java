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

    @Mock
    private DtoMapper dtoMapper;

    @InjectMocks
    private BookingService bookingService;

    @Mock
    private BookingHistoryService bookingHistoryService;

    private Booking testBooking;
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

        testBooking = new Booking();
        testBooking.setId(1L);
        testBooking.setUser(testUser);
        testBooking.setRoom(testRoom);
        testBooking.setStartTime(LocalDateTime.now().plusHours(1));
        testBooking.setEndTime(LocalDateTime.now().plusHours(2));
        testBooking.setStatus(BookingStatus.PENDING);
    }

    private void setupSecurityContext() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        lenient().when(authentication.getPrincipal()).thenReturn(testUser);
        lenient().when(authentication.isAuthenticated()).thenReturn(true);
    }

    @Test
    void createBooking_Success() {
        setupSecurityContext();
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(holidayRepository.findOverlappingHolidays(any(), any())).thenReturn(Collections.emptyList());
        when(bookingRepository.existsByRoomIdAndStatusInAndStartTimeLessThanAndEndTimeGreaterThan(any(), any(), any(), any())).thenReturn(false);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        doNothing().when(bookingHistoryService).createAuditEntry(any(), any(), any(), any());

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
        // No need to stub roomRepository; method returns before accessing it
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

    //Cancel Booking
    @Test
    void cancelBooking_Success() {
        setupSecurityContext();

        testBooking.setStatus(BookingStatus.PENDING);

        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(1L);
        bookingDto.setStatus(BookingStatus.CANCELLED);

        when(bookingRepository.findById(testBooking.getId())).thenReturn(Optional.of(testBooking));
        when(authentication.getName()).thenReturn(testUser.getUsername());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(dtoMapper.toBookingDto(any(Booking.class))).thenReturn(bookingDto);

        doNothing().when(bookingHistoryService).createAuditEntry(any(), any(), any(), any());

        BookingDto result = bookingService.cancelBooking(testBooking.getId());

        assertEquals(BookingStatus.CANCELLED, result.getStatus());
        verify(bookingRepository).save(testBooking);
        verify(dtoMapper).toBookingDto(testBooking);
    }

    @Test
    void cancelBooking_BookingNotFound_ThrowsException() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> bookingService.cancelBooking(99L));
    }

    @Test
    void cancelBooking_NotOwner_ThrowsAccessDenied() {
        setupSecurityContext();

        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("otheruser");

        testBooking.setUser(otherUser);

        when(bookingRepository.findById(testBooking.getId())).thenReturn(Optional.of(testBooking));
        when(authentication.getName()).thenReturn(testUser.getUsername());

        assertThrows(AccessDeniedException.class, () -> bookingService.cancelBooking(testBooking.getId()));
    }

    @Test
    void cancelBooking_AfterStart_ThrowsAccessDenied() {
        setupSecurityContext();

        testBooking.setStartTime(LocalDateTime.now().minusHours(1)); // already started

        when(bookingRepository.findById(testBooking.getId())).thenReturn(Optional.of(testBooking));
        when(authentication.getName()).thenReturn(testUser.getUsername());

        assertThrows(AccessDeniedException.class, () -> bookingService.cancelBooking(testBooking.getId()));
    }

    @Test
    void cancelBooking_InvalidStatus_ThrowsAccessDenied() {
        setupSecurityContext();

        testBooking.setStatus(BookingStatus.REJECTED); // not cancellable

        when(bookingRepository.findById(testBooking.getId())).thenReturn(Optional.of(testBooking));
        when(authentication.getName()).thenReturn(testUser.getUsername());

        assertThrows(AccessDeniedException.class, () -> bookingService.cancelBooking(testBooking.getId()));
    }

    //Reject Booking
    @Test
    void rejectBooking_Success() {
        setupSecurityContext();

        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(1L);
        bookingDto.setStatus(BookingStatus.REJECTED);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));
        when(dtoMapper.toBookingDto(any(Booking.class))).thenReturn(bookingDto);

        doNothing().when(bookingHistoryService).createAuditEntry(any(), any(), any(), any());

        var dto = new RejectBookingDto();

        BookingDto result = bookingService.rejectBooking(testBooking.getId(), dto);

        assertEquals(BookingStatus.REJECTED, result.getStatus());
        verify(bookingRepository).save(testBooking);
        verify(dtoMapper).toBookingDto(testBooking);
    }

    @Test
    void rejectBooking_BookingNotFound_ThrowsBadRequest() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                BadRequestException.class,
                () -> bookingService.rejectBooking(99L, new RejectBookingDto())
        );
    }

    @Test
    void rejectBooking_NotPending_ThrowsBadRequest() {
        testBooking.setStatus(BookingStatus.APPROVED);

        when(bookingRepository.findById(testBooking.getId())).thenReturn(Optional.of(testBooking));

        assertThrows(
                BadRequestException.class,
                () -> bookingService.rejectBooking(testBooking.getId(), new RejectBookingDto())
        );
    }

    //Approve Booking
    @Test
    void approveBooking_Success() {
        setupSecurityContext();

        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(1L);
        bookingDto.setStatus(BookingStatus.APPROVED);

        when(bookingRepository.findById(testBooking.getId())).thenReturn(Optional.of(testBooking));
        when(holidayRepository.findOverlappingHolidays(any(), any())).thenReturn(Collections.emptyList());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));
        when(dtoMapper.toBookingDto(any(Booking.class))).thenReturn(bookingDto);

        doNothing().when(bookingHistoryService).createAuditEntry(any(), any(), any(), any());

        BookingDto result = bookingService.approveBooking(testBooking.getId());

        assertEquals(BookingStatus.APPROVED, result.getStatus());
        verify(bookingRepository).save(testBooking);
        verify(dtoMapper).toBookingDto(testBooking);
    }

    @Test
    void approveBooking_BookingNotFound_ThrowsResourceNotFound() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> bookingService.approveBooking(99L)
        );
    }

    @Test
    void approveBooking_NotPending_ThrowsBadRequest() {

        testBooking.setStatus(BookingStatus.CANCELLED);

        when(bookingRepository.findById(testBooking.getId())).thenReturn(Optional.of(testBooking));

        assertThrows(
                BadRequestException.class,
                () -> bookingService.approveBooking(testBooking.getId())
        );
    }

    @Test
    void approveBooking_HolidayConflict_ThrowsResourceConflict() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));
        when(holidayRepository.findOverlappingHolidays(any(), any()))
                .thenReturn(List.of(new Holiday(1L, "Holiday", testBooking.getStartTime(), testBooking.getEndTime())));

        assertThrows(
                ResourceConflictException.class,
                () -> bookingService.approveBooking(testBooking.getId())
        );
    }
}

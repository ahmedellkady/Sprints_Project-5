package com.team2.university_room_booking.service;

import com.team2.university_room_booking.dto.response.BookingHistoryDto;
import com.team2.university_room_booking.enums.BookingStatus;
import com.team2.university_room_booking.mapper.DtoMapper;
import com.team2.university_room_booking.model.Booking;
import com.team2.university_room_booking.model.BookingHistory;
import com.team2.university_room_booking.model.User;
import com.team2.university_room_booking.repository.BookingHistoryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service

public class BookingHistoryService {

    private final BookingHistoryRepository bookingHistoryRepository;
    private final DtoMapper dtoMapper;

    public BookingHistoryService(BookingHistoryRepository bookingHistoryRepository,
                                 DtoMapper dtoMapper) {
        this.bookingHistoryRepository = bookingHistoryRepository;
        this.dtoMapper = dtoMapper;
    }

    // Create audit entry
    public void createAuditEntry(Booking booking, BookingStatus status, User actor, String reason) {
        BookingHistory history = new BookingHistory();
        history.setBooking(booking);
        history.setUser(booking.getUser());
        history.setActor(actor);
        history.setStatus(status);
        history.setTimestamp(LocalDateTime.now());
        history.setReason(reason);

        bookingHistoryRepository.save(history);
    }

    // GET /api/admin/booking-history (with all possible filters)
    public List<BookingHistoryDto> getAuditTrail(Long userId, Long bookingId,
                                                 BookingStatus action, LocalDateTime dateFrom,
                                                 LocalDateTime dateTo) {

        List<BookingHistory> entities = bookingHistoryRepository.findWithFilters(
                userId, bookingId, action, dateFrom, dateTo);

        return entities.stream()
                .map(dtoMapper::tobookingHistoryDto)
                .collect(Collectors.toList());
    }
}

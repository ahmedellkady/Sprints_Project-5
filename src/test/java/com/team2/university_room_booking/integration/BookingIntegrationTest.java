package com.team2.university_room_booking.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team2.university_room_booking.dto.request.CreateBookingRequestDto;
import com.team2.university_room_booking.dto.response.BookingHistoryDto;
import com.team2.university_room_booking.enums.BookingStatus;
import com.team2.university_room_booking.enums.Role;
import com.team2.university_room_booking.mapper.DtoMapper;
import com.team2.university_room_booking.model.*;
import com.team2.university_room_booking.repository.*;
import com.team2.university_room_booking.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BookingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private BuildingRepository buildingRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private RoomFeatureRepository roomFeatureRepository;

    @Autowired
    private DtoMapper dtoMapper;

    @Autowired
    private BookingHistoryRepository bookingHistoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;


    @BeforeEach
    void setup() {
        bookingHistoryRepository.deleteAll();
        bookingRepository.deleteAll();
        roomRepository.deleteAll();
        buildingRepository.deleteAll();
        departmentRepository.deleteAll();
        userRepository.deleteAll();
        roomFeatureRepository.deleteAll();

        Department department = new Department();
        department.setName("Computer Science");
        departmentRepository.save(department);

        Building building = new Building();
        building.setName("Science Building");
        building.setDepartment(department);
        buildingRepository.save(building);

        RoomFeature projector = new RoomFeature();
        projector.setName("Projector");
        RoomFeature whiteboard = new RoomFeature();
        whiteboard.setName("Whiteboard");
        roomFeatureRepository.save(projector);
        roomFeatureRepository.save(whiteboard);
        Set<RoomFeature> features = Set.of(projector, whiteboard);

        Room room = new Room();
        room.setName("Room 101");
        room.setBuilding(building);
        room.setCapacity(60);
        room.setAvailable(true);
        room.setFeatures(features);
        roomRepository.save(room);

        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password");
        user.setEmail("testuser@example.com");
        user.setRole(Role.STUDENT);
        userRepository.save(user);
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"STUDENT"})
    void testCreateBooking_success() throws Exception {
        Room room = roomRepository.findByNameWithFeatures("Room 101").orElseThrow();

        CreateBookingRequestDto request = new CreateBookingRequestDto();
        request.setRoomId(room.getId());
        request.setStartTime(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0));
        request.setEndTime(request.getStartTime().plusHours(2));
        request.setPurpose("Team Meeting");
        request.setRequiredFeatureIds(room.getFeatures().stream().map(RoomFeature::getId).collect(Collectors.toSet()));

        mockMvc.perform(post("/api/bookings")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        //verify booking and audit (book history)
        Booking booking = bookingRepository.findAll().get(0);
        assertEquals(room.getId(), booking.getRoom().getId());
        assertEquals(BookingStatus.PENDING, booking.getStatus());

        List<BookingHistoryDto> audit = bookingHistoryRepository.findAll().stream()
                .map(dtoMapper::tobookingHistoryDto)
                .toList();
        assertEquals(1, audit.size());
        assertEquals(BookingStatus.PENDING, audit.get(0).getStatus());

    }
}

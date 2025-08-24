package com.team2.university_room_booking.mapper;


import com.team2.university_room_booking.dto.request.*;
import com.team2.university_room_booking.dto.response.*;
import com.team2.university_room_booking.model.*;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class DtoMapper {
    private final ModelMapper modelMapper;

    public DtoMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public User toUserEntity(RegisterUserDto dto) {
        return modelMapper.map(dto, User.class);
    }

    public UserDto toUserDto(User user) {
        return modelMapper.map(user, UserDto.class);
    }

    public Room toRoomEntity(RoomRequestDto dto) {
        return modelMapper.map(dto, Room.class);
    }

    public RoomDto toRoomDto(Room room) {
        return modelMapper.map(room, RoomDto.class);
    }

    public Building toBuildingEntity(CreateBuildingDto dto) {
        return modelMapper.map(dto, Building.class);
    }


    public BuildingDto toBuildingDto(Building building) {
        return modelMapper.map(building, BuildingDto.class);
    }

    public Department toDepartmentEntity(CreateDepartmentDto dto) {
        return modelMapper.map(dto, Department.class);
    }

    public DepartmentDto toDepartmentDto(Department department) {
        return modelMapper.map(department, DepartmentDto.class);
    }

    public RoomFeature toRoomFeatureEntity(CreateRoomFeatureDto dto) {
        return modelMapper.map(dto, RoomFeature.class);
    }

    public RoomFeatureDto toRoomFeatureDto(RoomFeature roomFeature) {
        return modelMapper.map(roomFeature, RoomFeatureDto.class);
    }

    public Booking toBookingEntity(BookingDto dto){
        return modelMapper.map(dto, Booking.class);
    }

    public BookingDto toBookingDto(Booking booking){
        return modelMapper.map(booking, BookingDto.class);
    }
    public Holiday toHolidayEntity(HolidayRequestDto dto) {
        return modelMapper.map(dto, Holiday.class);
    }

    public HolidayDto toHolidayDto(Holiday holiday) {
        return modelMapper.map(holiday, HolidayDto.class);}

    public BookingHistory toBookingHistoryEntity(BookingHistoryDto dto){return modelMapper.map(dto,BookingHistory.class);}
    public BookingHistoryDto tobookingHistoryDto(BookingHistory bookingHistory){return modelMapper.map(bookingHistory,BookingHistoryDto.class);}
}
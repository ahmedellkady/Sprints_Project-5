package com.team2.university_room_booking.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.team2.university_room_booking.dto.request.HolidayRequestDto;
import com.team2.university_room_booking.dto.response.HolidayDto;
import com.team2.university_room_booking.exceptions.BadRequestException;
import com.team2.university_room_booking.mapper.DtoMapper;
import com.team2.university_room_booking.model.Holiday;
import com.team2.university_room_booking.repository.HolidayRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HolidayService {

    private final HolidayRepository holidayRepository;
    private final DtoMapper dtoMapper;

    @Transactional
    public HolidayDto createHoliday(HolidayRequestDto dto) {
        if (dto.getStartDate().isAfter(dto.getEndDate())) {
            throw new BadRequestException("End date must be after start date.");
        }

        return dtoMapper.toHolidayDto(holidayRepository.save(dtoMapper.toHolidayEntity(dto)));
    }

    public List<HolidayDto> getAllHolidays() {
        return holidayRepository.findAll().stream()
                .map(dtoMapper::toHolidayDto)
                .collect(Collectors.toList());
    }

    public HolidayDto getHolidayByName(String name) {
        return dtoMapper.toHolidayDto(holidayRepository.findByName(name)
                .orElseThrow(() -> new BadRequestException("Holiday not found.")));
    } 

    public HolidayDto updateHoliday(Long id, HolidayRequestDto dto) {
        Holiday holiday = holidayRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Holiday not found."));

        holiday.setName(dto.getName());
        holiday.setStartDate(dto.getStartDate());
        holiday.setEndDate(dto.getEndDate());

        return dtoMapper.toHolidayDto(holidayRepository.save(holiday));
    }

    @Transactional
    public void deleteHoliday(String name) {
        if (!holidayRepository.existsByName(name)) {
            throw new BadRequestException("Holiday not found.");
        }

        holidayRepository.deleteByName(name);
    }
}

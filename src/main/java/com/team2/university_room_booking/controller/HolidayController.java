package com.team2.university_room_booking.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.team2.university_room_booking.dto.request.HolidayRequestDto;
import com.team2.university_room_booking.dto.response.HolidayDto;
import com.team2.university_room_booking.service.HolidayService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/holidays")
public class HolidayController {
    
    private final HolidayService holidayService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HolidayDto> create(@RequestBody HolidayRequestDto dto) {
        HolidayDto saved = holidayService.createHoliday(dto);

        return ResponseEntity.created(URI.create("/api/holidays/" + saved.getId())).body(saved);
    }
    
    @GetMapping
    public List<HolidayDto> getHolidays() {
        return holidayService.getAllHolidays();
    }

    @GetMapping("/{name}")
    public ResponseEntity<HolidayDto> get(@PathVariable String name) {
        return ResponseEntity.ok(holidayService.getHolidayByName(name));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public HolidayDto update(@PathVariable Long id, @Valid @RequestBody HolidayRequestDto dto) {
        return holidayService.updateHoliday(id, dto);
    }

    @DeleteMapping("/{name}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String name) {
        holidayService.deleteHoliday(name);
        return ResponseEntity.noContent().build();
    }
}

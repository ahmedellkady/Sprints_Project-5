package com.team2.university_room_booking.service;

import com.team2.university_room_booking.dto.request.CreateDepartmentDto;
import com.team2.university_room_booking.dto.response.DepartmentDto;

import java.util.List;


public interface DepartmentService {
    DepartmentDto createDepartment(CreateDepartmentDto dto);
    List<DepartmentDto> getAllDepartments();
    DepartmentDto getDepartmentById(Long id);
    DepartmentDto updateDepartment(Long id, CreateDepartmentDto dto);
    void deleteDepartment(Long id);
}

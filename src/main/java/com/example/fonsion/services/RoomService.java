package com.example.fonsion.services;

import com.example.fonsion.dto.RoomDto;

import java.util.List;

public interface RoomService {

    List<RoomDto> getAllRooms();
    RoomDto getRoomById(Long id);
}

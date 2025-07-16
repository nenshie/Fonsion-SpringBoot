package com.nevena.fonsion.services;

import com.nevena.fonsion.dto.RoomDto;

import java.util.List;

public interface RoomService {

    List<RoomDto> getAllRooms();
    RoomDto getRoomById(Long id);
}

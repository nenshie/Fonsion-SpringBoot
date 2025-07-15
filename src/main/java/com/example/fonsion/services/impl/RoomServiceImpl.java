package com.example.fonsion.services.impl;

import com.example.fonsion.dto.RoomDto;
import com.example.fonsion.entities.Room;
import com.example.fonsion.repositroy.RoomRepository;
import com.example.fonsion.services.RoomService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;

    public RoomServiceImpl(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Override
    public List<RoomDto> getAllRooms() {
        return roomRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public RoomDto getRoomById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        return mapToDto(room);

    }


    private RoomDto mapToDto(Room room) {
        return new RoomDto(
                room.getId(),
                room.getName(),
                room.getCapacity(),
                room.getDescription(),
                room.getPricePerNight(),
                room.getImageUrl()
        );
    }
}

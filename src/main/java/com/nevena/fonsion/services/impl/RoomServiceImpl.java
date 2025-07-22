package com.nevena.fonsion.services.impl;

import com.nevena.fonsion.dto.RoomDto;
import com.nevena.fonsion.entities.Room;
import com.nevena.fonsion.repositroy.RoomRepository;
import com.nevena.fonsion.services.RoomService;
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

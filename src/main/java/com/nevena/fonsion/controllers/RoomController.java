package com.nevena.fonsion.controllers;

import com.nevena.fonsion.dto.RoomDto;
import com.nevena.fonsion.services.RoomService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@Slf4j
@RestController
@RequestMapping("/room")
public class RoomController {

    private final RoomService roomService;


    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping("/get-all")
    public List<RoomDto> getAllRooms() {
        return roomService.getAllRooms();
    }



}

package mariia.wicopt.paymentservice.presentation.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import mariia.wicopt.paymentservice.presentation.dto.response.RoomResponseDto;
import mariia.wicopt.paymentservice.service.RoomService;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;
    
    @GetMapping
    public List<RoomResponseDto> getRooms(@RequestParam String groupId) {
        return roomService.getRooms(groupId);
    }
}
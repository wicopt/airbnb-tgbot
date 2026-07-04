package mariia.sofiia.payment_service.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mariia.sofiia.payment_service.infrastructure.repository.RoomRepository;
import mariia.sofiia.payment_service.presentation.dto.response.RoomResponseDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomService {
private final RoomRepository roomRepository;

    public List<RoomResponseDto> getRooms(String groupId) {
        return roomRepository.findByGroupId(groupId).stream()
                .map(r -> new RoomResponseDto(r.getRoomNumber(), r.getMessageName()))
                .toList();
    }
}

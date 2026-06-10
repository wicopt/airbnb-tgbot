package mariia.sofiia.payment_service.infrastructure.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import mariia.sofiia.payment_service.infrastructure.entities.Room;


public interface RoomRepository extends JpaRepository<Room, String> {

    List<Room> findByGroupId(String groupId);

    Optional<Room> findByRoomNumberAndGroupId(String roomNumber, String groupId);
}
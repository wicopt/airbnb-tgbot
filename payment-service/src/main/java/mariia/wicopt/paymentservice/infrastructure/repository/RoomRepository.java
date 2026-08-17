package mariia.wicopt.paymentservice.infrastructure.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import mariia.wicopt.paymentservice.infrastructure.entities.Room;


public interface RoomRepository extends JpaRepository<Room, String> {

    List<Room> findByGroupId(String groupId);

    Optional<Room> findByRoomNumberAndGroupId(String roomNumber, String groupId);
}
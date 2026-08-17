package mariia.wicopt.authservice.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import mariia.wicopt.authservice.infrastructure.entity.Group;

@Repository
public interface GroupRepository extends JpaRepository<Group, String> {
}
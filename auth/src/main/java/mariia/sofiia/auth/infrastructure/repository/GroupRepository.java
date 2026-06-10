package mariia.sofiia.auth.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import mariia.sofiia.auth.infrastructure.entity.Group;

@Repository
public interface GroupRepository extends JpaRepository<Group, String> {
}
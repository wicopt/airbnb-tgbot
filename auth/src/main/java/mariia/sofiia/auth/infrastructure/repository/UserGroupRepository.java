package mariia.sofiia.auth.infrastructure.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import mariia.sofiia.auth.infrastructure.entity.UserGroup;
import mariia.sofiia.auth.infrastructure.enums.UserRole;

@Repository
public interface UserGroupRepository extends JpaRepository<UserGroup, Long> {

    List<UserGroup> findByGroup_GroupId(String groupId);

    Optional<UserGroup> findByUserIdAndGroup_GroupId(String userId, String groupId);

    List<UserGroup> findByGroup_GroupIdAndRole(String groupId, UserRole role);

    boolean existsByUserIdAndGroup_GroupId(String userId, String groupId);

    void deleteByUserIdAndGroup_GroupId(String userId, String groupId);
    List<UserGroup> findByUserId(String userId);
}
package mariia.wicopt.paymentservice.infrastructure.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import mariia.wicopt.paymentservice.infrastructure.entities.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Все категории доступные для группы (глобальные + свои)
    @Query("""
            SELECT c FROM Category c
            WHERE c.groupId IS NULL OR c.groupId = :groupId
            ORDER BY c.categoryName
            """)
    List<Category> findAvailableForGroup(@Param("groupId") String groupId);

    List<Category> findAllByGroupIdAndIsShared(String groupId, boolean isShared);

    boolean existsByCategoryNameAndGroupId(String categoryName, String groupId);
}
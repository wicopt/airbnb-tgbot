package mariia.wicopt.authservice.infrastructure.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import mariia.wicopt.authservice.infrastructure.entity.InviteCode;

@Repository
public interface InviteCodeRepository extends JpaRepository<InviteCode, String> {

    Optional<InviteCode> findByCode(String code);
}
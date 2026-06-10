package mariia.sofiia.auth.infrastructure.entity;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "invite_codes", schema = "auth")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InviteCode {

    @Id
    @Column(name = "code")
    private String code;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;

    @Column(name = "used_at")
    private OffsetDateTime usedAt;

    @Column(name = "used_by")
    private String usedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;
}
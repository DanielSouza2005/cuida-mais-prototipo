package br.com.cuidaplus.api.admin;

import br.com.cuidaplus.api.profile.CaregiverProfile;
import br.com.cuidaplus.api.profile.CaregiverApprovalStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CaregiverStatusHistoryRepository extends JpaRepository<CaregiverStatusHistory, UUID> {
  List<CaregiverStatusHistory> findByCaregiverOrderByCriadoEmDesc(CaregiverProfile caregiver);
  long countByNewStatusAndCriadoEmGreaterThanEqual(CaregiverApprovalStatus status, Instant since);
}

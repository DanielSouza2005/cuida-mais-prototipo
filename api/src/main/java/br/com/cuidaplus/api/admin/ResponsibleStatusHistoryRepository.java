package br.com.cuidaplus.api.admin;

import br.com.cuidaplus.api.profile.ResponsibleProfile;
import br.com.cuidaplus.api.profile.ResponsibleApprovalStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResponsibleStatusHistoryRepository extends JpaRepository<ResponsibleStatusHistory, UUID> {
  List<ResponsibleStatusHistory> findByResponsibleOrderByCriadoEmDesc(ResponsibleProfile responsible);
  long countByNewStatusAndCriadoEmGreaterThanEqual(ResponsibleApprovalStatus status, Instant since);
}

package br.com.cuidaplus.api.audit;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CriticalActionAuditRepository extends JpaRepository<CriticalActionAudit, UUID>, JpaSpecificationExecutor<CriticalActionAudit> {}


package br.com.cuidaplus.api.account;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountDeletionAuditRepository extends JpaRepository<AccountDeletionAudit, UUID> {}

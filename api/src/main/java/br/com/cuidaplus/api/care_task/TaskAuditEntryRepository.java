package br.com.cuidaplus.api.care_task;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskAuditEntryRepository extends JpaRepository<TaskAuditEntry, UUID> {
  List<TaskAuditEntry> findByTaskOrderByCreatedAtDesc(CareTask task);
}

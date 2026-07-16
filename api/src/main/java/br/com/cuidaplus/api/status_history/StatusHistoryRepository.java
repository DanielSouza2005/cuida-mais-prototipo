package br.com.cuidaplus.api.status_history;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatusHistoryRepository extends JpaRepository<StatusHistory, UUID> {
  List<StatusHistory> findByEntityTypeAndEntityIdOrderByCreatedAtAsc(StatusHistoryEntityType entityType, UUID entityId);
}

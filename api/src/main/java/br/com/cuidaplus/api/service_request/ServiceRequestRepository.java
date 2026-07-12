package br.com.cuidaplus.api.service_request;
import br.com.cuidaplus.api.user.User;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, UUID> {
  List<ServiceRequest> findByResponsibleUserAndCaregiverUserAndAssistedPersonIdAndStatus(User responsible, User caregiver, UUID assistedPersonId, ServiceRequestStatus status);
  List<ServiceRequest> findByResponsibleUserOrderByCreatedAtDesc(User responsible);
  Optional<ServiceRequest> findByIdAndResponsibleUser(UUID id, User responsible);
}

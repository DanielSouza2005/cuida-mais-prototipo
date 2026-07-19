package br.com.cuidaplus.api.service_request;
import br.com.cuidaplus.api.user.User;
import java.util.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, UUID> {
  List<ServiceRequest> findByResponsibleUserAndCaregiverUserAndAssistedPersonIdAndStatus(User responsible, User caregiver, UUID assistedPersonId, ServiceRequestStatus status);
  List<ServiceRequest> findByResponsibleUserOrderByCreatedAtDesc(User responsible);
  List<ServiceRequest> findByResponsibleUserOrderByUpdatedAtDesc(User responsible);
  Optional<ServiceRequest> findByIdAndResponsibleUser(UUID id, User responsible);
  Optional<ServiceRequest> findByIdAndCaregiverUser(UUID id, User caregiver);
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select request from ServiceRequest request where request.id = :id and request.caregiverUser = :caregiver")
  Optional<ServiceRequest> findForUpdateByIdAndCaregiverUser(@Param("id") UUID id, @Param("caregiver") User caregiver);
  List<ServiceRequest> findByCaregiverUserOrderByCreatedAtDesc(User caregiver);
  List<ServiceRequest> findByCaregiverUserOrderByUpdatedAtDesc(User caregiver);
  List<ServiceRequest> findByCaregiverUserAndStatusOrderByCreatedAtDesc(User caregiver, ServiceRequestStatus status);
  Page<ServiceRequest> findByCaregiverUser(User caregiver, Pageable pageable);
  Page<ServiceRequest> findByCaregiverUserAndStatus(User caregiver, ServiceRequestStatus status, Pageable pageable);
}

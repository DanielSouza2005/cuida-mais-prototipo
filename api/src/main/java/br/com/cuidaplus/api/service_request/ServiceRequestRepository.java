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
  boolean existsBySourceOpportunityAndCaregiverUser(ServiceRequest opportunity, User caregiver);
  List<ServiceRequest> findBySourceOpportunityAndCaregiverUser(ServiceRequest opportunity, User caregiver);
  List<ServiceRequest> findBySourceOpportunityOrderByCreatedAtDesc(ServiceRequest opportunity);
  @Query("""
    select request from ServiceRequest request
    where request.responsibleUser = :responsible
      and request.caregiverUser is null
      and request.sourceOpportunity is null
      and request.initiatedBy = br.com.cuidaplus.api.service_request.ServiceRequestInitiator.RESPONSIBLE
      and (:status is null or request.status = :status)
      and (:assistedPersonId is null or request.assistedPerson.id = :assistedPersonId)
      and (:hiringType is null or request.hiringType = :hiringType)
      and (:startDate is null or coalesce(request.endDate, request.startDate) >= :startDate)
      and (:endDate is null or request.startDate <= :endDate)
      and (:city = '' or lower(request.assistedPerson.enderecoCuidado.cidade) like concat('%', :city, '%'))
      and (:neighborhood = '' or lower(request.assistedPerson.enderecoCuidado.bairro) like concat('%', :neighborhood, '%'))
      and (:needs = '' or lower(request.needsDescription) like concat('%', :needs, '%'))
    """)
  Page<ServiceRequest> searchResponsiblePublications(@Param("responsible") User responsible, @Param("status") ServiceRequestStatus status, @Param("assistedPersonId") UUID assistedPersonId, @Param("hiringType") HiringType hiringType, @Param("startDate") java.time.LocalDate startDate, @Param("endDate") java.time.LocalDate endDate, @Param("city") String city, @Param("neighborhood") String neighborhood, @Param("needs") String needs, Pageable pageable);
  Page<ServiceRequest> findByCaregiverUserAndInitiatedBy(User caregiver, ServiceRequestInitiator initiatedBy, Pageable pageable);
  Page<ServiceRequest> findByCaregiverUserAndInitiatedByAndStatus(User caregiver, ServiceRequestInitiator initiatedBy, ServiceRequestStatus status, Pageable pageable);
  @Query("""
    select application from ServiceRequest application
    where application.caregiverUser = :caregiver
      and application.initiatedBy = br.com.cuidaplus.api.service_request.ServiceRequestInitiator.CAREGIVER
      and application.sourceOpportunity is not null
      and (:status is null or application.status = :status)
    """)
  Page<ServiceRequest> searchVisibleApplications(@Param("caregiver") User caregiver, @Param("status") ServiceRequestStatus status, Pageable pageable);
  List<ServiceRequest> findByResponsibleUserAndCaregiverUserIsNotNullOrderByUpdatedAtDesc(User responsible);
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select request from ServiceRequest request where request.id = :id and request.responsibleUser = :responsible")
  Optional<ServiceRequest> findForUpdateByIdAndResponsibleUser(@Param("id") UUID id, @Param("responsible") User responsible);
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select request from ServiceRequest request where request.id = :id and request.status = br.com.cuidaplus.api.service_request.ServiceRequestStatus.ABERTA")
  Optional<ServiceRequest> findOpenOpportunityForUpdate(@Param("id") UUID id);
  @Query("""
    select request from ServiceRequest request
    where request.status = br.com.cuidaplus.api.service_request.ServiceRequestStatus.ABERTA
      and request.caregiverUser is null
      and request.expiresAt > :now
      and (:city = '' or lower(request.assistedPerson.enderecoCuidado.cidade) = :city)
      and (:neighborhood = '' or lower(request.assistedPerson.enderecoCuidado.bairro) = :neighborhood)
      and (:state = '' or lower(request.assistedPerson.enderecoCuidado.estado) = :state)
      and (:hiringType is null or request.hiringType = :hiringType)
    """)
  Page<ServiceRequest> searchOpportunities(@Param("now") java.time.Instant now, @Param("city") String city, @Param("neighborhood") String neighborhood, @Param("state") String state, @Param("hiringType") HiringType hiringType, Pageable pageable);
  @Query("""
    select request from ServiceRequest request
    where request.status = br.com.cuidaplus.api.service_request.ServiceRequestStatus.ABERTA
      and request.caregiverUser is null
      and request.expiresAt > :now
    order by request.createdAt desc
    """)
  List<ServiceRequest> findDiscoverableOpportunities(@Param("now") java.time.Instant now);
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

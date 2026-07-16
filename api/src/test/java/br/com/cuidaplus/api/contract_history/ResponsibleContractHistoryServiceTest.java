package br.com.cuidaplus.api.contract_history;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.com.cuidaplus.api.care_contract.CareContractRepository;
import br.com.cuidaplus.api.common.BusinessException;
import br.com.cuidaplus.api.profile.CaregiverProfileRepository;
import br.com.cuidaplus.api.profile.AssistedPerson;
import br.com.cuidaplus.api.service_request.*;
import br.com.cuidaplus.api.status_history.StatusHistoryService;
import br.com.cuidaplus.api.user.*;
import java.util.*;
import java.time.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class ResponsibleContractHistoryServiceTest {
  @Mock ServiceRequestRepository requests;
  @Mock CareContractRepository contracts;
  @Mock CaregiverProfileRepository caregivers;
  @Mock UserService users;
  @Mock StatusHistoryService history;
  ResponsibleContractHistoryService service;

  @BeforeEach
  void setUp() { service = new ResponsibleContractHistoryService(requests, contracts, caregivers, users, history); }

  @Test
  void caregiverCannotUseResponsibleHistory() {
    UUID userId = UUID.randomUUID(); User caregiver = mock(User.class);
    when(users.findById(userId)).thenReturn(caregiver); when(caregiver.getUserType()).thenReturn(UserType.CUIDADOR);

    BusinessException error = assertThrows(BusinessException.class, () -> service.list(userId, null, null, null, null, null, 0, 10));

    assertEquals(HttpStatus.FORBIDDEN, error.getStatus());
    verifyNoInteractions(requests, contracts, history);
  }

  @Test
  void detailLookupIsScopedToAuthenticatedResponsible() {
    UUID userId = UUID.randomUUID(), itemId = UUID.randomUUID(); User responsible = mock(User.class);
    when(users.findById(userId)).thenReturn(responsible); when(responsible.getUserType()).thenReturn(UserType.RESPONSAVEL);
    when(requests.findByIdAndResponsibleUser(itemId, responsible)).thenReturn(Optional.empty());

    BusinessException error = assertThrows(BusinessException.class, () -> service.details(userId, ContractHistoryItemType.SERVICE_REQUEST, itemId));

    assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
    verify(requests).findByIdAndResponsibleUser(itemId, responsible);
    verify(requests, never()).findById(itemId);
  }

  @Test
  void listCombinesFiltersBeforePagination() {
    UUID userId = UUID.randomUUID(); User responsible = mock(User.class), caregiver = mock(User.class); AssistedPerson assisted = mock(AssistedPerson.class); ServiceRequest request = mock(ServiceRequest.class);
    when(users.findById(userId)).thenReturn(responsible); when(responsible.getUserType()).thenReturn(UserType.RESPONSAVEL);
    when(requests.findByResponsibleUserOrderByUpdatedAtDesc(responsible)).thenReturn(List.of(request));
    when(contracts.findByResponsibleUserOrderByUpdatedAtDesc(responsible)).thenReturn(List.of());
    when(request.getStatus()).thenReturn(ServiceRequestStatus.PENDENTE); when(request.getExpiresAt()).thenReturn(Instant.now().plusSeconds(3600));
    when(request.getId()).thenReturn(UUID.randomUUID()); when(request.getCaregiverUser()).thenReturn(caregiver); when(caregiver.getFullName()).thenReturn("Ana Paula");
    when(request.getAssistedPerson()).thenReturn(assisted); when(assisted.getNome()).thenReturn("Maria"); when(request.getHiringType()).thenReturn(HiringType.PERIODO_DETERMINADO);
    when(request.getStartDate()).thenReturn(LocalDate.of(2026, 7, 20)); when(request.getScheduleDays()).thenReturn(Set.of()); when(request.getUpdatedAt()).thenReturn(Instant.parse("2026-07-15T12:00:00Z"));

    var result = service.list(userId, ContractHistoryStatusGroup.PENDENTES, null, "ana", LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31), 0, 5);

    assertEquals(1, result.totalElements()); assertEquals("Ana Paula", result.content().get(0).participantName()); assertTrue(result.last());
  }
}

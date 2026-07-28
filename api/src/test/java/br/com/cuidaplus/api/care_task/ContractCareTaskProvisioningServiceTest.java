package br.com.cuidaplus.api.care_task;

import static org.mockito.Mockito.*;

import br.com.cuidaplus.api.care_contract.CareContract;
import br.com.cuidaplus.api.care_contract.CareContractRepository;
import br.com.cuidaplus.api.care_contract.CareContractStatus;
import br.com.cuidaplus.api.service_request.ServiceRequest;
import br.com.cuidaplus.api.service_request.ServiceRequestCareItemSnapshot;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ContractCareTaskProvisioningServiceTest {
  @Mock CareTaskRepository tasks;
  @Mock TaskRecurrenceService recurrence;
  @Mock CareContractRepository contracts;
  @Mock TaskOccurrenceRepository occurrences;
  @Mock TaskReminderService reminders;
  @Mock CareContract contract;
  @Mock ServiceRequest request;
  @Mock ServiceRequestCareItemSnapshot source;

  ContractCareTaskProvisioningService service;

  @BeforeEach
  void setUp() {
    service = new ContractCareTaskProvisioningService(tasks, recurrence, contracts, occurrences, reminders);
  }

  @Test
  void repeatedProvisioningAdoptsLegacySeriesOnlyOnce() {
    UUID contractId = UUID.randomUUID();
    UUID sourceId = UUID.randomUUID();
    LocalDate startDate = LocalDate.of(2026, 7, 27);
    LocalTime scheduledTime = LocalTime.of(8, 30);

    when(contract.getId()).thenReturn(contractId);
    when(contract.getStatus()).thenReturn(CareContractStatus.ATIVA);
    when(contract.getStartDate()).thenReturn(startDate);
    when(contract.getServiceRequest()).thenReturn(request);
    when(contracts.findForUpdateById(contractId)).thenReturn(Optional.of(contract));
    when(request.getCareItemsSnapshot()).thenReturn(List.of(source));

    when(source.getId()).thenReturn(sourceId);
    when(source.getTitle()).thenReturn("Medicação da manhã");
    when(source.getCategory()).thenReturn(TaskCategory.MEDICACAO);
    when(source.getRecurrenceType()).thenReturn(TaskRecurrenceType.DIARIA);
    when(source.getScheduledTime()).thenReturn(scheduledTime);
    when(source.getWeekdays()).thenReturn(Set.of());

    CareTask legacy = new CareTask();
    legacy.setContract(contract);
    legacy.setTitle("Medicação da manhã");
    legacy.setCategory(TaskCategory.MEDICACAO);
    legacy.setRecurrenceType(TaskRecurrenceType.DIARIA);
    legacy.setScheduledTime(scheduledTime);
    when(tasks.findByContractOrderByCreatedAtAsc(contract)).thenReturn(List.of(legacy));
    when(tasks.save(legacy)).thenReturn(legacy);

    service.provision(contract);
    service.provision(contract);

    verify(tasks, times(1)).save(legacy);
    verify(recurrence, times(2)).generate(legacy, startDate, startDate.plusDays(TaskRecurrenceService.GENERATION_WINDOW_DAYS - 1L));
  }
}

import type { ContractTerminationContract } from '@/types/contractTermination';

const caregiver = { id: 'caregiver-mariana', name: 'Mariana Costa', locationSummary: 'Vila Mariana, São Paulo - SP' };
const responsible = { id: 'responsible-daniel', name: 'Daniel Oliveira', relationship: 'Filho' };
const assistedPerson = { id: 'assisted-helena', name: 'Helena Oliveira', dependencyLevel: 'Dependência moderada', mobility: 'Caminha com auxílio', allergies: 'Dipirona', foodRestrictions: 'Dieta com pouco sódio', notes: 'Prefere atividades no período da manhã.' };
const careAddress = { cep: '04110001', street: 'Rua Domingos de Morais', number: '845', complement: 'Apto. 72', neighborhood: 'Vila Mariana', city: 'São Paulo', state: 'SP', referencePoint: 'Próximo à estação Ana Rosa' };
const scheduleDays = [
  { weekday: 'SEGUNDA' as const, startTime: '08:00', endTime: '14:00' },
  { weekday: 'QUARTA' as const, startTime: '08:00', endTime: '14:00' },
  { weekday: 'SEXTA' as const, startTime: '08:00', endTime: '14:00' },
];

function base(id: string): Omit<ContractTerminationContract, 'status' | 'statusGroup' | 'hiringType' | 'startDate' | 'endDate'> {
  return {
    id,
    itemType: 'CARE_CONTRACT',
    participantRole: 'RESPONSAVEL',
    participant: caregiver,
    caregiver,
    responsible,
    assistedPerson,
    careAddress,
    specificDates: [],
    scheduleDays,
    scheduleSummary: 'Segundas, quartas e sextas · 08:00 às 14:00',
    activities: ['Companhia', 'Preparo de refeições', 'Acompanhamento em consultas'],
    needsDescription: 'Apoio na rotina, alimentação e deslocamentos fora de casa.',
    additionalNotes: 'Manter os medicamentos organizados conforme orientação da família.',
    createdAt: '2026-06-28T10:15:00',
    updatedAt: '2026-07-20T08:00:00',
    statusHistory: [
      { id: `${id}-created`, label: 'Solicitação criada', newStatus: 'PENDENTE', changedAt: '2026-06-28T10:15:00', changedBy: 'Daniel Oliveira' },
      { id: `${id}-accepted`, label: 'Solicitação aceita', previousStatus: 'PENDENTE', newStatus: 'ACEITA', changedAt: '2026-06-29T16:20:00', changedBy: 'Mariana Costa' },
      { id: `${id}-scheduled`, label: 'Contratação agendada', previousStatus: 'ACEITA', newStatus: 'AGENDADA', changedAt: '2026-06-29T16:20:00', changedBy: 'Sistema Cuidar+' },
    ],
  };
}

export const initialContractsTerminationMock: ContractTerminationContract[] = [
  {
    ...base('rf12-active-fixed'), status: 'ATIVA', statusGroup: 'ATIVAS', hiringType: 'PERIODO_DETERMINADO', startDate: '2026-07-01', endDate: '2026-09-30',
    statusHistory: [...base('rf12-active-fixed').statusHistory, { id: 'fixed-active', label: 'Contratação ativa', previousStatus: 'AGENDADA', newStatus: 'ATIVA', changedAt: '2026-07-01T08:00:00', changedBy: 'Sistema Cuidar+' }],
  },
  {
    ...base('rf12-active-open'), status: 'ATIVA', statusGroup: 'ATIVAS', hiringType: 'PERIODO_INDETERMINADO', participantRole: 'CUIDADOR', startDate: '2026-07-05',
    statusHistory: [...base('rf12-active-open').statusHistory, { id: 'open-active', label: 'Contratação ativa', previousStatus: 'AGENDADA', newStatus: 'ATIVA', changedAt: '2026-07-05T08:00:00', changedBy: 'Sistema Cuidar+' }],
  },
  {
    ...base('rf12-scheduled'), status: 'AGENDADA', statusGroup: 'AGENDADAS', hiringType: 'PERIODO_DETERMINADO', startDate: '2026-08-03', endDate: '2026-10-30', updatedAt: '2026-07-12T11:00:00',
  },
  {
    ...base('rf12-termination-scheduled'), status: 'ENCERRAMENTO_AGENDADO', statusGroup: 'ATIVAS', hiringType: 'PERIODO_DETERMINADO', startDate: '2026-06-02', endDate: '2026-09-30', effectiveEndDate: '2026-07-30', terminationRequestedAt: '2026-07-15T14:30:00', terminationRequestedBy: 'Daniel Oliveira', terminationType: 'ANTECIPADO_RESPONSAVEL', terminationReason: 'Mudança na rotina da pessoa assistida.', closureReason: 'Mudança na rotina da pessoa assistida.', updatedAt: '2026-07-15T14:30:00',
    statusHistory: [...base('rf12-termination-scheduled').statusHistory, { id: 'term-active', label: 'Contratação ativa', previousStatus: 'AGENDADA', newStatus: 'ATIVA', changedAt: '2026-06-02T08:00:00', changedBy: 'Sistema Cuidar+' }, { id: 'term-request', label: 'Encerramento solicitado', previousStatus: 'ATIVA', newStatus: 'ATIVA', reason: 'Mudança na rotina da pessoa assistida.', changedAt: '2026-07-15T14:30:00', changedBy: 'Daniel Oliveira' }, { id: 'term-scheduled', label: 'Encerramento agendado para 30/07/2026', previousStatus: 'ATIVA', newStatus: 'ENCERRAMENTO_AGENDADO', changedAt: '2026-07-15T14:30:00', changedBy: 'Daniel Oliveira' }],
  },
  {
    ...base('rf12-ended'), status: 'ENCERRADA', statusGroup: 'ENCERRADAS', hiringType: 'PERIODO_DETERMINADO', startDate: '2026-04-06', endDate: '2026-06-30', effectiveEndDate: '2026-06-30', terminationRequestedAt: '2026-06-30T14:05:00', terminationRequestedBy: 'Sistema Cuidar+', terminationType: 'AUTOMATICO_TERMINO_PERIODO', terminationReason: 'Término do período contratado.', closureReason: 'Término do período contratado.', updatedAt: '2026-06-30T14:05:00',
    statusHistory: [...base('rf12-ended').statusHistory, { id: 'ended-active', label: 'Contratação ativa', previousStatus: 'AGENDADA', newStatus: 'ATIVA', changedAt: '2026-04-06T08:00:00', changedBy: 'Sistema Cuidar+' }, { id: 'ended-final', label: 'Encerramento automático ao término do período', previousStatus: 'ATIVA', newStatus: 'ENCERRADA', reason: 'Término do período contratado.', changedAt: '2026-06-30T14:05:00', changedBy: 'Sistema Cuidar+' }],
  },
  {
    ...base('rf12-cancelled'), status: 'CANCELADA', statusGroup: 'CANCELADAS', hiringType: 'PERIODO_DETERMINADO', startDate: '2026-08-10', endDate: '2026-09-25', effectiveEndDate: '2026-07-14', terminationRequestedAt: '2026-07-14T09:40:00', terminationRequestedBy: 'Daniel Oliveira', terminationType: 'CANCELAMENTO_ANTES_INICIO', terminationReason: 'A família reorganizou os cuidados antes do início.', cancellationReason: 'A família reorganizou os cuidados antes do início.', updatedAt: '2026-07-14T09:40:00',
    statusHistory: [...base('rf12-cancelled').statusHistory, { id: 'cancelled-final', label: 'Contratação cancelada', previousStatus: 'AGENDADA', newStatus: 'CANCELADA', reason: 'A família reorganizou os cuidados antes do início.', changedAt: '2026-07-14T09:40:00', changedBy: 'Daniel Oliveira' }],
  },
];

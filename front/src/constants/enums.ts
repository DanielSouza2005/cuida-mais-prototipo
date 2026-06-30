export type Option<T extends string> = {
  value: T;
  label: string;
};

export const caregiverEducationOptions = [
  { value: 'CURSO_CUIDADOR_IDOSOS', label: 'Curso de cuidador de idosos' },
  { value: 'TECNICO_ENFERMAGEM', label: 'Tecnico em enfermagem' },
  { value: 'AUXILIAR_ENFERMAGEM', label: 'Auxiliar de enfermagem' },
  { value: 'ENFERMAGEM_SUPERIOR', label: 'Ensino superior em enfermagem' },
  { value: 'PRIMEIROS_SOCORROS', label: 'Curso de primeiros socorros' },
  { value: 'EXPERIENCIA_PRATICA', label: 'Experiencia pratica sem formacao formal' },
  { value: 'OUTRO', label: 'Outro' },
] as const satisfies readonly Option<string>[];

export const careModalityOptions = [
  { value: 'DIURNO', label: 'Diurno' },
  { value: 'NOTURNO', label: 'Noturno' },
  { value: 'PLANTAO_12X36', label: 'Plantao 12x36' },
  { value: 'PLANTAO_24H', label: 'Plantao 24h' },
  { value: 'EVENTUAL', label: 'Eventual' },
  { value: 'FINAIS_DE_SEMANA', label: 'Finais de semana' },
  { value: 'ACOMPANHAMENTO_CONSULTAS', label: 'Acompanhamento em consultas' },
  { value: 'OUTRO', label: 'Outro' },
] as const satisfies readonly Option<string>[];

export const caregiverServiceOptions = [
  { value: 'HIGIENE_PESSOAL', label: 'Auxilio com higiene pessoal' },
  { value: 'BANHO', label: 'Auxilio no banho' },
  { value: 'ALIMENTACAO', label: 'Auxilio na alimentacao' },
  { value: 'LOCOMOCAO', label: 'Auxilio na locomocao' },
  { value: 'COMPANHIA', label: 'Companhia e acompanhamento' },
  { value: 'MEDICACAO_ORIENTADA', label: 'Apoio a rotina de medicacao conforme orientacao' },
  { value: 'CONSULTAS', label: 'Acompanhamento em consultas' },
  { value: 'ATIVIDADES_DOMESTICAS_LEVES', label: 'Atividades domesticas leves relacionadas ao cuidado' },
  { value: 'MONITORAMENTO_NOTURNO', label: 'Monitoramento noturno' },
  { value: 'OUTRO', label: 'Outro' },
] as const satisfies readonly Option<string>[];

export const relationshipOptions = [
  { value: 'FILHO', label: 'Filho(a)' },
  { value: 'CONJUGE', label: 'Conjuge' },
  { value: 'NETO', label: 'Neto(a)' },
  { value: 'IRMAO', label: 'Irmao/Irma' },
  { value: 'SOBRINHO', label: 'Sobrinho(a)' },
  { value: 'TUTOR_LEGAL', label: 'Tutor legal' },
  { value: 'RESPONSAVEL_CONTRATUAL', label: 'Responsavel contratual' },
  { value: 'AMIGO', label: 'Amigo(a)' },
  { value: 'OUTRO', label: 'Outro' },
] as const satisfies readonly Option<string>[];

export const contactPreferenceOptions = [
  { value: 'WHATSAPP', label: 'WhatsApp' },
  { value: 'LIGACAO', label: 'Ligacao' },
  { value: 'EMAIL', label: 'E-mail' },
  { value: 'SMS', label: 'SMS' },
  { value: 'QUALQUER', label: 'Qualquer opcao' },
] as const satisfies readonly Option<string>[];

export const dependencyLevelOptions = [
  { value: 'BAIXA', label: 'Baixa dependencia' },
  { value: 'MODERADA', label: 'Dependencia moderada' },
  { value: 'ALTA', label: 'Alta dependencia' },
  { value: 'TOTAL', label: 'Dependencia total' },
  { value: 'NAO_SEI_INFORMAR', label: 'Nao sei informar' },
] as const satisfies readonly Option<string>[];

export const mobilityOptions = [
  { value: 'INDEPENDENTE', label: 'Caminha sem ajuda' },
  { value: 'BENGALA', label: 'Usa bengala' },
  { value: 'ANDADOR', label: 'Usa andador' },
  { value: 'CADEIRA_RODAS', label: 'Usa cadeira de rodas' },
  { value: 'ACAMADO', label: 'Acamado' },
  { value: 'AUXILIO_PESSOA', label: 'Precisa de ajuda de outra pessoa' },
  { value: 'OUTRO', label: 'Outro' },
] as const satisfies readonly Option<string>[];

export const allergyOptions = [
  { value: 'NAO_POSSUI', label: 'Nao possui' },
  { value: 'MEDICAMENTOS', label: 'Medicamentos' },
  { value: 'ALIMENTOS', label: 'Alimentos' },
  { value: 'PRODUTOS_HIGIENE', label: 'Produtos de higiene' },
  { value: 'LATEX', label: 'Latex' },
  { value: 'POEIRA', label: 'Poeira' },
  { value: 'OUTRO', label: 'Outro' },
  { value: 'NAO_SEI_INFORMAR', label: 'Nao sei informar' },
] as const satisfies readonly Option<string>[];

export const foodRestrictionOptions = [
  { value: 'NAO_POSSUI', label: 'Nao possui' },
  { value: 'DIABETICA', label: 'Dieta para diabetes' },
  { value: 'HIPOSSODICA', label: 'Baixo consumo de sal' },
  { value: 'PASTOSA', label: 'Alimentacao pastosa' },
  { value: 'LIQUIDA', label: 'Alimentacao liquida' },
  { value: 'SEM_LACTOSE', label: 'Sem lactose' },
  { value: 'SEM_GLUTEN', label: 'Sem gluten' },
  { value: 'VEGETARIANA', label: 'Vegetariana' },
  { value: 'OUTRO', label: 'Outro' },
  { value: 'NAO_SEI_INFORMAR', label: 'Nao sei informar' },
] as const satisfies readonly Option<string>[];

export const weekDayOptions = [
  { value: 'SEGUNDA', label: 'Segunda' },
  { value: 'TERCA', label: 'Terca' },
  { value: 'QUARTA', label: 'Quarta' },
  { value: 'QUINTA', label: 'Quinta' },
  { value: 'SEXTA', label: 'Sexta' },
  { value: 'SABADO', label: 'Sabado' },
  { value: 'DOMINGO', label: 'Domingo' },
] as const satisfies readonly Option<string>[];

export const dayPeriodOptions = [
  { value: 'MANHA', label: 'Manha' },
  { value: 'TARDE', label: 'Tarde' },
  { value: 'NOITE', label: 'Noite' },
  { value: 'MADRUGADA', label: 'Madrugada' },
  { value: 'INTEGRAL', label: 'Integral' },
  { value: 'HORARIO_PERSONALIZADO', label: 'Horario personalizado' },
] as const satisfies readonly Option<string>[];

export type CaregiverEducation = typeof caregiverEducationOptions[number]['value'];
export type CareModality = typeof careModalityOptions[number]['value'];
export type CaregiverService = typeof caregiverServiceOptions[number]['value'];
export type Relationship = typeof relationshipOptions[number]['value'];
export type ContactPreference = typeof contactPreferenceOptions[number]['value'];
export type DependencyLevel = typeof dependencyLevelOptions[number]['value'];
export type Mobility = typeof mobilityOptions[number]['value'];
export type Allergy = typeof allergyOptions[number]['value'];
export type FoodRestriction = typeof foodRestrictionOptions[number]['value'];
export type WeekDay = typeof weekDayOptions[number]['value'];
export type DayPeriod = typeof dayPeriodOptions[number]['value'];

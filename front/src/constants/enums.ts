export type Option<T extends string> = {
  value: T;
  label: string;
};

export const caregiverEducationOptions = [
  { value: 'CURSO_CUIDADOR_IDOSOS', label: 'Curso de cuidador de idosos' },
  { value: 'TECNICO_ENFERMAGEM', label: 'Técnico em enfermagem' },
  { value: 'AUXILIAR_ENFERMAGEM', label: 'Auxiliar de enfermagem' },
  { value: 'ENFERMAGEM_SUPERIOR', label: 'Ensino superior em enfermagem' },
  { value: 'PRIMEIROS_SOCORROS', label: 'Curso de primeiros socorros' },
  { value: 'EXPERIENCIA_PRATICA', label: 'Experiência prática sem formação formal' },
  { value: 'OUTRO', label: 'Outro' },
] as const satisfies readonly Option<string>[];

export const caregiverExperienceRangeOptions = [
  { value: 'MENOS_DE_1_ANO', label: 'Menos de 1 ano' },
  { value: 'DE_1_A_2_ANOS', label: '1 a 2 anos' },
  { value: 'DE_3_A_5_ANOS', label: '3 a 5 anos' },
  { value: 'DE_6_A_10_ANOS', label: '6 a 10 anos' },
  { value: 'MAIS_DE_10_ANOS', label: 'Mais de 10 anos' },
] as const satisfies readonly Option<string>[];

export const careModalityOptions = [
  { value: 'DIURNO', label: 'Diurno' },
  { value: 'NOTURNO', label: 'Noturno' },
  { value: 'PLANTAO_12X36', label: 'Plantão 12x36' },
  { value: 'PLANTAO_24H', label: 'Plantão 24h' },
  { value: 'EVENTUAL', label: 'Eventual' },
  { value: 'FINAIS_DE_SEMANA', label: 'Finais de semana' },
  { value: 'ACOMPANHAMENTO_CONSULTAS', label: 'Acompanhamento em consultas' },
  { value: 'OUTRO', label: 'Outro' },
] as const satisfies readonly Option<string>[];

export const caregiverServiceOptions = [
  { value: 'HIGIENE_PESSOAL', label: 'Auxílio com higiene pessoal' },
  { value: 'BANHO', label: 'Auxílio no banho' },
  { value: 'ALIMENTACAO', label: 'Auxílio na alimentação' },
  { value: 'LOCOMOCAO', label: 'Auxílio na locomoção' },
  { value: 'COMPANHIA', label: 'Companhia e acompanhamento' },
  { value: 'MEDICACAO_ORIENTADA', label: 'Apoio à rotina de medicação conforme orientação' },
  { value: 'CONSULTAS', label: 'Acompanhamento em consultas' },
  { value: 'ATIVIDADES_DOMESTICAS_LEVES', label: 'Atividades domésticas leves relacionadas ao cuidado' },
  { value: 'MONITORAMENTO_NOTURNO', label: 'Monitoramento noturno' },
  { value: 'OUTRO', label: 'Outro' },
] as const satisfies readonly Option<string>[];

export const relationshipOptions = [
  { value: 'FILHO', label: 'Filho(a)' },
  { value: 'CONJUGE', label: 'Cônjuge' },
  { value: 'NETO', label: 'Neto(a)' },
  { value: 'IRMAO', label: 'Irmão/Irmã' },
  { value: 'SOBRINHO', label: 'Sobrinho(a)' },
  { value: 'TUTOR_LEGAL', label: 'Tutor legal' },
  { value: 'RESPONSAVEL_CONTRATUAL', label: 'Responsável contratual' },
  { value: 'AMIGO', label: 'Amigo(a)' },
  { value: 'OUTRO', label: 'Outro' },
] as const satisfies readonly Option<string>[];

export const contactPreferenceOptions = [
  { value: 'WHATSAPP', label: 'WhatsApp' },
  { value: 'LIGACAO', label: 'Ligação' },
  { value: 'EMAIL', label: 'E-mail' },
  { value: 'SMS', label: 'SMS' },
  { value: 'QUALQUER', label: 'Qualquer opção' },
] as const satisfies readonly Option<string>[];

export const dependencyLevelOptions = [
  { value: 'BAIXA', label: 'Baixa dependência' },
  { value: 'MODERADA', label: 'Dependência moderada' },
  { value: 'ALTA', label: 'Alta dependência' },
  { value: 'TOTAL', label: 'Dependência total' },
  { value: 'NAO_SEI_INFORMAR', label: 'Não sei informar' },
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
  { value: 'NAO_POSSUI', label: 'Não possui' },
  { value: 'MEDICAMENTOS', label: 'Medicamentos' },
  { value: 'ALIMENTOS', label: 'Alimentos' },
  { value: 'PRODUTOS_HIGIENE', label: 'Produtos de higiene' },
  { value: 'LATEX', label: 'Latex' },
  { value: 'POEIRA', label: 'Poeira' },
  { value: 'OUTRO', label: 'Outro' },
  { value: 'NAO_SEI_INFORMAR', label: 'Não sei informar' },
] as const satisfies readonly Option<string>[];

export const foodRestrictionOptions = [
  { value: 'NAO_POSSUI', label: 'Não possui' },
  { value: 'DIABETICA', label: 'Dieta para diabetes' },
  { value: 'HIPOSSODICA', label: 'Baixo consumo de sal' },
  { value: 'PASTOSA', label: 'Alimentação pastosa' },
  { value: 'LIQUIDA', label: 'Alimentação líquida' },
  { value: 'SEM_LACTOSE', label: 'Sem lactose' },
  { value: 'SEM_GLUTEN', label: 'Sem gluten' },
  { value: 'VEGETARIANA', label: 'Vegetariana' },
  { value: 'OUTRO', label: 'Outro' },
  { value: 'NAO_SEI_INFORMAR', label: 'Não sei informar' },
] as const satisfies readonly Option<string>[];

export const weekDayOptions = [
  { value: 'SEGUNDA', label: 'Segunda' },
  { value: 'TERCA', label: 'Terça' },
  { value: 'QUARTA', label: 'Quarta' },
  { value: 'QUINTA', label: 'Quinta' },
  { value: 'SEXTA', label: 'Sexta' },
  { value: 'SABADO', label: 'Sábado' },
  { value: 'DOMINGO', label: 'Domingo' },
] as const satisfies readonly Option<string>[];

export const dayPeriodOptions = [
  { value: 'MANHA', label: 'Manhã' },
  { value: 'TARDE', label: 'Tarde' },
  { value: 'NOITE', label: 'Noite' },
  { value: 'MADRUGADA', label: 'Madrugada' },
  { value: 'INTEGRAL', label: 'Integral' },
  { value: 'HORARIO_PERSONALIZADO', label: 'Horário personalizado' },
] as const satisfies readonly Option<string>[];

export type CaregiverEducation = typeof caregiverEducationOptions[number]['value'];
export type CaregiverExperienceRange = typeof caregiverExperienceRangeOptions[number]['value'];
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

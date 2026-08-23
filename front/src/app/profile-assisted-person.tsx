import { useEffect, useMemo, useState } from 'react';
import { HeartPulse, IdCard, Save, User } from 'lucide-react-native';
import { StyleSheet, Text, View } from 'react-native';

import { AppHeader } from '@/components/app-header';
import { AppTextInput } from '@/components/app-text-input';
import { DatePickerField } from '@/components/date-picker-field';
import { LoadingState } from '@/components/loading-state';
import { OptionGroup } from '@/components/option-group';
import { PrimaryButton } from '@/components/primary-button';
import { ScreenContainer } from '@/components/screen-container';
import { useBlockNavigationWhenBusy } from '@/hooks/useBlockNavigationWhenBusy';
import {
  allergyOptions,
  contactPreferenceOptions,
  dependencyLevelOptions,
  foodRestrictionOptions,
  mobilityOptions,
  relationshipOptions,
  type Allergy,
  type ContactPreference,
  type DependencyLevel,
  type FoodRestriction,
  type Mobility,
  type Relationship,
} from '@/constants/enums';
import { ApiError } from '@/services/api';
import { getMyProfile, updateAssistedPerson, updateResponsibleProfile } from '@/services/profileService';
import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';
import { formatCpf, unformatCpf } from '@/utils/masks';

function toDisplayDate(value?: string | null) {
  if (!value) return '';
  const [year, month, day] = value.split('-');
  if (!year || !month || !day) return value;
  return `${day}/${month}/${year}`;
}

function needsDetail(values: string[]) {
  return values.some((value) => value !== 'NAO_POSSUI' && value !== 'NAO_SEI_INFORMAR');
}

function normalizeExclusiveHealthOptions<T extends string>(values: T[]) {
  const lastValue = values[values.length - 1];
  const exclusiveOptions = ['NAO_POSSUI', 'NAO_SEI_INFORMAR'];

  if (!lastValue) return values;
  if (exclusiveOptions.includes(lastValue)) return [lastValue];
  return values.filter((value) => !exclusiveOptions.includes(value));
}

export default function ProfileAssistedPersonScreen() {
  const [assistedPersonId, setAssistedPersonId] = useState('');
  const [relationship, setRelationship] = useState<Relationship | null>(null);
  const [relationshipCustom, setRelationshipCustom] = useState('');
  const [contactPreference, setContactPreference] = useState<ContactPreference | null>(null);
  const [nome, setNome] = useState('');
  const [cpf, setCpf] = useState('');
  const [dataNascimento, setDataNascimento] = useState('');
  const [grauDependencia, setGrauDependencia] = useState<DependencyLevel | null>(null);
  const [mobilidade, setMobilidade] = useState<Mobility | null>(null);
  const [mobilidadeOutro, setMobilidadeOutro] = useState('');
  const [alergias, setAlergias] = useState<Allergy[]>([]);
  const [alergiasOutro, setAlergiasOutro] = useState('');
  const [alergiasDetalhes, setAlergiasDetalhes] = useState('');
  const [restricoesAlimentares, setRestricoesAlimentares] = useState<FoodRestriction[]>([]);
  const [restricoesAlimentaresOutro, setRestricoesAlimentaresOutro] = useState('');
  const [restricoesAlimentaresDetalhes, setRestricoesAlimentaresDetalhes] = useState('');
  const [medicamentos, setMedicamentos] = useState('');
  const [observacoes, setObservacoes] = useState('');
  const [feedback, setFeedback] = useState<string | null>(null);
  const [isSuccess, setIsSuccess] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const formDisabled = isLoading || isSaving;
  useBlockNavigationWhenBusy(isSaving);
  const today = useMemo(() => new Date(), []);

  const showAllergyCustom = alergias.includes('OUTRO');
  const showAllergyDetails = needsDetail(alergias);
  const showFoodRestrictionCustom = restricoesAlimentares.includes('OUTRO');
  const showFoodRestrictionDetails = needsDetail(restricoesAlimentares);

  useEffect(() => {
    let active = true;
    getMyProfile()
      .then((profile) => {
        if (!active) return;
        const assistedPerson = profile.assistedPersons?.[0];
        if (!assistedPerson) {
          setFeedback('Nenhuma pessoa assistida encontrada para este perfil.');
          return;
        }

        setAssistedPersonId(assistedPerson.id);
        setRelationship(profile.responsibleProfile?.parentesco ?? null);
        setRelationshipCustom(profile.responsibleProfile?.parentescoOutro ?? '');
        setContactPreference(profile.responsibleProfile?.preferenciaContato ?? null);
        setNome(assistedPerson.nome ?? '');
        setCpf(formatCpf(assistedPerson.cpf ?? ''));
        setDataNascimento(toDisplayDate(assistedPerson.dataNascimento));
        setGrauDependencia(assistedPerson.grauDependencia ?? null);
        setMobilidade(assistedPerson.mobilidade ?? null);
        setMobilidadeOutro(assistedPerson.mobilidadeOutro ?? '');
        setAlergias(assistedPerson.alergias ?? []);
        setAlergiasOutro(assistedPerson.alergiasOutro ?? '');
        setAlergiasDetalhes(assistedPerson.alergiasDetalhes ?? '');
        setRestricoesAlimentares(assistedPerson.restricoesAlimentares ?? []);
        setRestricoesAlimentaresOutro(assistedPerson.restricoesAlimentaresOutro ?? '');
        setRestricoesAlimentaresDetalhes(assistedPerson.restricoesAlimentaresDetalhes ?? '');
        setMedicamentos(assistedPerson.medicamentos ?? '');
        setObservacoes(assistedPerson.observacoes ?? '');
      })
      .catch((error) => setFeedback(error instanceof ApiError ? error.message : 'Não foi possível carregar a pessoa assistida.'))
      .finally(() => active && setIsLoading(false));

    return () => {
      active = false;
    };
  }, []);

  async function handleSave() {
    setFeedback(null);
    setIsSuccess(false);

    if (!assistedPersonId) return setFeedback('Pessoa assistida não encontrada.');
    if (!relationship) return setFeedback('Informe o parentesco ou vínculo.');
    if (relationship === 'OUTRO' && !relationshipCustom.trim()) return setFeedback('Informe o parentesco personalizado.');
    if (!contactPreference) return setFeedback('Informe a preferência de contato.');
    if (!nome.trim()) return setFeedback('Informe o nome da pessoa assistida.');
    if (!dataNascimento.trim()) return setFeedback('Informe a data de nascimento.');
    if (!grauDependencia) return setFeedback('Informe o grau de dependencia.');
    if (!mobilidade) return setFeedback('Informe a mobilidade.');
    if (mobilidade === 'OUTRO' && !mobilidadeOutro.trim()) return setFeedback('Informe a mobilidade personalizada.');
    if (alergias.length === 0) return setFeedback('Informe se a pessoa assistida possui alergias.');
    if (showAllergyCustom && !alergiasOutro.trim()) return setFeedback('Informe a alergia personalizada.');
    if (showAllergyDetails && !alergiasDetalhes.trim()) return setFeedback('Informe detalhes da alergia.');
    if (restricoesAlimentares.length === 0) return setFeedback('Informe se há restrições alimentares.');
    if (showFoodRestrictionCustom && !restricoesAlimentaresOutro.trim()) return setFeedback('Informe a restrição alimentar personalizada.');
    if (showFoodRestrictionDetails && !restricoesAlimentaresDetalhes.trim()) return setFeedback('Informe detalhes da restrição alimentar.');

    try {
      setIsSaving(true);
      await updateResponsibleProfile({
        parentesco: relationship,
        parentescoOutro: relationship === 'OUTRO' ? relationshipCustom.trim() : null,
        preferenciaContato: contactPreference,
      });
      const response = await updateAssistedPerson(assistedPersonId, {
        nome: nome.trim(),
        cpf: cpf.trim() ? unformatCpf(cpf) : null,
        dataNascimento,
        grauDependencia,
        mobilidade,
        mobilidadeOutro: mobilidade === 'OUTRO' ? mobilidadeOutro.trim() : null,
        alergias,
        alergiasOutro: showAllergyCustom ? alergiasOutro.trim() : null,
        alergiasDetalhes: showAllergyDetails ? alergiasDetalhes.trim() : null,
        restricoesAlimentares,
        restricoesAlimentaresOutro: showFoodRestrictionCustom ? restricoesAlimentaresOutro.trim() : null,
        restricoesAlimentaresDetalhes: showFoodRestrictionDetails ? restricoesAlimentaresDetalhes.trim() : null,
        medicamentos: medicamentos.trim() || null,
        observacoes: observacoes.trim() || null,
      });
      setFeedback(response.message);
      setIsSuccess(true);
    } catch (error) {
      setFeedback(error instanceof ApiError ? error.message : 'Não foi possível salvar a pessoa assistida.');
    } finally {
      setIsSaving(false);
    }
  }

  return (
    <ScreenContainer keyboardAvoiding contentStyle={styles.content}>
      <AppHeader showBack backDisabled={isSaving} title="Pessoa assistida" subtitle="Perfil de cuidado e necessidades importantes" />
      {isLoading ? (
        <LoadingState />
      ) : (
      <View style={styles.card}>
        <OptionGroup required disabled={formDisabled} label="Parentesco ou vínculo" options={relationshipOptions} value={relationship} onChange={(value) => setRelationship(value as Relationship)} />
        {relationship === 'OUTRO' ? (
          <AppTextInput required label="Parentesco personalizado" icon={HeartPulse} placeholder="Informe o vínculo" value={relationshipCustom} onChangeText={setRelationshipCustom} disabled={formDisabled} />
        ) : null}
        <OptionGroup required disabled={formDisabled} label="Preferência de contato" options={contactPreferenceOptions} value={contactPreference} onChange={(value) => setContactPreference(value as ContactPreference)} />
        <AppTextInput required label="Nome da pessoa assistida" icon={User} placeholder="Nome completo" value={nome} onChangeText={setNome} disabled={formDisabled} />
        <DatePickerField required label="Data de nascimento" value={dataNascimento} onChange={setDataNascimento} maxDate={today} disabled={formDisabled} />
        <AppTextInput optional label="CPF da pessoa assistida" icon={IdCard} placeholder="000.000.000-00" value={cpf} onChangeText={(value) => setCpf(formatCpf(value))} keyboardType="number-pad" disabled={formDisabled} />
        <OptionGroup required disabled={formDisabled} label="Grau de dependência" options={dependencyLevelOptions} value={grauDependencia} onChange={(value) => setGrauDependencia(value as DependencyLevel)} />
        <OptionGroup required disabled={formDisabled} label="Mobilidade" options={mobilityOptions} value={mobilidade} onChange={(value) => setMobilidade(value as Mobility)} />
        {mobilidade === 'OUTRO' ? (
          <AppTextInput required label="Mobilidade personalizada" icon={HeartPulse} placeholder="Descreva a mobilidade" value={mobilidadeOutro} onChangeText={setMobilidadeOutro} disabled={formDisabled} />
        ) : null}
        <OptionGroup required multiple disabled={formDisabled} label="Alergias" options={allergyOptions} value={alergias} onChange={(value) => setAlergias(normalizeExclusiveHealthOptions(value as Allergy[]))} />
        {showAllergyCustom ? (
          <AppTextInput required label="Alergia personalizada" icon={HeartPulse} placeholder="Informe a alergia" value={alergiasOutro} onChangeText={setAlergiasOutro} disabled={formDisabled} />
        ) : null}
        {showAllergyDetails ? (
          <AppTextInput required label="Detalhes da alergia" icon={HeartPulse} placeholder="Informe detalhes importantes" value={alergiasDetalhes} onChangeText={setAlergiasDetalhes} disabled={formDisabled} />
        ) : null}
        <OptionGroup required multiple disabled={formDisabled} label="Restrições alimentares" options={foodRestrictionOptions} value={restricoesAlimentares} onChange={(value) => setRestricoesAlimentares(normalizeExclusiveHealthOptions(value as FoodRestriction[]))} />
        {showFoodRestrictionCustom ? (
          <AppTextInput required label="Restrição personalizada" icon={HeartPulse} placeholder="Informe a restrição" value={restricoesAlimentaresOutro} onChangeText={setRestricoesAlimentaresOutro} disabled={formDisabled} />
        ) : null}
        {showFoodRestrictionDetails ? (
          <AppTextInput required label="Detalhes da restrição alimentar" icon={HeartPulse} placeholder="Informe detalhes importantes" value={restricoesAlimentaresDetalhes} onChangeText={setRestricoesAlimentaresDetalhes} disabled={formDisabled} />
        ) : null}
        <AppTextInput optional label="Medicamentos" icon={HeartPulse} placeholder="Liste se houver" value={medicamentos} onChangeText={setMedicamentos} disabled={formDisabled} />
        <AppTextInput optional label="Observações importantes" icon={HeartPulse} placeholder="Informações adicionais" value={observacoes} onChangeText={setObservacoes} disabled={formDisabled} />
        {feedback ? <Text style={[styles.feedback, isSuccess && styles.success]}>{feedback}</Text> : null}
        <PrimaryButton label={isSaving ? 'Salvando...' : 'Salvar alterações'} icon={Save} loading={isSaving} onPress={handleSave} disabled={formDisabled} />
      </View>
      )}
    </ScreenContainer>
  );
}

const styles = StyleSheet.create({
  content: {
    paddingHorizontal: spacing.xl,
    paddingBottom: spacing.lg,
    gap: spacing.xl,
  },
  card: {
    gap: spacing.lg,
    padding: spacing.xl,
    borderRadius: radii.xxl,
    backgroundColor: colors.card,
    borderWidth: 1,
    borderColor: colors.border,
    ...shadows.card,
  },
  visibilityBox: { gap: spacing.sm, padding: spacing.md, borderRadius: radii.lg, backgroundColor: colors.secondary },
  visibilityHelp: { fontFamily: fontFamily.regular, fontSize: 12, lineHeight: 18, color: colors.mutedForeground },
  feedback: {
    fontFamily: fontFamily.medium,
    fontSize: 12,
    lineHeight: 18,
    color: colors.destructive,
  },
  success: {
    color: colors.mintForeground,
  },
});

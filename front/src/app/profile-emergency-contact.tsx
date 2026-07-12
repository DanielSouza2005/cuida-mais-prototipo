import { useEffect, useState } from 'react';
import { Check, HeartPulse, Phone, Save, User } from 'lucide-react-native';
import { Pressable, StyleSheet, Text, View } from 'react-native';

import { AppHeader } from '@/components/app-header';
import { AppTextInput } from '@/components/app-text-input';
import { LoadingState } from '@/components/loading-state';
import { OptionGroup } from '@/components/option-group';
import { PrimaryButton } from '@/components/primary-button';
import { ScreenContainer } from '@/components/screen-container';
import { useBlockNavigationWhenBusy } from '@/hooks/useBlockNavigationWhenBusy';
import { relationshipOptions, type Relationship } from '@/constants/enums';
import { ApiError } from '@/services/api';
import { getMyProfile, updateEmergencyContact } from '@/services/profileService';
import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';
import { formatPhone, unformatPhone } from '@/utils/masks';

function relationshipLabel(value?: string | null) {
  return relationshipOptions.find((option) => option.value === value)?.label ?? value ?? '';
}

function resolveRelationshipState(value?: string | null) {
  if (!value) {
    return { selected: null, custom: '' };
  }

  const normalized = value.trim();
  const option = relationshipOptions.find((item) => item.value === normalized || item.label === normalized);

  if (option) {
    return { selected: option.value as Relationship, custom: '' };
  }

  return { selected: 'OUTRO' as Relationship, custom: normalized };
}

export default function ProfileEmergencyContactScreen() {
  const [assistedPersonId, setAssistedPersonId] = useState('');
  const [responsibleName, setResponsibleName] = useState('');
  const [responsiblePhone, setResponsiblePhone] = useState('');
  const [responsibleRelationship, setResponsibleRelationship] = useState('');
  const [isResponsibleContact, setIsResponsibleContact] = useState(false);
  const [nome, setNome] = useState('');
  const [telefone, setTelefone] = useState('');
  const [vinculo, setVinculo] = useState<Relationship | null>(null);
  const [vinculoOutro, setVinculoOutro] = useState('');
  const [feedback, setFeedback] = useState<string | null>(null);
  const [isSuccess, setIsSuccess] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const formDisabled = isLoading || isSaving;
  useBlockNavigationWhenBusy(isSaving);

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

        const relationship = profile.responsibleProfile?.parentescoOutro
          || profile.responsibleProfile?.parentesco
          || 'Responsável';
        const contactRelationship = resolveRelationshipState(assistedPerson.contatoEmergencia?.vinculo);

        setAssistedPersonId(assistedPerson.id);
        setResponsibleName(profile.user.fullName ?? '');
        setResponsiblePhone(formatPhone(profile.user.phone ?? ''));
        setResponsibleRelationship(relationship);
        setIsResponsibleContact(Boolean(assistedPerson.contatoEmergencia?.isResponsibleContact));
        setNome(assistedPerson.contatoEmergencia?.nome ?? '');
        setTelefone(formatPhone(assistedPerson.contatoEmergencia?.telefone ?? ''));
        setVinculo(contactRelationship.selected);
        setVinculoOutro(contactRelationship.custom);
      })
      .catch((error) => setFeedback(error instanceof ApiError ? error.message : 'Não foi possível carregar o contato de emergência.'))
      .finally(() => active && setIsLoading(false));

    return () => {
      active = false;
    };
  }, []);

  async function handleSave() {
    setFeedback(null);
    setIsSuccess(false);

    if (!assistedPersonId) return setFeedback('Pessoa assistida não encontrada.');
    if (!isResponsibleContact && (!nome.trim() || !telefone.trim() || !vinculo)) {
      return setFeedback('Informe nome, telefone e vínculo do contato de emergência.');
    }
    if (!isResponsibleContact && vinculo === 'OUTRO' && !vinculoOutro.trim()) {
      return setFeedback('Informe o vínculo personalizado.');
    }

    try {
      setIsSaving(true);
      const manualRelationship = vinculo === 'OUTRO' ? vinculoOutro.trim() : vinculo;
      const response = await updateEmergencyContact(assistedPersonId, {
        isResponsibleContact,
        nome: isResponsibleContact ? responsibleName : nome.trim(),
        telefone: isResponsibleContact ? unformatPhone(responsiblePhone) : unformatPhone(telefone),
        vinculo: isResponsibleContact ? responsibleRelationship : manualRelationship,
      });
      setFeedback(response.message);
      setIsSuccess(true);
    } catch (error) {
      setFeedback(error instanceof ApiError ? error.message : 'Não foi possível salvar o contato de emergência.');
    } finally {
      setIsSaving(false);
    }
  }

  return (
    <ScreenContainer keyboardAvoiding contentStyle={styles.content}>
      <AppHeader showBack backDisabled={isSaving} title="Contato de emergência" subtitle="Nome, telefone e vínculo de apoio" />
      <View style={styles.card}>
        {isLoading ? (
          <LoadingState />
        ) : (
          <>
            <Pressable
              accessibilityRole="checkbox"
              accessibilityState={{ checked: isResponsibleContact }}
              disabled={formDisabled}
              onPress={() => setIsResponsibleContact((value) => !value)}
              style={({ pressed }) => [styles.checkRow, formDisabled && styles.disabled, pressed && styles.pressed]}
            >
              <View style={[styles.checkbox, isResponsibleContact && styles.checkboxChecked]}>
                {isResponsibleContact ? <Check color={colors.primaryForeground} size={14} strokeWidth={3} /> : null}
              </View>
              <View style={styles.checkboxTextBlock}>
                <Text style={styles.checkTitle}>Sou o contato de emergência</Text>
                <Text style={styles.helperText}>Usaremos seu nome, telefone e vínculo como contato principal.</Text>
              </View>
            </Pressable>

            {isResponsibleContact ? (
              <View style={styles.preview}>
                <Text style={styles.previewText}>{responsibleName || 'Nome do responsável'}</Text>
                <Text style={styles.previewText}>{responsiblePhone || 'Telefone do responsável'}</Text>
                <Text style={styles.previewText}>{relationshipLabel(responsibleRelationship) || 'Vínculo informado'}</Text>
              </View>
            ) : (
              <>
                <AppTextInput required label="Contato de emergência" icon={User} placeholder="Nome completo" value={nome} onChangeText={setNome} disabled={formDisabled} />
                <AppTextInput required label="Telefone de emergência" icon={Phone} placeholder="(00) 00000-0000" value={telefone} onChangeText={(value) => setTelefone(formatPhone(value))} keyboardType="phone-pad" disabled={formDisabled} />
                <OptionGroup required label="Vínculo do contato" options={relationshipOptions} value={vinculo} onChange={(value) => setVinculo(value as Relationship)} disabled={formDisabled} />
                {vinculo === 'OUTRO' ? (
                  <AppTextInput required label="Vínculo personalizado" icon={HeartPulse} placeholder="Informe o vínculo" value={vinculoOutro} onChangeText={setVinculoOutro} disabled={formDisabled} />
                ) : null}
              </>
            )}
          </>
        )}

        {feedback ? <Text style={[styles.feedback, isSuccess && styles.success]}>{feedback}</Text> : null}
        <PrimaryButton label={isSaving ? 'Salvando...' : 'Salvar alterações'} icon={Save} onPress={handleSave} disabled={formDisabled} loading={isSaving} />
      </View>
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
  checkRow: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    gap: spacing.sm,
  },
  pressed: {
    opacity: 0.78,
  },
  disabled: {
    opacity: 0.62,
  },
  checkbox: {
    width: 20,
    height: 20,
    borderRadius: radii.sm,
    borderWidth: 1,
    borderColor: colors.border,
    backgroundColor: colors.card,
    alignItems: 'center',
    justifyContent: 'center',
    marginTop: 1,
  },
  checkboxChecked: {
    borderColor: colors.primary,
    backgroundColor: colors.primary,
  },
  checkboxTextBlock: {
    flex: 1,
    gap: spacing.xxs,
  },
  checkTitle: {
    fontFamily: fontFamily.semiBold,
    fontSize: 13,
    color: colors.foreground,
  },
  helperText: {
    fontFamily: fontFamily.regular,
    fontSize: 11.5,
    lineHeight: 17,
    color: colors.mutedForeground,
  },
  preview: {
    gap: spacing.xs,
    borderRadius: radii.lg,
    borderWidth: 1,
    borderColor: colors.border,
    backgroundColor: colors.secondary,
    padding: spacing.md,
  },
  previewText: {
    fontFamily: fontFamily.medium,
    fontSize: 12,
    lineHeight: 18,
    color: colors.secondaryForeground,
  },
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

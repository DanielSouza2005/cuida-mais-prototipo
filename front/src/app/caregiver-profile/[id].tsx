import { useLocalSearchParams } from 'expo-router';
import { Alert, Linking, Platform, ScrollView, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { CalendarDays, MapPin, MessageCircle, Navigation, Stethoscope } from 'lucide-react-native';
import type { LucideIcon } from 'lucide-react-native';
import { useEffect, useState } from 'react';

import { AppHeader } from '@/components/app-header';
import { LoadingState } from '@/components/loading-state';
import { PrimaryButton } from '@/components/primary-button';
import { getCaregiverDetails } from '@/services/caregiverSearchService';
import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';
import type { CaregiverProfileDetails } from '@/types/caregiverSearch';
import {
  formatDistance,
  formatLocation,
  getAvailabilityLabel,
  getEducationLabel,
  getExperienceLabel,
  getInitials,
  getModalityLabel,
  getServiceLabel,
  getWeekDayLabel,
  sortWeekDays,
} from '@/utils/caregiverSearch';

export default function CaregiverProfileDetailsScreen() {
  const { id, distanciaKm } = useLocalSearchParams<{ id: string; distanciaKm?: string }>();
  const [caregiver, setCaregiver] = useState<CaregiverProfileDetails | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!id) return;

    let active = true;
    setLoading(true);
    setError(null);

    getCaregiverDetails(id)
      .then((response) => {
        if (active) setCaregiver(response);
      })
      .catch(() => {
        if (active) setError('Não foi possível carregar o perfil do cuidador.');
      })
      .finally(() => {
        if (active) setLoading(false);
      });

    return () => {
      active = false;
    };
  }, [id]);

  function showDevelopmentMessage() {
    Alert.alert('Funcionalidade em desenvolvimento');
  }

  async function openMap() {
    if (!caregiver) return;

    const query = [caregiver.bairro, caregiver.cidade, caregiver.estado].filter(Boolean).join(', ');
    if (!query) return;

    const encodedQuery = encodeURIComponent(query);
    const nativeUrl = Platform.OS === 'ios'
      ? `http://maps.apple.com/?q=${encodedQuery}`
      : `geo:0,0?q=${encodedQuery}`;
    const fallbackUrl = `https://www.google.com/maps/search/?api=1&query=${encodedQuery}`;

    try {
      const canOpenNativeUrl = await Linking.canOpenURL(nativeUrl);
      await Linking.openURL(canOpenNativeUrl ? nativeUrl : fallbackUrl);
    } catch {
      await Linking.openURL(fallbackUrl);
    }
  }

  function retry() {
    if (!id) return;
    setCaregiver(null);
    setLoading(true);
    setError(null);
    getCaregiverDetails(id)
      .then(setCaregiver)
      .catch(() => setError('Não foi possível carregar o perfil do cuidador.'))
      .finally(() => setLoading(false));
  }

  if (loading) {
    return (
      <SafeAreaView style={styles.screen} edges={['top', 'right', 'bottom', 'left']}>
        <View style={styles.loadingContent}>
          <AppHeader showBack title="Perfil do cuidador" />
          <LoadingState message="Carregando perfil..." />
        </View>
      </SafeAreaView>
    );
  }

  if (error || !caregiver) {
    return (
      <SafeAreaView style={styles.screen} edges={['top', 'right', 'bottom', 'left']}>
        <View style={styles.loadingContent}>
          <AppHeader showBack title="Perfil do cuidador" />
          <View style={styles.emptyState}>
            <Text style={styles.emptyTitle}>Perfil não encontrado</Text>
            <Text style={styles.emptyText}>{error ?? 'Não encontramos esse cuidador.'}</Text>
            <PrimaryButton label="Tentar novamente" variant="secondary" onPress={retry} style={styles.emptyButton} />
          </View>
        </View>
      </SafeAreaView>
    );
  }

  const routeDistance = typeof distanciaKm === 'string' ? Number(distanciaKm) : null;
  const distance = formatDistance(caregiver.distanciaKm ?? routeDistance);

  return (
    <SafeAreaView style={styles.screen} edges={['top', 'right', 'bottom', 'left']}>
      <ScrollView contentContainerStyle={styles.content} showsVerticalScrollIndicator={false}>
        <AppHeader showBack title="Perfil do cuidador" />

        <View style={styles.heroCard}>
          <View style={styles.avatar}>
            <Text style={styles.avatarText}>{getInitials(caregiver.nome)}</Text>
          </View>
          <View style={styles.heroCopy}>
            <View style={styles.nameRow}>
              <Text style={styles.name}>{caregiver.nome}</Text>
            </View>
            <Text style={styles.location}>{formatLocation(caregiver)}</Text>
            <View style={styles.statusPill}>
              <MessageCircle color={colors.mintForeground} size={14} strokeWidth={2.4} />
              <Text style={styles.statusText}>Disponível para contato</Text>
            </View>
          </View>
        </View>

        <View style={styles.metricsGrid}>
          <Metric icon={CalendarDays} label="Experiência" value={getExperienceLabel(caregiver.experienciaRange)} />
          <Metric icon={Stethoscope} label="Modalidades" value={`${caregiver.modalidadesAtendimento.length} opções`} />
          <Metric icon={distance ? Navigation : MapPin} label={distance ? 'Distância' : 'Localização'} value={distance ?? caregiver.estado ?? 'Não informada'} />
        </View>

        <Section title="Sobre o profissional">
          <Text style={styles.paragraph}>{caregiver.biografia || 'Biografia profissional ainda não informada.'}</Text>
          <InfoLine
            label="Formação"
            value={caregiver.formacoes.length > 0 ? caregiver.formacoes.map(getEducationLabel).join(', ') : 'Não informada'}
          />
          <InfoLine
            label="Modalidades"
            value={caregiver.modalidadesAtendimento.length > 0 ? caregiver.modalidadesAtendimento.map(getModalityLabel).join(', ') : 'Não informadas'}
          />
        </Section>

        <Section title="Serviços oferecidos">
          <View style={styles.wrap}>
            {caregiver.servicosOferecidos.length > 0 ? (
              caregiver.servicosOferecidos.map((service) => (
                <View key={service} style={styles.chip}>
                  <Text style={styles.chipText}>{getServiceLabel(service)}</Text>
                </View>
              ))
            ) : (
              <Text style={styles.paragraph}>Serviços ainda não informados.</Text>
            )}
          </View>
        </Section>

        <Section title="Disponibilidade">
          <View style={styles.wrap}>
            {caregiver.disponibilidade.periodos.length > 0 ? (
              caregiver.disponibilidade.periodos.map((period) => (
                <View key={period} style={styles.availabilityChip}>
                  <Text style={styles.availabilityChipText}>{getAvailabilityLabel(period)}</Text>
                </View>
              ))
            ) : (
              <Text style={styles.paragraph}>Períodos ainda não informados.</Text>
            )}
          </View>
          {caregiver.disponibilidade.diasSemana.length > 0 ? (
            <InfoLine label="Dias da semana" value={sortWeekDays(caregiver.disponibilidade.diasSemana).map(getWeekDayLabel).join(', ')} />
          ) : null}
          {caregiver.disponibilidade.observacao ? (
            <Text style={styles.paragraph}>{caregiver.disponibilidade.observacao}</Text>
          ) : null}
        </Section>

        <Section title="Localização">
          <InfoLine label="Cidade" value={[caregiver.cidade, caregiver.estado].filter(Boolean).join(' - ') || 'Não informada'} />
          <InfoLine label="Bairro" value={caregiver.bairro || 'Não informado'} />
          {distance ? <InfoLine label="Distância" value={`${distance} da origem informada`} /> : null}
          {[caregiver.bairro, caregiver.cidade, caregiver.estado].some(Boolean) ? (
            <PrimaryButton label="Abrir no mapa" icon={MapPin} variant="secondary" onPress={openMap} style={styles.mapButton} />
          ) : null}
        </Section>

        <View style={styles.actions}>
          <PrimaryButton label="Solicitar contato" icon={MessageCircle} onPress={showDevelopmentMessage} />
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}

function Metric({ icon: Icon, label, value }: { icon: LucideIcon; label: string; value: string }) {
  return (
    <View style={styles.metric}>
      <Icon color={colors.primary} size={17} strokeWidth={2.4} />
      <Text numberOfLines={1} style={styles.metricValue}>{value}</Text>
      <Text style={styles.metricLabel}>{label}</Text>
    </View>
  );
}

function Section({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <View style={styles.section}>
      <Text style={styles.sectionTitle}>{title}</Text>
      <View style={styles.sectionBody}>{children}</View>
    </View>
  );
}

function InfoLine({ label, value }: { label: string; value: string }) {
  return (
    <View style={styles.infoLine}>
      <Text style={styles.infoLabel}>{label}</Text>
      <Text style={styles.infoValue}>{value}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  screen: {
    flex: 1,
    backgroundColor: colors.background,
  },
  content: {
    paddingTop: spacing.md,
    paddingHorizontal: spacing.lg,
    paddingBottom: spacing.xxl,
    gap: spacing.lg,
  },
  loadingContent: {
    flex: 1,
    gap: spacing.lg,
    paddingTop: spacing.md,
    paddingHorizontal: spacing.lg,
    paddingBottom: spacing.xxl,
  },
  heroCard: {
    flexDirection: 'row',
    gap: spacing.md,
    borderRadius: radii.xxl,
    borderWidth: 1,
    borderColor: colors.border,
    backgroundColor: colors.card,
    padding: spacing.lg,
    ...shadows.soft,
  },
  avatar: {
    width: 76,
    height: 76,
    borderRadius: radii.xl,
    backgroundColor: colors.mint,
    alignItems: 'center',
    justifyContent: 'center',
  },
  avatarText: {
    fontFamily: fontFamily.extraBold,
    fontSize: 23,
    color: colors.mintForeground,
  },
  heroCopy: {
    flex: 1,
    minWidth: 0,
    gap: spacing.xs,
  },
  nameRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.xs,
  },
  name: {
    flex: 1,
    fontFamily: fontFamily.extraBold,
    fontSize: 21,
    lineHeight: 27,
    color: colors.foreground,
  },
  location: {
    fontFamily: fontFamily.medium,
    fontSize: 13,
    color: colors.mutedForeground,
  },
  statusPill: {
    alignSelf: 'flex-start',
    minHeight: 28,
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.xs,
    borderRadius: radii.full,
    backgroundColor: colors.secondary,
    paddingHorizontal: spacing.sm,
    marginTop: spacing.xs,
  },
  statusText: {
    fontFamily: fontFamily.bold,
    fontSize: 11,
    color: colors.mintForeground,
  },
  metricsGrid: {
    flexDirection: 'row',
    gap: spacing.sm,
  },
  metric: {
    flex: 1,
    minHeight: 82,
    alignItems: 'center',
    justifyContent: 'center',
    gap: spacing.xs,
    borderRadius: radii.lg,
    borderWidth: 1,
    borderColor: colors.border,
    backgroundColor: colors.card,
    paddingHorizontal: spacing.sm,
    ...shadows.card,
  },
  metricValue: {
    maxWidth: '100%',
    fontFamily: fontFamily.extraBold,
    fontSize: 12,
    color: colors.foreground,
  },
  metricLabel: {
    fontFamily: fontFamily.medium,
    fontSize: 10,
    color: colors.mutedForeground,
  },
  section: {
    gap: spacing.sm,
  },
  sectionTitle: {
    fontFamily: fontFamily.extraBold,
    fontSize: 16,
    color: colors.foreground,
  },
  sectionBody: {
    gap: spacing.md,
    borderRadius: radii.xl,
    borderWidth: 1,
    borderColor: colors.border,
    backgroundColor: colors.card,
    padding: spacing.lg,
    ...shadows.card,
  },
  paragraph: {
    fontFamily: fontFamily.regular,
    fontSize: 13,
    lineHeight: 21,
    color: colors.foreground,
  },
  infoLine: {
    gap: 2,
  },
  infoLabel: {
    fontFamily: fontFamily.bold,
    fontSize: 11,
    color: colors.mutedForeground,
    textTransform: 'uppercase',
  },
  infoValue: {
    fontFamily: fontFamily.semiBold,
    fontSize: 13,
    lineHeight: 20,
    color: colors.foreground,
  },
  wrap: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: spacing.sm,
  },
  chip: {
    maxWidth: '100%',
    borderRadius: radii.full,
    backgroundColor: colors.secondary,
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.sm,
  },
  chipText: {
    fontFamily: fontFamily.semiBold,
    fontSize: 12,
    color: colors.secondaryForeground,
  },
  availabilityChip: {
    borderRadius: radii.full,
    backgroundColor: colors.primary,
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.sm,
  },
  availabilityChipText: {
    fontFamily: fontFamily.bold,
    fontSize: 12,
    color: colors.primaryForeground,
  },
  actions: {
    gap: spacing.md,
    marginTop: spacing.xs,
  },
  mapButton: {
    minHeight: 46,
    marginTop: spacing.xs,
  },
  emptyState: {
    alignItems: 'center',
    gap: spacing.sm,
    borderRadius: radii.xl,
    borderWidth: 1,
    borderColor: colors.border,
    backgroundColor: colors.card,
    padding: spacing.xl,
    ...shadows.card,
  },
  emptyTitle: {
    fontFamily: fontFamily.extraBold,
    fontSize: 17,
    color: colors.foreground,
  },
  emptyText: {
    textAlign: 'center',
    fontFamily: fontFamily.regular,
    fontSize: 13,
    lineHeight: 20,
    color: colors.mutedForeground,
  },
  emptyButton: {
    marginTop: spacing.sm,
    minHeight: 46,
  },
});

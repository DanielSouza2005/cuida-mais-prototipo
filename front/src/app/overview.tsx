import { Link, type Href } from 'expo-router';
import type { LucideIcon } from 'lucide-react-native';
import {
  CalendarDays,
  FilePenLine,
  HeartPulse,
  Home,
  IdCard,
  KeyRound,
  LogIn,
  Search,
  ShieldCheck,
  Sparkles,
  User,
  UserPlus,
} from 'lucide-react-native';
import { Pressable, StyleSheet, Text, View } from 'react-native';

import { BrandMark } from '@/components/brand';
import { ScreenContainer } from '@/components/screen-container';
import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';

type OverviewItem = {
  href: Href;
  title: string;
  route: string;
  icon: LucideIcon;
  status?: 'pronto' | 'visual';
};

type Section = {
  title: string;
  range: string;
  items: OverviewItem[];
};

const sections: Section[] = [
  {
    title: 'Autenticação e Cadastro',
    range: 'RF01 · RF02 · RF03',
    items: [
      { href: '/', title: 'Onboarding', route: '/', icon: Sparkles, status: 'pronto' },
      { href: '/login', title: 'Login', route: '/login', icon: LogIn, status: 'pronto' },
      { href: '/signup', title: 'Cadastro família e cuidador', route: '/signup', icon: UserPlus, status: 'pronto' },
      { href: '/forgot-password', title: 'Esqueci a senha', route: '/forgot-password', icon: KeyRound, status: 'pronto' },
      { href: '/reset-password', title: 'Nova senha', route: '/reset-password', icon: ShieldCheck, status: 'pronto' },
    ],
  },
  {
    title: 'Versão Família / Paciente',
    range: 'RF04 · RF05 · RF06 · RF11',
    items: [
      { href: '/profile', title: 'Perfil', route: '/profile', icon: User, status: 'visual' },
      { href: '/caregiver-search' as Href, title: 'Buscar cuidadores', route: '/caregiver-search', icon: Search, status: 'visual' },
      { href: '/caregiver-profile/ana-paula-martins' as Href, title: 'Perfil de cuidador', route: '/caregiver-profile/[id]', icon: IdCard, status: 'visual' },
      { href: '/edit-profile', title: 'Editar perfil', route: '/edit-profile', icon: FilePenLine, status: 'visual' },
      { href: '/overview', title: 'Visão geral', route: '/overview', icon: Home, status: 'visual' },
      { href: '/profile', title: 'Minha rotina', route: '/profile', icon: CalendarDays, status: 'visual' },
      { href: '/edit-profile', title: 'Dados pessoais', route: '/edit-profile', icon: IdCard, status: 'visual' },
      { href: '/profile', title: 'Plano de cuidado', route: '/profile', icon: HeartPulse, status: 'visual' },
    ],
  },
];

export default function OverviewScreen() {
  return (
    <ScreenContainer contentStyle={styles.content}>
      <View style={styles.header}>
        <BrandMark />
        <Text style={styles.title}>Visão geral do protótipo</Text>
        <Text style={styles.description}>
          Todas as telas do app, organizadas por fluxo. Cada cartão abre a rota navegável dentro da migração visual.
        </Text>
      </View>

      <View style={styles.sections}>
        {sections.map((section) => (
          <View key={section.title} style={styles.section}>
            <View style={styles.sectionHeader}>
              <Text style={styles.sectionTitle}>{section.title}</Text>
              <Text style={styles.sectionRange}>{section.range}</Text>
            </View>

            <View style={styles.grid}>
              {section.items.map((item) => (
                <OverviewCard key={`${section.title}-${item.title}`} item={item} />
              ))}
            </View>
          </View>
        ))}
      </View>
    </ScreenContainer>
  );
}

function OverviewCard({ item }: { item: OverviewItem }) {
  const Icon = item.icon;

  return (
    <Link href={item.href} asChild>
      <Pressable style={({ pressed }) => [styles.card, pressed && styles.cardPressed]}>
        <View style={styles.iconBox}>
          <Icon color={colors.primary} size={16} strokeWidth={2.4} />
        </View>
        <View style={styles.cardCopy}>
          <Text numberOfLines={1} style={styles.cardTitle}>{item.title}</Text>
          <Text numberOfLines={1} style={styles.cardRoute}>{item.route}</Text>
        </View>
        <View style={styles.statusPill}>
          <Text style={styles.statusText}>{item.status === 'pronto' ? 'pronto' : 'visual'}</Text>
        </View>
      </Pressable>
    </Link>
  );
}

const styles = StyleSheet.create({
  content: {
    paddingHorizontal: spacing.lg,
    paddingBottom: spacing.lg,
  },
  header: {
    paddingTop: spacing.sm,
    gap: spacing.sm,
  },
  title: {
    marginTop: spacing.md,
    fontFamily: fontFamily.extraBold,
    fontSize: 24,
    lineHeight: 30,
    letterSpacing: -0.8,
    color: colors.foreground,
  },
  description: {
    maxWidth: 360,
    fontFamily: fontFamily.regular,
    fontSize: 12,
    lineHeight: 18,
    color: colors.mutedForeground,
  },
  sections: {
    marginTop: spacing.xl,
    gap: spacing.xl,
  },
  section: {
    gap: spacing.md,
  },
  sectionHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: spacing.md,
  },
  sectionTitle: {
    flex: 1,
    fontFamily: fontFamily.extraBold,
    fontSize: 13,
    color: colors.foreground,
  },
  sectionRange: {
    fontFamily: fontFamily.bold,
    fontSize: 9,
    letterSpacing: 0.5,
    color: colors.coral,
  },
  grid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: spacing.md,
  },
  card: {
    width: '47%',
    minHeight: 70,
    borderRadius: radii.lg,
    backgroundColor: colors.card,
    borderWidth: 1,
    borderColor: colors.border,
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.md,
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.sm,
    ...shadows.card,
  },
  cardPressed: {
    opacity: 0.78,
    transform: [{ scale: 0.99 }],
  },
  iconBox: {
    width: 30,
    height: 30,
    borderRadius: radii.full,
    backgroundColor: colors.secondary,
    alignItems: 'center',
    justifyContent: 'center',
  },
  cardCopy: {
    flex: 1,
    minWidth: 0,
  },
  cardTitle: {
    fontFamily: fontFamily.bold,
    fontSize: 11,
    color: colors.foreground,
  },
  cardRoute: {
    marginTop: 2,
    fontFamily: fontFamily.regular,
    fontSize: 9,
    color: colors.mutedForeground,
  },
  statusPill: {
    minWidth: 31,
    height: 18,
    borderRadius: radii.full,
    backgroundColor: colors.mint,
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: spacing.xs,
  },
  statusText: {
    fontFamily: fontFamily.bold,
    fontSize: 7,
    color: colors.mintForeground,
  },
});

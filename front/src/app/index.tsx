import { Link } from 'expo-router';
import { ArrowRight } from 'lucide-react-native';
import { Image, StyleSheet, Text, View } from 'react-native';

import { PrimaryButton } from '@/components/primary-button';
import { ScreenContainer } from '@/components/screen-container';
import { colors, fontFamily, radii, shadows, spacing } from '@/theme/tokens';

const heroImage = require('../../assets/images/hero-cuida.png');

export default function OnboardingScreen() {
  return (
    <ScreenContainer contentStyle={styles.content}>
      <View style={styles.handle} />

      <View style={styles.hero}>
        <Image
          source={heroImage}
          resizeMode="cover"
          style={styles.heroImage}
          accessibilityLabel="Cuidadora conversando com uma pessoa idosa em casa"
        />
      </View>

      <View style={styles.copy}>
        <Text style={styles.title}>
          Bem-vindo ao Cuidar<Text style={styles.titlePlus}>+</Text>
        </Text>
        <Text style={styles.description}>
          Conectando corações e organizando o cuidado domiciliar com carinho e tecnologia.
        </Text>
      </View>

      <View style={styles.actions}>
        <Link href="/login" asChild>
          <PrimaryButton label="Começar" icon={ArrowRight} style={styles.startButton} />
        </Link>

        <View style={styles.dots}>
          <View style={styles.activeDot} />
          <View style={styles.dot} />
          <View style={styles.dot} />
        </View>

        <Text style={styles.loginHint}>
          Já possui uma conta? <Link href="/login" style={styles.link}>Já tenho conta</Link>
        </Text>

        <Link href="/overview" style={styles.overviewLink}>
          Ver visão geral do protótipo →
        </Link>
      </View>
    </ScreenContainer>
  );
}

const styles = StyleSheet.create({
  content: {
    flexGrow: 1,
    paddingHorizontal: spacing.xl,
    paddingBottom: spacing.lg,
  },
  handle: {
    alignSelf: 'center',
    width: 48,
    height: 5,
    borderRadius: radii.full,
    backgroundColor: colors.border,
    marginBottom: spacing.lg,
  },
  hero: {
    width: '100%',
    aspectRatio: 1,
    borderRadius: radii.xxl,
    backgroundColor: colors.muted,
    overflow: 'hidden',
    marginBottom: spacing.xxl,
    ...shadows.card,
  },
  heroImage: {
    width: '100%',
    height: '100%',
  },
  copy: {
    alignItems: 'center',
    gap: spacing.sm,
    paddingHorizontal: spacing.md,
  },
  title: {
    textAlign: 'center',
    fontFamily: fontFamily.extraBold,
    fontSize: 22,
    lineHeight: 28,
    letterSpacing: -0.5,
    color: colors.foreground,
  },
  titlePlus: {
    color: colors.primary,
  },
  description: {
    maxWidth: 300,
    textAlign: 'center',
    fontFamily: fontFamily.regular,
    fontSize: 13,
    lineHeight: 20,
    color: colors.mutedForeground,
  },
  actions: {
    marginTop: 'auto',
    paddingTop: spacing.xxl,
    gap: spacing.lg,
    alignItems: 'center',
    width: '100%',
  },
  startButton: {
    width: '100%',
    minHeight: 58,
    borderRadius: radii.xl,
  },
  dots: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.xs,
  },
  activeDot: {
    width: 22,
    height: 5,
    borderRadius: radii.full,
    backgroundColor: colors.primary,
  },
  dot: {
    width: 5,
    height: 5,
    borderRadius: radii.full,
    backgroundColor: colors.border,
  },
  loginHint: {
    fontFamily: fontFamily.regular,
    fontSize: 12,
    color: colors.mutedForeground,
  },
  link: {
    fontFamily: fontFamily.semiBold,
    color: colors.primary,
  },
  overviewLink: {
    fontFamily: fontFamily.medium,
    fontSize: 11,
    color: colors.mutedForeground,
  },
});

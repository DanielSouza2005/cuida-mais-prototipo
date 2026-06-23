import { Platform } from 'react-native';

export const colors = {
  background: '#FCFBF7', foreground: '#1B2D40', card: '#FFFFFF', primary: '#2F8FC4',
  primaryForeground: '#F8FCFF', secondary: '#E7F3F5', secondaryForeground: '#31516D',
  muted: '#F1F4F5', mutedForeground: '#6C7A86', accent: '#79C9A8', mint: '#E1F4EC',
  mintForeground: '#315E58', sage: '#79C9A8', coral: '#E58968', sunny: '#F4DE91',
  border: '#E3E9EC', destructive: '#D64D45',
} as const;

export const spacing = { xxs: 4, xs: 6, sm: 8, md: 12, lg: 16, xl: 24, xxl: 32, xxxl: 40 } as const;
export const radii = { sm: 10, md: 12, lg: 16, xl: 22, xxl: 28, xxxl: 32, full: 999 } as const;
export const fontFamily = {
  regular: 'PlusJakartaSans_400Regular', medium: 'PlusJakartaSans_500Medium',
  semiBold: 'PlusJakartaSans_600SemiBold', bold: 'PlusJakartaSans_700Bold',
  extraBold: 'PlusJakartaSans_800ExtraBold',
} as const;
export const shadows = {
  card: Platform.select({ ios: { shadowColor: '#263C50', shadowOpacity: 0.06, shadowRadius: 12, shadowOffset: { width: 0, height: 2 } }, android: { elevation: 2 } }),
  soft: Platform.select({ ios: { shadowColor: '#263C50', shadowOpacity: 0.1, shadowRadius: 16, shadowOffset: { width: 0, height: 4 } }, android: { elevation: 4 } }),
  glow: Platform.select({ ios: { shadowColor: colors.primary, shadowOpacity: 0.35, shadowRadius: 14, shadowOffset: { width: 0, height: 8 } }, android: { elevation: 6 } }),
} as const;

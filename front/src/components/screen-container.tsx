import type { PropsWithChildren, Ref } from 'react';
import { useSegments } from 'expo-router';
import {
  KeyboardAvoidingView,
  Platform,
  ScrollView,
  StyleSheet,
  type ScrollViewProps,
  type StyleProp,
  View,
  type ViewStyle,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import type { Edge } from 'react-native-safe-area-context';

import { colors, spacing } from '@/theme/tokens';

type Props = PropsWithChildren<{
  contentStyle?: StyleProp<ViewStyle>;
  scroll?: boolean;
  keyboardAvoiding?: boolean;
  safeAreaEdges?: Edge[];
  scrollViewRef?: Ref<ScrollView>;
  scrollViewProps?: Omit<ScrollViewProps, 'contentContainerStyle'>;
}>;

export function ScreenContainer({
  children,
  contentStyle,
  scroll = true,
  keyboardAvoiding = false,
  safeAreaEdges,
  scrollViewRef,
  scrollViewProps,
}: Props) {
  const segments = useSegments();
  const insideTabs = (segments as string[])[0] === '(tabs)';
  const resolvedEdges = safeAreaEdges ?? (insideTabs ? ['top', 'right', 'left'] : ['top', 'right', 'bottom', 'left']);
  const content = scroll ? (
    <ScrollView
      ref={scrollViewRef}
      {...scrollViewProps}
      contentContainerStyle={[styles.content, contentStyle]}
      keyboardShouldPersistTaps="handled"
      showsVerticalScrollIndicator={false}
    >
      {children}
    </ScrollView>
  ) : (
    <View style={[styles.staticContent, contentStyle]}>{children}</View>
  );

  return (
    <SafeAreaView style={styles.safeArea} edges={resolvedEdges}>
      {keyboardAvoiding ? (
        <KeyboardAvoidingView
          behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
          style={styles.keyboard}
        >
          {content}
        </KeyboardAvoidingView>
      ) : (
        content
      )}
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: colors.background,
  },
  keyboard: {
    flex: 1,
  },
  content: {
    flexGrow: 1,
    paddingTop: spacing.md,
    paddingBottom: spacing.lg,
  },
  staticContent: {
    flex: 1,
    paddingTop: spacing.md,
  },
});

import type { PropsWithChildren } from 'react';
import {
  KeyboardAvoidingView,
  Platform,
  ScrollView,
  StyleSheet,
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
}>;

export function ScreenContainer({
  children,
  contentStyle,
  scroll = true,
  keyboardAvoiding = false,
  safeAreaEdges = ['top', 'right', 'bottom', 'left'],
}: Props) {
  const content = scroll ? (
    <ScrollView
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
    <SafeAreaView style={styles.safeArea} edges={safeAreaEdges}>
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
  },
  staticContent: {
    flex: 1,
    paddingTop: spacing.md,
  },
});

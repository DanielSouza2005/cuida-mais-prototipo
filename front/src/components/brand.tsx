import { Image, StyleSheet, View } from 'react-native';

import { BrandAssets } from '@/constants/brandAssets';

export function Brand({ size = 'small', centered = false }: { size?: 'small' | 'large'; centered?: boolean }) {
  const large = size === 'large';
  return (
    <View style={[styles.brand, large && styles.largeBrand, centered && styles.centered]}>
      <Image
        accessible
        accessibilityLabel="Cuida+. Cuidado simples, próximo e organizado."
        resizeMode="contain"
        source={BrandAssets.logoHorizontal}
        style={styles.horizontalLogo}
      />
    </View>
  );
}

export function BrandMark() {
  return (
    <Image
      accessible
      accessibilityLabel="Cuida+"
      resizeMode="contain"
      source={BrandAssets.logoMark}
      style={styles.mark}
    />
  );
}

const styles = StyleSheet.create({
  brand: { width: 170, height: 55 },
  largeBrand: { width: 280, height: 91 },
  centered: { alignSelf: 'center' },
  horizontalLogo: { width: '100%', height: '100%' },
  mark: { width: 40, height: 40 },
});

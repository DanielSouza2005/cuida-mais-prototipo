import { Image } from 'react-native';

import { BrandAssets } from '@/constants/brandAssets';

export function BrandMark({ size = 40 }: { size?: number }) {
  return (
    <Image
      accessible
      accessibilityLabel="Cuida+"
      resizeMode="contain"
      source={BrandAssets.logoMark}
      style={{ width: size, height: size }}
    />
  );
}

import { useEffect } from 'react';
import { useNavigation } from 'expo-router';

type NavigationEvent = {
  preventDefault: () => void;
};

type NavigationWithEvents = {
  addListener?: (eventName: 'beforeRemove', callback: (event: NavigationEvent) => void) => () => void;
};

export function useBlockNavigationWhenBusy(isBusy: boolean) {
  const navigation = useNavigation() as NavigationWithEvents;

  useEffect(() => {
    if (!navigation.addListener) return undefined;

    return navigation.addListener('beforeRemove', (event) => {
      if (!isBusy) return;
      event.preventDefault();
    });
  }, [isBusy, navigation]);
}

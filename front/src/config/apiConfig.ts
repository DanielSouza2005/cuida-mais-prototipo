const rawDelay = Number(process.env.EXPO_PUBLIC_API_ARTIFICIAL_DELAY_MS ?? 0);

export const API_ARTIFICIAL_DELAY_MS =
  typeof __DEV__ !== 'undefined' && __DEV__ && Number.isFinite(rawDelay) && rawDelay > 0
    ? rawDelay
    : 0;

export function sleep(ms: number) {
  return new Promise<void>((resolve) => {
    setTimeout(resolve, ms);
  });
}

export async function applyArtificialApiDelay() {
  if (API_ARTIFICIAL_DELAY_MS > 0) {
    await sleep(API_ARTIFICIAL_DELAY_MS);
  }
}

export function guardPress<TArgs extends unknown[]>(isBusy: boolean, callback?: (...args: TArgs) => void) {
  return (...args: TArgs) => {
    if (isBusy) return;
    callback?.(...args);
  };
}

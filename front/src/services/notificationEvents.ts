type Listener = () => void;

const listeners = new Set<Listener>();

export function emitNotificationsChanged() {
  listeners.forEach((listener) => listener());
}

export function subscribeNotificationsChanged(listener: Listener) {
  listeners.add(listener);
  return () => { listeners.delete(listener); };
}

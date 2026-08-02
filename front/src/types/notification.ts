export type NotificationItem = {
  id: string;
  type: string;
  title: string;
  message: string;
  relatedEntityType: string;
  relatedEntityId: string;
  readAt?: string | null;
  createdAt: string;
};

export type NotificationPreferenceItem = {
  type: string;
  label: string;
  description: string;
  enabled: boolean;
  configurable: boolean;
  required: boolean;
  icon: string;
  colorKey: string;
};

export type NotificationPreferenceGroup = {
  category: string;
  categoryLabel: string;
  items: NotificationPreferenceItem[];
};

export type NotificationPreferencesResponse = {
  groups: NotificationPreferenceGroup[];
};

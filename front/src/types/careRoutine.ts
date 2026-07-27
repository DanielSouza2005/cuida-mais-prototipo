import type { Medication, TaskCategory, TaskPriority, TaskRecurrenceType, TaskWeekday } from '@/types/careTasks';

export type CareRoutineItem = {
  id?: string; title: string; description?: string | null; sortOrder: number;
  category?: TaskCategory | null; categoryLabel?: string; customCategory?: string | null; priority?: TaskPriority | null;
  recurrenceType?: TaskRecurrenceType | null; scheduledTime?: string | null; intervalDays?: number | null; weekdays?: TaskWeekday[];
  reminderEnabled?: boolean | null; reminderMinutesBefore?: number | null; notes?: string | null; medication?: Medication | null;
};
export type CareRoutine = {
  id: string; name: string; description?: string | null; active: boolean;
  assistedPerson?: { id: string; name: string } | null;
  items: CareRoutineItem[]; createdAt: string; updatedAt: string;
};
export type CareRoutinePayload = { name: string; description: string | null; assistedPersonId: string | null; items: {
  title: string; description: string | null; sortOrder: number; category: TaskCategory; customCategory: string | null;
  priority: TaskPriority; recurrenceType: TaskRecurrenceType; scheduledTime: string; intervalDays: number | null;
  weekdays: TaskWeekday[]; reminderEnabled: boolean; reminderMinutesBefore: number | null; notes: string | null; medication: Medication | null;
}[] };
export type CareRoutineFormData = { assistedPersons: { id: string; name: string }[] };

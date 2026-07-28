import CaregiverTasksScreen from '../caregiver-tasks';
import ResponsibleCareOccurrencesScreen from '../responsible-care-occurrences';
import { useAuth } from '@/hooks/useAuth';

export default function CareOccurrencesTab() {
  const { user } = useAuth();
  return user?.userType === 'caregiver'
    ? <CaregiverTasksScreen />
    : <ResponsibleCareOccurrencesScreen />;
}

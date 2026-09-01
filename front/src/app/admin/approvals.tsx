import { useLocalSearchParams } from 'expo-router';
import { AdministrationView, type AdministrationArea } from '../administration';
import type { CaregiverApprovalStatus } from '@/types/admin';

export default function AdminApprovalsScreen(){const {profile,status}=useLocalSearchParams<{profile?:string;status?:string}>();const area:AdministrationArea=profile==='responsibles'?'responsibles':'caregivers';const initial=['PENDENTE','APROVADO','REPROVADO','BLOQUEADO'].includes(status??'')?status as CaregiverApprovalStatus:'PENDENTE';return <AdministrationView initialArea={area} areas={['caregivers','responsibles']} initialApprovalStatus={initial}/>;}

import { useLocalSearchParams } from 'expo-router';
import { AdministrationView } from '../administration';
import type { AccountStatus } from '@/types/admin';

export default function AdminUsersScreen(){const {status}=useLocalSearchParams<{status?:string}>();const initial=status==='ATIVO'||status==='BLOQUEADO'||status==='INATIVO'?status as AccountStatus:undefined;return <AdministrationView initialArea="users" areas={['users']} initialAccountStatus={initial}/>;}

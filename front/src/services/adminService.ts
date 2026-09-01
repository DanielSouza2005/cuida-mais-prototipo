import { apiRequest } from '@/services/api';
import type { AccountStatus, AdminCaregiverDetails, AdminCaregiverPage, AdminDashboardSummary, AdminResponsibleDetails, AdminResponsiblePage, AdminUserDetails, AdminUserPage, CaregiverApprovalStatus, ResponsibleApprovalStatus } from '@/types/admin';

function query(params:Record<string,string|number|undefined|null>) {
  const entries = Object.entries(params).filter(([,value]) => value !== undefined && value !== null && value !== '');
  return entries.length ? `?${entries.map(([key,value]) => `${encodeURIComponent(key)}=${encodeURIComponent(String(value))}`).join('&')}` : '';
}
export function getDashboard() { return apiRequest<AdminDashboardSummary>('/api/admin/dashboard'); }
export function listUsers(search:string, status?:AccountStatus) { return apiRequest<AdminUserPage>(`/api/admin/users${query({query:search,status})}`); }
export function getUser(id:string) { return apiRequest<AdminUserDetails>(`/api/admin/users/${id}`); }
export function blockUser(id:string, reason:string) { return apiRequest<AdminUserDetails>(`/api/admin/users/${id}/block`,{method:'PATCH',body:{reason}}); }
export function unblockUser(id:string) { return apiRequest<AdminUserDetails>(`/api/admin/users/${id}/unblock`,{method:'PATCH'}); }
export function listCaregivers(search:string, status?:CaregiverApprovalStatus) { return apiRequest<AdminCaregiverPage>(`/api/admin/caregivers${query({query:search,status})}`); }
export function getCaregiver(id:string) { return apiRequest<AdminCaregiverDetails>(`/api/admin/caregivers/${id}`); }
export function approveCaregiver(id:string) { return apiRequest<AdminCaregiverDetails>(`/api/admin/caregivers/${id}/approve`,{method:'PATCH'}); }
export function rejectCaregiver(id:string,reason:string) { return apiRequest<AdminCaregiverDetails>(`/api/admin/caregivers/${id}/reject`,{method:'PATCH',body:{reason}}); }
export function blockCaregiver(id:string,reason:string) { return apiRequest<AdminCaregiverDetails>(`/api/admin/caregivers/${id}/block`,{method:'PATCH',body:{reason}}); }
export function listResponsibles(search:string, status?:ResponsibleApprovalStatus) { return apiRequest<AdminResponsiblePage>(`/api/admin/responsibles${query({query:search,status})}`); }
export function getResponsible(id:string) { return apiRequest<AdminResponsibleDetails>(`/api/admin/responsibles/${id}`); }
export function approveResponsible(id:string) { return apiRequest<AdminResponsibleDetails>(`/api/admin/responsibles/${id}/approve`,{method:'PATCH'}); }
export function rejectResponsible(id:string,reason:string) { return apiRequest<AdminResponsibleDetails>(`/api/admin/responsibles/${id}/reject`,{method:'PATCH',body:{reason}}); }
export function blockResponsible(id:string,reason:string) { return apiRequest<AdminResponsibleDetails>(`/api/admin/responsibles/${id}/block`,{method:'PATCH',body:{reason}}); }

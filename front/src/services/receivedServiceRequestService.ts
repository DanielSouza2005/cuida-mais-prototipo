import { apiRequest } from '@/services/api';
import type { CaregiverNotification, ReceivedServiceRequest, ReceivedServiceRequestPage, RejectionReasonPayload, ResponsibleServiceRequest } from '@/types/receivedServiceRequest';
import type { ServiceRequestStatus } from '@/types/serviceRequest';
export function getReceivedServiceRequests(status?: ServiceRequestStatus,page=0,size=10){const params=new URLSearchParams({page:String(page),size:String(size)});if(status)params.set('status',status);return apiRequest<ReceivedServiceRequestPage>(`/api/caregiver/service-requests?${params.toString()}`);}
export function getReceivedServiceRequestDetails(id:string){return apiRequest<ReceivedServiceRequest>(`/api/caregiver/service-requests/${id}`);}
export function getResponsibleServiceRequestDetails(id:string){return apiRequest<ResponsibleServiceRequest>(`/api/responsible/service-requests/${id}`);}
export function acceptServiceRequest(id:string){return apiRequest<ReceivedServiceRequest>(`/api/caregiver/service-requests/${id}/accept`,{method:'POST'});}
export function rejectServiceRequest(id:string,payload:RejectionReasonPayload){return apiRequest<ReceivedServiceRequest>(`/api/caregiver/service-requests/${id}/reject`,{method:'POST',body:payload});}
export function getNotifications(){return apiRequest<CaregiverNotification[]>('/api/notifications');}
export function getUnreadNotificationCount(){return apiRequest<{count:number}>('/api/notifications/unread-count');}
export function readNotification(id:string){return apiRequest<CaregiverNotification>(`/api/notifications/${id}/read`,{method:'PATCH'});}
export function clearAllNotifications(){return apiRequest<{message:string}>('/api/notifications/clear-all',{method:'PATCH'});}

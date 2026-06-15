import api from './api';

export type ApplicationStatus = 
  | 'SAVED' 
  | 'APPLIED' 
  | 'RECRUITER_SCREEN' 
  | 'ONLINE_ASSESSMENT' 
  | 'TECHNICAL_INTERVIEW' 
  | 'FINAL_INTERVIEW' 
  | 'OFFER_RECEIVED' 
  | 'REJECTED' 
  | 'WITHDRAWN';

export type CompanyType = 
  | 'FINTECH' 
  | 'PRODUCT' 
  | 'CONSULTING' 
  | 'BANKING' 
  | 'OTHER';

export interface JobApplicationRequest {
  companyName: string;
  roleTitle: string;
  location?: string;
  status: ApplicationStatus;
  companyType?: CompanyType;
  url?: string;
  notes?: string;
  appliedDate?: string;
  resumeId?: number;
  coverLetterId?: number;
}

export interface JobApplicationResponse {
  id: number;
  companyName: string;
  roleTitle: string;
  location: string;
  status: ApplicationStatus;
  companyType: CompanyType;
  url: string;
  notes: string;
  appliedDate: string;
  resumeId?: number;
  resumeFileName?: string;
  coverLetterId?: number;
  coverLetterVersionNumber?: number;
  createdAt: string;
  updatedAt: string;
}

export const getJobApplications = async (): Promise<JobApplicationResponse[]> => {
  const response = await api.get('/job-applications');
  return response.data;
};

export const createJobApplication = async (request: JobApplicationRequest): Promise<JobApplicationResponse> => {
  const response = await api.post('/job-applications', request);
  return response.data;
};

export const updateJobApplication = async (id: number, request: JobApplicationRequest): Promise<JobApplicationResponse> => {
  const response = await api.put(`/job-applications/${id}`, request);
  return response.data;
};

export const updateJobApplicationStatus = async (id: number, status: ApplicationStatus): Promise<JobApplicationResponse> => {
  const response = await api.put(`/job-applications/${id}/status`, { status });
  return response.data;
};

export const deleteJobApplication = async (id: number): Promise<void> => {
  await api.delete(`/job-applications/${id}`);
};

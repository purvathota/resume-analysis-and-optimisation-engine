import api from './api';

export interface CoverLetterRequest {
  resumeId: number;
  jobDescriptionId?: number;
  companyName: string;
  roleTitle: string;
  versionNotes?: string;
}

export interface Traceability {
  referencedExperiences: string[];
  referencedProjects: string[];
  referencedTechnologies: string[];
  referencedMetrics: string[];
  referencedCertifications: string[];
  referencedAchievements: string[];
}

export interface CoverLetterVersionResponse {
  id: number;
  versionNumber: number;
  versionNotes?: string;
  generatedContent: string;
  traceability: Traceability;
  createdAt: string;
}

export interface CoverLetterResponse {
  id: number;
  companyName: string;
  roleTitle: string;
  versions: CoverLetterVersionResponse[];
  createdAt: string;
}

export const generateCoverLetter = async (request: CoverLetterRequest): Promise<CoverLetterResponse> => {
  const response = await api.post('/cover-letters/generate', request);
  return response.data;
};

export const getUserCoverLetters = async (): Promise<CoverLetterResponse[]> => {
  const response = await api.get('/cover-letters');
  return response.data;
};

export const getCoverLetter = async (id: number): Promise<CoverLetterResponse> => {
  const response = await api.get(`/cover-letters/${id}`);
  return response.data;
};

export const downloadCoverLetterPdf = async (versionId: number, companyName: string, versionNumber: number): Promise<void> => {
  const response = await api.get(`/cover-letters/versions/${versionId}/pdf`, { responseType: 'blob' });
  const url = window.URL.createObjectURL(new Blob([response.data]));
  const link = document.createElement('a');
  link.href = url;
  link.setAttribute('download', `CoverLetter_${companyName}_V${versionNumber}.pdf`);
  document.body.appendChild(link);
  link.click();
  link.remove();
};

export const downloadCoverLetterDocx = async (versionId: number, companyName: string, versionNumber: number): Promise<void> => {
  const response = await api.get(`/cover-letters/versions/${versionId}/docx`, { responseType: 'blob' });
  const url = window.URL.createObjectURL(new Blob([response.data]));
  const link = document.createElement('a');
  link.href = url;
  link.setAttribute('download', `CoverLetter_${companyName}_V${versionNumber}.docx`);
  document.body.appendChild(link);
  link.click();
  link.remove();
};

export const deleteCoverLetterVersion = async (versionId: number): Promise<void> => {
  await api.delete(`/cover-letters/versions/${versionId}`);
};

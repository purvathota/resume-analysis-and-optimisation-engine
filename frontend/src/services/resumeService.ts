import api from './api';

export const getResumes = async (): Promise<any[]> => {
  const response = await api.get('/resumes');
  return response.data;
};

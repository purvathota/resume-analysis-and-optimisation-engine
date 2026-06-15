import api from './api';

export const getJobDescriptions = async (): Promise<any[]> => {
  const response = await api.get('/job-descriptions');
  return response.data;
};

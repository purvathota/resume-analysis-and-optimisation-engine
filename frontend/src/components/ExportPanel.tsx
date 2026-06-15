import { useState } from 'react';
import api from '../services/api';

interface ExportPanelProps {
  resumeId: number;
  jobDescriptionId: number;
}

export default function ExportPanel({ resumeId, jobDescriptionId }: ExportPanelProps) {
  const [downloadingPdf, setDownloadingPdf] = useState(false);
  const [downloadingDocx, setDownloadingDocx] = useState(false);

  const handleDownload = async (format: 'pdf' | 'docx') => {
    try {
      if (format === 'pdf') setDownloadingPdf(true);
      else setDownloadingDocx(true);

      const response = await api.get(`/analyses/${resumeId}/export/${format}`, {
        params: { jobDescriptionId },
        responseType: 'blob'
      });

      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `Optimized_Resume.${format}`);
      document.body.appendChild(link);
      link.click();
      link.parentNode?.removeChild(link);
    } catch (error) {
      console.error(`Error downloading ${format}:`, error);
      alert(`Failed to download ${format.toUpperCase()}`);
    } finally {
      if (format === 'pdf') setDownloadingPdf(false);
      else setDownloadingDocx(false);
    }
  };

  return (
    <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-200 mt-6 flex flex-col items-center">
      <h2 className="text-xl font-bold mb-4 text-gray-800">Download Tailored Resume</h2>
      <div className="flex gap-4">
        <button
          onClick={() => handleDownload('pdf')}
          disabled={downloadingPdf}
          className="px-6 py-2 bg-red-600 text-white font-bold rounded shadow hover:bg-red-700 disabled:opacity-50"
        >
          {downloadingPdf ? 'Generating PDF...' : 'Download PDF'}
        </button>
        <button
          onClick={() => handleDownload('docx')}
          disabled={downloadingDocx}
          className="px-6 py-2 bg-blue-600 text-white font-bold rounded shadow hover:bg-blue-700 disabled:opacity-50"
        >
          {downloadingDocx ? 'Generating DOCX...' : 'Download DOCX'}
        </button>
      </div>
    </div>
  );
}

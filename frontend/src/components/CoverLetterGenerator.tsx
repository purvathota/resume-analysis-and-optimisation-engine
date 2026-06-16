import React, { useState, useEffect } from 'react';
import { generateCoverLetter, type CoverLetterRequest, type CoverLetterResponse } from '../services/coverLetterService';
import { getResumes } from '../services/resumeService';
import { getJobDescriptions } from '../services/jobDescriptionService';
import CoverLetterVersionHistory from './CoverLetterVersionHistory';

const CoverLetterGenerator: React.FC = () => {
  const [resumes, setResumes] = useState<any[]>([]);
  const [jds, setJds] = useState<any[]>([]);
  const [selectedResumeId, setSelectedResumeId] = useState<number | ''>('');
  const [selectedJdId, setSelectedJdId] = useState<number | ''>('');
  const [companyName, setCompanyName] = useState('');
  const [roleTitle, setRoleTitle] = useState('');
  const [versionNotes, setVersionNotes] = useState('');

  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [result, setResult] = useState<CoverLetterResponse | null>(null);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [resumeRes, jdRes] = await Promise.all([getResumes(), getJobDescriptions()]);
        setResumes(resumeRes);
        setJds(jdRes);
      } catch (err) {
        console.error('Failed to load initial data', err);
      }
    };
    fetchData();
  }, []);

  const handleGenerate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedResumeId || !companyName || !roleTitle) return;

    setIsLoading(true);
    setError(null);
    setResult(null);

    try {
      const req: CoverLetterRequest = {
        resumeId: Number(selectedResumeId),
        jobDescriptionId: selectedJdId ? Number(selectedJdId) : undefined,
        companyName,
        roleTitle,
        versionNotes: versionNotes || undefined
      };
      const response = await generateCoverLetter(req);
      setResult(response);
      setVersionNotes(''); // Clear notes after successful generation
    } catch (err: any) {
      if (err.response?.data?.message === "COVER_LETTER_VALIDATION_FAILED") {
        setError("Cover letter failed integrity validation. Review generated content.");
      } else {
        setError(err.response?.data?.message || err.message || "Failed to generate cover letter.");
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="flex flex-col lg:flex-row gap-6 p-6">
      {/* Input Panel */}
      <div className="flex-1 bg-white dark:bg-gray-800 rounded-xl shadow p-6 border border-gray-100 dark:border-gray-700 h-fit sticky top-6">
        <h2 className="text-2xl font-bold mb-6 text-gray-900 dark:text-white">AI Cover Letter Generator</h2>
        
        <form onSubmit={handleGenerate} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Select Resume *</label>
            <select 
              required
              className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm dark:bg-gray-700 dark:border-gray-600 dark:text-white"
              value={selectedResumeId}
              onChange={(e) => setSelectedResumeId(e.target.value ? Number(e.target.value) : '')}
            >
              <option value="">-- Choose a Resume --</option>
              {resumes.map(r => (
                <option key={r.id} value={r.id}>{r.fileName}</option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Select Job Description (Optional)</label>
            <select 
              className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm dark:bg-gray-700 dark:border-gray-600 dark:text-white"
              value={selectedJdId}
              onChange={(e) => setSelectedJdId(e.target.value ? Number(e.target.value) : '')}
            >
              <option value="">-- Choose a JD --</option>
              {jds.map(jd => (
                <option key={jd.id} value={jd.id}>
                  {jd.company && jd.title 
                    ? `${jd.company} - ${jd.title}` 
                    : jd.fileName || `Pasted Job Description #${jd.id}`}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Target Company Name *</label>
            <input 
              required
              type="text"
              placeholder="e.g. Skyscanner"
              className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm dark:bg-gray-700 dark:border-gray-600 dark:text-white"
              value={companyName}
              onChange={(e) => setCompanyName(e.target.value)}
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Target Role Title *</label>
            <input 
              required
              type="text"
              placeholder="e.g. Senior Backend Engineer"
              className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm dark:bg-gray-700 dark:border-gray-600 dark:text-white"
              value={roleTitle}
              onChange={(e) => setRoleTitle(e.target.value)}
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Version Notes (Optional)</label>
            <input 
              type="text"
              placeholder="e.g. Updated JD, Added new project"
              className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm dark:bg-gray-700 dark:border-gray-600 dark:text-white"
              value={versionNotes}
              onChange={(e) => setVersionNotes(e.target.value)}
            />
            <p className="mt-1 text-xs text-gray-500 dark:text-gray-400">Helps you identify this specific version later.</p>
          </div>

          <button
            type="submit"
            disabled={isLoading || !selectedResumeId || !companyName || !roleTitle}
            className="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 disabled:opacity-50 transition-colors"
          >
            {isLoading ? "Generating Recruiter-Grade Cover Letter..." : "Generate Cover Letter"}
          </button>
        </form>

        {error && (
          <div className="mt-4 p-4 bg-red-50 border-l-4 border-red-500 text-red-700 dark:bg-red-900/30 dark:text-red-300">
            {error}
          </div>
        )}
      </div>

      {/* Output Panel */}
      <div className="flex-[2] flex flex-col gap-6">
        {result ? (
          <CoverLetterVersionHistory coverLetter={result} />
        ) : (
          <div className="bg-gray-50 dark:bg-gray-800/50 rounded-xl border-2 border-dashed border-gray-300 dark:border-gray-700 flex items-center justify-center p-12 h-full text-gray-500 dark:text-gray-400">
            Fill out the form and generate your cover letter to see the version history, preview, and traceability analysis here.
          </div>
        )}
      </div>
    </div>
  );
};

export default CoverLetterGenerator;

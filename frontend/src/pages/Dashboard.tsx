import { useState, useContext } from 'react';
import api from '../services/api';
import { AuthContext } from '../context/AuthContext';
import KeywordImprovementReport from '../components/KeywordImprovementReport';
import ExportPanel from '../components/ExportPanel';
import OptimizedResumeView from '../components/OptimizedResumeView';
import ResumeDiffView from '../components/ResumeDiffView';
import ValidationReport from '../components/ValidationReport';
import AtsImpactReport from '../components/AtsImpactReport';
import CoverLetterGenerator from '../components/CoverLetterGenerator';
import JobTracker from '../components/JobTracker';

export default function Dashboard() {
  const auth = useContext(AuthContext);
  const [activeTab, setActiveTab] = useState<'RESUME' | 'COVER_LETTER' | 'JOB_TRACKER'>('RESUME');

  const [resumeFile, setResumeFile] = useState<File | null>(null);
  const [jdText, setJdText] = useState('');
  const [uploading, setUploading] = useState(false);
  const [optimizing, setOptimizing] = useState(false);
  const [results, setResults] = useState<any>(null);
  const [currentResumeId, setCurrentResumeId] = useState<number | null>(null);
  const [currentJdId, setCurrentJdId] = useState<number | null>(null);

  const handleRunAnalysis = async () => {
    if (!resumeFile || !jdText) {
      alert('Please upload a resume and provide a job description.');
      return;
    }

    setUploading(true);
    setResults(null);
    try {
      const resumeFormData = new FormData();
      resumeFormData.append('file', resumeFile);
      const resumeRes = await api.post('/resumes/upload', resumeFormData);
      const resumeId = resumeRes.data.id;
      setCurrentResumeId(resumeId);

      const jdRes = await api.post('/job-descriptions/upload', null, {
        params: { text: jdText }
      });
      const jdId = jdRes.data.id;
      setCurrentJdId(jdId);

      const atsRes = await api.post(`/analyses/${resumeId}/ats`, null, {
        params: { jobDescriptionId: jdId }
      });

      const recruiterRes = await api.post(`/analyses/${resumeId}/recruiter`, null, {
        params: { jobDescriptionId: jdId }
      });

      setResults({ ...atsRes.data, ...recruiterRes.data });
      alert('Analysis Complete! You can now run Resume Optimization.');
    } catch (error) {
      console.error(error);
      alert('An error occurred during analysis.');
    } finally {
      setUploading(false);
    }
  };

  const handleOptimize = async () => {
    if (!currentResumeId || !currentJdId) return;
    setOptimizing(true);
    try {
      const optRes = await api.post(`/analyses/${currentResumeId}/optimize`, null, {
        params: { jobDescriptionId: currentJdId }
      });
      setResults(optRes.data);
      if (optRes.data.analysisStatus === 'OPTIMIZATION_FAILED_VALIDATION') {
        alert('Resume failed integrity validation. Review optimization changes.');
      } else {
        alert('Optimization Complete!');
      }
    } catch (error) {
      console.error(error);
      alert('An error occurred during optimization.');
    } finally {
      setOptimizing(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 p-8 dark:bg-gray-900">
      <div className="max-w-7xl mx-auto">
        <div className="flex justify-between items-center mb-8">
          <h1 className="text-3xl font-bold text-gray-800 dark:text-white">AI Resume Optimizer</h1>
          <button onClick={auth?.logout} className="text-red-600 font-semibold hover:underline">
            Logout
          </button>
        </div>

        {/* Tabs */}
        <div className="flex border-b border-gray-200 dark:border-gray-700 mb-8">
          <button
            className={`py-2 px-4 font-semibold text-sm focus:outline-none ${activeTab === 'RESUME' ? 'border-b-2 border-blue-600 text-blue-600 dark:text-blue-400' : 'text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200'}`}
            onClick={() => setActiveTab('RESUME')}
          >
            Resume Optimizer
          </button>
          <button 
            className={`py-2 px-4 font-semibold text-sm focus:outline-none ${activeTab === 'COVER_LETTER' ? 'border-b-2 border-blue-600 text-blue-600 dark:text-blue-400' : 'text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200'}`}
            onClick={() => setActiveTab('COVER_LETTER')}
          >
            Cover Letter Generator
          </button>
          <button 
            className={`py-2 px-4 font-semibold text-sm focus:outline-none ${activeTab === 'JOB_TRACKER' ? 'border-b-2 border-blue-600 text-blue-600 dark:text-blue-400' : 'text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200'}`}
            onClick={() => setActiveTab('JOB_TRACKER')}
          >
            Job Tracker
          </button>
        </div>

        {activeTab === 'COVER_LETTER' && <CoverLetterGenerator />}
        {activeTab === 'JOB_TRACKER' && <JobTracker />}

        {activeTab === 'RESUME' && (
          <div className="max-w-4xl mx-auto">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-8 mb-8">
              <div className="bg-white dark:bg-gray-800 p-6 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700">
                <h2 className="text-xl font-semibold mb-4 dark:text-white">1. Upload Resume</h2>
                <input 
                  type="file" 
                  accept=".pdf,.docx"
                  onChange={(e) => setResumeFile(e.target.files ? e.target.files[0] : null)}
                  className="w-full text-sm text-gray-500 dark:text-gray-300 file:mr-4 file:py-2 file:px-4 file:rounded-md file:border-0 file:text-sm file:font-semibold file:bg-blue-50 file:text-blue-700 hover:file:bg-blue-100 dark:file:bg-blue-900 dark:file:text-blue-200"
                />
              </div>

              <div className="bg-white dark:bg-gray-800 p-6 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700">
                <h2 className="text-xl font-semibold mb-4 dark:text-white">2. Job Description</h2>
                <textarea 
                  rows={4}
                  placeholder="Paste job description here..."
                  className="w-full border border-gray-300 dark:border-gray-600 rounded-md p-2 text-sm dark:bg-gray-700 dark:text-white"
                  value={jdText}
                  onChange={(e) => setJdText(e.target.value)}
                />
              </div>
            </div>

            <div className="flex justify-center gap-4 mb-8">
              <button 
                onClick={handleRunAnalysis}
                disabled={uploading}
                className="px-8 py-3 bg-blue-600 text-white font-bold rounded-lg shadow-md hover:bg-blue-700 disabled:opacity-50"
              >
                {uploading ? 'Analyzing...' : 'Run Analysis'}
              </button>
              
              {results && (!results.optimizedResumeJson || results.analysisStatus === 'OPTIMIZATION_FAILED_VALIDATION') && (
                <button 
                  onClick={handleOptimize}
                  disabled={optimizing}
                  className="px-8 py-3 bg-green-600 text-white font-bold rounded-lg shadow-md hover:bg-green-700 disabled:opacity-50"
                >
                  {optimizing ? 'Optimizing...' : 'Tailor Resume Now'}
                </button>
              )}
            </div>

            {results && (
              <>
                {results.analysisStatus === 'OPTIMIZATION_FAILED_VALIDATION' && (
                  <div className="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 mb-6 rounded shadow-sm">
                    <p className="font-bold">Optimization Blocked</p>
                    <p>Resume failed integrity validation. Review optimization changes.</p>
                  </div>
                )}
                
                <div className="bg-white dark:bg-gray-800 p-6 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700">
                  <h2 className="text-2xl font-bold mb-6 text-gray-800 dark:text-white">Analysis Results</h2>
                  
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
                    <div className="bg-blue-50 dark:bg-blue-900/30 p-4 rounded-lg">
                      <h3 className="text-lg font-semibold text-blue-800 dark:text-blue-300 mb-2">ATS Match Score</h3>
                      <p className="text-4xl font-bold text-blue-600 dark:text-blue-400">{results.atsScore}%</p>
                    </div>
                    <div className="bg-green-50 dark:bg-green-900/30 p-4 rounded-lg">
                      <h3 className="text-lg font-semibold text-green-800 dark:text-green-300 mb-2">Recruiter Fit</h3>
                      <p className="text-4xl font-bold text-green-600 dark:text-green-400">{results.recruiterFitScore}%</p>
                      <p className="text-sm text-green-700 dark:text-green-400 font-medium mt-1">Shortlist Probability: {results.shortlistingProbability}</p>
                    </div>
                  </div>

                  <div className="mb-6">
                    <h3 className="font-semibold text-gray-800 dark:text-gray-200 mb-2">Missing Technical Keywords</h3>
                    <div className="flex flex-wrap gap-2">
                      {results.missingTechnicalKeywords?.map((kw: string, i: number) => (
                        <span key={i} className="px-3 py-1 bg-red-100 dark:bg-red-900 text-red-800 dark:text-red-200 rounded-full text-sm">{kw}</span>
                      ))}
                    </div>
                  </div>

                  <div className="mb-6">
                    <h3 className="font-semibold text-gray-800 dark:text-gray-200 mb-2">Strengths</h3>
                    <ul className="list-disc pl-5 space-y-1 text-gray-700 dark:text-gray-300 text-sm">
                      {results.strengths?.map((s: string, i: number) => (
                        <li key={i}>{s}</li>
                      ))}
                    </ul>
                  </div>

                  <div>
                    <h3 className="font-semibold text-gray-800 dark:text-gray-200 mb-2">Improvement Suggestions</h3>
                    <div className="space-y-3">
                      {results.improvementSuggestions?.map((s: any, i: number) => (
                        <div key={i} className="bg-gray-50 dark:bg-gray-700/50 p-3 rounded border border-gray-200 dark:border-gray-600">
                          <span className="font-semibold text-gray-700 dark:text-gray-300 text-sm">{s.section}: </span>
                          <span className="text-gray-600 dark:text-gray-400 text-sm">{s.suggestion}</span>
                        </div>
                      ))}
                    </div>
                  </div>
                </div>

                {results.validationReportJson && (
                  <ValidationReport report={results.validationReportJson} />
                )}

                {results.atsImpactReportJson && (
                  <AtsImpactReport report={results.atsImpactReportJson} />
                )}

                {results.missingSkillsJson && results.missingSkillsJson.length > 0 && (
                  <div className="bg-red-50 dark:bg-red-900/20 p-6 rounded-lg shadow-sm border border-red-200 dark:border-red-800 mt-6">
                    <h3 className="text-xl font-bold text-red-800 dark:text-red-400 mb-2">Unsupported Keywords (Not Added)</h3>
                    <p className="text-sm text-red-700 dark:text-red-300 mb-4">The following keywords from the job description were not added because they could not be traced to your existing experience:</p>
                    <div className="flex flex-wrap gap-2">
                      {results.missingSkillsJson.map((kw: string, i: number) => (
                        <span key={i} className="px-3 py-1 bg-red-200 dark:bg-red-800 text-red-900 dark:text-red-100 rounded-full text-sm font-medium">{kw}</span>
                      ))}
                    </div>
                  </div>
                )}

                {results.resumeDiffJson && (
                  <ResumeDiffView diffData={results.resumeDiffJson} />
                )}

                {results.keywordImprovementReportJson && (
                  <KeywordImprovementReport report={results.keywordImprovementReportJson} />
                )}

                {results.optimizedResumeJson && (
                  <>
                    {results.analysisStatus !== 'OPTIMIZATION_FAILED_VALIDATION' && (
                      <ExportPanel resumeId={currentResumeId!} jobDescriptionId={currentJdId!} />
                    )}
                    <OptimizedResumeView resumeData={results.optimizedResumeJson} />
                  </>
                )}
              </>
            )}
          </div>
        )}
      </div>
    </div>
  );
}

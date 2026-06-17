import React, { useState, useEffect } from 'react';
import { type CoverLetterResponse, type CoverLetterVersionResponse, downloadCoverLetterPdf, downloadCoverLetterDocx, deleteCoverLetterVersion } from '../services/coverLetterService';
import TraceabilityPanel from './TraceabilityPanel';
import CoverLetterDiffViewer from './CoverLetterDiffViewer';

interface Props {
  coverLetter: CoverLetterResponse;
}

import { createJobApplication } from '../services/jobApplicationService';

const CoverLetterVersionHistory: React.FC<Props> = ({ coverLetter }) => {
  const [localVersions, setLocalVersions] = useState<CoverLetterVersionResponse[]>(
    [...coverLetter.versions].sort((a, b) => b.versionNumber - a.versionNumber)
  );

  useEffect(() => {
    setLocalVersions([...coverLetter.versions].sort((a, b) => b.versionNumber - a.versionNumber));
  }, [coverLetter]);

  const [selectedVersionId, setSelectedVersionId] = useState<number>(
    localVersions.length > 0 ? localVersions[0].id : 0
  );
  const [compareMode, setCompareMode] = useState<boolean>(false);
  const [isCreatingApp, setIsCreatingApp] = useState(false);

  const versions = localVersions;
  const activeVersion = versions.find(v => v.id === selectedVersionId) || versions[0];
  
  const previousVersion = activeVersion && activeVersion.versionNumber > 1 
    ? versions.find(v => v.versionNumber < activeVersion.versionNumber) 
    : null;

  const handleDeleteVersion = async (v: CoverLetterVersionResponse) => {
    if (versions.length <= 1) {
      alert("Cannot delete the only version of a cover letter.");
      return;
    }
    if (window.confirm(`Are you sure you want to delete Version ${v.versionNumber}?`)) {
      try {
        await deleteCoverLetterVersion(v.id);
        const newVersions = versions.filter(version => version.id !== v.id);
        setLocalVersions(newVersions);
        
        if (selectedVersionId === v.id) {
          setSelectedVersionId(newVersions[0].id);
        }
      } catch (err: any) {
        console.error('Failed to delete version', err);
        alert(err.response?.data?.detail || 'Failed to delete version.');
      }
    }
  };

  const handleDownloadPdf = async (v: CoverLetterVersionResponse) => {
    try {
      await downloadCoverLetterPdf(v.id, coverLetter.companyName, v.versionNumber);
    } catch (err) {
      console.error('Download failed', err);
    }
  };

  const handleDownloadDocx = async (v: CoverLetterVersionResponse) => {
    try {
      await downloadCoverLetterDocx(v.id, coverLetter.companyName, v.versionNumber);
    } catch (err) {
      console.error('Download failed', err);
    }
  };

  const handleCreateApplication = async () => {
    try {
      setIsCreatingApp(true);
      await createJobApplication({
        companyName: coverLetter.companyName,
        roleTitle: coverLetter.roleTitle,
        status: 'SAVED',
        coverLetterId: coverLetter.id
      });
      alert('Application successfully created and saved to your Job Tracker!');
    } catch (err) {
      console.error('Failed to create application', err);
      alert('Failed to create application from Cover Letter.');
    } finally {
      setIsCreatingApp(false);
    }
  };

  if (!activeVersion) return null;

  return (
    <div className="flex flex-col gap-6">
      {/* Version Selector Panel */}
      <div className="bg-white dark:bg-gray-800 rounded-xl shadow border border-gray-100 dark:border-gray-700 p-6">
        <div className="flex justify-between items-start mb-4">
          <div>
            <h2 className="text-2xl font-bold text-gray-900 dark:text-white">
              {coverLetter.companyName} - {coverLetter.roleTitle}
            </h2>
            <p className="text-sm text-gray-500 dark:text-gray-400">Version History</p>
          </div>
          <button 
            onClick={handleCreateApplication}
            disabled={isCreatingApp}
            className="bg-green-600 hover:bg-green-700 text-white px-4 py-2 rounded-md font-medium text-sm transition-colors disabled:opacity-50"
          >
            {isCreatingApp ? 'Creating...' : '+ Create Application'}
          </button>
        </div>

        <div className="overflow-x-auto">
          <div className="flex space-x-4 pb-2">
            {versions.map(v => (
              <div 
                key={v.id} 
                onClick={() => { setSelectedVersionId(v.id); setCompareMode(false); }}
                className={`min-w-[200px] cursor-pointer p-4 rounded-lg border-2 transition-all ${selectedVersionId === v.id ? 'border-blue-500 bg-blue-50 dark:bg-blue-900/20' : 'border-gray-200 dark:border-gray-700 hover:border-blue-300 dark:hover:border-blue-700'}`}
              >
                <div className="flex justify-between items-start mb-2">
                  <span className={`font-bold ${selectedVersionId === v.id ? 'text-blue-700 dark:text-blue-400' : 'text-gray-700 dark:text-gray-300'}`}>
                    Version {v.versionNumber}
                  </span>
                  <span className="text-xs text-gray-500 dark:text-gray-400">
                    {new Date(v.createdAt).toLocaleDateString()}
                  </span>
                </div>
                {v.versionNotes ? (
                  <p className="text-xs text-gray-600 dark:text-gray-400 italic">"{v.versionNotes}"</p>
                ) : (
                  <p className="text-xs text-gray-500 dark:text-gray-500 italic">No notes provided</p>
                )}
                
                {selectedVersionId === v.id && (
                  <div className="flex gap-2 mt-4">
                    <button 
                      onClick={(e) => { e.stopPropagation(); handleDownloadPdf(v); }}
                      className="text-xs px-2 py-1 bg-white dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded hover:bg-gray-50 dark:hover:bg-gray-600 text-gray-700 dark:text-gray-200"
                    >
                      PDF
                    </button>
                    <button 
                      onClick={(e) => { e.stopPropagation(); handleDownloadDocx(v); }}
                      className="text-xs px-2 py-1 bg-white dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded hover:bg-gray-50 dark:hover:bg-gray-600 text-gray-700 dark:text-gray-200"
                    >
                      DOCX
                    </button>
                    <button 
                      onClick={(e) => { e.stopPropagation(); handleDeleteVersion(v); }}
                      className="text-xs px-2 py-1 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded hover:bg-red-100 dark:hover:bg-red-900/40 text-red-600 dark:text-red-400"
                    >
                      Delete
                    </button>
                  </div>
                )}
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Main View Area */}
      <div className="flex justify-between items-center bg-gray-50 dark:bg-gray-800/50 p-4 rounded-lg border border-gray-200 dark:border-gray-700">
        <h3 className="font-semibold text-gray-800 dark:text-gray-200">
          Viewing Version {activeVersion.versionNumber}
        </h3>
        
        {previousVersion && (
          <button 
            onClick={() => setCompareMode(!compareMode)}
            className={`px-4 py-2 rounded-md text-sm font-medium transition-colors ${compareMode ? 'bg-indigo-100 text-indigo-700 dark:bg-indigo-900/50 dark:text-indigo-300 border border-indigo-200 dark:border-indigo-800' : 'bg-white dark:bg-gray-700 text-gray-700 dark:text-gray-200 border border-gray-300 dark:border-gray-600 hover:bg-gray-50 dark:hover:bg-gray-600'}`}
          >
            {compareMode ? 'View Content' : `Compare with V${previousVersion.versionNumber}`}
          </button>
        )}
      </div>

      {compareMode && previousVersion ? (
        <CoverLetterDiffViewer 
          oldValue={previousVersion.generatedContent} 
          newValue={activeVersion.generatedContent} 
        />
      ) : (
        <>
          <div className="bg-white dark:bg-gray-800 rounded-xl shadow border border-gray-100 dark:border-gray-700 p-6">
            <div className="prose dark:prose-invert max-w-none text-sm whitespace-pre-wrap font-serif text-gray-800 dark:text-gray-200 bg-gray-50 dark:bg-gray-900/50 p-6 rounded-lg border border-gray-200 dark:border-gray-700">
              {activeVersion.generatedContent}
            </div>
          </div>
          <TraceabilityPanel traceability={activeVersion.traceability} />
        </>
      )}

    </div>
  );
};

export default CoverLetterVersionHistory;

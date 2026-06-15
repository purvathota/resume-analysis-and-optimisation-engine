interface ResumeDiffEntry {
  originalBullet: string;
  optimizedBullet: string;
  reason: string;
  changeType: string;
  confidenceScore: number;
  keywordsAdded?: string[];
}

interface ResumeDiffViewProps {
  diffData: ResumeDiffEntry[];
}

export default function ResumeDiffView({ diffData }: ResumeDiffViewProps) {
  if (!diffData || diffData.length === 0) return null;

  const getBadgeColor = (type: string) => {
    switch (type) {
      case 'KEYWORD_ALIGNMENT': return 'bg-blue-100 text-blue-800 border-blue-200';
      case 'REORDERING': return 'bg-purple-100 text-purple-800 border-purple-200';
      case 'FORMATTING': return 'bg-gray-100 text-gray-800 border-gray-200';
      case 'SUMMARY_ENHANCEMENT': return 'bg-yellow-100 text-yellow-800 border-yellow-200';
      case 'BULLET_REWRITE': return 'bg-indigo-100 text-indigo-800 border-indigo-200';
      case 'SKILL_REORDERING': return 'bg-pink-100 text-pink-800 border-pink-200';
      default: return 'bg-gray-100 text-gray-800 border-gray-200';
    }
  };

  const getConfidenceColor = (score: number) => {
    if (score >= 90) return 'text-green-600';
    if (score >= 70) return 'text-yellow-600';
    return 'text-red-600';
  };

  return (
    <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-200 mt-6">
      <h2 className="text-xl font-bold mb-6 text-gray-800 border-b pb-2">Resume Optimization Traceability (Diff View)</h2>
      
      <div className="space-y-6">
        {diffData.map((entry, i) => (
          <div key={i} className="border border-gray-200 rounded-lg overflow-hidden">
            <div className="bg-gray-50 px-4 py-3 border-b border-gray-200 flex justify-between items-center">
              <span className={`px-2.5 py-1 text-xs font-semibold rounded-full border ${getBadgeColor(entry.changeType)}`}>
                {entry.changeType.replace(/_/g, ' ')}
              </span>
              <span className={`text-sm font-bold ${getConfidenceColor(entry.confidenceScore)}`}>
                Confidence: {entry.confidenceScore}%
              </span>
            </div>
            
            <div className="p-4 grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <h4 className="text-xs font-bold text-gray-500 uppercase mb-2">Original Master Resume</h4>
                <div className="text-sm text-red-800 bg-red-50 p-3 rounded h-full">
                  {entry.originalBullet}
                </div>
              </div>
              
              <div>
                <h4 className="text-xs font-bold text-gray-500 uppercase mb-2">Optimized Tailored Version</h4>
                <div className="text-sm text-green-800 bg-green-50 p-3 rounded h-full">
                  {entry.optimizedBullet}
                </div>
              </div>
            </div>

            <div className="px-4 py-3 bg-gray-50 border-t border-gray-200 flex flex-col md:flex-row md:items-start justify-between gap-4">
              <div className="flex-1">
                <h4 className="text-xs font-bold text-gray-500 uppercase mb-1">Reason for Change</h4>
                <p className="text-sm text-gray-700">{entry.reason}</p>
              </div>
              {entry.keywordsAdded && entry.keywordsAdded.length > 0 && (
                <div className="md:max-w-xs">
                  <h4 className="text-xs font-bold text-gray-500 uppercase mb-1">Keywords Traced & Added</h4>
                  <div className="flex flex-wrap gap-1">
                    {entry.keywordsAdded.map((kw, idx) => (
                      <span key={idx} className="px-2 py-0.5 bg-blue-100 text-blue-800 rounded text-xs">{kw}</span>
                    ))}
                  </div>
                </div>
              )}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}



interface KeywordImprovementReportProps {
  report: {
    changesMade?: string[];
    keywordsAdded?: string[];
    skillsReordered?: string[];
    experienceImprovements?: string[];
    atsImprovements?: string[];
  };
}

export default function KeywordImprovementReport({ report }: KeywordImprovementReportProps) {
  if (!report) return null;

  return (
    <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-200 mt-6">
      <h2 className="text-2xl font-bold mb-6 text-gray-800">Resume Change Summary</h2>
      
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Changes Made */}
        {report.changesMade && report.changesMade.length > 0 && (
          <div className="bg-blue-50 p-4 rounded-lg">
            <h3 className="text-lg font-semibold text-blue-800 mb-2">Changes Made</h3>
            <ul className="list-disc pl-5 space-y-1 text-sm text-blue-900">
              {report.changesMade.map((item, i) => (
                <li key={i}>{item}</li>
              ))}
            </ul>
          </div>
        )}

        {/* Keywords Added */}
        {report.keywordsAdded && report.keywordsAdded.length > 0 && (
          <div className="bg-green-50 p-4 rounded-lg">
            <h3 className="text-lg font-semibold text-green-800 mb-2">Keywords Added</h3>
            <div className="flex flex-wrap gap-2">
              {report.keywordsAdded.map((kw, i) => (
                <span key={i} className="px-2 py-1 bg-green-200 text-green-900 rounded text-xs font-medium">{kw}</span>
              ))}
            </div>
          </div>
        )}

        {/* Experience Improvements */}
        {report.experienceImprovements && report.experienceImprovements.length > 0 && (
          <div className="bg-purple-50 p-4 rounded-lg">
            <h3 className="text-lg font-semibold text-purple-800 mb-2">Experience Improvements</h3>
            <ul className="list-disc pl-5 space-y-1 text-sm text-purple-900">
              {report.experienceImprovements.map((item, i) => (
                <li key={i}>{item}</li>
              ))}
            </ul>
          </div>
        )}

        {/* ATS Improvements removed to enforce strict real ATS metrics */}
      </div>
    </div>
  );
}

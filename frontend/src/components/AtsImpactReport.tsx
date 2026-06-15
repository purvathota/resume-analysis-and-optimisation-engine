interface AtsImpactReportProps {
  report: {
    originalAtsScore: number;
    optimizedAtsScore: number;
    atsImprovementPercentage: number;
    keywordsAdded: string[];
    unsupportedKeywordsNotAdded: string[];
  };
}

export default function AtsImpactReport({ report }: AtsImpactReportProps) {
  if (!report) return null;

  return (
    <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-200 mt-6">
      <h2 className="text-2xl font-bold mb-6 text-gray-800 border-b pb-2">ATS Impact Analysis</h2>
      
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-6">
        <div className="bg-gray-50 p-4 rounded-lg border border-gray-200 text-center">
          <h3 className="text-sm font-semibold text-gray-600 uppercase tracking-wider mb-1">Before</h3>
          <p className="text-4xl font-bold text-gray-800">{report.originalAtsScore}%</p>
        </div>
        
        <div className="bg-blue-50 p-4 rounded-lg border border-blue-200 text-center flex flex-col justify-center">
          <h3 className="text-sm font-semibold text-blue-600 uppercase tracking-wider mb-1">Impact</h3>
          <p className="text-2xl font-bold text-blue-700">+{report.atsImprovementPercentage}%</p>
        </div>

        <div className="bg-green-50 p-4 rounded-lg border border-green-200 text-center">
          <h3 className="text-sm font-semibold text-green-600 uppercase tracking-wider mb-1">After</h3>
          <p className="text-4xl font-bold text-green-700">{report.optimizedAtsScore}%</p>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div className="bg-green-50 p-4 rounded-lg border border-green-200">
          <h3 className="text-sm font-semibold text-green-800 uppercase mb-3">Keywords Successfully Traced & Added</h3>
          <div className="flex flex-wrap gap-2">
            {report.keywordsAdded && report.keywordsAdded.length > 0 ? (
              report.keywordsAdded.map((kw, i) => (
                <span key={i} className="px-2 py-1 bg-green-200 text-green-900 rounded text-xs font-medium">{kw}</span>
              ))
            ) : (
              <span className="text-sm text-green-700">No new keywords added.</span>
            )}
          </div>
        </div>

        <div className="bg-red-50 p-4 rounded-lg border border-red-200">
          <h3 className="text-sm font-semibold text-red-800 uppercase mb-3">Unsupported Keywords (Rejected)</h3>
          <div className="flex flex-wrap gap-2">
            {report.unsupportedKeywordsNotAdded && report.unsupportedKeywordsNotAdded.length > 0 ? (
              report.unsupportedKeywordsNotAdded.map((kw, i) => (
                <span key={i} className="px-2 py-1 bg-red-200 text-red-900 rounded text-xs font-medium line-through opacity-70">{kw}</span>
              ))
            ) : (
              <span className="text-sm text-red-700">All required keywords were supported.</span>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

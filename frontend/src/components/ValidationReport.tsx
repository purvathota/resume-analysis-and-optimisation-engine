interface ValidationReportProps {
  report: {
    headerIntegrityScore: number;
    sectionPreservationScore: number;
    metricPreservationScore: number;
    technologyPreservationScore: number;
    hyperlinkIntegrityScore: number;
    structureIntegrityScore: number;
    truthfulnessScore: number;
    overallResumeIntegrityScore: number;
  };
}

export default function ValidationReport({ report }: ValidationReportProps) {
  if (!report) return null;

  const getScoreColor = (score: number) => {
    if (score >= 95) return 'text-green-600 bg-green-50 border-green-200';
    if (score >= 80) return 'text-yellow-600 bg-yellow-50 border-yellow-200';
    return 'text-red-600 bg-red-50 border-red-200';
  };

  const getBarColor = (score: number) => {
    if (score >= 95) return 'bg-green-500';
    if (score >= 80) return 'bg-yellow-500';
    return 'bg-red-500';
  };

  const metrics = [
    { label: 'Header & Contact Integrity', score: report.headerIntegrityScore },
    { label: 'Hyperlinks Preserved', score: report.hyperlinkIntegrityScore },
    { label: 'Section Preservation', score: report.sectionPreservationScore },
    { label: 'Metric Preservation', score: report.metricPreservationScore },
    { label: 'Technology Preservation', score: report.technologyPreservationScore },
    { label: 'Structure Integrity', score: report.structureIntegrityScore },
    { label: 'AI Truthfulness (No Hallucinations)', score: report.truthfulnessScore },
  ];

  return (
    <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-200 mt-6">
      <div className="flex justify-between items-center mb-6 border-b pb-4">
        <div>
          <h2 className="text-2xl font-bold text-gray-800">Resume Integrity Guard</h2>
          <p className="text-sm text-gray-500 mt-1">Recruiter-grade validation scoring to ensure 95% preservation.</p>
        </div>
        <div className={`px-4 py-2 rounded-lg border-2 ${getScoreColor(report.overallResumeIntegrityScore)}`}>
          <span className="text-sm font-bold uppercase tracking-wider block text-center opacity-70">Overall</span>
          <span className="text-3xl font-black">{report.overallResumeIntegrityScore}%</span>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {metrics.map((m, i) => (
          <div key={i} className="flex flex-col space-y-1">
            <div className="flex justify-between text-sm font-medium">
              <span className="text-gray-700">{m.label}</span>
              <span className={m.score >= 95 ? 'text-green-600' : 'text-red-600'}>{m.score}%</span>
            </div>
            <div className="w-full bg-gray-200 rounded-full h-2">
              <div 
                className={`h-2 rounded-full ${getBarColor(m.score)}`} 
                style={{ width: `${m.score}%` }}
              ></div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

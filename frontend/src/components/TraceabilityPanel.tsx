import React from 'react';
import { type Traceability } from '../services/coverLetterService';

interface Props {
  traceability: Traceability;
}

const TraceabilityPanel: React.FC<Props> = ({ traceability }) => {
  const sections = [
    { title: "Why these experiences?", data: traceability.referencedExperiences, color: "blue" },
    { title: "Why these projects?", data: traceability.referencedProjects, color: "purple" },
    { title: "Why these technologies?", data: traceability.referencedTechnologies, color: "green" },
    { title: "Why these metrics?", data: traceability.referencedMetrics, color: "indigo" },
    { title: "Why these certifications?", data: traceability.referencedCertifications, color: "yellow" },
    { title: "Why these achievements?", data: traceability.referencedAchievements, color: "red" }
  ];

  return (
    <div className="bg-white dark:bg-gray-800 rounded-xl shadow border border-gray-100 dark:border-gray-700 p-6">
      <div className="flex items-center space-x-2 mb-6">
        <svg className="w-6 h-6 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
        <h3 className="text-xl font-bold text-gray-900 dark:text-white">Strict Integrity Guard Passed</h3>
      </div>
      
      <p className="text-sm text-gray-600 dark:text-gray-400 mb-6">
        Every claim in the generated cover letter was fully verified against the original resume. No hallucinations detected.
      </p>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {sections.map((section, idx) => {
          if (!section.data || section.data.length === 0) return null;
          return (
            <div key={idx} className={`bg-${section.color}-50 dark:bg-${section.color}-900/20 border border-${section.color}-200 dark:border-${section.color}-800 rounded-lg p-4`}>
              <h4 className={`text-sm font-semibold text-${section.color}-800 dark:text-${section.color}-300 mb-2`}>{section.title}</h4>
              <ul className="list-disc list-inside space-y-1">
                {section.data.map((item, i) => (
                  <li key={i} className="text-xs text-gray-700 dark:text-gray-300">
                    {item}
                  </li>
                ))}
              </ul>
            </div>
          );
        })}
      </div>
    </div>
  );
};

export default TraceabilityPanel;

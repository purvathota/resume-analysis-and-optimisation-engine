

interface OptimizedResumeProps {
  resumeData: any;
}

export default function OptimizedResumeView({ resumeData }: OptimizedResumeProps) {
  if (!resumeData) return null;

  return (
    <div className="bg-white p-8 rounded-lg shadow-sm border border-gray-200 mt-6 font-serif">
      <h2 className="text-2xl font-bold mb-6 text-gray-800 font-sans text-center border-b pb-2">Tailored Resume Preview</h2>
      
      {resumeData.professionalSummary && (
        <div className="mb-6">
          <h3 className="font-bold text-lg mb-2 uppercase border-b border-black">Professional Summary</h3>
          <p className="text-sm text-justify">{resumeData.professionalSummary}</p>
        </div>
      )}

      {resumeData.professionalExperience && (
        <div className="mb-6">
          <h3 className="font-bold text-lg mb-2 uppercase border-b border-black">Professional Experience</h3>
          {resumeData.professionalExperience.map((exp: any, i: number) => (
            <div key={i} className="mb-4">
              <div className="font-bold text-sm">{exp.title}</div>
              <div className="text-sm">{exp.company}</div>
              <div className="text-sm">{exp.location}</div>
              <div className="text-sm mb-1">{exp.dates}</div>
              <ul className="list-disc pl-5 text-sm space-y-1">
                {exp.bullets?.map((b: string, j: number) => (
                  <li key={j}>{b}</li>
                ))}
              </ul>
            </div>
          ))}
        </div>
      )}

      {resumeData.skills && (
        <div className="mb-6">
          <h3 className="font-bold text-lg mb-2 uppercase border-b border-black">Skills</h3>
          <div className="text-sm">
            {resumeData.skills.map((skill: any, i: number) => (
              <div key={i} className="mb-1">
                <span className="font-bold">{skill.category}: </span>
                <span>{skill.items}</span>
              </div>
            ))}
          </div>
        </div>
      )}

      {resumeData.education && (
        <div className="mb-6">
          <h3 className="font-bold text-lg mb-2 uppercase border-b border-black">Education</h3>
          {resumeData.education.map((edu: any, i: number) => (
            <div key={i} className="mb-4">
              <div className="flex justify-between font-bold text-sm">
                <span>{edu.degree}</span>
                <span>{edu.dates}</span>
              </div>
              <div className="flex justify-between text-sm italic mb-1">
                <span>{edu.university}</span>
                <span>{edu.details}</span>
              </div>
            </div>
          ))}
        </div>
      )}

      {resumeData.projects && (
        <div className="mb-6">
          <h3 className="font-bold text-lg mb-2 uppercase border-b border-black">Projects</h3>
          {resumeData.projects.map((proj: any, i: number) => (
            <div key={i} className="mb-4">
              <div className="font-bold text-sm">
                <span>{proj.title}</span>
                {proj.link && (
                  <span className="font-normal italic">
                    {" | "}
                    <a href={proj.link.startsWith('http') ? proj.link : `https://${proj.link}`} target="_blank" rel="noopener noreferrer" className="text-blue-600 hover:underline">
                      {proj.link}
                    </a>
                  </span>
                )}
              </div>
              {proj.techStack && (
                <div className="text-sm font-semibold mb-1">Tech Stack: {proj.techStack}</div>
              )}
              <ul className="list-disc pl-5 text-sm space-y-1">
                {proj.bullets?.map((b: string, j: number) => (
                  <li key={j}>{b}</li>
                ))}
              </ul>
            </div>
          ))}
        </div>
      )}

      {resumeData.certifications && (
        <div className="mb-6">
          <h3 className="font-bold text-lg mb-2 uppercase border-b border-black">Certifications</h3>
          {resumeData.certifications.map((cert: any, i: number) => (
            <div key={i} className="mb-4">
              <div className="flex justify-between font-bold text-sm mb-1">
                <span>{cert.title}</span>
                <span>{cert.date}</span>
              </div>
              {cert.bullets && cert.bullets.length > 0 && (
                <ul className="list-disc pl-5 text-sm space-y-1">
                  {cert.bullets.map((b: string, j: number) => (
                    <li key={j}>{b}</li>
                  ))}
                </ul>
              )}
            </div>
          ))}
        </div>
      )}

      {resumeData.achievements && (
        <div className="mb-6">
          <h3 className="font-bold text-lg mb-2 uppercase border-b border-black">Achievements</h3>
          <ul className="list-disc pl-5 text-sm space-y-1">
            {resumeData.achievements.map((achievement: string, i: number) => (
              <li key={i}>{achievement}</li>
            ))}
          </ul>
        </div>
      )}

    </div>
  );
}

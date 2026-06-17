import React, { useState, useEffect, useMemo } from 'react';
import { 
  getJobApplications, 
  createJobApplication, 
  updateJobApplication, 
  deleteJobApplication, 
  type JobApplicationResponse, 
  type JobApplicationRequest, 
  type ApplicationStatus, 
  type CompanyType 
} from '../services/jobApplicationService';
import { getResumes } from '../services/resumeService';
import { getUserCoverLetters } from '../services/coverLetterService';

const STATUSES: ApplicationStatus[] = [
  'SAVED', 'APPLIED', 'RECRUITER_SCREEN', 'ONLINE_ASSESSMENT', 
  'TECHNICAL_INTERVIEW', 'FINAL_INTERVIEW', 'OFFER_RECEIVED', 'REJECTED', 'WITHDRAWN'
];

const COMPANY_TYPES: CompanyType[] = ['FINTECH', 'PRODUCT', 'CONSULTING', 'BANKING', 'OTHER'];

const JobTracker: React.FC = () => {
  const [applications, setApplications] = useState<JobApplicationResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  
  // Form State
  const [editingId, setEditingId] = useState<number | null>(null);
  const [formData, setFormData] = useState<Partial<JobApplicationRequest>>({
    status: 'SAVED',
    companyType: 'OTHER'
  });

  // Supporting Data
  const [resumes, setResumes] = useState<any[]>([]);
  const [coverLetters, setCoverLetters] = useState<any[]>([]);

  // Filter & Sort State
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState<ApplicationStatus | ''>('');
  const [sortField, setSortField] = useState<keyof JobApplicationResponse>('createdAt');
  const [sortDirection, setSortDirection] = useState<'asc' | 'desc'>('desc');

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      setIsLoading(true);
      const [appsRes, resRes, clRes] = await Promise.all([
        getJobApplications(),
        getResumes(),
        getUserCoverLetters()
      ]);
      setApplications(appsRes);
      setResumes(resRes);
      setCoverLetters(clRes);
    } catch (error) {
      console.error('Failed to load job tracker data', error);
    } finally {
      setIsLoading(false);
    }
  };

  // Analytics Calculation
  const analytics = useMemo(() => {
    const total = applications.length;
    const interviews = applications.filter(a => 
      ['RECRUITER_SCREEN', 'ONLINE_ASSESSMENT', 'TECHNICAL_INTERVIEW', 'FINAL_INTERVIEW'].includes(a.status)
    ).length;
    const offers = applications.filter(a => a.status === 'OFFER_RECEIVED').length;
    const rejections = applications.filter(a => a.status === 'REJECTED').length;
    const conversionRate = total > 0 ? Math.round((offers / total) * 100) : 0;

    return { total, interviews, offers, rejections, conversionRate };
  }, [applications]);

  // Filtering & Sorting
  const filteredAndSortedApplications = useMemo(() => {
    return applications
      .filter(app => {
        const matchesSearch = app.companyName.toLowerCase().includes(searchTerm.toLowerCase()) || 
                              app.roleTitle.toLowerCase().includes(searchTerm.toLowerCase());
        const matchesStatus = statusFilter ? app.status === statusFilter : true;
        return matchesSearch && matchesStatus;
      })
      .sort((a, b) => {
        let valA = a[sortField];
        let valB = b[sortField];
        
        if (!valA) valA = '';
        if (!valB) valB = '';

        if (valA < valB) return sortDirection === 'asc' ? -1 : 1;
        if (valA > valB) return sortDirection === 'asc' ? 1 : -1;
        return 0;
      });
  }, [applications, searchTerm, statusFilter, sortField, sortDirection]);

  const handleSort = (field: keyof JobApplicationResponse) => {
    if (sortField === field) {
      setSortDirection(sortDirection === 'asc' ? 'desc' : 'asc');
    } else {
      setSortField(field);
      setSortDirection('asc');
    }
  };

  const handleOpenModal = (app?: JobApplicationResponse) => {
    if (app) {
      setEditingId(app.id);
      setFormData({
        companyName: app.companyName,
        roleTitle: app.roleTitle,
        location: app.location,
        status: app.status,
        companyType: app.companyType,
        url: app.url,
        notes: app.notes,
        appliedDate: app.appliedDate,
        resumeId: app.resumeId,
        coverLetterId: app.coverLetterId
      });
    } else {
      setEditingId(null);
      setFormData({ status: 'SAVED', companyType: 'OTHER' });
    }
    setIsModalOpen(true);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      if (editingId) {
        await updateJobApplication(editingId, formData as JobApplicationRequest);
      } else {
        await createJobApplication(formData as JobApplicationRequest);
      }
      setIsModalOpen(false);
      fetchData();
    } catch (error) {
      console.error('Failed to save application', error);
      alert('Failed to save application. Please check all required fields.');
    }
  };

  const handleDelete = async (id: number) => {
    if (window.confirm('Are you sure you want to delete this application?')) {
      try {
        await deleteJobApplication(id);
        fetchData();
      } catch (error) {
        console.error('Failed to delete application', error);
      }
    }
  };

  if (isLoading) return <div className="p-8 text-center">Loading Job Tracker...</div>;

  return (
    <div className="p-6 max-w-7xl mx-auto space-y-6">
      
      {/* Analytics Cards */}
      <div className="grid grid-cols-2 md:grid-cols-5 gap-4 mb-8">
        <div className="bg-white dark:bg-gray-800 p-4 rounded-xl shadow border border-gray-100 dark:border-gray-700 text-center">
          <p className="text-sm text-gray-500 dark:text-gray-400 font-medium">Total Apps</p>
          <p className="text-3xl font-bold text-gray-900 dark:text-white">{analytics.total}</p>
        </div>
        <div className="bg-white dark:bg-gray-800 p-4 rounded-xl shadow border border-gray-100 dark:border-gray-700 text-center">
          <p className="text-sm text-gray-500 dark:text-gray-400 font-medium">Interviews</p>
          <p className="text-3xl font-bold text-blue-600 dark:text-blue-400">{analytics.interviews}</p>
        </div>
        <div className="bg-white dark:bg-gray-800 p-4 rounded-xl shadow border border-gray-100 dark:border-gray-700 text-center">
          <p className="text-sm text-gray-500 dark:text-gray-400 font-medium">Offers</p>
          <p className="text-3xl font-bold text-green-600 dark:text-green-400">{analytics.offers}</p>
        </div>
        <div className="bg-white dark:bg-gray-800 p-4 rounded-xl shadow border border-gray-100 dark:border-gray-700 text-center">
          <p className="text-sm text-gray-500 dark:text-gray-400 font-medium">Rejections</p>
          <p className="text-3xl font-bold text-red-600 dark:text-red-400">{analytics.rejections}</p>
        </div>
        <div className="bg-white dark:bg-gray-800 p-4 rounded-xl shadow border border-gray-100 dark:border-gray-700 text-center">
          <p className="text-sm text-gray-500 dark:text-gray-400 font-medium">Conversion</p>
          <p className="text-3xl font-bold text-purple-600 dark:text-purple-400">{analytics.conversionRate}%</p>
        </div>
      </div>

      {/* Controls */}
      <div className="flex flex-col md:flex-row justify-between items-center gap-4 bg-white dark:bg-gray-800 p-4 rounded-xl shadow border border-gray-100 dark:border-gray-700">
        <div className="flex gap-4 flex-1 w-full">
          <input 
            type="text" 
            placeholder="Search Company or Role..." 
            className="flex-1 rounded-md border border-gray-300 px-3 py-2 text-sm dark:bg-gray-700 dark:border-gray-600 dark:text-white"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
          <select 
            className="rounded-md border border-gray-300 px-3 py-2 text-sm dark:bg-gray-700 dark:border-gray-600 dark:text-white"
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value as ApplicationStatus | '')}
          >
            <option value="">All Statuses</option>
            {STATUSES.map(s => <option key={s} value={s}>{s}</option>)}
          </select>
        </div>
        <button 
          onClick={() => handleOpenModal()}
          className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-md font-medium text-sm transition-colors w-full md:w-auto"
        >
          + Add Application
        </button>
      </div>

      {/* Data Table */}
      <div className="bg-white dark:bg-gray-800 rounded-xl shadow border border-gray-100 dark:border-gray-700 overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-gray-50 dark:bg-gray-900/50 border-b border-gray-200 dark:border-gray-700 text-xs uppercase text-gray-500 dark:text-gray-400">
                <th className="p-4 cursor-pointer hover:text-gray-900 dark:hover:text-white" onClick={() => handleSort('companyName')}>Company {sortField === 'companyName' && (sortDirection === 'asc' ? '↑' : '↓')}</th>
                <th className="p-4 cursor-pointer hover:text-gray-900 dark:hover:text-white" onClick={() => handleSort('roleTitle')}>Role {sortField === 'roleTitle' && (sortDirection === 'asc' ? '↑' : '↓')}</th>
                <th className="p-4 cursor-pointer hover:text-gray-900 dark:hover:text-white" onClick={() => handleSort('status')}>Status {sortField === 'status' && (sortDirection === 'asc' ? '↑' : '↓')}</th>
                <th className="p-4 cursor-pointer hover:text-gray-900 dark:hover:text-white" onClick={() => handleSort('appliedDate')}>Applied {sortField === 'appliedDate' && (sortDirection === 'asc' ? '↑' : '↓')}</th>
                <th className="p-4">Documents</th>
                <th className="p-4 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200 dark:divide-gray-700 text-sm">
              {filteredAndSortedApplications.map(app => (
                <tr key={app.id} className="hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors">
                  <td className="p-4 font-medium text-gray-900 dark:text-white">
                    {app.url ? <a href={app.url} target="_blank" rel="noreferrer" className="text-blue-600 hover:underline">{app.companyName}</a> : app.companyName}
                  </td>
                  <td className="p-4 text-gray-700 dark:text-gray-300">{app.roleTitle}</td>
                  <td className="p-4">
                    <span className="px-2 py-1 bg-gray-100 dark:bg-gray-700 text-gray-800 dark:text-gray-200 rounded text-xs font-semibold">
                      {app.status}
                    </span>
                  </td>
                  <td className="p-4 text-gray-500 dark:text-gray-400">{app.appliedDate || 'N/A'}</td>
                  <td className="p-4">
                    <div className="flex flex-col gap-1 text-xs text-gray-500 dark:text-gray-400">
                      {app.resumeFileName ? <span>📄 {app.resumeFileName}</span> : null}
                      {app.coverLetterVersionNumber ? <span>✉️ CL V{app.coverLetterVersionNumber}</span> : null}
                    </div>
                  </td>
                  <td className="p-4 text-right space-x-2">
                    <button onClick={() => handleOpenModal(app)} className="text-blue-600 hover:text-blue-800 dark:text-blue-400 dark:hover:text-blue-300">Edit</button>
                    <button onClick={() => handleDelete(app.id)} className="text-red-600 hover:text-red-800 dark:text-red-400 dark:hover:text-red-300">Delete</button>
                  </td>
                </tr>
              ))}
              {filteredAndSortedApplications.length === 0 && (
                <tr>
                  <td colSpan={6} className="p-8 text-center text-gray-500 dark:text-gray-400">No applications found.</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Modal */}
      {isModalOpen && (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center p-4 z-50">
          <div className="bg-white dark:bg-gray-800 rounded-xl shadow-xl w-full max-w-2xl overflow-hidden flex flex-col max-h-[90vh]">
            <div className="p-6 border-b border-gray-200 dark:border-gray-700">
              <h3 className="text-xl font-bold text-gray-900 dark:text-white">
                {editingId ? 'Edit Application' : 'Add Application'}
              </h3>
            </div>
            
            <form onSubmit={handleSubmit} className="p-6 overflow-y-auto space-y-4 flex-1">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Company Name *</label>
                  <input required type="text" className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm dark:bg-gray-700 dark:border-gray-600 dark:text-white" value={formData.companyName || ''} onChange={e => setFormData({...formData, companyName: e.target.value})} />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Role Title *</label>
                  <input required type="text" className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm dark:bg-gray-700 dark:border-gray-600 dark:text-white" value={formData.roleTitle || ''} onChange={e => setFormData({...formData, roleTitle: e.target.value})} />
                </div>
              </div>

              <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Status *</label>
                  <select required className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm dark:bg-gray-700 dark:border-gray-600 dark:text-white" value={formData.status || ''} onChange={e => setFormData({...formData, status: e.target.value as ApplicationStatus})}>
                    {STATUSES.map(s => <option key={s} value={s}>{s}</option>)}
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Company Type</label>
                  <select className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm dark:bg-gray-700 dark:border-gray-600 dark:text-white" value={formData.companyType || ''} onChange={e => setFormData({...formData, companyType: e.target.value as CompanyType})}>
                    {COMPANY_TYPES.map(s => <option key={s} value={s}>{s}</option>)}
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Applied Date</label>
                  <input type="date" className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm dark:bg-gray-700 dark:border-gray-600 dark:text-white" value={formData.appliedDate || ''} onChange={e => setFormData({...formData, appliedDate: e.target.value})} />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Location</label>
                  <input type="text" className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm dark:bg-gray-700 dark:border-gray-600 dark:text-white" value={formData.location || ''} onChange={e => setFormData({...formData, location: e.target.value})} />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Job URL</label>
                  <input type="url" className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm dark:bg-gray-700 dark:border-gray-600 dark:text-white" value={formData.url || ''} onChange={e => setFormData({...formData, url: e.target.value})} />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Linked Resume</label>
                  <select className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm dark:bg-gray-700 dark:border-gray-600 dark:text-white" value={formData.resumeId || ''} onChange={e => setFormData({...formData, resumeId: e.target.value ? Number(e.target.value) : undefined})}>
                    <option value="">-- Select Resume --</option>
                    {resumes.map(r => <option key={r.id} value={r.id}>{r.fileName}</option>)}
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Linked Cover Letter</label>
                  <select className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm dark:bg-gray-700 dark:border-gray-600 dark:text-white" value={formData.coverLetterId || ''} onChange={e => setFormData({...formData, coverLetterId: e.target.value ? Number(e.target.value) : undefined})}>
                    <option value="">-- Select Cover Letter --</option>
                    {coverLetters.map(c => <option key={c.id} value={c.id}>{c.companyName} - {c.roleTitle}</option>)}
                  </select>
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Notes</label>
                <textarea rows={3} className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm dark:bg-gray-700 dark:border-gray-600 dark:text-white" value={formData.notes || ''} onChange={e => setFormData({...formData, notes: e.target.value})}></textarea>
              </div>

              <div className="flex justify-end gap-3 pt-4 border-t border-gray-200 dark:border-gray-700">
                <button type="button" onClick={() => setIsModalOpen(false)} className="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 hover:bg-gray-200 dark:bg-gray-700 dark:text-gray-200 dark:hover:bg-gray-600 rounded-md">Cancel</button>
                <button type="submit" className="px-4 py-2 text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 rounded-md">Save Application</button>
              </div>
            </form>
          </div>
        </div>
      )}

    </div>
  );
};

export default JobTracker;

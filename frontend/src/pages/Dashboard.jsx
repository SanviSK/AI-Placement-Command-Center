import React, { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../utils/api';
import { 
  ResponsiveContainer, AreaChart, Area, XAxis, YAxis, Tooltip, CartesianGrid 
} from 'recharts';
import { 
  User, GraduationCap, Calendar, BookOpen, Edit3, Save, X, 
  LogOut, Plus, Award, Briefcase, Brain, CheckSquare, Target, Mail, 
  UploadCloud, Trash2, Eye, CalendarClock, ChevronRight, FileText, CheckCircle,
  Sparkles, FileCheck
} from 'lucide-react';

// Circular Gauge Sub-component
const ReadinessGauge = ({ score }) => {
  const radius = 80;
  const stroke = 14;
  const normalizedRadius = radius - stroke * 2;
  const circumference = normalizedRadius * 2 * Math.PI;
  const strokeDashoffset = circumference - (score / 100) * circumference;

  let colorClass = 'stroke-secondary';
  let glowColor = 'rgba(236, 72, 153, 0.5)';
  if (score >= 75) {
    colorClass = 'stroke-success';
    glowColor = 'rgba(16, 185, 129, 0.5)';
  } else if (score >= 40) {
    colorClass = 'stroke-primary';
    glowColor = 'rgba(99, 102, 241, 0.5)';
  }

  return (
    <div className="relative flex items-center justify-center">
      <svg height={radius * 2} width={radius * 2} className="transform -rotate-90">
        {/* Track */}
        <circle
          className="stroke-dark-100/50"
          fill="transparent"
          strokeWidth={stroke}
          r={normalizedRadius}
          cx={radius}
          cy={radius}
        />
        {/* Progress */}
        <circle
          className={`${colorClass} transition-all duration-1000 ease-out`}
          fill="transparent"
          strokeWidth={stroke}
          strokeDasharray={circumference + ' ' + circumference}
          style={{ 
            strokeDashoffset,
            filter: `drop-shadow(0 0 6px ${glowColor})`
          }}
          strokeLinecap="round"
          r={normalizedRadius}
          cx={radius}
          cy={radius}
        />
      </svg>
      <div className="absolute flex flex-col items-center justify-center">
        <span className="text-4xl font-extrabold text-white font-display text-glow">{score}%</span>
        <span className="text-[10px] uppercase tracking-widest text-gray-400 font-bold mt-1">Ready</span>
      </div>
    </div>
  );
};

const Dashboard = () => {
  const { logout } = useAuth();
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [activeTab, setActiveTab] = useState('overview');

  // Edit Basic Profile details State
  const [isEditingProfile, setIsEditingProfile] = useState(false);
  const [profileForm, setProfileForm] = useState({
    name: '', college: '', branch: '', batch: ''
  });

  // Readiness State
  const [readinessDetails, setReadinessDetails] = useState(null);

  // Marks State
  const [marksData, setMarksData] = useState([]);
  const [overallCgpa, setOverallCgpa] = useState(0.0);
  const [isAddingMark, setIsAddingMark] = useState(false);
  const [markForm, setMarkForm] = useState({ semester: 1, sgpa: '' });

  // Skills State
  const [skillsList, setSkillsList] = useState([]);
  const [newSkillInput, setNewSkillInput] = useState('');

  // Target Companies State
  const [companiesList, setCompaniesList] = useState([]);
  const [isAddingCompany, setIsAddingCompany] = useState(false);
  const [companyForm, setCompanyForm] = useState({
    companyName: '', role: '', packageBand: '', applicationDeadline: ''
  });

  // Resume State
  const [resumeUrl, setResumeUrl] = useState('');
  const [parsedData, setParsedData] = useState(null);
  const [uploadingResume, setUploadingResume] = useState(false);
  const [resumeFile, setResumeFile] = useState(null);

  // Project Portfolio State
  const [projectRecommendations, setProjectRecommendations] = useState([]);
  const [myProjects, setMyProjects] = useState([]);
  const [isAddingCustomProject, setIsAddingCustomProject] = useState(false);
  const [customProjectForm, setCustomProjectForm] = useState({
    title: '', description: '', techStack: '', status: 'planned'
  });

  const reloadProjects = async () => {
    try {
      const recRes = await api.get('/api/students/me/projects/recommendations');
      if (recRes.data && recRes.data.success) {
        setProjectRecommendations(recRes.data.data || []);
      }
      const projRes = await api.get('/api/students/me/projects');
      if (projRes.data && projRes.data.success) {
        setMyProjects(projRes.data.data || []);
      }
    } catch (err) {
      console.error("Failed to reload projects", err);
    }
  };

  const handleAddProjectFromRecommendation = async (rec) => {
    try {
      const res = await api.post('/api/students/me/projects', {
        title: rec.title,
        description: rec.description,
        techStack: rec.techStack,
        sourceRecommendationId: rec.id,
        status: 'planned'
      });
      if (res.data && res.data.success) {
        await reloadProjects();
        await refreshReadiness();
        triggerNotification('success', 'Project added to planned portfolio!');
      }
    } catch (err) {
      triggerNotification('error', 'Failed to add project');
    }
  };

  const handleAddCustomProjectSubmit = async (e) => {
    e.preventDefault();
    try {
      const techArray = customProjectForm.techStack
        ? customProjectForm.techStack.split(',').map(t => t.trim()).filter(Boolean)
        : [];
      
      const res = await api.post('/api/students/me/projects', {
        title: customProjectForm.title,
        description: customProjectForm.description,
        techStack: techArray,
        status: customProjectForm.status
      });
      if (res.data && res.data.success) {
        setCustomProjectForm({ title: '', description: '', techStack: '', status: 'planned' });
        setIsAddingCustomProject(false);
        await reloadProjects();
        await refreshReadiness();
        triggerNotification('success', 'Custom project added successfully!');
      }
    } catch (err) {
      triggerNotification('error', 'Failed to add custom project');
    }
  };

  const handleUpdateProjectStatus = async (projectId, newStatus) => {
    try {
      const res = await api.put(`/api/students/me/projects/${projectId}`, { status: newStatus });
      if (res.data && res.data.success) {
        await reloadProjects();
        await refreshReadiness();
        triggerNotification('success', `Project status updated to ${newStatus}!`);
      }
    } catch (err) {
      triggerNotification('error', 'Failed to update status');
    }
  };

  const handleDeleteProject = async (projectId) => {
    try {
      const res = await api.delete(`/api/students/me/projects/${projectId}`);
      if (res.data && res.data.success) {
        await reloadProjects();
        await refreshReadiness();
        triggerNotification('success', 'Project deleted from portfolio.');
      }
    } catch (err) {
      triggerNotification('error', 'Failed to delete project');
    }
  };

  // Application Tracker State
  const [applicationsList, setApplicationsList] = useState([]);
  const [upcomingDeadlines, setUpcomingDeadlines] = useState([]);
  const [isAddingApplication, setIsAddingApplication] = useState(false);
  const [isEditingApplication, setIsEditingApplication] = useState(null); // application object
  const [isViewingHistory, setIsViewingHistory] = useState(null); // application object
  const [historyList, setHistoryList] = useState([]);
  const [applicationForm, setApplicationForm] = useState({
    companyName: '', role: '', appliedDate: new Date().toISOString().split('T')[0], packageBand: '', jobUrl: '', notes: '', reminderDate: ''
  });

  // ATS Resume Generator State
  const [resumesList, setResumesList] = useState([]);
  const [selectedResume, setSelectedResume] = useState(null);
  const [isTailoring, setIsTailoring] = useState(false);
  const [tailorForm, setTailorForm] = useState({ targetCompanyId: '', jobDescription: '' });

  // AI Mock Interview State
  const [interviewSessions, setInterviewSessions] = useState([]);
  const [activeSession, setActiveSession] = useState(null); // { sessionId, currentQuestion, questionNumber, totalQuestions }
  const [activeFeedback, setActiveFeedback] = useState(null); // { score, strengths[], improvements[] }
  const [answerText, setAnswerText] = useState('');
  const [isStartingInterview, setIsStartingInterview] = useState(false);
  const [isSubmittingAnswer, setIsSubmittingAnswer] = useState(false);
  const [selectedTranscript, setSelectedTranscript] = useState(null);
  const [interviewConfig, setInterviewConfig] = useState({ targetCompanyId: '', interviewType: 'technical' });

  const reloadInterviews = async () => {
    try {
      const res = await api.get('/api/students/me/interviews');
      if (res.data && res.data.success) {
        setInterviewSessions(res.data.data || []);
      }
    } catch (err) {
      console.error("Failed to load interview list", err);
    }
  };

  const handleStartInterview = async (e) => {
    e.preventDefault();
    setIsStartingInterview(true);
    setActiveFeedback(null);
    try {
      const res = await api.post('/api/students/me/interviews/start', {
        targetCompanyId: interviewConfig.targetCompanyId ? Number(interviewConfig.targetCompanyId) : null,
        interviewType: interviewConfig.interviewType
      });
      if (res.data && res.data.success) {
        const data = res.data.data;
        setActiveSession({
          sessionId: data.sessionId,
          currentQuestion: data.question,
          questionNumber: data.questionNumber,
          totalQuestions: data.totalQuestions
        });
        setAnswerText('');
        triggerNotification('success', 'Mock interview session initiated!');
      }
    } catch (err) {
      triggerNotification('error', 'Failed to start interview');
    } finally {
      setIsStartingInterview(false);
    }
  };

  const handleSubmitAnswer = async (e) => {
    e.preventDefault();
    if (!answerText.trim()) return;
    setIsSubmittingAnswer(true);
    try {
      const res = await api.post(`/api/students/me/interviews/${activeSession.sessionId}/answer`, {
        questionId: activeSession.currentQuestion.id,
        answerText: answerText
      });
      if (res.data && res.data.success) {
        const data = res.data.data;
        setActiveFeedback(data.feedback);
        setAnswerText('');
        
        if (data.nextQuestion) {
          // Progress to next question
          setActiveSession(prev => ({
            ...prev,
            currentQuestion: data.nextQuestion,
            questionNumber: data.questionNumber + 1
          }));
          triggerNotification('success', 'Answer graded! Moving to next question.');
        } else {
          // Finished interview!
          setActiveSession(null);
          await reloadInterviews();
          // Recalculate score (overall preparation might level up!)
          await refreshReadiness();
          triggerNotification('success', 'Mock interview completed! Scroll down to review your final transcript score.');
        }
      }
    } catch (err) {
      triggerNotification('error', 'Failed to submit answer');
    } finally {
      setIsSubmittingAnswer(false);
    }
  };

  const handleLoadTranscript = async (id) => {
    try {
      const res = await api.get(`/api/students/me/interviews/${id}`);
      if (res.data && res.data.success) {
        setSelectedTranscript(res.data.data);
      }
    } catch (err) {
      triggerNotification('error', 'Failed to load transcript');
    }
  };

  const handleDeleteInterviewSession = async (id) => {
    try {
      const res = await api.delete(`/api/students/me/interviews/${id}`);
      if (res.data && res.data.success) {
        if (selectedTranscript?.sessionId === id) {
          setSelectedTranscript(null);
        }
        await reloadInterviews();
        triggerNotification('success', 'Session deleted.');
      }
    } catch (err) {
      triggerNotification('error', 'Failed to delete session');
    }
  };

  const reloadResumes = async () => {
    try {
      const res = await api.get('/api/students/me/resumes');
      if (res.data && res.data.success) {
        const list = res.data.data || [];
        setResumesList(list);
        
        // Load active or first one if nothing is selected yet
        const active = list.find(r => r.isActive);
        if (active && !selectedResume) {
          await loadResumeDetail(active.id);
        } else if (list.length > 0 && !selectedResume) {
          await loadResumeDetail(list[0].id);
        }
      }
    } catch (err) {
      console.error("Failed to load resume list", err);
    }
  };

  const loadResumeDetail = async (id) => {
    try {
      const res = await api.get(`/api/students/me/resumes/${id}`);
      if (res.data && res.data.success) {
        setSelectedResume(res.data.data);
      }
    } catch (err) {
      console.error("Failed to load resume details", err);
    }
  };

  const handleGenerateTailoredResume = async (e) => {
    e.preventDefault();
    setIsTailoring(true);
    try {
      const res = await api.post('/api/students/me/resumes/generate', {
        targetCompanyId: tailorForm.targetCompanyId ? Number(tailorForm.targetCompanyId) : null,
        jobDescription: tailorForm.jobDescription
      });
      if (res.data && res.data.success) {
        setTailorForm({ targetCompanyId: '', jobDescription: '' });
        await reloadResumes();
        if (res.data.data) {
          await loadResumeDetail(res.data.data.id);
        }
        await refreshReadiness();
        triggerNotification('success', 'ATS-Tailored resume generated successfully!');
      }
    } catch (err) {
      triggerNotification('error', err.response?.data?.message || 'Failed to generate tailored resume');
    } finally {
      setIsTailoring(false);
    }
  };

  const handleActivateResume = async (id) => {
    try {
      const res = await api.put(`/api/students/me/resumes/${id}/activate`);
      if (res.data && res.data.success) {
        await reloadResumes();
        await loadResumeDetail(id);
        await refreshReadiness();
        triggerNotification('success', 'Resume version set to Active!');
      }
    } catch (err) {
      triggerNotification('error', 'Failed to activate resume');
    }
  };

  const handleDeleteResume = async (id) => {
    try {
      const res = await api.delete(`/api/students/me/resumes/${id}`);
      if (res.data && res.data.success) {
        if (selectedResume?.id === id) {
          setSelectedResume(null);
        }
        await reloadResumes();
        await refreshReadiness();
        triggerNotification('success', 'Resume version deleted.');
      }
    } catch (err) {
      triggerNotification('error', 'Failed to delete resume version');
    }
  };

  const handleDownloadResumeFile = async (id, format) => {
    try {
      const response = await api.get(`/api/students/me/resumes/${id}/download?format=${format}`, {
        responseType: 'blob'
      });
      const blob = new Blob([response.data], {
        type: format === 'pdf' ? 'application/pdf' : 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
      });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `ats_tailored_resume.${format}`);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch (err) {
      triggerNotification('error', 'Failed to download resume document');
    }
  };

  const reloadApplications = async () => {
    try {
      const appRes = await api.get('/api/students/me/applications');
      if (appRes.data && appRes.data.success) {
        setApplicationsList(appRes.data.data || []);
      }
      const deadRes = await api.get('/api/students/me/applications/upcoming-deadlines');
      if (deadRes.data && deadRes.data.success) {
        setUpcomingDeadlines(deadRes.data.data || []);
      }
    } catch (err) {
      console.error("Failed to load applications", err);
    }
  };

  const handleApplicationSubmit = async (e) => {
    e.preventDefault();
    try {
      const res = await api.post('/api/students/me/applications', applicationForm);
      if (res.data && res.data.success) {
        setApplicationForm({
          companyName: '', role: '', appliedDate: new Date().toISOString().split('T')[0], packageBand: '', jobUrl: '', notes: '', reminderDate: ''
        });
        setIsAddingApplication(false);
        await reloadApplications();
        // Force refresh readiness score to update target metrics
        await refreshReadiness();
        triggerNotification('success', 'Application added to tracker!');
      }
    } catch (err) {
      triggerNotification('error', 'Failed to add application');
    }
  };

  const handleEditApplicationSubmit = async (e) => {
    e.preventDefault();
    try {
      const res = await api.put(`/api/students/me/applications/${isEditingApplication.id}`, isEditingApplication);
      if (res.data && res.data.success) {
        setIsEditingApplication(null);
        await reloadApplications();
        await refreshReadiness();
        triggerNotification('success', 'Application details updated!');
      }
    } catch (err) {
      triggerNotification('error', 'Failed to edit application');
    }
  };

  const handleUpdateApplicationStage = async (id, stage) => {
    try {
      const res = await api.put(`/api/students/me/applications/${id}/stage`, { stage });
      if (res.data && res.data.success) {
        await reloadApplications();
        triggerNotification('success', `Stage updated to ${stage}!`);
      }
    } catch (err) {
      triggerNotification('error', 'Failed to update stage');
    }
  };

  const handleDeleteApplication = async (id) => {
    try {
      const res = await api.delete(`/api/students/me/applications/${id}`);
      if (res.data && res.data.success) {
        await reloadApplications();
        await refreshReadiness();
        triggerNotification('success', 'Application deleted from board.');
      }
    } catch (err) {
      triggerNotification('error', 'Failed to delete application');
    }
  };

  const viewApplicationHistory = async (app) => {
    setIsViewingHistory(app);
    try {
      const res = await api.get(`/api/students/me/applications/${app.id}/history`);
      if (res.data && res.data.success) {
        setHistoryList(res.data.data || []);
      }
    } catch (err) {
      console.error("Failed to load history log", err);
    }
  };

  const handleDragStart = (e, id) => {
    e.dataTransfer.setData("applicationId", id.toString());
  };

  const handleDrop = async (e, stage) => {
    e.preventDefault();
    const id = e.dataTransfer.getData("applicationId");
    if (!id) return;
    try {
      const res = await api.put(`/api/students/me/applications/${id}/stage`, { stage });
      if (res.data && res.data.success) {
        await reloadApplications();
        triggerNotification('success', `Moved application to ${stage}!`);
      }
    } catch (err) {
      triggerNotification('error', 'Failed to update stage');
    }
  };

  // DSA Practice State
  const [dsaTasks, setDsaTasks] = useState([]);
  const [dsaProgress, setDsaProgress] = useState(null);

  const toggleDsaTask = async (problemId, currentStatus) => {
    const newStatus = currentStatus === 'solved' ? 'pending' : 'solved';
    try {
      const res = await api.put(`/api/students/me/dsa/tasks/${problemId}/status`, { status: newStatus });
      if (res.data && res.data.success) {
        // Refresh tasks and stats
        const dsaTasksRes = await api.get('/api/students/me/dsa/daily-tasks');
        if (dsaTasksRes.data && dsaTasksRes.data.success) {
          setDsaTasks(dsaTasksRes.data.data.tasks || []);
        }
        const dsaProgRes = await api.get('/api/students/me/dsa/progress');
        if (dsaProgRes.data && dsaProgRes.data.success) {
          setDsaProgress(dsaProgRes.data.data);
        }
        // Force refresh readiness score to link new solved count
        await refreshReadiness();
        // Refresh overall profile for streak updates
        const profRes = await api.get('/api/students/me');
        if (profRes.data && profRes.data.success) {
          setProfile(profRes.data.data);
        }
        triggerNotification('success', `Problem marked as ${newStatus}!`);
      }
    } catch (err) {
      triggerNotification('error', 'Failed to update problem status');
    }
  };

  // Load All Profile and Intake Data
  const loadData = async () => {
    try {
      // 1. Fetch Profile
      const profileRes = await api.get('/api/students/me');
      if (profileRes.data && profileRes.data.success) {
        const p = profileRes.data.data;
        setProfile(p);
        setProfileForm({
          name: p.name || '',
          college: p.college || '',
          branch: p.branch || '',
          batch: p.batch || ''
        });
      }

      // 2. Fetch Marks
      const marksRes = await api.get('/api/students/me/marks');
      if (marksRes.data && marksRes.data.success) {
        setMarksData(marksRes.data.data.marks || []);
        setOverallCgpa(marksRes.data.data.overallCgpa || 0.0);
      }

      // 3. Fetch Skills
      const skillsRes = await api.get('/api/students/me/skills');
      if (skillsRes.data && skillsRes.data.success) {
        setSkillsList(skillsRes.data.data.skills || []);
      }

      // 4. Fetch Companies
      const companiesRes = await api.get('/api/students/me/target-companies');
      if (companiesRes.data && companiesRes.data.success) {
        setCompaniesList(companiesRes.data.data || []);
      }

      // 5. Fetch Resume Details
      const resumeRes = await api.get('/api/students/me/resume');
      if (resumeRes.data && resumeRes.data.success) {
        setResumeUrl(resumeRes.data.data.resumeUrl || '');
        setParsedData(resumeRes.data.data.parsedData || null);
      }

      // 6. Fetch Readiness Breakdown
      const readinessRes = await api.get('/api/students/me/readiness-score');
      if (readinessRes.data && readinessRes.data.success) {
        setReadinessDetails(readinessRes.data.data);
      }

      // 7. Fetch DSA Daily Tasks
      try {
        const dsaTasksRes = await api.get('/api/students/me/dsa/daily-tasks');
        if (dsaTasksRes.data && dsaTasksRes.data.success) {
          setDsaTasks(dsaTasksRes.data.data.tasks || []);
        }
      } catch (err) {
        console.error("Failed to load DSA tasks", err);
      }

      // 8. Fetch DSA Progress Stats
      try {
        const dsaProgRes = await api.get('/api/students/me/dsa/progress');
        if (dsaProgRes.data && dsaProgRes.data.success) {
          setDsaProgress(dsaProgRes.data.data);
        }
      } catch (err) {
        console.error("Failed to load DSA progress", err);
      }

      // 9. Fetch Project Recommendations
      try {
        const recRes = await api.get('/api/students/me/projects/recommendations');
        if (recRes.data && recRes.data.success) {
          setProjectRecommendations(recRes.data.data || []);
        }
      } catch (err) {
        console.error("Failed to load project recommendations", err);
      }

      // 10. Fetch Student Projects
      try {
        const projRes = await api.get('/api/students/me/projects');
        if (projRes.data && projRes.data.success) {
          setMyProjects(projRes.data.data || []);
        }
      } catch (err) {
        console.error("Failed to load student projects", err);
      }

      // 11. Fetch Generated Resumes list
      try {
        const resListRes = await api.get('/api/students/me/resumes');
        if (resListRes.data && resListRes.data.success) {
          const list = resListRes.data.data || [];
          setResumesList(list);
          const active = list.find(r => r.isActive);
          if (active) {
            const detailRes = await api.get(`/api/students/me/resumes/${active.id}`);
            if (detailRes.data && detailRes.data.success) {
              setSelectedResume(detailRes.data.data);
            }
          } else if (list.length > 0) {
            const detailRes = await api.get(`/api/students/me/resumes/${list[0].id}`);
            if (detailRes.data && detailRes.data.success) {
              setSelectedResume(detailRes.data.data);
            }
          }
        }
      } catch (err) {
        console.error("Failed to load resume list at startup", err);
      }

      // 12. Fetch Mock Interview Sessions list
      try {
        const interviewRes = await api.get('/api/students/me/interviews');
        if (interviewRes.data && interviewRes.data.success) {
          setInterviewSessions(interviewRes.data.data || []);
        }
      } catch (err) {
        console.error("Failed to load interview list at startup", err);
      }

    } catch (err) {
      console.error(err);
      setError('Failed to refresh data from server');
    } finally {
      setLoading(false);
    }
  };

  const refreshReadiness = async () => {
    try {
      const res = await api.post('/api/students/me/readiness-score/recalculate');
      if (res.data && res.data.success) {
        setReadinessDetails(res.data.data);
      }
    } catch (err) {
      console.error("Failed to recalculate readiness details", err);
    }
  };

  const handleRecalculate = async () => {
    setLoading(true);
    try {
      const res = await api.post('/api/students/me/readiness-score/recalculate');
      if (res.data && res.data.success) {
        setReadinessDetails(res.data.data);
        // Refresh profile score
        const profRes = await api.get('/api/students/me');
        if (profRes.data && profRes.data.success) setProfile(profRes.data.data);
        triggerNotification('success', 'Readiness score recalculated successfully!');
      }
    } catch (err) {
      triggerNotification('error', 'Failed to recalculate score');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const triggerNotification = (type, msg) => {
    if (type === 'success') {
      setSuccess(msg);
      setTimeout(() => setSuccess(''), 4000);
    } else {
      setError(msg);
      setTimeout(() => setError(''), 4000);
    }
  };

  // Profile Save
  const handleProfileSave = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const res = await api.put('/api/students/me', {
        name: profileForm.name,
        college: profileForm.college,
        branch: profileForm.branch,
        batch: profileForm.batch
      });
      if (res.data && res.data.success) {
        setProfile(res.data.data);
        setIsEditingProfile(false);
        refreshReadiness();
        triggerNotification('success', 'Academic profile updated successfully!');
      }
    } catch (err) {
      triggerNotification('error', 'Failed to update profile details');
    } finally {
      setLoading(false);
    }
  };

  // Add/Update Marks
  const handleMarkSubmit = async (e) => {
    e.preventDefault();
    if (!markForm.sgpa) return;
    setLoading(true);
    try {
      const res = await api.post('/api/students/me/marks', {
        semester: parseInt(markForm.semester),
        sgpa: parseFloat(markForm.sgpa)
      });
      if (res.data && res.data.success) {
        setMarksData(res.data.data.marks);
        setOverallCgpa(res.data.data.overallCgpa);
        setIsAddingMark(false);
        setMarkForm(prev => ({ ...prev, sgpa: '' }));
        // Refresh profile score
        const profRes = await api.get('/api/students/me');
        if (profRes.data && profRes.data.success) setProfile(profRes.data.data);
        refreshReadiness();
        triggerNotification('success', `Semester ${markForm.semester} mark recorded!`);
      }
    } catch (err) {
      triggerNotification('error', err.response?.data?.message || 'Failed to submit SGPA');
    } finally {
      setLoading(false);
    }
  };

  // Add Skill
  const handleAddSkill = async (e) => {
    e.preventDefault();
    if (!newSkillInput.trim()) return;
    
    const cleanSkill = newSkillInput.trim();
    if (skillsList.includes(cleanSkill)) {
      triggerNotification('error', 'Skill already exists in your inventory');
      return;
    }

    const updatedSkills = [...skillsList, cleanSkill];
    try {
      const res = await api.put('/api/students/me/skills', { skills: updatedSkills });
      if (res.data && res.data.success) {
        setSkillsList(res.data.data.skills);
        setNewSkillInput('');
        // Refresh score
        const profRes = await api.get('/api/students/me');
        if (profRes.data && profRes.data.success) setProfile(profRes.data.data);
        refreshReadiness();
        triggerNotification('success', `Skill "${cleanSkill}" added!`);
      }
    } catch (err) {
      triggerNotification('error', 'Failed to add skill');
    }
  };

  // Remove Skill
  const handleRemoveSkill = async (skillToRemove) => {
    const updatedSkills = skillsList.filter(s => s !== skillToRemove);
    try {
      const res = await api.put('/api/students/me/skills', { skills: updatedSkills });
      if (res.data && res.data.success) {
        setSkillsList(res.data.data.skills);
        // Refresh score
        const profRes = await api.get('/api/students/me');
        if (profRes.data && profRes.data.success) setProfile(profRes.data.data);
        refreshReadiness();
        triggerNotification('success', `Skill "${skillToRemove}" removed`);
      }
    } catch (err) {
      triggerNotification('error', 'Failed to remove skill');
    }
  };

  // Add Company
  const handleCompanySubmit = async (e) => {
    e.preventDefault();
    const { companyName, role, packageBand, applicationDeadline } = companyForm;
    if (!companyName || !role) return;

    setLoading(true);
    try {
      const res = await api.post('/api/students/me/target-companies', {
        companyName,
        role,
        packageBand,
        applicationDeadline: applicationDeadline || null
      });
      if (res.data && res.data.success) {
        setCompaniesList(prev => [...prev, res.data.data]);
        setIsAddingCompany(false);
        setCompanyForm({ companyName: '', role: '', packageBand: '', applicationDeadline: '' });
        // Refresh score
        const profRes = await api.get('/api/students/me');
        if (profRes.data && profRes.data.success) setProfile(profRes.data.data);
        refreshReadiness();
        triggerNotification('success', `Added ${companyName} to targets!`);
      }
    } catch (err) {
      triggerNotification('error', 'Failed to add company');
    } finally {
      setLoading(false);
    }
  };

  // Delete Company
  const handleDeleteCompany = async (id) => {
    try {
      const res = await api.delete(`/api/students/me/target-companies/${id}`);
      if (res.data && res.data.success) {
        setCompaniesList(prev => prev.filter(c => c.id !== id));
        // Refresh score
        const profRes = await api.get('/api/students/me');
        if (profRes.data && profRes.data.success) setProfile(profRes.data.data);
        refreshReadiness();
        triggerNotification('success', 'Company removed from target list');
      }
    } catch (err) {
      triggerNotification('error', 'Failed to delete company');
    }
  };

  // Upload Resume
  const handleResumeUpload = async (e) => {
    e.preventDefault();
    if (!resumeFile) return;

    setUploadingResume(true);
    const formData = new FormData();
    formData.append('file', resumeFile);

    try {
      const res = await api.post('/api/students/me/resume', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      });
      if (res.data && res.data.success) {
        setResumeUrl(res.data.data.resumeUrl);
        setParsedData(res.data.data.parsedData);
        setResumeFile(null);
        // Refresh skills & profile score
        const skillsRes = await api.get('/api/students/me/skills');
        if (skillsRes.data && skillsRes.data.success) setSkillsList(skillsRes.data.data.skills);
        const profRes = await api.get('/api/students/me');
        if (profRes.data && profRes.data.success) setProfile(profRes.data.data);
        refreshReadiness();
        triggerNotification('success', 'Resume parsed with Claude API. Skills merged!');
      }
    } catch (err) {
      triggerNotification('error', err.response?.data?.message || 'Failed to upload resume');
    } finally {
      setUploadingResume(false);
    }
  };

  if (loading && !profile) {
    return (
      <div className="min-h-screen bg-background flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-primary"></div>
      </div>
    );
  }

  // Pre-process charts SGPA data
  const chartData = marksData.map(m => ({
    name: `Sem ${m.semester}`,
    SGPA: m.sgpa
  }));

  return (
    <div className="min-h-screen bg-background text-gray-100 flex flex-col">
      {/* Header */}
      <header className="glass-panel border-b border-border/80 sticky top-0 z-50 px-6 py-4">
        <div className="max-w-7xl mx-auto flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-gradient-accent p-0.5 shadow-glow-primary">
              <div className="w-full h-full bg-dark-900 rounded-[10px] flex items-center justify-center font-bold text-white">
                🎯
              </div>
            </div>
            <div>
              <span className="font-extrabold text-xl bg-gradient-accent bg-clip-text text-transparent font-display">
                AI PLACEMENT
              </span>
              <span className="text-[10px] block text-gray-400 tracking-widest font-semibold uppercase -mt-1">
                Command Center
              </span>
            </div>
          </div>

          <div className="flex items-center gap-4">
            <div className="hidden md:flex flex-col text-right">
              <span className="font-medium text-sm text-white">{profile?.name}</span>
              <span className="text-xs text-gray-400">{profile?.email}</span>
            </div>
            <button 
              onClick={logout}
              className="flex items-center gap-2 px-3 py-2 bg-dark-100/50 hover:bg-red-500/10 border border-border hover:border-red-500/20 text-gray-300 hover:text-red-400 rounded-xl transition-all duration-200 text-sm font-semibold"
            >
              <LogOut size={16} />
              <span className="hidden sm:inline">Sign Out</span>
            </button>
          </div>
        </div>
      </header>

      {/* Main Container */}
      <main className="flex-1 max-w-7xl w-full mx-auto p-6 md:p-8 space-y-8">
        
        {/* Alerts */}
        {error && (
          <div className="bg-red-500/10 border border-red-500/20 text-red-400 p-4 rounded-2xl text-sm animate-pulse">
            {error}
          </div>
        )}
        {success && (
          <div className="bg-success/10 border border-success/20 text-success-light p-4 rounded-2xl text-sm flex items-center gap-2">
            <CheckCircle size={18} />
            <span>{success}</span>
          </div>
        )}

        {/* Top Profile Summary */}
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-6 bg-gradient-to-r from-card to-dark-900 border border-border p-6 rounded-3xl shadow-lg relative overflow-hidden">
          <div className="absolute top-0 right-0 w-64 h-64 bg-primary/5 rounded-full blur-3xl"></div>
          
          <div className="flex items-center gap-4">
            <div className="w-16 h-16 rounded-full bg-primary/10 border border-primary/20 flex items-center justify-center text-primary text-2xl font-bold">
              {profile?.name ? profile.name.charAt(0) : 'S'}
            </div>
            <div>
              <h1 className="text-2xl font-extrabold font-display text-white">
                {profile?.name}
              </h1>
              <p className="text-gray-400 text-sm flex items-center gap-1 mt-1">
                <span>{profile?.college}</span>
                <span className="text-gray-600">•</span>
                <span>{profile?.branch}</span>
                <span className="text-gray-600">•</span>
                <span>Batch {profile?.batch}</span>
              </p>
            </div>
          </div>

          <div className="flex gap-3">
            <button
              onClick={() => setIsEditingProfile(true)}
              className="flex items-center gap-2 px-4 py-2 bg-dark-100 hover:bg-dark-200 border border-border hover:border-gray-500 text-white text-sm font-semibold rounded-xl transition-all duration-200"
            >
              <Edit3 size={16} />
              <span>Edit Details</span>
            </button>
          </div>
        </div>

        {/* Tab Buttons */}
        <div className="flex border-b border-border/80 overflow-x-auto gap-2 scrollbar-none">
          {[
            { id: 'overview', label: 'Command Center', icon: CheckSquare },
            { id: 'resume', label: 'ATS Resume Workspace', icon: FileText },
            { id: 'academic', label: 'Academic Tracker', icon: Award },
            { id: 'skills', label: 'Skills Inventory', icon: Brain },
            { id: 'companies', label: 'Target Board', icon: Target },
            { id: 'dsa', label: 'DSA Practice', icon: BookOpen },
            { id: 'interview', label: 'AI Mock Interview', icon: Briefcase },
          ].map(tab => (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id)}
              className={`flex items-center gap-2 px-5 py-3.5 text-sm font-bold border-b-2 transition-all duration-200 whitespace-nowrap ${
                activeTab === tab.id 
                  ? 'border-primary text-primary bg-primary/5' 
                  : 'border-transparent text-gray-400 hover:text-white hover:bg-dark-100/20'
              }`}
            >
              <tab.icon size={16} />
              <span>{tab.label}</span>
            </button>
          ))}
        </div>

        {/* Tab Content Panels */}
        <div className="space-y-8">
          
          {/* TAB 1: OVERVIEW */}
          {activeTab === 'overview' && (
            <div className="space-y-8 animate-fadeIn">
              
              {/* Header card with Recalculate Trigger */}
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-dark-900 border border-border/60 p-5 rounded-2xl">
                <div>
                  <h3 className="font-bold text-sm text-gray-400 uppercase tracking-wider">Scoring Diagnostics Engine</h3>
                  {readinessDetails?.lastCalculatedAt && (
                    <span className="text-xs text-gray-500 mt-1 block">
                      Last calculated: {new Date(readinessDetails.lastCalculatedAt).toLocaleString()}
                    </span>
                  )}
                </div>
                <button
                  onClick={handleRecalculate}
                  className="px-5 py-2 bg-primary hover:bg-primary-hover active:scale-[0.98] text-white text-xs font-bold rounded-xl transition-all shadow-glow-primary flex items-center gap-1.5"
                >
                  <CalendarClock size={14} />
                  <span>Recalculate Score</span>
                </button>
              </div>

              {/* Main Score cards */}
              <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                
                {/* 1. Readiness Circular Gauge */}
                <div className="glass-panel rounded-3xl p-6 flex flex-col items-center justify-center text-center space-y-6">
                  <h3 className="font-bold text-lg text-white font-display border-b border-border/50 pb-2 w-full">
                    Overall Readiness
                  </h3>
                  
                  <ReadinessGauge score={readinessDetails?.overallScore ?? profile?.readinessScore ?? 0} />

                  <div className="text-sm text-gray-400 max-w-xs leading-relaxed">
                    {(readinessDetails?.overallScore ?? profile?.readinessScore ?? 0) >= 75 ? (
                      <span className="text-success-light font-semibold">Outstanding preparation! You are fully equipped to start mock interview trials.</span>
                    ) : (readinessDetails?.overallScore ?? profile?.readinessScore ?? 0) >= 40 ? (
                      <span className="text-primary-light font-semibold">Good progress. Review the diagnostics breakdown below and recommendations to level up.</span>
                    ) : (
                      <span className="text-secondary-light font-semibold">Initial status. Upload your resume or add target companies to start calculating readiness.</span>
                    )}
                  </div>
                </div>

                {/* 2. Sub-metrics Diagnostics breakdown */}
                <div className="lg:col-span-2 glass-panel rounded-3xl p-6 space-y-6">
                  <h3 className="font-bold text-lg text-white font-display border-b border-border/50 pb-2 flex items-center gap-2">
                    <CheckSquare className="text-primary" size={20} />
                    <span>Diagnostics Breakdown</span>
                  </h3>

                  <div className="space-y-4">
                    {/* Academics (20%) */}
                    <div className="bg-dark-900 border border-border/40 p-4 rounded-2xl space-y-2">
                      <div className="flex justify-between items-center text-xs">
                        <div className="flex items-center gap-2">
                          <span className="font-bold text-white uppercase tracking-wider">Academics</span>
                          <span className="text-[10px] text-gray-500 font-semibold">(20% Weight)</span>
                        </div>
                        <span className="font-extrabold text-primary-light">
                          {readinessDetails?.breakdown?.academics?.score ?? 0}%
                        </span>
                      </div>
                      <div className="w-full bg-dark-100 h-2 rounded-full overflow-hidden">
                        <div 
                          className="bg-primary h-full rounded-full transition-all duration-500" 
                          style={{ width: `${readinessDetails?.breakdown?.academics?.score ?? 0}%` }}
                        ></div>
                      </div>
                      <p className="text-xs text-gray-400">
                        CGPA: <span className="text-white font-bold">{readinessDetails?.breakdown?.academics?.cgpa ?? 0.0} / 10.0</span>. Academics score is based on cumulative performance.
                      </p>
                    </div>

                    {/* DSA Progress (25%) */}
                    <div className="bg-dark-900 border border-border/40 p-4 rounded-2xl space-y-2">
                      <div className="flex justify-between items-center text-xs">
                        <div className="flex items-center gap-2">
                          <span className="font-bold text-white uppercase tracking-wider">DSA Progress</span>
                          <span className="text-[10px] text-gray-500 font-semibold">(25% Weight)</span>
                        </div>
                        <span className="font-extrabold text-secondary-light">
                          {readinessDetails?.breakdown?.dsaProgress?.score ?? 0}%
                        </span>
                      </div>
                      <div className="w-full bg-dark-100 h-2 rounded-full overflow-hidden">
                        <div 
                          className="bg-secondary h-full rounded-full transition-all duration-500" 
                          style={{ width: `${readinessDetails?.breakdown?.dsaProgress?.score ?? 0}%` }}
                        ></div>
                      </div>
                      <p className="text-xs text-gray-400">
                        Problems Solved: <span className="text-white font-bold">{readinessDetails?.breakdown?.dsaProgress?.problemsSolved ?? 0} solved</span>. (DSA Practice module unlock in Phase 4).
                      </p>
                    </div>

                    {/* Projects (15%) */}
                    <div className="bg-dark-900 border border-border/40 p-4 rounded-2xl space-y-2">
                      <div className="flex justify-between items-center text-xs">
                        <div className="flex items-center gap-2">
                          <span className="font-bold text-white uppercase tracking-wider">Projects Completeness</span>
                          <span className="text-[10px] text-gray-500 font-semibold">(15% Weight)</span>
                        </div>
                        <span className="font-extrabold text-success-light">
                          {readinessDetails?.breakdown?.projects?.score ?? 0}%
                        </span>
                      </div>
                      <div className="w-full bg-dark-100 h-2 rounded-full overflow-hidden">
                        <div 
                          className="bg-success h-full rounded-full transition-all duration-500" 
                          style={{ width: `${readinessDetails?.breakdown?.projects?.score ?? 0}%` }}
                        ></div>
                      </div>
                      <p className="text-xs text-gray-400">
                        Project Count: <span className="text-white font-bold">{readinessDetails?.breakdown?.projects?.count ?? 0} project(s)</span> detected from your resume details.
                      </p>
                    </div>
                  </div>
                </div>

              </div>

              {/* Skill Matching & Resume Completeness Boards */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                
                {/* Skill Match Board (25%) */}
                <div className="glass-panel rounded-3xl p-6 space-y-4">
                  <h3 className="font-bold text-base text-white font-display border-b border-border/50 pb-2 flex justify-between items-center">
                    <span className="flex items-center gap-2">
                      <Target className="text-secondary" size={18} />
                      <span>Skill Matching (25% Weight)</span>
                    </span>
                    <span className="text-xs text-secondary-light font-extrabold bg-secondary/10 px-2 py-0.5 rounded-lg border border-secondary/20">
                      {readinessDetails?.breakdown?.skillMatch?.score ?? 0}% Match
                    </span>
                  </h3>

                  <div className="space-y-4">
                    {/* Matched skills */}
                    <div>
                      <span className="text-[10px] text-success font-bold block uppercase tracking-wider mb-2">Matched Skills</span>
                      <div className="flex flex-wrap gap-1.5">
                        {readinessDetails?.breakdown?.skillMatch?.matchedSkills?.length > 0 ? (
                          readinessDetails.breakdown.skillMatch.matchedSkills.map((sk, i) => (
                            <span key={i} className="text-xs px-2.5 py-1 bg-success/15 text-success rounded-lg border border-success/25">
                              {sk}
                            </span>
                          ))
                        ) : (
                          <span className="text-gray-500 text-xs italic">No matched skills. Check company target roles.</span>
                        )}
                      </div>
                    </div>

                    {/* Missing skills */}
                    <div>
                      <span className="text-[10px] text-red-400 font-bold block uppercase tracking-wider mb-2">Missing Skills Requirements</span>
                      <div className="flex flex-wrap gap-1.5">
                        {readinessDetails?.breakdown?.skillMatch?.missingSkills?.length > 0 ? (
                          readinessDetails.breakdown.skillMatch.missingSkills.map((sk, i) => (
                            <span key={i} className="text-xs px-2.5 py-1 bg-red-500/10 text-red-400 rounded-lg border border-red-500/20">
                              {sk}
                            </span>
                          ))
                        ) : (
                          <span className="text-success text-xs font-semibold">All required skills matched! Excellent alignment.</span>
                        )}
                      </div>
                    </div>
                  </div>
                </div>

                {/* Resume Completeness Board (15%) */}
                <div className="glass-panel rounded-3xl p-6 space-y-4">
                  <h3 className="font-bold text-base text-white font-display border-b border-border/50 pb-2 flex justify-between items-center">
                    <span className="flex items-center gap-2">
                      <FileText className="text-primary" size={18} />
                      <span>Resume Completeness (15% Weight)</span>
                    </span>
                    <span className="text-xs text-primary-light font-extrabold bg-primary/10 px-2 py-0.5 rounded-lg border border-primary/20">
                      {readinessDetails?.breakdown?.resumeCompleteness?.score ?? 0}% Complete
                    </span>
                  </h3>

                  <div className="space-y-3">
                    <span className="text-[10px] text-gray-400 font-bold block uppercase tracking-wider">Parser Status Check</span>
                    
                    {readinessDetails?.breakdown?.resumeCompleteness?.missingFields?.length > 0 ? (
                      <div className="space-y-1.5">
                        {readinessDetails.breakdown.resumeCompleteness.missingFields.map((fld, idx) => (
                          <div key={idx} className="flex items-center gap-2 bg-dark-900 border border-border/40 p-2.5 rounded-xl text-xs text-gray-400">
                            <div className="w-1.5 h-1.5 rounded-full bg-red-400"></div>
                            <span>Missing resume block: <strong className="text-gray-300 font-semibold">{fld}</strong></span>
                          </div>
                        ))}
                      </div>
                    ) : (
                      <div className="flex items-center gap-2 bg-success/10 border border-success/20 p-3 rounded-xl text-xs text-success-light">
                        <CheckSquare size={16} />
                        <span>All core resume blocks parsed successfully. Strong formatting!</span>
                      </div>
                    )}
                  </div>
                </div>

              </div>

            </div>
          )}

          {/* TAB 2: RESUME PARSER */}
          {activeTab === 'resume' && (
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
              
              {/* File Upload Zone */}
              <div className="glass-panel rounded-3xl p-6 space-y-6">
                <h3 className="font-bold text-lg text-white font-display border-b border-border/50 pb-2 flex items-center gap-2">
                  <UploadCloud className="text-primary" size={20} />
                  <span>Upload Resume</span>
                </h3>

                <form onSubmit={handleResumeUpload} className="space-y-4">
                  <div className="border-2 border-dashed border-border/80 hover:border-primary/50 rounded-2xl p-6 flex flex-col items-center justify-center text-center cursor-pointer transition-colors duration-200 bg-dark-900/40 relative">
                    <input
                      type="file"
                      accept=".pdf,.docx"
                      onChange={(e) => setResumeFile(e.target.files[0])}
                      className="absolute inset-0 w-full h-full opacity-0 cursor-pointer"
                    />
                    <UploadCloud className="text-gray-500 mb-3" size={40} />
                    <span className="text-sm font-bold text-white block">
                      {resumeFile ? resumeFile.name : 'Choose a file'}
                    </span>
                    <span className="text-xs text-gray-500 mt-1 block">Supports PDF and DOCX (Max 10MB)</span>
                  </div>

                  {resumeFile && (
                    <div className="bg-dark-100/50 p-3 rounded-xl border border-border flex justify-between items-center text-xs">
                      <span className="truncate text-gray-300 font-semibold">{resumeFile.name}</span>
                      <button type="button" onClick={() => setResumeFile(null)} className="text-gray-500 hover:text-white">
                        <X size={14} />
                      </button>
                    </div>
                  )}

                  <button
                    type="submit"
                    disabled={!resumeFile || uploadingResume}
                    className="w-full flex items-center justify-center gap-2 py-2.5 bg-primary hover:bg-primary-hover active:scale-[0.98] text-white font-bold rounded-xl transition-all shadow-glow-primary disabled:opacity-50 disabled:pointer-events-none"
                  >
                    {uploadingResume ? 'Processing with Gemini...' : 'Parse Resume'}
                  </button>
                </form>

                {resumeUrl && (
                  <div className="pt-4 border-t border-border/50">
                    <span className="text-xs text-gray-400 block font-semibold mb-2">CURRENT RESUME FILE</span>
                    <a
                      href={resumeUrl}
                      target="_blank"
                      rel="noreferrer"
                      className="flex items-center justify-between p-3 bg-dark-100/40 border border-border/60 rounded-xl hover:border-primary transition-all duration-200 text-sm font-semibold text-primary"
                    >
                      <span className="truncate max-w-[180px]">{resumeUrl.split('/').pop()}</span>
                      <Eye size={16} />
                    </a>
                  </div>
                )}
              </div>

              {/* Parsed JSON Result Display */}
              <div className="lg:col-span-2 glass-panel rounded-3xl p-6 space-y-6">
                <h3 className="font-bold text-lg text-white font-display border-b border-border/50 pb-2 flex items-center gap-2">
                  <FileText className="text-success" size={20} />
                  <span>AI Extracted Profile Details</span>
                </h3>

                {parsedData ? (
                  <div className="space-y-6 max-h-[60vh] overflow-y-auto pr-2">
                    
                    {/* Education */}
                    {parsedData.education && parsedData.education.length > 0 && (
                      <div className="space-y-2">
                        <span className="text-xs font-bold text-primary uppercase tracking-wider block">Education</span>
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                          {parsedData.education.map((edu, idx) => (
                            <div key={idx} className="bg-dark-950 border border-border/40 p-4 rounded-2xl">
                              <span className="font-bold text-white block text-sm">{edu.degree}</span>
                              <span className="text-xs text-gray-400 block mt-1">{edu.school}</span>
                              <span className="text-xs text-secondary-light font-semibold block mt-1">{edu.year}</span>
                            </div>
                          ))}
                        </div>
                      </div>
                    )}

                    {/* Work Experience */}
                    {parsedData.experience && parsedData.experience.length > 0 && (
                      <div className="space-y-2 pt-2 border-t border-border/30">
                        <span className="text-xs font-bold text-secondary uppercase tracking-wider block">Work Experience</span>
                        <div className="space-y-3">
                          {parsedData.experience.map((exp, idx) => (
                            <div key={idx} className="bg-dark-950 border border-border/40 p-4 rounded-2xl">
                              <div className="flex justify-between items-start">
                                <span className="font-bold text-white text-sm">{exp.role}</span>
                                <span className="text-xs text-gray-500 font-semibold">{exp.duration}</span>
                              </div>
                              <span className="text-xs text-primary-light block mt-0.5">{exp.company}</span>
                              <p className="text-xs text-gray-400 mt-2 leading-relaxed">{exp.description}</p>
                            </div>
                          ))}
                        </div>
                      </div>
                    )}

                    {/* Projects */}
                    {parsedData.projects && parsedData.projects.length > 0 && (
                      <div className="space-y-2 pt-2 border-t border-border/30">
                        <span className="text-xs font-bold text-success uppercase tracking-wider block">Academic/Side Projects</span>
                        <div className="space-y-3">
                          {parsedData.projects.map((proj, idx) => (
                            <div key={idx} className="bg-dark-950 border border-border/40 p-4 rounded-2xl">
                              <span className="font-bold text-white text-sm block">{proj.name}</span>
                              <p className="text-xs text-gray-400 mt-1.5 leading-relaxed">{proj.description}</p>
                              {proj.technologies && (
                                <span className="inline-block mt-3 text-[10px] font-bold text-primary bg-primary/10 px-2.5 py-1 rounded-full">
                                  {proj.technologies}
                                </span>
                              )}
                            </div>
                          ))}
                        </div>
                      </div>
                    )}

                    {/* Certifications */}
                    {parsedData.certifications && parsedData.certifications.length > 0 && (
                      <div className="space-y-2 pt-2 border-t border-border/30">
                        <span className="text-xs font-bold text-warning-light uppercase tracking-wider block">Certifications</span>
                        <div className="flex flex-wrap gap-2">
                          {parsedData.certifications.map((cert, idx) => (
                            <span key={idx} className="px-3 py-1 bg-warning/10 text-warning-light border border-warning/20 rounded-lg text-xs font-medium">
                              {cert}
                            </span>
                          ))}
                        </div>
                      </div>
                    )}

                  </div>
                ) : (
                  <div className="text-center py-16 text-gray-500 text-sm">
                    No resume details parsed yet. Choose a file on the left and submit.
                  </div>
                )}
              </div>

              {/* Horizontal Divider */}
              <hr className="border-border/40 my-8 col-span-1 lg:col-span-3" />

              {/* ATS Tailored Resumes Section */}
              <div className="lg:col-span-3 grid grid-cols-1 lg:grid-cols-3 gap-8">
                
                {/* Tailoring Control Panel */}
                <div className="glass-panel rounded-3xl p-6 space-y-6">
                  <h3 className="font-bold text-lg text-white font-display border-b border-border/50 pb-2 flex items-center gap-2">
                    <Sparkles className="text-secondary" size={20} />
                    <span>ATS Custom Tailor</span>
                  </h3>

                  <form onSubmit={handleGenerateTailoredResume} className="space-y-4 text-xs">
                    <div>
                      <label className="block font-bold text-gray-400 uppercase tracking-wider mb-2">Select Target Company/Role</label>
                      <select
                        value={tailorForm.targetCompanyId}
                        onChange={(e) => setTailorForm({ ...tailorForm, targetCompanyId: e.target.value })}
                        className="w-full px-4 py-2.5 bg-dark-300 border border-border rounded-xl text-white focus:outline-none focus:border-primary"
                      >
                        <option value="">-- Optional: Select Target Target --</option>
                        {applicationsList.map(app => (
                          <option key={app.id} value={app.id}>{app.companyName} - {app.role}</option>
                        ))}
                      </select>
                    </div>

                    <div>
                      <label className="block font-bold text-gray-400 uppercase tracking-wider mb-2">Job Description (Raw text)</label>
                      <textarea
                        value={tailorForm.jobDescription}
                        onChange={(e) => setTailorForm({ ...tailorForm, jobDescription: e.target.value })}
                        placeholder="Paste target job qualifications, requirements, or responsibilities here to match keywords..."
                        className="w-full px-4 py-2.5 bg-dark-300 border border-border rounded-xl text-white focus:outline-none focus:border-primary h-36 resize-none"
                      ></textarea>
                    </div>

                    <button
                      type="submit"
                      disabled={isTailoring || !parsedData}
                      className="w-full py-3 bg-secondary hover:bg-secondary-hover active:scale-[0.98] text-white font-bold rounded-xl transition-all shadow-glow-secondary disabled:opacity-50 disabled:pointer-events-none flex items-center justify-center gap-2"
                    >
                      {isTailoring ? (
                        <>
                          <div className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin"></div>
                          <span>Tuning Bullets with Gemini...</span>
                        </>
                      ) : (
                        <>
                          <Sparkles size={16} />
                          <span>Generate ATS Tailored Resume</span>
                        </>
                      )}
                    </button>
                  </form>

                  {/* Versions history list */}
                  <div className="pt-6 border-t border-border/50 space-y-4">
                    <span className="text-[10px] text-gray-400 font-bold block uppercase tracking-wider">Generated Versions ({resumesList.length})</span>
                    <div className="space-y-2.5 max-h-[30vh] overflow-y-auto pr-1">
                      {resumesList.length > 0 ? (
                        resumesList.map(res => (
                          <div
                            key={res.id}
                            onClick={() => loadResumeDetail(res.id)}
                            className={`p-3 rounded-xl border cursor-pointer transition-all flex justify-between items-center text-xs ${
                              selectedResume?.id === res.id 
                                ? 'bg-primary/10 border-primary text-white' 
                                : 'bg-dark-900 border-border/60 text-gray-400 hover:border-gray-500 hover:text-white'
                            }`}
                          >
                            <div className="truncate max-w-[150px] space-y-0.5">
                              <span className="font-bold block text-white truncate">{res.jobTitle}</span>
                              <span className="text-[9px] text-gray-500">{new Date(res.createdAt).toLocaleDateString()}</span>
                            </div>
                            
                            <div className="flex items-center gap-2">
                              {res.isActive && (
                                <span className="text-[9px] font-extrabold uppercase px-1.5 py-0.5 bg-success/15 border border-success/30 text-success rounded-md">
                                  Active
                                </span>
                              )}
                              <button
                                onClick={(e) => {
                                  e.stopPropagation();
                                  handleDeleteResume(res.id);
                                }}
                                className="p-1 hover:text-red-400 rounded hover:bg-dark-100 transition-colors"
                              >
                                <Trash2 size={12} />
                              </button>
                            </div>
                          </div>
                        ))
                      ) : (
                        <p className="text-gray-500 text-[10px] italic">No tailored versions created.</p>
                      )}
                    </div>
                  </div>

                </div>

                {/* Resume Version Preview Board */}
                <div className="lg:col-span-2 glass-panel rounded-3xl p-6 space-y-6">
                  {selectedResume ? (
                    <div className="space-y-6">
                      
                      {/* Document Meta & Downloads */}
                      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center border-b border-border/50 pb-4 gap-4">
                        <div>
                          <div className="flex items-center gap-2.5">
                            <h4 className="font-extrabold text-base text-white font-display">{selectedResume.jobTitle}</h4>
                            {selectedResume.isActive ? (
                              <span className="text-[10px] font-bold bg-success/10 border border-success/30 text-success px-2 py-0.5 rounded-lg">
                                Active Version
                              </span>
                            ) : (
                              <button
                                onClick={() => handleActivateResume(selectedResume.id)}
                                className="text-[10px] font-bold bg-dark-100 hover:bg-primary border border-border hover:border-primary text-gray-400 hover:text-white px-2 py-0.5 rounded-lg transition-all"
                              >
                                Set as Active
                              </button>
                            )}
                          </div>
                          <span className="text-[10px] text-gray-500 mt-1 block">Compiled at: {new Date(selectedResume.createdAt).toLocaleString()}</span>
                        </div>

                        {/* Download buttons */}
                        <div className="flex gap-2 w-full sm:w-auto">
                          <button
                            onClick={() => handleDownloadResumeFile(selectedResume.id, 'pdf')}
                            className="flex-1 sm:flex-none flex items-center justify-center gap-1.5 px-3.5 py-2 bg-dark-100 hover:bg-red-500/10 border border-border hover:border-red-500/20 text-gray-300 hover:text-red-400 text-xs font-bold rounded-xl transition-all"
                          >
                            <FileText size={14} />
                            <span>Download PDF</span>
                          </button>
                          <button
                            onClick={() => handleDownloadResumeFile(selectedResume.id, 'docx')}
                            className="flex-1 sm:flex-none flex items-center justify-center gap-1.5 px-3.5 py-2 bg-dark-100 hover:bg-primary/10 border border-border hover:border-primary/20 text-gray-300 hover:text-primary-light text-xs font-bold rounded-xl transition-all"
                          >
                            <FileCheck size={14} />
                            <span>Download Word</span>
                          </button>
                        </div>
                      </div>

                      {/* Keywords list */}
                      {selectedResume.matchedKeywords && selectedResume.matchedKeywords.length > 0 && (
                        <div className="space-y-2">
                          <span className="text-[10px] text-success font-bold block uppercase tracking-wider">ATS Keyword Matches</span>
                          <div className="flex flex-wrap gap-1.5">
                            {selectedResume.matchedKeywords.map((kw, i) => (
                              <span key={i} className="text-xs px-2.5 py-1 bg-success/10 border border-success/20 text-success rounded-lg font-medium">
                                ✓ {kw}
                              </span>
                            ))}
                          </div>
                        </div>
                      )}

                      {/* Version Content preview */}
                      <div className="space-y-6 max-h-[50vh] overflow-y-auto pr-2 text-xs border-t border-border/30 pt-4">
                        
                        {/* Summary */}
                        {selectedResume.generatedContent?.summary && (
                          <div className="space-y-1.5">
                            <span className="text-[10px] text-gray-400 font-bold uppercase tracking-wider block">ATS Tailored Summary</span>
                            <p className="text-gray-300 leading-relaxed italic">{selectedResume.generatedContent.summary}</p>
                          </div>
                        )}

                        {/* Tailored Experience */}
                        {selectedResume.generatedContent?.experience && selectedResume.generatedContent.experience.length > 0 && (
                          <div className="space-y-3 pt-3 border-t border-border/20">
                            <span className="text-[10px] text-gray-400 font-bold uppercase tracking-wider block">Work History (Enhanced)</span>
                            <div className="space-y-3">
                              {selectedResume.generatedContent.experience.map((exp, i) => (
                                <div key={i} className="bg-dark-950 p-4 rounded-xl border border-border/30 space-y-1">
                                  <div className="flex justify-between items-start">
                                    <span className="font-bold text-white text-sm">{exp.role}</span>
                                    <span className="text-gray-500 font-semibold text-[10px]">{exp.duration}</span>
                                  </div>
                                  <span className="text-primary-light block font-semibold">{exp.company}</span>
                                  <p className="text-gray-400 mt-2 whitespace-pre-line leading-relaxed">{exp.description}</p>
                                </div>
                              ))}
                            </div>
                          </div>
                        )}

                        {/* Tailored Projects */}
                        {selectedResume.generatedContent?.projects && selectedResume.generatedContent.projects.length > 0 && (
                          <div className="space-y-3 pt-3 border-t border-border/20">
                            <span className="text-[10px] text-gray-400 font-bold uppercase tracking-wider block">Tailored Projects</span>
                            <div className="space-y-3">
                              {selectedResume.generatedContent.projects.map((proj, i) => (
                                <div key={i} className="bg-dark-950 p-4 rounded-xl border border-border/30 space-y-1.5">
                                  <span className="font-bold text-white text-sm block">{proj.name}</span>
                                  <p className="text-gray-400 whitespace-pre-line leading-relaxed">{proj.description}</p>
                                  {proj.technologies && (
                                    <span className="inline-block mt-2 text-[10px] font-bold text-primary bg-primary/10 px-2 py-0.5 rounded-lg border border-primary/20">
                                      {proj.technologies}
                                    </span>
                                  )}
                                </div>
                              ))}
                            </div>
                          </div>
                        )}

                      </div>

                    </div>
                  ) : (
                    <div className="text-center py-24 text-gray-500 text-sm">
                      No resume version selected. Select or generate a tailored ATS version on the left.
                    </div>
                  )}
                </div>

              </div>
            </div>
          )}

          {/* TAB 3: ACADEMIC TRACKER */}
          {activeTab === 'academic' && (
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
              
              {/* CGPA Progression Chart */}
              <div className="lg:col-span-2 glass-panel rounded-3xl p-6 space-y-4">
                <h3 className="font-bold text-lg text-white font-display flex justify-between items-center border-b border-border/50 pb-2">
                  <span>SGPA Progression</span>
                  <span className="text-sm font-semibold text-primary">Cumulative CGPA: {overallCgpa} / 10.0</span>
                </h3>

                {marksData.length > 0 ? (
                  <div className="h-64 pt-4">
                    <ResponsiveContainer width="100%" height="100%">
                      <AreaChart data={chartData} margin={{ top: 10, right: 10, left: -25, bottom: 0 }}>
                        <defs>
                          <linearGradient id="gpaGrad" x1="0" y1="0" x2="0" y2="1">
                            <stop offset="5%" stopColor="#6366F1" stopOpacity={0.4}/>
                            <stop offset="95%" stopColor="#6366F1" stopOpacity={0}/>
                          </linearGradient>
                        </defs>
                        <CartesianGrid strokeDasharray="3 3" stroke="#1f293d" vertical={false} />
                        <XAxis dataKey="name" stroke="#4b5563" tick={{ fontSize: 11 }} />
                        <YAxis domain={[0, 10]} stroke="#4b5563" tick={{ fontSize: 11 }} />
                        <Tooltip 
                          contentStyle={{ backgroundColor: '#161c2c', borderColor: '#2a344a', borderRadius: '12px' }}
                          labelStyle={{ color: '#fff', fontWeight: 'bold' }}
                        />
                        <Area type="monotone" dataKey="SGPA" stroke="#6366F1" strokeWidth={3} fillOpacity={1} fill="url(#gpaGrad)" />
                      </AreaChart>
                    </ResponsiveContainer>
                  </div>
                ) : (
                  <div className="text-center py-20 text-gray-500 text-sm">
                    No academic marks entered. Log semester details to generate the trend chart.
                  </div>
                )}
              </div>

              {/* Semester List & Add Block */}
              <div className="glass-panel rounded-3xl p-6 space-y-6">
                <div className="flex justify-between items-center border-b border-border/50 pb-2">
                  <h3 className="font-bold text-lg text-white font-display">Semesters</h3>
                  <button
                    onClick={() => setIsAddingMark(true)}
                    className="p-1 bg-primary/20 hover:bg-primary/30 text-primary border border-primary/40 rounded-lg transition-colors duration-200"
                  >
                    <Plus size={16} />
                  </button>
                </div>

                {isAddingMark && (
                  <form onSubmit={handleMarkSubmit} className="bg-dark-900/60 p-4 border border-border/50 rounded-2xl space-y-4">
                    <div className="grid grid-cols-2 gap-4">
                      <div>
                        <label className="block text-[10px] font-bold text-gray-400 uppercase mb-1">Semester</label>
                        <select
                          value={markForm.semester}
                          onChange={(e) => setMarkForm({ ...markForm, semester: e.target.value })}
                          className="w-full bg-dark-300 border border-border rounded-xl text-xs py-2 px-3 focus:outline-none focus:border-primary text-white"
                        >
                          {[1,2,3,4,5,6,7,8].map(sem => (
                            <option key={sem} value={sem}>{sem}</option>
                          ))}
                        </select>
                      </div>
                      <div>
                        <label className="block text-[10px] font-bold text-gray-400 uppercase mb-1">SGPA</label>
                        <input
                          type="number"
                          step="0.01"
                          min="0"
                          max="10"
                          value={markForm.sgpa}
                          onChange={(e) => setMarkForm({ ...markForm, sgpa: e.target.value })}
                          placeholder="e.g. 9.15"
                          className="w-full bg-dark-300 border border-border rounded-xl text-xs py-2 px-3 focus:outline-none focus:border-primary text-white"
                          required
                        />
                      </div>
                    </div>

                    <div className="flex justify-end gap-2 text-xs">
                      <button
                        type="button"
                        onClick={() => setIsAddingMark(false)}
                        className="px-3 py-1.5 border border-border rounded-lg text-gray-300"
                      >
                        Cancel
                      </button>
                      <button
                        type="submit"
                        className="px-3 py-1.5 bg-primary text-white font-bold rounded-lg"
                      >
                        Record
                      </button>
                    </div>
                  </form>
                )}

                <div className="space-y-3">
                  {marksData.length > 0 ? (
                    marksData.map((m) => (
                      <div key={m.id} className="flex justify-between items-center p-3 bg-dark-950/80 border border-border/30 rounded-xl">
                        <span className="font-semibold text-sm text-gray-300">Semester {m.semester}</span>
                        <span className="font-extrabold text-sm text-white bg-primary/10 border border-primary/20 px-3 py-1 rounded-xl">
                          {m.sgpa} SGPA
                        </span>
                      </div>
                    ))
                  ) : (
                    <p className="text-gray-500 text-xs text-center py-6">No semesters logged.</p>
                  )}
                </div>
              </div>

            </div>
          )}

          {/* TAB 4: SKILLS INVENTORY */}
          {activeTab === 'skills' && (
            <div className="glass-panel rounded-3xl p-6 space-y-6">
              <h3 className="font-bold text-lg text-white font-display border-b border-border/50 pb-2 flex items-center gap-2">
                <Brain className="text-primary" size={20} />
                <span>Skills Inventory Management</span>
              </h3>

              <form onSubmit={handleAddSkill} className="flex gap-3 max-w-lg">
                <input
                  type="text"
                  value={newSkillInput}
                  onChange={(e) => setNewSkillInput(e.target.value)}
                  placeholder="e.g. Docker, TypeScript, Go"
                  className="flex-1 px-4 py-2.5 bg-dark-300 border border-border rounded-xl text-sm focus:outline-none focus:border-primary text-white"
                />
                <button
                  type="submit"
                  className="px-5 py-2.5 bg-primary hover:bg-primary-hover active:scale-[0.98] text-white text-sm font-bold rounded-xl transition-all shadow-glow-primary flex items-center gap-1.5"
                >
                  <Plus size={16} />
                  <span>Add Skill</span>
                </button>
              </form>

              <div className="space-y-2">
                <span className="text-xs text-gray-400 font-bold block uppercase tracking-wider">CURRENT SKILL SET ({skillsList.length})</span>
                <div className="flex flex-wrap gap-2 pt-2">
                  {skillsList.length > 0 ? (
                    skillsList.map((skill, idx) => (
                      <span 
                        key={idx}
                        className="inline-flex items-center gap-1.5 px-3 py-1.5 bg-primary/10 hover:bg-primary/20 border border-primary/20 hover:border-primary/40 text-primary-light rounded-xl text-sm font-semibold transition-all duration-200"
                      >
                        <span>{skill}</span>
                        <button 
                          onClick={() => handleRemoveSkill(skill)}
                          className="hover:text-red-400 p-0.5 rounded-full hover:bg-dark-100 transition-colors"
                        >
                          <X size={12} />
                        </button>
                      </span>
                    ))
                  ) : (
                    <p className="text-gray-500 text-sm py-4">No skills entered. Type a skill and click Add, or upload your resume for automatic extraction.</p>
                  )}
                </div>
              </div>

              {/* Horizontal Divider */}
              <hr className="border-border/40 my-8" />

              <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
                
                {/* 1. Project Recommendations */}
                <div className="space-y-6">
                  <div className="border-b border-border/50 pb-2 flex justify-between items-center">
                    <div>
                      <h4 className="font-bold text-base text-white font-display">Targeted Project Recommendations</h4>
                      <p className="text-xs text-gray-400 mt-0.5">Recommendations matching your target companies' skill gaps.</p>
                    </div>
                  </div>

                  <div className="space-y-4">
                    {projectRecommendations && projectRecommendations.length > 0 ? (
                      projectRecommendations.map(rec => {
                        let difficultyColor = 'text-success-light bg-success/15 border-success/25';
                        if (rec.difficulty === 'intermediate') difficultyColor = 'text-primary-light bg-primary/15 border-primary/25';
                        if (rec.difficulty === 'advanced') difficultyColor = 'text-secondary-light bg-secondary/15 border-secondary/25';

                        return (
                          <div key={rec.id} className="bg-dark-900 border border-border p-5 rounded-2xl space-y-3 relative overflow-hidden">
                            <div className="absolute top-0 right-0 w-24 h-24 bg-primary/5 rounded-full blur-2xl"></div>
                            
                            <div className="flex justify-between items-start gap-4">
                              <div>
                                <h5 className="font-bold text-white text-sm">{rec.title}</h5>
                                <span className={`inline-block text-[10px] font-extrabold uppercase px-2 py-0.5 mt-1.5 rounded border ${difficultyColor}`}>
                                  {rec.difficulty} • {rec.estimatedWeeks} Weeks
                                </span>
                              </div>
                              
                              <button
                                onClick={() => handleAddProjectFromRecommendation(rec)}
                                className="px-3 py-1.5 bg-primary/10 hover:bg-primary text-primary-light hover:text-white border border-primary/25 text-xs font-bold rounded-xl transition-all"
                              >
                                Add to Portfolio
                              </button>
                            </div>

                            <p className="text-xs text-gray-400 leading-relaxed">{rec.description}</p>
                            
                            {rec.techStack && rec.techStack.length > 0 && (
                              <div className="flex flex-wrap gap-1">
                                {rec.techStack.map((tech, i) => (
                                  <span key={i} className="text-[10px] font-semibold text-gray-300 bg-dark-100 border border-border/40 px-2 py-0.5 rounded-lg">
                                    {tech}
                                  </span>
                                ))}
                              </div>
                            )}

                            <div className="text-[10px] text-warning-light bg-warning/5 border border-warning/10 p-2 rounded-xl mt-2 leading-relaxed">
                              <strong>Why suggested:</strong> {rec.reasoning}
                            </div>
                          </div>
                        );
                      })
                    ) : (
                      <p className="text-gray-500 text-xs italic">All skills matched! No missing requirement gaps found.</p>
                    )}
                  </div>
                </div>

                {/* 2. Portfolio Tracker */}
                <div className="space-y-6">
                  <div className="border-b border-border/50 pb-2 flex justify-between items-center">
                    <div>
                      <h4 className="font-bold text-base text-white font-display">My Project Portfolio</h4>
                      <p className="text-xs text-gray-400 mt-0.5">Track your project progressions and updates.</p>
                    </div>
                    <button
                      onClick={() => setIsAddingCustomProject(!isAddingCustomProject)}
                      className="px-3 py-1.5 bg-secondary/15 hover:bg-secondary text-secondary-light hover:text-white border border-secondary/30 text-xs font-bold rounded-xl transition-all"
                    >
                      {isAddingCustomProject ? 'Cancel' : 'Add Custom'}
                    </button>
                  </div>

                  {/* Add Custom Project Form */}
                  {isAddingCustomProject && (
                    <form onSubmit={handleAddCustomProjectSubmit} className="bg-dark-900 border border-border/60 p-4 rounded-2xl space-y-3">
                      <div>
                        <label className="block text-[10px] text-gray-400 font-bold uppercase mb-1">Project Title</label>
                        <input
                          type="text"
                          value={customProjectForm.title}
                          onChange={(e) => setCustomProjectForm({ ...customProjectForm, title: e.target.value })}
                          placeholder="e.g. Serverless API Gate"
                          className="w-full px-3 py-2 bg-dark-300 border border-border rounded-xl text-xs text-white focus:outline-none focus:border-primary"
                          required
                        />
                      </div>
                      <div>
                        <label className="block text-[10px] text-gray-400 font-bold uppercase mb-1">Description</label>
                        <textarea
                          value={customProjectForm.description}
                          onChange={(e) => setCustomProjectForm({ ...customProjectForm, description: e.target.value })}
                          placeholder="Project specifications..."
                          className="w-full px-3 py-2 bg-dark-300 border border-border rounded-xl text-xs text-white focus:outline-none focus:border-primary h-20 resize-none"
                        ></textarea>
                      </div>
                      <div className="grid grid-cols-2 gap-3">
                        <div>
                          <label className="block text-[10px] text-gray-400 font-bold uppercase mb-1">Tech Stack (comma-separated)</label>
                          <input
                            type="text"
                            value={customProjectForm.techStack}
                            onChange={(e) => setCustomProjectForm({ ...customProjectForm, techStack: e.target.value })}
                            placeholder="React, Docker, Go"
                            className="w-full px-3 py-2 bg-dark-300 border border-border rounded-xl text-xs text-white focus:outline-none focus:border-primary"
                          />
                        </div>
                        <div>
                          <label className="block text-[10px] text-gray-400 font-bold uppercase mb-1">Initial Status</label>
                          <select
                            value={customProjectForm.status}
                            onChange={(e) => setCustomProjectForm({ ...customProjectForm, status: e.target.value })}
                            className="w-full px-3 py-2 bg-dark-300 border border-border rounded-xl text-xs text-white focus:outline-none focus:border-primary"
                          >
                            <option value="planned">Planned</option>
                            <option value="in-progress">In Progress</option>
                            <option value="completed">Completed</option>
                          </select>
                        </div>
                      </div>
                      <button
                        type="submit"
                        className="w-full py-2 bg-secondary hover:bg-secondary-hover text-white text-xs font-bold rounded-xl transition-all"
                      >
                        Add to Portfolio
                      </button>
                    </form>
                  )}

                  <div className="space-y-4">
                    {myProjects && myProjects.length > 0 ? (
                      myProjects.map(proj => {
                        let statusColor = 'text-gray-400 bg-dark-100 border-border';
                        if (proj.status === 'in-progress') statusColor = 'text-primary-light bg-primary/10 border-primary/20';
                        if (proj.status === 'completed') statusColor = 'text-success-light bg-success/15 border-success/20';

                        return (
                          <div key={proj.id} className="bg-dark-900 border border-border p-4 rounded-2xl space-y-3 relative group">
                            <div className="flex justify-between items-start gap-4">
                              <div>
                                <h5 className="font-bold text-white text-sm">{proj.title}</h5>
                                <span className={`inline-block text-[10px] font-bold uppercase px-2 py-0.5 mt-1 rounded border ${statusColor}`}>
                                  {proj.status}
                                </span>
                              </div>

                              <button
                                onClick={() => handleDeleteProject(proj.id)}
                                className="p-1 text-gray-500 hover:text-red-400 bg-dark-100 hover:bg-red-500/10 border border-border hover:border-red-500/20 rounded-xl transition-all"
                              >
                                <Trash2 size={14} />
                              </button>
                            </div>

                            <p className="text-xs text-gray-400 leading-relaxed">{proj.description}</p>

                            {proj.techStack && proj.techStack.length > 0 && (
                              <div className="flex flex-wrap gap-1">
                                {proj.techStack.map((tech, i) => (
                                  <span key={i} className="text-[10px] font-semibold text-gray-300 bg-dark-100 border border-border/40 px-2 py-0.5 rounded-lg">
                                    {tech}
                                  </span>
                                ))}
                              </div>
                            )}

                            {/* Status controls */}
                            <div className="flex items-center justify-between border-t border-border/50 pt-2.5 mt-2.5 text-xs text-gray-400 font-medium">
                              <span>Update Status:</span>
                              <div className="flex gap-1.5">
                                {['planned', 'in-progress', 'completed'].map(st => (
                                  <button
                                    key={st}
                                    onClick={() => handleUpdateProjectStatus(proj.id, st)}
                                    className={`px-2 py-1 text-[10px] font-bold rounded-lg border transition-all ${
                                      proj.status === st 
                                        ? 'bg-primary border-primary text-white'
                                        : 'bg-dark-100 border-border text-gray-400 hover:text-white'
                                    }`}
                                  >
                                    {st === 'in-progress' ? 'In Progress' : st.charAt(0).toUpperCase() + st.slice(1)}
                                  </button>
                                ))}
                              </div>
                            </div>
                          </div>
                        );
                      })
                    ) : (
                      <p className="text-gray-500 text-xs italic text-center py-8">Your portfolio is currently empty. Add projects manually or choose from recommendations.</p>
                    )}
                  </div>
                </div>

              </div>
            </div>
          )}

          {/* TAB 5: KANBAN APPLICATION TRACKER */}
          {activeTab === 'companies' && (
            <div className="space-y-8 animate-fadeIn">
              
              {/* Board Header & Upcoming Deadlines bar */}
              <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                
                <div className="lg:col-span-2 bg-dark-900 border border-border/60 p-5 rounded-2xl flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
                  <div>
                    <h3 className="font-bold text-base text-white font-display">Target Application Board</h3>
                    <p className="text-xs text-gray-400 mt-0.5">Drag and drop cards across columns to update application stages.</p>
                  </div>
                  <button
                    onClick={() => setIsAddingApplication(true)}
                    className="px-5 py-2.5 bg-secondary hover:bg-secondary-hover active:scale-[0.98] text-white text-xs font-bold rounded-xl transition-all shadow-glow-secondary flex items-center gap-1.5"
                  >
                    <Plus size={16} />
                    <span>Track New Application</span>
                  </button>
                </div>

                {/* Deadlines Quick-check Card */}
                <div className="bg-dark-900 border border-border/60 p-5 rounded-2xl space-y-3">
                  <h4 className="font-bold text-xs uppercase tracking-wider text-gray-400 flex items-center gap-1.5">
                    <CalendarClock className="text-secondary" size={14} />
                    <span>Impending Deadlines</span>
                  </h4>
                  <div className="space-y-2 max-h-[120px] overflow-y-auto scrollbar-none">
                    {upcomingDeadlines && upcomingDeadlines.length > 0 ? (
                      upcomingDeadlines.slice(0, 2).map(dl => (
                        <div key={dl.id} className="flex justify-between items-center bg-dark-950 p-2 rounded-xl text-xs border border-border/25">
                          <div>
                            <span className="font-bold text-white block">{dl.companyName}</span>
                            <span className="text-[10px] text-gray-400">{dl.role} • {dl.stage}</span>
                          </div>
                          <span className="text-[10px] px-2 py-0.5 bg-warning/15 text-warning font-semibold rounded-lg border border-warning/25 whitespace-nowrap">
                            {dl.daysRemaining === 0 ? 'Today' : `${dl.daysRemaining} days left`}
                          </span>
                        </div>
                      ))
                    ) : (
                      <p className="text-gray-500 text-[10px] italic py-2">No manual deadlines logged.</p>
                    )}
                  </div>
                </div>

              </div>

              {/* Kanban Columns Grid */}
              <div className="grid grid-cols-1 md:grid-cols-5 gap-4 overflow-x-auto pb-4">
                
                {['Applied', 'OA', 'Interview', 'Offer', 'Rejected'].map(stage => {
                  const stageCards = applicationsList.filter(app => app.stage.toLowerCase() === stage.toLowerCase());
                  
                  let headerBorder = 'border-primary/20 bg-primary/5 text-primary-light';
                  if (stage === 'OA') headerBorder = 'border-secondary/20 bg-secondary/5 text-secondary-light';
                  if (stage === 'Interview') headerBorder = 'border-primary/20 bg-primary/5 text-primary-light';
                  if (stage === 'Offer') headerBorder = 'border-success/20 bg-success/5 text-success-light';
                  if (stage === 'Rejected') headerBorder = 'border-border/30 bg-dark-100/10 text-gray-400';

                  return (
                    <div
                      key={stage}
                      onDragOver={(e) => e.preventDefault()}
                      onDrop={(e) => handleDrop(e, stage)}
                      className="bg-dark-950 border border-border/40 p-4 rounded-3xl min-h-[500px] flex flex-col space-y-4"
                    >
                      {/* Column Header */}
                      <div className={`flex justify-between items-center p-3 rounded-2xl border text-xs font-bold ${headerBorder}`}>
                        <span>{stage === 'OA' ? 'Online Assessment (OA)' : stage}</span>
                        <span className="px-2 py-0.5 bg-dark-900 border border-border/40 rounded-lg text-white font-extrabold">
                          {stageCards.length}
                        </span>
                      </div>

                      {/* Cards Container */}
                      <div className="flex-1 space-y-3 overflow-y-auto scrollbar-none">
                        {stageCards.length > 0 ? (
                          stageCards.map(app => (
                            <div
                              key={app.id}
                              draggable
                              onDragStart={(e) => handleDragStart(e, app.id)}
                              className="bg-dark-900 border border-border/60 hover:border-gray-500 p-4 rounded-2xl space-y-2 cursor-grab active:cursor-grabbing transition-all relative overflow-hidden group"
                            >
                              <div className="absolute top-0 right-0 w-16 h-16 bg-primary/5 rounded-full blur-xl"></div>
                              
                              <div className="space-y-0.5">
                                <h5 className="font-extrabold text-white text-sm tracking-tight leading-tight">{app.companyName}</h5>
                                <span className="text-[10px] text-primary-light font-bold block">{app.role}</span>
                              </div>

                              {app.packageBand && (
                                <span className="inline-block text-[9px] font-bold text-gray-300 bg-dark-100 border border-border/30 px-2 py-0.5 rounded-lg">
                                  💰 {app.packageBand}
                                </span>
                              )}

                              {app.reminderDate && (
                                <div className="flex items-center gap-1 text-[9px] text-warning-light bg-warning/5 border border-warning/10 px-2 py-1 rounded-lg">
                                  <CalendarClock size={10} />
                                  <span>Due: {app.reminderDate}</span>
                                </div>
                              )}

                              {/* Card Actions */}
                              <div className="flex justify-between items-center border-t border-border/40 pt-2.5 mt-3 text-xs">
                                <button
                                  onClick={() => viewApplicationHistory(app)}
                                  className="text-[9px] font-bold text-gray-400 hover:text-white flex items-center gap-0.5 transition-colors"
                                  title="View Transition History"
                                >
                                  <ChevronRight size={12} />
                                  <span>Logs</span>
                                </button>
                                
                                <div className="flex gap-1.5 opacity-60 group-hover:opacity-100 transition-opacity">
                                  <button
                                    onClick={() => setIsEditingApplication(app)}
                                    className="p-1 hover:text-primary rounded hover:bg-dark-100 transition-colors"
                                    title="Edit details"
                                  >
                                    <Edit3 size={11} />
                                  </button>
                                  <button
                                    onClick={() => handleDeleteApplication(app.id)}
                                    className="p-1 hover:text-red-400 rounded hover:bg-dark-100 transition-colors"
                                    title="Delete application"
                                  >
                                    <Trash2 size={11} />
                                  </button>
                                </div>
                              </div>
                            </div>
                          ))
                        ) : (
                          <div className="h-full border border-dashed border-border/20 rounded-2xl flex items-center justify-center py-16 text-center">
                            <span className="text-[10px] text-gray-600 italic px-2">Drag cards here</span>
                          </div>
                        )}
                      </div>
                    </div>
                  );
                })}

              </div>

              {/* Add Application Modal */}
              {isAddingApplication && (
                <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
                  <div className="absolute inset-0 bg-background/80 backdrop-blur-sm" onClick={() => setIsAddingApplication(false)}></div>
                  <div className="glass-panel w-full max-w-lg rounded-3xl p-6 md:p-8 z-10 shadow-glass space-y-6">
                    <div className="flex justify-between items-center border-b border-border/50 pb-3">
                      <h4 className="font-extrabold text-lg text-white font-display">Track Application</h4>
                      <button onClick={() => setIsAddingApplication(false)} className="text-gray-400 hover:text-white"><X size={18} /></button>
                    </div>

                    <form onSubmit={handleApplicationSubmit} className="space-y-4 text-xs">
                      <div className="grid grid-cols-2 gap-4">
                        <div>
                          <label className="block font-bold text-gray-400 uppercase tracking-wider mb-1.5">Company Name</label>
                          <input
                            type="text"
                            value={applicationForm.companyName}
                            onChange={(e) => setApplicationForm({...applicationForm, companyName: e.target.value})}
                            placeholder="e.g. Google"
                            className="w-full px-4 py-2.5 bg-dark-300 border border-border rounded-xl text-white focus:outline-none focus:border-primary"
                            required
                          />
                        </div>
                        <div>
                          <label className="block font-bold text-gray-400 uppercase tracking-wider mb-1.5">Role / Position</label>
                          <input
                            type="text"
                            value={applicationForm.role}
                            onChange={(e) => setApplicationForm({...applicationForm, role: e.target.value})}
                            placeholder="e.g. Graduate SWE"
                            className="w-full px-4 py-2.5 bg-dark-300 border border-border rounded-xl text-white focus:outline-none focus:border-primary"
                            required
                          />
                        </div>
                      </div>

                      <div className="grid grid-cols-2 gap-4">
                        <div>
                          <label className="block font-bold text-gray-400 uppercase tracking-wider mb-1.5">Applied Date</label>
                          <input
                            type="date"
                            value={applicationForm.appliedDate}
                            onChange={(e) => setApplicationForm({...applicationForm, appliedDate: e.target.value})}
                            className="w-full px-4 py-2.5 bg-dark-300 border border-border rounded-xl text-white focus:outline-none focus:border-primary"
                            required
                          />
                        </div>
                        <div>
                          <label className="block font-bold text-gray-400 uppercase tracking-wider mb-1.5">Compensation Package Band</label>
                          <input
                            type="text"
                            value={applicationForm.packageBand}
                            onChange={(e) => setApplicationForm({...applicationForm, packageBand: e.target.value})}
                            placeholder="e.g. 15-18 LPA"
                            className="w-full px-4 py-2.5 bg-dark-300 border border-border rounded-xl text-white focus:outline-none focus:border-primary"
                          />
                        </div>
                      </div>

                      <div className="grid grid-cols-2 gap-4">
                        <div>
                          <label className="block font-bold text-gray-400 uppercase tracking-wider mb-1.5">Job Posting URL</label>
                          <input
                            type="url"
                            value={applicationForm.jobUrl}
                            onChange={(e) => setApplicationForm({...applicationForm, jobUrl: e.target.value})}
                            placeholder="e.g. https://careers.google.com"
                            className="w-full px-4 py-2.5 bg-dark-300 border border-border rounded-xl text-white focus:outline-none focus:border-primary"
                          />
                        </div>
                        <div>
                          <label className="block font-bold text-gray-400 uppercase tracking-wider mb-1.5">Reminder Date (OA/Interview)</label>
                          <input
                            type="date"
                            value={applicationForm.reminderDate}
                            onChange={(e) => setApplicationForm({...applicationForm, reminderDate: e.target.value})}
                            className="w-full px-4 py-2.5 bg-dark-300 border border-border rounded-xl text-white focus:outline-none focus:border-primary"
                          />
                        </div>
                      </div>

                      <div>
                        <label className="block font-bold text-gray-400 uppercase tracking-wider mb-1.5">Notes</label>
                        <textarea
                          value={applicationForm.notes}
                          onChange={(e) => setApplicationForm({...applicationForm, notes: e.target.value})}
                          placeholder="Referral names, preparation topics, etc."
                          className="w-full px-4 py-2.5 bg-dark-300 border border-border rounded-xl text-white focus:outline-none focus:border-primary h-20 resize-none"
                        ></textarea>
                      </div>

                      <button
                        type="submit"
                        className="w-full py-3 bg-secondary hover:bg-secondary-hover text-white font-bold rounded-xl transition-all shadow-glow-secondary mt-2"
                      >
                        Add Application
                      </button>
                    </form>
                  </div>
                </div>
              )}

              {/* Edit Application Modal */}
              {isEditingApplication && (
                <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
                  <div className="absolute inset-0 bg-background/80 backdrop-blur-sm" onClick={() => setIsEditingApplication(null)}></div>
                  <div className="glass-panel w-full max-w-lg rounded-3xl p-6 md:p-8 z-10 shadow-glass space-y-6">
                    <div className="flex justify-between items-center border-b border-border/50 pb-3">
                      <h4 className="font-extrabold text-lg text-white font-display">Edit Application Details</h4>
                      <button onClick={() => setIsEditingApplication(null)} className="text-gray-400 hover:text-white"><X size={18} /></button>
                    </div>

                    <form onSubmit={handleEditApplicationSubmit} className="space-y-4 text-xs">
                      <div className="grid grid-cols-2 gap-4">
                        <div>
                          <label className="block font-bold text-gray-400 uppercase tracking-wider mb-1.5">Company Name</label>
                          <input
                            type="text"
                            value={isEditingApplication.companyName}
                            onChange={(e) => setIsEditingApplication({...isEditingApplication, companyName: e.target.value})}
                            className="w-full px-4 py-2.5 bg-dark-300 border border-border rounded-xl text-white focus:outline-none focus:border-primary"
                            required
                          />
                        </div>
                        <div>
                          <label className="block font-bold text-gray-400 uppercase tracking-wider mb-1.5">Role / Position</label>
                          <input
                            type="text"
                            value={isEditingApplication.role}
                            onChange={(e) => setIsEditingApplication({...isEditingApplication, role: e.target.value})}
                            className="w-full px-4 py-2.5 bg-dark-300 border border-border rounded-xl text-white focus:outline-none focus:border-primary"
                            required
                          />
                        </div>
                      </div>

                      <div className="grid grid-cols-2 gap-4">
                        <div>
                          <label className="block font-bold text-gray-400 uppercase tracking-wider mb-1.5">Applied Date</label>
                          <input
                            type="date"
                            value={isEditingApplication.appliedDate}
                            onChange={(e) => setIsEditingApplication({...isEditingApplication, appliedDate: e.target.value})}
                            className="w-full px-4 py-2.5 bg-dark-300 border border-border rounded-xl text-white focus:outline-none focus:border-primary"
                            required
                          />
                        </div>
                        <div>
                          <label className="block font-bold text-gray-400 uppercase tracking-wider mb-1.5">Compensation Package Band</label>
                          <input
                            type="text"
                            value={isEditingApplication.packageBand || ''}
                            onChange={(e) => setIsEditingApplication({...isEditingApplication, packageBand: e.target.value})}
                            className="w-full px-4 py-2.5 bg-dark-300 border border-border rounded-xl text-white focus:outline-none focus:border-primary"
                          />
                        </div>
                      </div>

                      <div className="grid grid-cols-2 gap-4">
                        <div>
                          <label className="block font-bold text-gray-400 uppercase tracking-wider mb-1.5">Job Posting URL</label>
                          <input
                            type="url"
                            value={isEditingApplication.jobUrl || ''}
                            onChange={(e) => setIsEditingApplication({...isEditingApplication, jobUrl: e.target.value})}
                            className="w-full px-4 py-2.5 bg-dark-300 border border-border rounded-xl text-white focus:outline-none focus:border-primary"
                          />
                        </div>
                        <div>
                          <label className="block font-bold text-gray-400 uppercase tracking-wider mb-1.5">Reminder Date (OA/Interview)</label>
                          <input
                            type="date"
                            value={isEditingApplication.reminderDate || ''}
                            onChange={(e) => setIsEditingApplication({...isEditingApplication, reminderDate: e.target.value})}
                            className="w-full px-4 py-2.5 bg-dark-300 border border-border rounded-xl text-white focus:outline-none focus:border-primary"
                          />
                        </div>
                      </div>

                      <div>
                        <label className="block font-bold text-gray-400 uppercase tracking-wider mb-1.5">Notes</label>
                        <textarea
                          value={isEditingApplication.notes || ''}
                          onChange={(e) => setIsEditingApplication({...isEditingApplication, notes: e.target.value})}
                          className="w-full px-4 py-2.5 bg-dark-300 border border-border rounded-xl text-white focus:outline-none focus:border-primary h-20 resize-none"
                        ></textarea>
                      </div>

                      <button
                        type="submit"
                        className="w-full py-3 bg-secondary hover:bg-secondary-hover text-white font-bold rounded-xl transition-all shadow-glow-secondary mt-2"
                      >
                        Save Changes
                      </button>
                    </form>
                  </div>
                </div>
              )}

              {/* View Timeline History Modal */}
              {isViewingHistory && (
                <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
                  <div className="absolute inset-0 bg-background/80 backdrop-blur-sm" onClick={() => setIsViewingHistory(null)}></div>
                  <div className="glass-panel w-full max-w-md rounded-3xl p-6 z-10 shadow-glass space-y-6">
                    <div className="flex justify-between items-center border-b border-border/50 pb-3">
                      <div>
                        <h4 className="font-extrabold text-base text-white font-display">{isViewingHistory.companyName}</h4>
                        <span className="text-[10px] text-primary-light font-bold block">{isViewingHistory.role}</span>
                      </div>
                      <button onClick={() => setIsViewingHistory(null)} className="text-gray-400 hover:text-white"><X size={18} /></button>
                    </div>

                    <div className="space-y-4 max-h-[300px] overflow-y-auto scrollbar-none pr-1">
                      <span className="text-[10px] text-gray-400 font-bold block uppercase tracking-wider">Application stage transition logs</span>
                      
                      <div className="relative border-l border-border/50 pl-4 space-y-4 ml-2">
                        {historyList && historyList.length > 0 ? (
                          historyList.map((hist, idx) => (
                            <div key={idx} className="relative">
                              {/* Dot */}
                              <div className="absolute -left-[21px] mt-1.5 w-2.5 h-2.5 rounded-full bg-primary border-2 border-dark-900"></div>
                              <div>
                                <span className="font-bold text-xs text-white block">Moved to: {hist.stage === 'OA' ? 'Online Assessment' : hist.stage}</span>
                                <span className="text-[10px] text-gray-500">{new Date(hist.changedAt).toLocaleString()}</span>
                              </div>
                            </div>
                          ))
                        ) : (
                          <p className="text-gray-500 text-xs italic">No logs tracked.</p>
                        )}
                      </div>
                    </div>
                  </div>
                </div>
              )}

            </div>
          )}

          {/* TAB 6: DSA PRACTICE */}
          {activeTab === 'dsa' && (
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8 animate-fadeIn">
              
              {/* Daily Tasks Board */}
              <div className="lg:col-span-2 glass-panel rounded-3xl p-6 space-y-6">
                <div className="border-b border-border/50 pb-2">
                  <h3 className="font-bold text-lg text-white font-display flex items-center gap-2">
                    <CheckSquare className="text-primary" size={20} />
                    <span>Today's Daily Checklist</span>
                  </h3>
                  <p className="text-xs text-gray-400 mt-1">
                    Adaptive problem recommendation targeting your weakest topics. Solve them directly on LeetCode.
                  </p>
                </div>

                <div className="space-y-4">
                  {dsaTasks && dsaTasks.length > 0 ? (
                    dsaTasks.map(task => {
                      const isSolved = task.status === 'solved';
                      let diffColor = 'bg-primary/10 text-primary-light border-primary/20';
                      if (task.difficulty === 'EASY') diffColor = 'bg-success/10 text-success-light border-success/20';
                      if (task.difficulty === 'HARD') diffColor = 'bg-secondary/10 text-secondary-light border-secondary/20';

                      return (
                        <div 
                          key={task.problemId} 
                          className={`bg-dark-900 border transition-all duration-200 p-4 rounded-2xl flex items-center justify-between shadow ${
                            isSolved ? 'border-success/30 opacity-70 bg-success/5' : 'border-border hover:border-gray-500'
                          }`}
                        >
                          <div className="flex items-center gap-4">
                            <button
                              onClick={() => toggleDsaTask(task.problemId, task.status)}
                              className={`w-6 h-6 rounded-lg border flex items-center justify-center transition-all ${
                                isSolved 
                                  ? 'bg-success border-success text-white' 
                                  : 'border-border hover:border-primary bg-dark-100'
                              }`}
                            >
                              {isSolved && <CheckCircle size={14} />}
                            </button>
                            
                            <div>
                              <div className="flex items-center gap-2 flex-wrap">
                                <span className={`font-bold text-sm ${isSolved ? 'text-gray-400 line-through' : 'text-white'}`}>
                                  {task.title}
                                </span>
                                <span className={`text-[10px] uppercase font-extrabold px-2 py-0.5 rounded border ${diffColor}`}>
                                  {task.difficulty}
                                </span>
                                <span className="text-[10px] text-gray-500 font-semibold bg-dark-300 px-2 py-0.5 rounded border border-border/40">
                                  {task.topic}
                                </span>
                              </div>
                            </div>
                          </div>

                          <a
                            href={task.leetcodeUrl}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="flex items-center gap-1.5 px-3 py-1.5 bg-dark-100 hover:bg-dark-200 border border-border rounded-xl text-xs text-gray-300 font-semibold transition-all"
                          >
                            <span>LeetCode</span>
                            <Eye size={12} />
                          </a>
                        </div>
                      );
                    })
                  ) : (
                    <div className="text-center py-16 text-gray-500 text-sm">
                      Generating today's custom tasks... Please wait or refresh.
                    </div>
                  )}
                </div>
              </div>

              {/* Progress & Streaks Board */}
              <div className="space-y-8">
                
                {/* Streak widget */}
                <div className="glass-panel rounded-3xl p-6 relative overflow-hidden flex flex-col justify-between min-h-[160px] bg-gradient-to-br from-card to-dark-900 border-border">
                  <div className="absolute top-0 right-0 w-32 h-32 bg-secondary/5 rounded-full blur-2xl"></div>
                  <div className="flex justify-between items-start z-10">
                    <div>
                      <h4 className="font-bold text-xs uppercase tracking-wider text-gray-400">DSA Streak Dashboard</h4>
                      <span className="text-4xl font-extrabold text-white mt-2 block font-display">
                        {dsaProgress?.currentStreak ?? 0} <span className="text-sm font-semibold text-secondary-light">days active</span>
                      </span>
                    </div>
                    <div className="w-12 h-12 bg-secondary/10 border border-secondary/20 rounded-2xl flex items-center justify-center text-secondary animate-pulse">
                      {/* fire/flame representation */}
                      <span className="text-2xl font-bold">🔥</span>
                    </div>
                  </div>

                  <div className="border-t border-border/50 pt-3 mt-4 text-xs text-gray-400 flex justify-between z-10 font-medium">
                    <span>Longest Streak Milestone:</span>
                    <span className="text-white font-extrabold">{dsaProgress?.longestStreak ?? 0} days</span>
                  </div>
                </div>

                {/* Solved Statistics */}
                <div className="glass-panel rounded-3xl p-6 space-y-6">
                  <h3 className="font-bold text-base text-white font-display border-b border-border/50 pb-2">
                    Solve Summary
                  </h3>

                  <div className="grid grid-cols-3 gap-4 text-center">
                    <div className="bg-dark-900 border border-border p-3 rounded-2xl">
                      <span className="text-xs text-success font-semibold block">Easy</span>
                      <span className="text-lg font-bold text-white block mt-1">
                        {dsaProgress?.byDifficulty?.easy ?? 0}
                      </span>
                    </div>
                    <div className="bg-dark-900 border border-border p-3 rounded-2xl">
                      <span className="text-xs text-primary-light font-semibold block">Medium</span>
                      <span className="text-lg font-bold text-white block mt-1">
                        {dsaProgress?.byDifficulty?.medium ?? 0}
                      </span>
                    </div>
                    <div className="bg-dark-900 border border-border p-3 rounded-2xl">
                      <span className="text-xs text-secondary-light font-semibold block">Hard</span>
                      <span className="text-lg font-bold text-white block mt-1">
                        {dsaProgress?.byDifficulty?.hard ?? 0}
                      </span>
                    </div>
                  </div>

                  {/* Heatmap calendar log (last 28 days) */}
                  <div className="space-y-3 pt-2">
                    <span className="text-[10px] text-gray-400 font-bold block uppercase tracking-wider">Activity Tracker (Past 4 Weeks)</span>
                    <div className="grid grid-cols-7 gap-1.5 p-3 bg-dark-900 border border-border/40 rounded-2xl">
                      {Array.from({ length: 28 }).map((_, i) => {
                        const date = new Date();
                        date.setDate(date.getDate() - (27 - i));
                        const dateStr = date.toISOString().split('T')[0];
                        
                        // Check if exists in history
                        const dayRecord = dsaProgress?.history?.find(h => h.date === dateStr);
                        const solveCount = dayRecord ? dayRecord.solvedCount : 0;
                        
                        let colorClass = 'bg-dark-100 border border-border/20'; // no solves
                        if (solveCount === 1) colorClass = 'bg-success/20 border border-success/30';
                        if (solveCount === 2) colorClass = 'bg-success/50 border border-success/60';
                        if (solveCount >= 3) colorClass = 'bg-success border border-success text-white';

                        return (
                          <div 
                            key={i} 
                            title={`${date.toLocaleDateString()}: ${solveCount} solved`}
                            className={`aspect-square rounded-md transition-all duration-200 cursor-pointer ${colorClass}`}
                          ></div>
                        );
                      })}
                    </div>
                  </div>
                </div>

              </div>

            </div>
          )}

          {/* TAB 7: AI MOCK INTERVIEW */}
          {activeTab === 'interview' && (
            <div className="space-y-8 animate-fadeIn">
              
              {/* Active Session Chat Board */}
              {activeSession ? (
                <div className="glass-panel rounded-3xl p-6 md:p-8 space-y-6">
                  
                  {/* Active Header */}
                  <div className="flex justify-between items-center border-b border-border/50 pb-4">
                    <div>
                      <h3 className="font-extrabold text-lg text-white font-display flex items-center gap-2">
                        <span className="relative flex h-3 w-3">
                          <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-secondary opacity-75"></span>
                          <span className="relative inline-flex rounded-full h-3 w-3 bg-secondary"></span>
                        </span>
                        <span>Interview in Progress</span>
                      </h3>
                      <p className="text-xs text-gray-400 mt-1">
                        Please answer the question clearly. The AI will evaluate your reply and generate the next prompt.
                      </p>
                    </div>

                    <span className="text-xs text-secondary-light font-extrabold bg-secondary/10 px-3 py-1.5 rounded-xl border border-secondary/25">
                      Question {activeSession.questionNumber} of {activeSession.totalQuestions}
                    </span>
                  </div>

                  {/* Question Prompt Card */}
                  <div className="bg-dark-900 border border-border/60 p-6 rounded-2xl space-y-3 relative overflow-hidden">
                    <div className="absolute top-0 right-0 w-32 h-32 bg-primary/5 rounded-full blur-2xl"></div>
                    <span className="text-[10px] text-primary-light font-bold block uppercase tracking-wider">Interviewer Question</span>
                    <p className="text-sm md:text-base text-white leading-relaxed font-medium">
                      {activeSession.currentQuestion.text}
                    </p>
                  </div>

                  {/* Submit Answer Form */}
                  <form onSubmit={handleSubmitAnswer} className="space-y-4">
                    <div>
                      <label className="block text-xs font-semibold text-gray-400 uppercase tracking-wider mb-2">Your Answer</label>
                      <textarea
                        value={answerText}
                        onChange={(e) => setAnswerText(e.target.value)}
                        placeholder="Type your response here... (Be detailed for a higher evaluation score)"
                        className="w-full px-4 py-3 bg-dark-300 border border-border rounded-xl text-sm focus:outline-none focus:border-primary text-white h-40 resize-none leading-relaxed"
                        required
                        disabled={isSubmittingAnswer}
                      ></textarea>
                    </div>

                    <div className="flex justify-between items-center gap-4">
                      <button
                        type="button"
                        onClick={() => {
                          if (confirm("Are you sure you want to end this interview session? Your progress will be saved.")) {
                            setActiveSession(null);
                            reloadInterviews();
                          }
                        }}
                        className="px-5 py-2.5 bg-dark-100 hover:bg-dark-200 border border-border text-gray-300 text-xs font-bold rounded-xl transition-all"
                      >
                        Abandon Session
                      </button>

                      <button
                        type="submit"
                        disabled={isSubmittingAnswer || !answerText.trim()}
                        className="px-6 py-2.5 bg-secondary hover:bg-secondary-hover active:scale-[0.98] text-white text-xs font-bold rounded-xl transition-all shadow-glow-secondary disabled:opacity-50 disabled:pointer-events-none flex items-center gap-1.5"
                      >
                        {isSubmittingAnswer ? (
                          <>
                            <div className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin"></div>
                            <span>Evaluating answer...</span>
                          </>
                        ) : (
                          <>
                            <CheckCircle size={14} />
                            <span>Submit Answer</span>
                          </>
                        )}
                      </button>
                    </div>
                  </form>

                  {/* Live Feedback of Previous Question */}
                  {activeFeedback && (
                    <div className="border-t border-border/50 pt-6 mt-6 space-y-4 animate-fadeIn">
                      <div className="flex justify-between items-center">
                        <h4 className="font-extrabold text-sm text-white font-display">Previous Answer Assessment</h4>
                        <span className="text-xs font-bold bg-success/15 border border-success/30 text-success-light px-3 py-1 rounded-xl">
                          Score: {activeFeedback.score}/10
                        </span>
                      </div>

                      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <div className="bg-success/5 border border-success/20 p-4 rounded-xl space-y-2">
                          <span className="text-[10px] text-success-light font-bold block uppercase tracking-wider">Strengths</span>
                          <ul className="list-disc pl-4 space-y-1 text-xs text-gray-300 leading-relaxed">
                            {activeFeedback.strengths?.map((str, i) => <li key={i}>{str}</li>)}
                          </ul>
                        </div>
                        <div className="bg-warning/5 border border-warning/20 p-4 rounded-xl space-y-2">
                          <span className="text-[10px] text-warning-light font-bold block uppercase tracking-wider">Areas for Improvement</span>
                          <ul className="list-disc pl-4 space-y-1 text-xs text-gray-300 leading-relaxed">
                            {activeFeedback.improvements?.map((imp, i) => <li key={i}>{imp}</li>)}
                          </ul>
                        </div>
                      </div>
                    </div>
                  )}

                </div>
              ) : (
                
                /* Start Config Panel */
                <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                  
                  {/* Configure Panel */}
                  <div className="glass-panel rounded-3xl p-6 space-y-6">
                    <h3 className="font-bold text-lg text-white font-display border-b border-border/50 pb-2 flex items-center gap-2">
                      <Sparkles className="text-primary" size={20} />
                      <span>Start Mock Session</span>
                    </h3>

                    <form onSubmit={handleStartInterview} className="space-y-4 text-xs">
                      <div>
                        <label className="block font-bold text-gray-400 uppercase tracking-wider mb-2">Select Target Company/Role</label>
                        <select
                          value={interviewConfig.targetCompanyId}
                          onChange={(e) => setInterviewConfig({...interviewConfig, targetCompanyId: e.target.value})}
                          className="w-full px-4 py-2.5 bg-dark-300 border border-border rounded-xl text-white focus:outline-none focus:border-primary"
                        >
                          <option value="">-- General Interview (No Target) --</option>
                          {applicationsList.map(app => (
                            <option key={app.id} value={app.id}>{app.companyName} - {app.role}</option>
                          ))}
                        </select>
                      </div>

                      <div>
                        <label className="block font-bold text-gray-400 uppercase tracking-wider mb-2">Interview Track</label>
                        <div className="grid grid-cols-3 gap-2">
                          {['technical', 'behavioral', 'hr'].map(track => (
                            <button
                              key={track}
                              type="button"
                              onClick={() => setInterviewConfig({...interviewConfig, interviewType: track})}
                              className={`py-2 px-3 rounded-xl border text-center font-bold capitalize transition-all ${
                                interviewConfig.interviewType === track
                                  ? 'bg-primary border-primary text-white shadow-glow-primary'
                                  : 'bg-dark-300 border-border text-gray-400 hover:text-white'
                              }`}
                            >
                              {track}
                            </button>
                          ))}
                        </div>
                      </div>

                      <button
                        type="submit"
                        disabled={isStartingInterview}
                        className="w-full py-3 bg-secondary hover:bg-secondary-hover active:scale-[0.98] text-white font-bold rounded-xl transition-all shadow-glow-secondary mt-4 flex items-center justify-center gap-2"
                      >
                        {isStartingInterview ? (
                          <>
                            <div className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin"></div>
                            <span>Generating Question...</span>
                          </>
                        ) : (
                          <>
                            <Sparkles size={16} />
                            <span>Initiate Mock Interview</span>
                          </>
                        )}
                      </button>
                    </form>
                  </div>

                  {/* History of mock lists */}
                  <div className="lg:col-span-2 glass-panel rounded-3xl p-6 space-y-6">
                    <h3 className="font-bold text-lg text-white font-display border-b border-border/50 pb-2 flex items-center gap-2">
                      <Award className="text-secondary" size={20} />
                      <span>Past Performance Log</span>
                    </h3>

                    <div className="space-y-4 max-h-[50vh] overflow-y-auto pr-2 scrollbar-none">
                      {interviewSessions && interviewSessions.length > 0 ? (
                        interviewSessions.map(sess => (
                          <div 
                            key={sess.sessionId}
                            className="bg-dark-900 border border-border p-4 rounded-2xl flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 relative overflow-hidden group"
                          >
                            <div className="absolute top-0 right-0 w-24 h-24 bg-primary/5 rounded-full blur-2xl"></div>
                            
                            <div className="space-y-1 z-10 text-xs">
                              <div className="flex items-center gap-2 flex-wrap">
                                <span className="font-extrabold text-white text-sm block capitalize">
                                  {sess.interviewType} Interview
                                </span>
                                <span className="text-[9px] uppercase font-extrabold px-2 py-0.5 rounded border border-primary/20 bg-primary/10 text-primary-light">
                                  {sess.companyName}
                                </span>
                              </div>
                              <span className="text-gray-400 block">{sess.role}</span>
                              <span className="text-[10px] text-gray-500 block">
                                {sess.completedAt ? `Completed: ${new Date(sess.completedAt).toLocaleString()}` : 'Incomplete/Abandoned'}
                              </span>
                            </div>

                            <div className="flex items-center gap-4 z-10 w-full sm:w-auto justify-between sm:justify-end">
                              {sess.overallScore != null && (
                                <div className="text-right">
                                  <span className="text-[10px] text-gray-400 uppercase tracking-wider block font-bold">Overall Rating</span>
                                  <span className="text-base font-black text-success-light">{sess.overallScore.toFixed(1)}/10</span>
                                </div>
                              )}
                              
                              <div className="flex gap-2">
                                <button
                                  onClick={() => handleLoadTranscript(sess.sessionId)}
                                  className="px-3 py-1.5 bg-primary/10 hover:bg-primary border border-primary/25 hover:border-primary text-primary-light hover:text-white text-xs font-bold rounded-xl transition-all"
                                >
                                  Transcript
                                </button>
                                <button
                                  onClick={() => handleDeleteInterviewSession(sess.sessionId)}
                                  className="p-1.5 text-gray-500 hover:text-red-400 bg-dark-100 hover:bg-red-500/10 border border-border hover:border-red-500/20 rounded-xl transition-all"
                                >
                                  <Trash2 size={13} />
                                </button>
                              </div>
                            </div>
                          </div>
                        ))
                      ) : (
                        <div className="text-center py-16 text-gray-500 text-sm">
                          No mock interview trials logged yet. Choose a company target and start!
                        </div>
                      )}
                    </div>
                  </div>

                </div>
              )}

              {/* Transcript details view modal */}
              {selectedTranscript && (
                <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
                  <div className="absolute inset-0 bg-background/80 backdrop-blur-sm" onClick={() => setSelectedTranscript(null)}></div>
                  <div className="glass-panel w-full max-w-2xl rounded-3xl p-6 md:p-8 z-10 shadow-glass space-y-6">
                    
                    <div className="flex justify-between items-start border-b border-border/50 pb-4">
                      <div>
                        <h4 className="font-extrabold text-base text-white font-display capitalize">
                          {selectedTranscript.interviewType} Interview Transcript
                        </h4>
                        <span className="text-xs text-primary-light font-bold block mt-0.5">
                          {selectedTranscript.companyName} • {selectedTranscript.role}
                        </span>
                      </div>
                      
                      <div className="flex items-center gap-4">
                        {selectedTranscript.overallScore != null && (
                          <div className="text-right">
                            <span className="text-[10px] text-gray-400 uppercase tracking-wider block font-bold">Session Score</span>
                            <span className="text-base font-black text-success-light">{selectedTranscript.overallScore.toFixed(1)}/10</span>
                          </div>
                        )}
                        <button onClick={() => setSelectedTranscript(null)} className="text-gray-400 hover:text-white"><X size={18} /></button>
                      </div>
                    </div>

                    <div className="space-y-6 max-h-[55vh] overflow-y-auto pr-2 text-xs scrollbar-none">
                      {selectedTranscript.questions?.map((q, idx) => (
                        <div key={idx} className="bg-dark-900 border border-border/60 p-5 rounded-2xl space-y-4">
                          <div className="flex justify-between items-center border-b border-border/30 pb-2">
                            <span className="font-bold text-white uppercase text-[10px] tracking-wider text-primary-light">Question {idx + 1}</span>
                            {q.feedback && (
                              <span className="font-extrabold text-success-light bg-success/15 border border-success/25 px-2 py-0.5 rounded-lg">
                                Rating: {q.feedback.score}/10
                              </span>
                            )}
                          </div>

                          <div className="space-y-1">
                            <span className="text-[10px] text-gray-500 font-bold block">Interviewer Prompt</span>
                            <p className="text-white text-xs font-semibold leading-relaxed">{q.questionText}</p>
                          </div>

                          {q.answerText && (
                            <div className="space-y-1 bg-dark-950 p-3 rounded-xl border border-border/25">
                              <span className="text-[10px] text-gray-500 font-bold block">Your Answer</span>
                              <p className="text-gray-300 leading-relaxed italic">{q.answerText}</p>
                            </div>
                          )}

                          {q.feedback && (
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-3 pt-2">
                              <div className="space-y-1 text-success-light">
                                <span className="text-[9px] uppercase font-extrabold block">Strengths</span>
                                <ul className="list-disc pl-4 space-y-0.5 text-gray-400 leading-relaxed">
                                  {q.feedback.strengths?.map((str, i) => <li key={i}>{str}</li>)}
                                </ul>
                              </div>
                              <div className="space-y-1 text-warning-light">
                                <span className="text-[9px] uppercase font-extrabold block">Improvements</span>
                                <ul className="list-disc pl-4 space-y-0.5 text-gray-400 leading-relaxed">
                                  {q.feedback.improvements?.map((imp, i) => <li key={i}>{imp}</li>)}
                                </ul>
                              </div>
                            </div>
                          )}
                        </div>
                      ))}
                    </div>
                  </div>
                </div>
              )}

            </div>
          )}

        </div>

      </main>

      {/* Edit Profile Modal */}
      {isEditingProfile && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div className="absolute inset-0 bg-background/80 backdrop-blur-sm" onClick={() => setIsEditingProfile(false)}></div>
          
          <div className="glass-panel w-full max-w-lg rounded-3xl p-6 md:p-8 z-10 shadow-glass">
            <div className="flex items-center justify-between border-b border-border/50 pb-4 mb-6">
              <h3 className="font-extrabold text-xl text-white font-display flex items-center gap-2">
                <Edit3 className="text-primary" size={22} />
                <span>Edit Profile Details</span>
              </h3>
              <button 
                onClick={() => setIsEditingProfile(false)}
                className="p-1.5 bg-dark-100 hover:bg-dark-200 border border-border hover:border-gray-500 rounded-lg text-gray-400 hover:text-white transition-all"
              >
                <X size={18} />
              </button>
            </div>

            <form onSubmit={handleProfileSave} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-gray-400 uppercase tracking-wider mb-2">Student Name</label>
                <input
                  type="text"
                  value={profileForm.name}
                  onChange={(e) => setProfileForm({ ...profileForm, name: e.target.value })}
                  className="w-full px-4 py-2.5 bg-dark-300 border border-border rounded-xl text-white focus:outline-none focus:border-primary transition-all"
                  required
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-400 uppercase tracking-wider mb-2">College Name</label>
                <input
                  type="text"
                  value={profileForm.college}
                  onChange={(e) => setProfileForm({ ...profileForm, college: e.target.value })}
                  className="w-full px-4 py-2.5 bg-dark-300 border border-border rounded-xl text-white focus:outline-none focus:border-primary transition-all"
                  required
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-gray-400 uppercase tracking-wider mb-2">Branch / Major</label>
                  <input
                    type="text"
                    value={profileForm.branch}
                    onChange={(e) => setProfileForm({ ...profileForm, branch: e.target.value })}
                    className="w-full px-4 py-2.5 bg-dark-300 border border-border rounded-xl text-white focus:outline-none focus:border-primary transition-all"
                    required
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-gray-400 uppercase tracking-wider mb-2">Graduation Batch (Year)</label>
                  <input
                    type="text"
                    value={profileForm.batch}
                    onChange={(e) => setProfileForm({ ...profileForm, batch: e.target.value })}
                    className="w-full px-4 py-2.5 bg-dark-300 border border-border rounded-xl text-white focus:outline-none focus:border-primary transition-all"
                    required
                  />
                </div>
              </div>

              <div className="flex items-center justify-end gap-3 border-t border-border/50 pt-4 mt-6">
                <button
                  type="button"
                  onClick={() => setIsEditingProfile(false)}
                  className="px-4 py-2 bg-dark-100 hover:bg-dark-200 border border-border text-gray-300 text-sm font-semibold rounded-xl transition-all"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="flex items-center gap-2 px-5 py-2.5 bg-primary hover:bg-primary-hover text-white text-sm font-bold rounded-xl transition-all shadow-glow-primary"
                >
                  <Save size={16} />
                  <span>Save Changes</span>
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default Dashboard;

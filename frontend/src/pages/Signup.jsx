import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { User, Lock, Mail, GraduationCap, Calendar, BookOpen, AlertCircle, ArrowRight, Loader2, CheckCircle } from 'lucide-react';

const Signup = () => {
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    password: '',
    college: '',
    branch: '',
    batch: ''
  });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const { signup } = useAuth();
  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const { name, email, password, college, branch, batch } = formData;

    if (!name || !email || !password || !college || !branch || !batch) {
      setError('Please fill in all fields');
      return;
    }

    if (password.length < 6) {
      setError('Password must be at least 6 characters long');
      return;
    }

    setError('');
    setIsSubmitting(true);

    const result = await signup(formData);
    if (result.success) {
      setSuccess(true);
      setTimeout(() => {
        navigate('/login');
      }, 2000);
    } else {
      setError(result.error);
      setIsSubmitting(false);
    }
  };

  return (
    <div className="relative min-h-screen flex items-center justify-center bg-background overflow-hidden px-4 py-12">
      {/* Decorative Blur Blobs */}
      <div className="absolute top-1/4 left-1/4 w-96 h-96 bg-primary/20 rounded-full blur-3xl animate-blob"></div>
      <div className="absolute bottom-1/4 right-1/4 w-96 h-96 bg-secondary/15 rounded-full blur-3xl animate-blob animation-delay-2000"></div>

      <div className="w-full max-w-lg z-10">
        {/* Brand Logo */}
        <div className="text-center mb-6">
          <div className="inline-flex items-center justify-center w-12 h-12 rounded-2xl bg-gradient-accent p-0.5 shadow-glow-primary mb-3">
            <div className="w-full h-full bg-dark-900 rounded-[14px] flex items-center justify-center">
              <span className="text-xl font-bold bg-gradient-accent bg-clip-text text-transparent">PM</span>
            </div>
          </div>
          <h1 className="text-2xl font-extrabold tracking-tight font-display bg-gradient-accent bg-clip-text text-transparent">
            Create Student Account
          </h1>
          <p className="text-gray-400 mt-1 text-sm">Join the AI Placement Command Center</p>
        </div>

        {/* Signup Card */}
        <div className="glass-panel rounded-3xl p-8 shadow-glass transition-all duration-300">
          {success ? (
            <div className="text-center py-8 space-y-4">
              <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-success/15 text-success mb-2">
                <CheckCircle size={36} />
              </div>
              <h2 className="text-xl font-bold text-white">Registration Successful!</h2>
              <p className="text-gray-400 text-sm">Redirecting you to the sign-in page...</p>
            </div>
          ) : (
            <>
              {error && (
                <div className="flex items-center gap-2 bg-red-500/10 border border-red-500/20 text-red-400 p-3 rounded-xl text-sm mb-6 animate-pulse">
                  <AlertCircle size={18} />
                  <span>{error}</span>
                </div>
              )}

              <form onSubmit={handleSubmit} className="space-y-4">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-xs font-semibold text-gray-400 uppercase tracking-wider mb-2">Full Name</label>
                    <div className="relative">
                      <User className="absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-500" size={18} />
                      <input
                        type="text"
                        name="name"
                        value={formData.name}
                        onChange={handleChange}
                        placeholder="John Doe"
                        className="w-full pl-11 pr-4 py-2.5 bg-dark-300/80 border border-border rounded-xl text-white placeholder-gray-500 focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary transition-all duration-200"
                        required
                      />
                    </div>
                  </div>

                  <div>
                    <label className="block text-xs font-semibold text-gray-400 uppercase tracking-wider mb-2">Email Address</label>
                    <div className="relative">
                      <Mail className="absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-500" size={18} />
                      <input
                        type="email"
                        name="email"
                        value={formData.email}
                        onChange={handleChange}
                        placeholder="john@college.edu"
                        className="w-full pl-11 pr-4 py-2.5 bg-dark-300/80 border border-border rounded-xl text-white placeholder-gray-500 focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary transition-all duration-200"
                        required
                      />
                    </div>
                  </div>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-gray-400 uppercase tracking-wider mb-2">Password</label>
                  <div className="relative">
                    <Lock className="absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-500" size={18} />
                    <input
                      type="password"
                      name="password"
                      value={formData.password}
                      onChange={handleChange}
                      placeholder="•••••••• (min 6 characters)"
                      className="w-full pl-11 pr-4 py-2.5 bg-dark-300/80 border border-border rounded-xl text-white placeholder-gray-500 focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary transition-all duration-200"
                      required
                    />
                  </div>
                </div>

                <div className="border-t border-border/50 my-4 pt-4">
                  <h3 className="text-xs font-bold text-primary uppercase tracking-wider mb-3">Academic Information</h3>
                  
                  <div className="space-y-4">
                    <div>
                      <label className="block text-xs font-semibold text-gray-400 uppercase tracking-wider mb-2">College Name</label>
                      <div className="relative">
                        <GraduationCap className="absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-500" size={18} />
                        <input
                          type="text"
                          name="college"
                          value={formData.college}
                          onChange={handleChange}
                          placeholder="State Institute of Technology"
                          className="w-full pl-11 pr-4 py-2.5 bg-dark-300/80 border border-border rounded-xl text-white placeholder-gray-500 focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary transition-all duration-200"
                          required
                        />
                      </div>
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      <div>
                        <label className="block text-xs font-semibold text-gray-400 uppercase tracking-wider mb-2">Branch / Major</label>
                        <div className="relative">
                          <BookOpen className="absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-500" size={18} />
                          <input
                            type="text"
                            name="branch"
                            value={formData.branch}
                            onChange={handleChange}
                            placeholder="Computer Science"
                            className="w-full pl-11 pr-4 py-2.5 bg-dark-300/80 border border-border rounded-xl text-white placeholder-gray-500 focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary transition-all duration-200"
                            required
                          />
                        </div>
                      </div>

                      <div>
                        <label className="block text-xs font-semibold text-gray-400 uppercase tracking-wider mb-2">Graduation Batch (Year)</label>
                        <div className="relative">
                          <Calendar className="absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-500" size={18} />
                          <input
                            type="text"
                            name="batch"
                            value={formData.batch}
                            onChange={handleChange}
                            placeholder="2026"
                            className="w-full pl-11 pr-4 py-2.5 bg-dark-300/80 border border-border rounded-xl text-white placeholder-gray-500 focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary transition-all duration-200"
                            required
                          />
                        </div>
                      </div>
                    </div>
                  </div>
                </div>

                <button
                  type="submit"
                  disabled={isSubmitting}
                  className="w-full flex items-center justify-center gap-2 mt-2 py-3 bg-gradient-accent hover:opacity-90 active:scale-[0.98] text-white font-semibold rounded-xl transition-all duration-200 shadow-glow-primary disabled:opacity-50 disabled:pointer-events-none"
                >
                  {isSubmitting ? (
                    <>
                      <Loader2 className="animate-spin" size={18} />
                      <span>Creating Account...</span>
                    </>
                  ) : (
                    <>
                      <span>Sign Up</span>
                      <ArrowRight size={18} />
                    </>
                  )}
                </button>
              </form>

              <div className="text-center mt-6 text-sm text-gray-400">
                Already have an account?{' '}
                <Link to="/login" className="text-primary hover:text-primary-light font-semibold transition-colors duration-200">
                  Sign in
                </Link>
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
};

export default Signup;

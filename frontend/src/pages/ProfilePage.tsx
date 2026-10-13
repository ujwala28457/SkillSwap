import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { api } from '../services/api';
import { UserProfileResponse, ApiError } from '../types/api';
import { ProficiencyBadge } from '../components/Badge';
import { Alert } from '../components/Alert';
import {
  User as UserIcon,
  BookOpen,
  GraduationCap,
  ArrowLeft,
  Loader2,
  Sparkles,
} from 'lucide-react';

export const ProfilePage: React.FC = () => {
  const { userId } = useParams<{ userId: string }>();
  const [profile, setProfile] = useState<UserProfileResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<ApiError | null>(null);

  useEffect(() => {
    const fetchProfile = async () => {
      if (!userId) return;
      setLoading(true);
      try {
        const data = await api.getUserProfile(Number(userId));
        setProfile(data);
      } catch (err: any) {
        setError(err as ApiError);
      } finally {
        setLoading(false);
      }
    };

    fetchProfile();
  }, [userId]);

  if (loading) {
    return (
      <div className="min-h-[60vh] flex flex-col items-center justify-center gap-3">
        <Loader2 className="w-8 h-8 text-brand-600 animate-spin" />
        <p className="text-sm text-slate-500 font-medium">Loading user profile...</p>
      </div>
    );
  }

  if (error || !profile) {
    return (
      <div className="max-w-3xl mx-auto px-4 py-12">
        <Alert
          type="error"
          message={error?.message || 'User profile could not be found.'}
        />
        <div className="mt-4">
          <Link
            to="/discover"
            className="inline-flex items-center gap-2 text-sm font-semibold text-brand-600 hover:text-brand-700"
          >
            <ArrowLeft className="w-4 h-4" />
            Back to Discovery
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Back button */}
      <div>
        <Link
          to="/discover"
          className="inline-flex items-center gap-1.5 text-xs font-semibold text-slate-500 hover:text-slate-800 transition-colors"
        >
          <ArrowLeft className="w-3.5 h-3.5" />
          Back to marketplace
        </Link>
      </div>

      {/* User Header Profile Card */}
      <div className="bg-white rounded-3xl border border-slate-200/80 p-8 shadow-xs flex flex-col sm:flex-row items-center sm:items-start gap-6">
        <div className="w-20 h-20 rounded-2xl bg-gradient-to-tr from-brand-600 to-teal-400 text-white flex items-center justify-center font-bold text-3xl shadow-md shadow-brand-500/20 shrink-0">
          <UserIcon className="w-10 h-10" />
        </div>

        <div className="text-center sm:text-left flex-1">
          <span className="text-xs uppercase font-bold tracking-wider text-brand-600">
            Public Member Profile
          </span>
          <h1 className="text-3xl font-extrabold text-slate-900 tracking-tight mt-1">
            {profile.name}
          </h1>
          <p className="mt-2 text-sm text-slate-500">
            Offers {profile.offeredSkills.length} skills &bull; Seeking {profile.learningSkills.length} skills
          </p>

          <div className="mt-5 flex flex-wrap items-center justify-center sm:justify-start gap-3">
            <Link
              to="/matches"
              className="inline-flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-semibold text-white bg-brand-600 hover:bg-brand-700 shadow-sm"
            >
              <Sparkles className="w-3.5 h-3.5" />
              Check Reciprocal Matches
            </Link>
          </div>
        </div>
      </div>

      {/* Skills Sections */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Offered Skills */}
        <div className="bg-white rounded-2xl border border-slate-200/80 p-6 shadow-xs">
          <div className="flex items-center gap-2 pb-4 border-b border-slate-100 text-slate-900 font-bold">
            <BookOpen className="w-5 h-5 text-teal-600" />
            <h2>Skills {profile.name} Can Teach ({profile.offeredSkills.length})</h2>
          </div>

          <div className="mt-4 space-y-3">
            {profile.offeredSkills.length === 0 ? (
              <p className="text-xs text-slate-400 py-4 text-center">No teaching skills listed.</p>
            ) : (
              profile.offeredSkills.map((s) => (
                <div
                  key={s.id}
                  className="p-3.5 rounded-xl bg-slate-50 border border-slate-100 flex items-center justify-between"
                >
                  <div>
                    <p className="text-sm font-semibold text-slate-900">{s.skillName}</p>
                    {s.description && (
                      <p className="text-xs text-slate-500 mt-0.5">{s.description}</p>
                    )}
                  </div>
                  <ProficiencyBadge level={s.proficiencyLevel} />
                </div>
              ))
            )}
          </div>
        </div>

        {/* Learning Goals */}
        <div className="bg-white rounded-2xl border border-slate-200/80 p-6 shadow-xs">
          <div className="flex items-center gap-2 pb-4 border-b border-slate-100 text-slate-900 font-bold">
            <GraduationCap className="w-5 h-5 text-indigo-600" />
            <h2>Skills {profile.name} Wants to Learn ({profile.learningSkills.length})</h2>
          </div>

          <div className="mt-4 space-y-3">
            {profile.learningSkills.length === 0 ? (
              <p className="text-xs text-slate-400 py-4 text-center">No learning goals listed.</p>
            ) : (
              profile.learningSkills.map((s) => (
                <div
                  key={s.id}
                  className="p-3.5 rounded-xl bg-slate-50 border border-slate-100 flex items-center justify-between"
                >
                  <div>
                    <p className="text-sm font-semibold text-slate-900">{s.skillName}</p>
                    {s.description && (
                      <p className="text-xs text-slate-500 mt-0.5">{s.description}</p>
                    )}
                  </div>
                  <ProficiencyBadge level={s.proficiencyLevel} />
                </div>
              ))
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

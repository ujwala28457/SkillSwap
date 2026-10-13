import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { api } from '../services/api';
import { UserSkillResponse, SkillMatchResponse, SwapRequestResponse } from '../types/api';
import { ProficiencyBadge, StatusBadge } from '../components/Badge';
import {
  BookOpen,
  GraduationCap,
  Sparkles,
  ArrowLeftRight,
  ArrowRight,
  Plus,
  Loader2,
  Compass,
} from 'lucide-react';

export const DashboardPage: React.FC = () => {
  const { user } = useAuth();

  const [offeredSkills, setOfferedSkills] = useState<UserSkillResponse[]>([]);
  const [learningSkills, setLearningSkills] = useState<UserSkillResponse[]>([]);
  const [matches, setMatches] = useState<SkillMatchResponse[]>([]);
  const [incomingSwaps, setIncomingSwaps] = useState<SwapRequestResponse[]>([]);
  const [outgoingSwaps, setOutgoingSwaps] = useState<SwapRequestResponse[]>([]);
  const [loading, setLoading] = useState<boolean>(true);

  useEffect(() => {
    const fetchDashboardData = async () => {
      setLoading(true);
      try {
        const [offered, learning, matchesData, incoming, outgoing] = await Promise.all([
          api.getMyOfferedSkills(),
          api.getMyLearningSkills(),
          api.getComplementaryMatches(),
          api.getIncomingSwapRequests(),
          api.getOutgoingSwapRequests(),
        ]);
        setOfferedSkills(offered);
        setLearningSkills(learning);
        setMatches(matchesData);
        setIncomingSwaps(incoming);
        setOutgoingSwaps(outgoing);
      } catch (err) {
        console.error('Error fetching dashboard data:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchDashboardData();
  }, []);

  if (loading) {
    return (
      <div className="min-h-[60vh] flex flex-col items-center justify-center gap-3">
        <Loader2 className="w-8 h-8 text-brand-600 animate-spin" />
        <p className="text-sm text-slate-500 font-medium">Loading your dashboard...</p>
      </div>
    );
  }

  const pendingIncoming = incomingSwaps.filter((s) => s.status === 'PENDING').length;
  const pendingOutgoing = outgoingSwaps.filter((s) => s.status === 'PENDING').length;

  return (
    <div className="space-y-8 py-6 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      {/* Welcome Banner */}
      <div className="relative overflow-hidden rounded-3xl bg-gradient-to-r from-slate-900 via-slate-800 to-teal-950 p-8 sm:p-10 text-white shadow-xl">
        <div className="relative z-10 max-w-2xl">
          <span className="text-xs uppercase font-bold tracking-wider text-teal-400">
            Welcome back
          </span>
          <h1 className="text-3xl sm:text-4xl font-extrabold tracking-tight mt-1">
            Hi, {user?.name}!
          </h1>
          <p className="mt-2 text-slate-300 text-sm sm:text-base leading-relaxed">
            Ready to exchange knowledge? You have{' '}
            <span className="font-semibold text-teal-300">{matches.length} reciprocal matches</span>{' '}
            waiting in the marketplace.
          </p>

          <div className="mt-6 flex flex-wrap gap-3">
            <Link
              to="/matches"
              className="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl text-sm font-semibold bg-brand-500 hover:bg-brand-600 text-white shadow-md shadow-brand-500/25 transition-all"
            >
              <Sparkles className="w-4 h-4" />
              View Matches ({matches.length})
            </Link>
            <Link
              to="/my-skills"
              className="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl text-sm font-semibold bg-white/10 hover:bg-white/15 text-white backdrop-blur-xs transition-all border border-white/10"
            >
              <Plus className="w-4 h-4" />
              Manage Skills
            </Link>
          </div>
        </div>

        <div className="absolute -right-12 -bottom-12 w-64 h-64 rounded-full bg-teal-500/10 blur-3xl pointer-events-none" />
      </div>

      {/* Metrics Grid */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 sm:gap-6">
        <div className="bg-white p-6 rounded-2xl border border-slate-200/80 shadow-xs">
          <div className="flex items-center justify-between text-teal-600 mb-3">
            <span className="text-xs font-bold uppercase tracking-wider text-slate-500">
              Teaching
            </span>
            <BookOpen className="w-5 h-5 text-teal-500" />
          </div>
          <div className="text-3xl font-extrabold text-slate-900">{offeredSkills.length}</div>
          <p className="text-xs text-slate-500 mt-1">Skills offered to peers</p>
        </div>

        <div className="bg-white p-6 rounded-2xl border border-slate-200/80 shadow-xs">
          <div className="flex items-center justify-between text-indigo-600 mb-3">
            <span className="text-xs font-bold uppercase tracking-wider text-slate-500">
              Learning
            </span>
            <GraduationCap className="w-5 h-5 text-indigo-500" />
          </div>
          <div className="text-3xl font-extrabold text-slate-900">{learningSkills.length}</div>
          <p className="text-xs text-slate-500 mt-1">Skills you want to master</p>
        </div>

        <div className="bg-white p-6 rounded-2xl border border-slate-200/80 shadow-xs">
          <div className="flex items-center justify-between text-brand-600 mb-3">
            <span className="text-xs font-bold uppercase tracking-wider text-slate-500">
              Matches
            </span>
            <Sparkles className="w-5 h-5 text-brand-500" />
          </div>
          <div className="text-3xl font-extrabold text-slate-900">{matches.length}</div>
          <p className="text-xs text-slate-500 mt-1">Two-way match opportunities</p>
        </div>

        <div className="bg-white p-6 rounded-2xl border border-slate-200/80 shadow-xs">
          <div className="flex items-center justify-between text-amber-600 mb-3">
            <span className="text-xs font-bold uppercase tracking-wider text-slate-500">
              Pending Swaps
            </span>
            <ArrowLeftRight className="w-5 h-5 text-amber-500" />
          </div>
          <div className="text-3xl font-extrabold text-slate-900">
            {pendingIncoming + pendingOutgoing}
          </div>
          <p className="text-xs text-slate-500 mt-1">
            {pendingIncoming} in / {pendingOutgoing} out
          </p>
        </div>
      </div>

      {/* Main Content Grid: Skills Overview & Match Opportunities */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Left Column: My Skills Preview */}
        <div className="lg:col-span-2 space-y-6">
          <div className="bg-white rounded-2xl border border-slate-200/80 p-6 shadow-xs">
            <div className="flex items-center justify-between pb-4 border-b border-slate-100">
              <h2 className="text-lg font-bold text-slate-900 flex items-center gap-2">
                <BookOpen className="w-5 h-5 text-brand-600" />
                Skills You Offer to Teach
              </h2>
              <Link
                to="/my-skills"
                className="text-xs font-semibold text-brand-600 hover:text-brand-700 flex items-center gap-1"
              >
                Manage
                <ArrowRight className="w-3.5 h-3.5" />
              </Link>
            </div>

            <div className="mt-4">
              {offeredSkills.length === 0 ? (
                <div className="text-center py-8 bg-slate-50 rounded-xl border border-dashed border-slate-200">
                  <p className="text-sm text-slate-500">You haven't added any teaching skills yet.</p>
                  <Link
                    to="/my-skills"
                    className="inline-flex items-center gap-1.5 mt-3 text-xs font-semibold text-brand-600 hover:text-brand-700"
                  >
                    <Plus className="w-3.5 h-3.5" />
                    Add your first skill
                  </Link>
                </div>
              ) : (
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                  {offeredSkills.slice(0, 4).map((skill) => (
                    <div
                      key={skill.id}
                      className="p-3.5 rounded-xl border border-slate-100 bg-slate-50/60 flex items-center justify-between"
                    >
                      <div>
                        <p className="text-sm font-semibold text-slate-900">{skill.skillName}</p>
                        {skill.description && (
                          <p className="text-xs text-slate-500 truncate max-w-[180px]">
                            {skill.description}
                          </p>
                        )}
                      </div>
                      <ProficiencyBadge level={skill.proficiencyLevel} />
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>

          <div className="bg-white rounded-2xl border border-slate-200/80 p-6 shadow-xs">
            <div className="flex items-center justify-between pb-4 border-b border-slate-100">
              <h2 className="text-lg font-bold text-slate-900 flex items-center gap-2">
                <GraduationCap className="w-5 h-5 text-indigo-600" />
                Skills You Want to Learn
              </h2>
              <Link
                to="/my-skills"
                className="text-xs font-semibold text-brand-600 hover:text-brand-700 flex items-center gap-1"
              >
                Manage
                <ArrowRight className="w-3.5 h-3.5" />
              </Link>
            </div>

            <div className="mt-4">
              {learningSkills.length === 0 ? (
                <div className="text-center py-8 bg-slate-50 rounded-xl border border-dashed border-slate-200">
                  <p className="text-sm text-slate-500">You haven't listed any learning goals yet.</p>
                  <Link
                    to="/my-skills"
                    className="inline-flex items-center gap-1.5 mt-3 text-xs font-semibold text-indigo-600 hover:text-indigo-700"
                  >
                    <Plus className="w-3.5 h-3.5" />
                    Add a learning goal
                  </Link>
                </div>
              ) : (
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                  {learningSkills.slice(0, 4).map((skill) => (
                    <div
                      key={skill.id}
                      className="p-3.5 rounded-xl border border-slate-100 bg-slate-50/60 flex items-center justify-between"
                    >
                      <div>
                        <p className="text-sm font-semibold text-slate-900">{skill.skillName}</p>
                        {skill.description && (
                          <p className="text-xs text-slate-500 truncate max-w-[180px]">
                            {skill.description}
                          </p>
                        )}
                      </div>
                      <ProficiencyBadge level={skill.proficiencyLevel} />
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        </div>

        {/* Right Column: Top Reciprocal Matches & Quick Actions */}
        <div className="space-y-6">
          {/* Matches Box */}
          <div className="bg-white rounded-2xl border border-slate-200/80 p-6 shadow-xs">
            <div className="flex items-center justify-between pb-4 border-b border-slate-100">
              <h2 className="text-lg font-bold text-slate-900 flex items-center gap-2">
                <Sparkles className="w-5 h-5 text-amber-500" />
                Top Matches
              </h2>
              <Link
                to="/matches"
                className="text-xs font-semibold text-brand-600 hover:text-brand-700 flex items-center gap-1"
              >
                View all
                <ArrowRight className="w-3.5 h-3.5" />
              </Link>
            </div>

            <div className="mt-4 space-y-3">
              {matches.length === 0 ? (
                <div className="text-center py-6 text-sm text-slate-500">
                  <p>No complementary matches found yet.</p>
                  <p className="text-xs text-slate-400 mt-1">
                    Add more skills you can teach and want to learn to unlock reciprocal pairs.
                  </p>
                </div>
              ) : (
                matches.slice(0, 3).map((match) => (
                  <div
                    key={match.matchedUser.id}
                    className="p-3.5 rounded-xl border border-slate-100 hover:border-brand-200 transition-colors bg-slate-50/50"
                  >
                    <div className="flex items-center justify-between">
                      <span className="font-semibold text-sm text-slate-900">
                        {match.matchedUser.name}
                      </span>
                      <Link
                        to="/matches"
                        className="text-xs font-bold text-brand-600 hover:text-brand-700"
                      >
                        Swap &rarr;
                      </Link>
                    </div>
                    <div className="mt-2 text-xs space-y-1">
                      <div className="text-slate-600">
                        <span className="text-slate-400">Teaches you:</span>{' '}
                        <span className="font-medium text-teal-700">
                          {match.skillsTheyTeachYou.map((s) => s.skillName).join(', ')}
                        </span>
                      </div>
                      <div className="text-slate-600">
                        <span className="text-slate-400">Learns from you:</span>{' '}
                        <span className="font-medium text-indigo-700">
                          {match.skillsYouTeachThem.map((s) => s.skillName).join(', ')}
                        </span>
                      </div>
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>

          {/* Active Swaps Box */}
          <div className="bg-white rounded-2xl border border-slate-200/80 p-6 shadow-xs">
            <div className="flex items-center justify-between pb-4 border-b border-slate-100">
              <h2 className="text-lg font-bold text-slate-900 flex items-center gap-2">
                <ArrowLeftRight className="w-5 h-5 text-teal-600" />
                Recent Swap Requests
              </h2>
              <Link
                to="/swaps"
                className="text-xs font-semibold text-brand-600 hover:text-brand-700 flex items-center gap-1"
              >
                View all
                <ArrowRight className="w-3.5 h-3.5" />
              </Link>
            </div>

            <div className="mt-4 space-y-3">
              {[...incomingSwaps, ...outgoingSwaps].length === 0 ? (
                <div className="text-center py-6 text-sm text-slate-500">
                  <p>No swap requests yet.</p>
                  <Link
                    to="/discover"
                    className="inline-flex items-center gap-1 mt-2 text-xs font-semibold text-brand-600"
                  >
                    <Compass className="w-3.5 h-3.5" />
                    Explore marketplace
                  </Link>
                </div>
              ) : (
                [...incomingSwaps, ...outgoingSwaps].slice(0, 3).map((swap) => (
                  <div
                    key={swap.id}
                    className="p-3 rounded-xl border border-slate-100 bg-slate-50/50 flex items-center justify-between text-xs"
                  >
                    <div>
                      <p className="font-semibold text-slate-900">
                        {swap.requester.id === user?.id ? `To ${swap.recipient.name}` : `From ${swap.requester.name}`}
                      </p>
                      <p className="text-slate-500 text-[11px] mt-0.5">
                        {swap.offeredSkill.skillName} &harr; {swap.requestedSkill.skillName}
                      </p>
                    </div>
                    <StatusBadge status={swap.status} />
                  </div>
                ))
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

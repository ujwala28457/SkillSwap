import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../services/api';
import { SkillMatchResponse, SkillSummaryDto, ApiError } from '../types/api';
import { ProficiencyBadge } from '../components/Badge';
import { Alert } from '../components/Alert';
import { Modal } from '../components/Modal';
import {
  Sparkles,
  ArrowRight,
  ArrowLeftRight,
  User as UserIcon,
  Loader2,
  CheckCircle2,
  Plus,
} from 'lucide-react';

export const MatchesPage: React.FC = () => {
  const [matches, setMatches] = useState<SkillMatchResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<ApiError | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  // Swap Request Modal State
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedMatch, setSelectedMatch] = useState<SkillMatchResponse | null>(null);
  const [selectedOfferedSkillId, setSelectedOfferedSkillId] = useState<number | ''>('');
  const [selectedRequestedSkillId, setSelectedRequestedSkillId] = useState<number | ''>('');
  const [swapMessage, setSwapMessage] = useState('');
  const [submittingSwap, setSubmittingSwap] = useState(false);
  const [swapError, setSwapError] = useState<ApiError | null>(null);

  const fetchMatches = async () => {
    setLoading(true);
    try {
      const data = await api.getComplementaryMatches();
      setMatches(data);
    } catch (err: any) {
      setError(err as ApiError);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchMatches();
  }, []);

  const openSwapModal = (match: SkillMatchResponse) => {
    setSelectedMatch(match);
    setSelectedOfferedSkillId(match.skillsYouTeachThem[0]?.skillId || '');
    setSelectedRequestedSkillId(match.skillsTheyTeachYou[0]?.skillId || '');
    setSwapMessage(`Hi ${match.matchedUser.name}, I would love to trade skills with you!`);
    setSwapError(null);
    setIsModalOpen(true);
  };

  const handleSendSwapRequest = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedMatch || !selectedOfferedSkillId || !selectedRequestedSkillId) {
      setSwapError({
        status: 400,
        error: 'Bad Request',
        message: 'Please select both an offered and a requested skill.',
      });
      return;
    }

    setSubmittingSwap(true);
    setSwapError(null);

    try {
      await api.createSwapRequest({
        recipientId: selectedMatch.matchedUser.id,
        offeredSkillId: Number(selectedOfferedSkillId),
        requestedSkillId: Number(selectedRequestedSkillId),
        message: swapMessage.trim() || undefined,
      });

      setSuccessMessage(
        `Swap request sent to ${selectedMatch.matchedUser.name}! You can track it under Swap Requests.`
      );
      setIsModalOpen(false);
    } catch (err: any) {
      setSwapError(err as ApiError);
    } finally {
      setSubmittingSwap(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-[60vh] flex flex-col items-center justify-center gap-3">
        <Loader2 className="w-8 h-8 text-brand-600 animate-spin" />
        <p className="text-sm text-slate-500 font-medium">
          Running reciprocal matchmaking engine...
        </p>
      </div>
    );
  }

  return (
    <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Header */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 pb-6 border-b border-slate-200">
        <div>
          <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-brand-50 border border-brand-200 text-brand-700 text-xs font-semibold mb-2">
            <Sparkles className="w-3.5 h-3.5 text-brand-600" />
            Reciprocal Matchmaker
          </div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
            Complementary Skill Matches
          </h1>
          <p className="mt-1 text-sm text-slate-500">
            Peers who want what you offer AND offer what you want to learn
          </p>
        </div>

        <Link
          to="/my-skills"
          className="inline-flex items-center gap-2 px-4 py-2.5 rounded-xl text-xs font-semibold text-slate-700 bg-white border border-slate-200 hover:bg-slate-50 transition-colors shadow-xs"
        >
          <Plus className="w-3.5 h-3.5" />
          Update Your Skills
        </Link>
      </div>

      {/* Global Alerts */}
      {error && (
        <Alert
          type="error"
          message={error.message}
          validationErrors={error.validationErrors}
          onClose={() => setError(null)}
        />
      )}
      {successMessage && (
        <Alert
          type="success"
          message={successMessage}
          onClose={() => setSuccessMessage(null)}
        />
      )}

      {matches.length === 0 ? (
        <div className="text-center py-16 bg-white rounded-3xl border border-dashed border-slate-300 p-8">
          <div className="mx-auto w-14 h-14 rounded-2xl bg-amber-50 text-amber-500 flex items-center justify-center mb-4">
            <Sparkles className="w-7 h-7" />
          </div>
          <h3 className="text-lg font-bold text-slate-900">No reciprocal matches yet</h3>
          <p className="mt-2 text-sm text-slate-500 max-w-md mx-auto leading-relaxed">
            A reciprocal match occurs when another member offers a skill you want to learn, and they also want to learn a skill you can teach.
          </p>
          <div className="mt-6 flex flex-wrap items-center justify-center gap-3">
            <Link
              to="/my-skills"
              className="px-5 py-2.5 rounded-xl text-xs font-semibold text-white bg-brand-600 hover:bg-brand-700 shadow-sm"
            >
              Add more teaching or learning skills
            </Link>
            <Link
              to="/discover"
              className="px-5 py-2.5 rounded-xl text-xs font-semibold text-slate-700 bg-slate-100 hover:bg-slate-200"
            >
              Browse all offered skills
            </Link>
          </div>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {matches.map((match) => (
            <div
              key={match.matchedUser.id}
              className="bg-white rounded-2xl border border-slate-200/80 p-6 shadow-xs hover:shadow-md transition-all flex flex-col justify-between"
            >
              <div>
                {/* User Header */}
                <div className="flex items-center justify-between pb-4 border-b border-slate-100">
                  <Link
                    to={`/profile/${match.matchedUser.id}`}
                    className="flex items-center gap-3 group"
                  >
                    <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-brand-600 to-teal-400 text-white flex items-center justify-center font-bold text-sm shadow-xs">
                      <UserIcon className="w-5 h-5" />
                    </div>
                    <div>
                      <h3 className="text-base font-bold text-slate-900 group-hover:text-brand-600 transition-colors">
                        {match.matchedUser.name}
                      </h3>
                      <p className="text-xs text-slate-400">{match.matchedUser.email}</p>
                    </div>
                  </Link>

                  <button
                    onClick={() => openSwapModal(match)}
                    className="inline-flex items-center gap-1.5 px-3.5 py-2 rounded-xl text-xs font-bold text-white bg-brand-600 hover:bg-brand-700 shadow-xs transition-colors"
                  >
                    <ArrowLeftRight className="w-3.5 h-3.5" />
                    Request Swap
                  </button>
                </div>

                {/* Exchange Details Grid */}
                <div className="mt-5 grid grid-cols-1 sm:grid-cols-2 gap-4">
                  {/* They Teach You */}
                  <div className="p-3.5 rounded-xl bg-teal-50/50 border border-teal-100">
                    <span className="text-[11px] font-bold uppercase tracking-wider text-teal-800 block mb-2">
                      They Teach You
                    </span>
                    <div className="space-y-1.5">
                      {match.skillsTheyTeachYou.map((skill: SkillSummaryDto) => (
                        <div
                          key={skill.skillId}
                          className="flex items-center justify-between gap-1 text-xs"
                        >
                          <span className="font-semibold text-slate-800">{skill.skillName}</span>
                          <ProficiencyBadge level={skill.proficiencyLevel} />
                        </div>
                      ))}
                    </div>
                  </div>

                  {/* You Teach Them */}
                  <div className="p-3.5 rounded-xl bg-indigo-50/50 border border-indigo-100">
                    <span className="text-[11px] font-bold uppercase tracking-wider text-indigo-800 block mb-2">
                      You Teach Them
                    </span>
                    <div className="space-y-1.5">
                      {match.skillsYouTeachThem.map((skill: SkillSummaryDto) => (
                        <div
                          key={skill.skillId}
                          className="flex items-center justify-between gap-1 text-xs"
                        >
                          <span className="font-semibold text-slate-800">{skill.skillName}</span>
                          <ProficiencyBadge level={skill.proficiencyLevel} />
                        </div>
                      ))}
                    </div>
                  </div>
                </div>
              </div>

              <div className="mt-6 pt-3 border-t border-slate-100 flex items-center justify-between text-xs text-slate-400">
                <span>Mutual barter compatible</span>
                <Link
                  to={`/profile/${match.matchedUser.id}`}
                  className="font-medium text-brand-600 hover:text-brand-700 flex items-center gap-1"
                >
                  View Profile
                  <ArrowRight className="w-3 h-3" />
                </Link>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Request Swap Modal */}
      {selectedMatch && (
        <Modal
          isOpen={isModalOpen}
          onClose={() => setIsModalOpen(false)}
          title={`Request Skill Swap with ${selectedMatch.matchedUser.name}`}
        >
          {swapError && (
            <Alert
              type="error"
              message={swapError.message}
              validationErrors={swapError.validationErrors}
              onClose={() => setSwapError(null)}
              className="mb-4"
            />
          )}

          <form onSubmit={handleSendSwapRequest} className="space-y-4">
            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-2">
                You Will Teach ({selectedMatch.matchedUser.name} wants to learn) *
              </label>
              <select
                required
                value={selectedOfferedSkillId}
                onChange={(e) => setSelectedOfferedSkillId(Number(e.target.value))}
                className="w-full px-4 py-2.5 rounded-xl border border-slate-200 bg-slate-50/50 text-slate-900 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500/20 focus:border-brand-500"
              >
                {selectedMatch.skillsYouTeachThem.map((skill) => (
                  <option key={skill.skillId} value={skill.skillId}>
                    {skill.skillName} ({skill.proficiencyLevel || 'Level unspecified'})
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-2">
                They Will Teach ({selectedMatch.matchedUser.name} teaches) *
              </label>
              <select
                required
                value={selectedRequestedSkillId}
                onChange={(e) => setSelectedRequestedSkillId(Number(e.target.value))}
                className="w-full px-4 py-2.5 rounded-xl border border-slate-200 bg-slate-50/50 text-slate-900 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500/20 focus:border-brand-500"
              >
                {selectedMatch.skillsTheyTeachYou.map((skill) => (
                  <option key={skill.skillId} value={skill.skillId}>
                    {skill.skillName} ({skill.proficiencyLevel || 'Level unspecified'})
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-2">
                Proposal Message (Optional)
              </label>
              <textarea
                rows={3}
                value={swapMessage}
                onChange={(e) => setSwapMessage(e.target.value)}
                placeholder="Suggest how you'd like to structure the exchange..."
                className="w-full px-4 py-2.5 rounded-xl border border-slate-200 bg-slate-50/50 text-slate-900 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500/20 focus:border-brand-500"
              />
            </div>

            <div className="flex items-center justify-end gap-3 pt-4 border-t border-slate-100">
              <button
                type="button"
                onClick={() => setIsModalOpen(false)}
                className="px-4 py-2 rounded-xl text-sm font-medium text-slate-600 hover:bg-slate-100"
              >
                Cancel
              </button>
              <button
                type="submit"
                disabled={submittingSwap}
                className="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl text-sm font-semibold text-white bg-brand-600 hover:bg-brand-700 disabled:opacity-50 shadow-sm"
              >
                {submittingSwap ? (
                  <>
                    <Loader2 className="w-4 h-4 animate-spin" />
                    Sending...
                  </>
                ) : (
                  <>
                    <CheckCircle2 className="w-4 h-4" />
                    Send Request
                  </>
                )}
              </button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  );
};

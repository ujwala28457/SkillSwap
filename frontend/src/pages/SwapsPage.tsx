import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../services/api';
import { SwapRequestResponse, ApiError } from '../types/api';
import { StatusBadge, ProficiencyBadge } from '../components/Badge';
import { Alert } from '../components/Alert';
import {
  ArrowLeftRight,
  ArrowDownLeft,
  ArrowUpRight,
  Check,
  X,
  Ban,
  Loader2,
  Calendar,
  MessageSquare,
  Sparkles,
  User as UserIcon,
} from 'lucide-react';

export const SwapsPage: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'incoming' | 'outgoing'>('incoming');
  const [incoming, setIncoming] = useState<SwapRequestResponse[]>([]);
  const [outgoing, setOutgoing] = useState<SwapRequestResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [actionLoadingId, setActionLoadingId] = useState<number | null>(null);
  const [error, setError] = useState<ApiError | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const fetchSwaps = async () => {
    try {
      const [inc, out] = await Promise.all([
        api.getIncomingSwapRequests(),
        api.getOutgoingSwapRequests(),
      ]);
      setIncoming(inc);
      setOutgoing(out);
    } catch (err: any) {
      setError(err as ApiError);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchSwaps();
  }, []);

  const handleAccept = async (id: number) => {
    setActionLoadingId(id);
    setError(null);
    setSuccessMessage(null);
    try {
      await api.acceptSwapRequest(id);
      setSuccessMessage('Swap request accepted! You can now collaborate.');
      await fetchSwaps();
    } catch (err: any) {
      setError(err as ApiError);
    } finally {
      setActionLoadingId(null);
    }
  };

  const handleReject = async (id: number) => {
    if (!window.confirm('Are you sure you want to decline this swap request?')) return;
    setActionLoadingId(id);
    setError(null);
    setSuccessMessage(null);
    try {
      await api.rejectSwapRequest(id);
      setSuccessMessage('Swap request declined.');
      await fetchSwaps();
    } catch (err: any) {
      setError(err as ApiError);
    } finally {
      setActionLoadingId(null);
    }
  };

  const handleCancel = async (id: number) => {
    if (!window.confirm('Are you sure you want to cancel your swap request?')) return;
    setActionLoadingId(id);
    setError(null);
    setSuccessMessage(null);
    try {
      await api.cancelSwapRequest(id);
      setSuccessMessage('Swap request cancelled.');
      await fetchSwaps();
    } catch (err: any) {
      setError(err as ApiError);
    } finally {
      setActionLoadingId(null);
    }
  };

  if (loading) {
    return (
      <div className="min-h-[60vh] flex flex-col items-center justify-center gap-3">
        <Loader2 className="w-8 h-8 text-brand-600 animate-spin" />
        <p className="text-sm text-slate-500 font-medium">Loading swap requests...</p>
      </div>
    );
  }

  const currentRequests = activeTab === 'incoming' ? incoming : outgoing;

  return (
    <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 pb-6 border-b border-slate-200">
        <div>
          <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-brand-50 border border-brand-200 text-brand-700 text-xs font-semibold mb-2">
            <ArrowLeftRight className="w-3.5 h-3.5 text-brand-600" />
            Exchange Tracker
          </div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
            Swap Requests
          </h1>
          <p className="mt-1 text-sm text-slate-500">
            Review incoming barter proposals and track your outgoing requests
          </p>
        </div>

        <Link
          to="/matches"
          className="inline-flex items-center gap-2 px-4 py-2.5 rounded-xl text-xs font-semibold text-white bg-brand-600 hover:bg-brand-700 shadow-xs transition-colors"
        >
          <Sparkles className="w-3.5 h-3.5" />
          Find New Matches
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

      {/* Tabs */}
      <div className="flex border-b border-slate-200 gap-8">
        <button
          onClick={() => {
            setActiveTab('incoming');
            setError(null);
          }}
          className={`pb-3 text-sm font-bold flex items-center gap-2 border-b-2 transition-all ${
            activeTab === 'incoming'
              ? 'border-brand-600 text-brand-600'
              : 'border-transparent text-slate-500 hover:text-slate-800'
          }`}
        >
          <ArrowDownLeft className="w-4 h-4" />
          Incoming Proposals ({incoming.length})
        </button>
        <button
          onClick={() => {
            setActiveTab('outgoing');
            setError(null);
          }}
          className={`pb-3 text-sm font-bold flex items-center gap-2 border-b-2 transition-all ${
            activeTab === 'outgoing'
              ? 'border-brand-600 text-brand-600'
              : 'border-transparent text-slate-500 hover:text-slate-800'
          }`}
        >
          <ArrowUpRight className="w-4 h-4" />
          Outgoing Requests ({outgoing.length})
        </button>
      </div>

      {/* Request Cards */}
      {currentRequests.length === 0 ? (
        <div className="text-center py-16 bg-white rounded-3xl border border-dashed border-slate-300 p-8">
          <div className="mx-auto w-12 h-12 rounded-2xl bg-slate-100 flex items-center justify-center text-slate-400 mb-3">
            <ArrowLeftRight className="w-6 h-6" />
          </div>
          <h3 className="text-base font-bold text-slate-800">
            {activeTab === 'incoming'
              ? 'No incoming swap proposals yet'
              : 'No outgoing swap requests yet'}
          </h3>
          <p className="mt-1 text-sm text-slate-500 max-w-sm mx-auto">
            {activeTab === 'incoming'
              ? 'When another member requests a skill swap with you, their proposal will appear here.'
              : 'Explore the marketplace and reciprocal matches to propose skill exchanges to peers.'}
          </p>
          {activeTab === 'outgoing' && (
            <Link
              to="/matches"
              className="mt-4 inline-flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-semibold text-white bg-brand-600 hover:bg-brand-700 shadow-sm"
            >
              <Sparkles className="w-3.5 h-3.5" />
              Find Matches to Request
            </Link>
          )}
        </div>
      ) : (
        <div className="space-y-4">
          {currentRequests.map((request) => {
            const isIncoming = activeTab === 'incoming';
            const partner = isIncoming ? request.requester : request.recipient;
            const isPending = request.status === 'PENDING';
            const isActionBusy = actionLoadingId === request.id;

            return (
              <div
                key={request.id}
                className="bg-white rounded-2xl border border-slate-200/80 p-6 shadow-xs hover:shadow-md transition-shadow"
              >
                <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 pb-4 border-b border-slate-100">
                  <div className="flex items-center gap-3">
                    <Link
                      to={`/profile/${partner.id}`}
                      className="w-10 h-10 rounded-xl bg-slate-100 flex items-center justify-center text-slate-600 hover:bg-brand-50 hover:text-brand-600 transition-colors shrink-0"
                    >
                      <UserIcon className="w-5 h-5" />
                    </Link>
                    <div>
                      <div className="flex items-center gap-2">
                        <span className="text-xs text-slate-400 font-medium">
                          {isIncoming ? 'From:' : 'To:'}
                        </span>
                        <Link
                          to={`/profile/${partner.id}`}
                          className="font-bold text-slate-900 hover:text-brand-600 transition-colors"
                        >
                          {partner.name}
                        </Link>
                      </div>
                      <p className="text-xs text-slate-400">{partner.email}</p>
                    </div>
                  </div>

                  <div className="flex items-center gap-3">
                    <StatusBadge status={request.status} />
                    <div className="flex items-center gap-1 text-xs text-slate-400">
                      <Calendar className="w-3.5 h-3.5" />
                      <span>{new Date(request.createdAt).toLocaleDateString()}</span>
                    </div>
                  </div>
                </div>

                {/* Skills Barter Representation */}
                <div className="py-5 grid grid-cols-1 md:grid-cols-2 gap-4">
                  {/* Offered Skill */}
                  <div className="p-4 rounded-xl bg-teal-50/40 border border-teal-100">
                    <span className="text-[11px] font-bold uppercase tracking-wider text-teal-800 block mb-1">
                      {isIncoming ? `${partner.name} offers to teach` : 'You offer to teach'}
                    </span>
                    <div className="flex items-center justify-between">
                      <span className="text-base font-bold text-slate-900">
                        {request.offeredSkill.skillName}
                      </span>
                      <ProficiencyBadge level={request.offeredSkill.proficiencyLevel} />
                    </div>
                  </div>

                  {/* Requested Skill */}
                  <div className="p-4 rounded-xl bg-indigo-50/40 border border-indigo-100">
                    <span className="text-[11px] font-bold uppercase tracking-wider text-indigo-800 block mb-1">
                      {isIncoming ? `${partner.name} wants to learn from you` : `You want to learn from ${partner.name}`}
                    </span>
                    <div className="flex items-center justify-between">
                      <span className="text-base font-bold text-slate-900">
                        {request.requestedSkill.skillName}
                      </span>
                      <ProficiencyBadge level={request.requestedSkill.proficiencyLevel} />
                    </div>
                  </div>
                </div>

                {/* Message note if present */}
                {request.message && (
                  <div className="mb-4 p-3 rounded-xl bg-slate-50 border border-slate-100 flex items-start gap-2.5 text-xs text-slate-600">
                    <MessageSquare className="w-4 h-4 text-slate-400 shrink-0 mt-0.5" />
                    <div className="italic">"{request.message}"</div>
                  </div>
                )}

                {/* Actions Footer */}
                {isPending && (
                  <div className="pt-4 border-t border-slate-100 flex items-center justify-end gap-3">
                    {isIncoming ? (
                      <>
                        <button
                          onClick={() => handleReject(request.id)}
                          disabled={isActionBusy}
                          className="inline-flex items-center gap-1.5 px-4 py-2 rounded-xl text-xs font-semibold text-rose-700 bg-rose-50 hover:bg-rose-100 border border-rose-200 transition-colors disabled:opacity-50"
                        >
                          <X className="w-3.5 h-3.5" />
                          Decline
                        </button>
                        <button
                          onClick={() => handleAccept(request.id)}
                          disabled={isActionBusy}
                          className="inline-flex items-center gap-1.5 px-5 py-2 rounded-xl text-xs font-semibold text-white bg-brand-600 hover:bg-brand-700 shadow-sm transition-all disabled:opacity-50"
                        >
                          {isActionBusy ? (
                            <Loader2 className="w-3.5 h-3.5 animate-spin" />
                          ) : (
                            <Check className="w-3.5 h-3.5" />
                          )}
                          Accept Swap
                        </button>
                      </>
                    ) : (
                      <button
                        onClick={() => handleCancel(request.id)}
                        disabled={isActionBusy}
                        className="inline-flex items-center gap-1.5 px-4 py-2 rounded-xl text-xs font-semibold text-slate-600 bg-slate-100 hover:bg-slate-200 transition-colors disabled:opacity-50"
                      >
                        {isActionBusy ? (
                          <Loader2 className="w-3.5 h-3.5 animate-spin" />
                        ) : (
                          <Ban className="w-3.5 h-3.5" />
                        )}
                        Cancel Request
                      </button>
                    )}
                  </div>
                )}
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};

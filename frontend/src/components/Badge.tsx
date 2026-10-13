import React from 'react';
import { ProficiencyLevel, SwapRequestStatus, SkillType } from '../types/api';

export const ProficiencyBadge: React.FC<{ level: ProficiencyLevel | null }> = ({ level }) => {
  if (!level) return null;

  const config = {
    BEGINNER: { bg: 'bg-emerald-50 text-emerald-700 border-emerald-200', label: 'Beginner' },
    INTERMEDIATE: { bg: 'bg-sky-50 text-sky-700 border-sky-200', label: 'Intermediate' },
    ADVANCED: { bg: 'bg-indigo-50 text-indigo-700 border-indigo-200', label: 'Advanced' },
    EXPERT: { bg: 'bg-purple-50 text-purple-700 border-purple-200', label: 'Expert' },
  }[level] || { bg: 'bg-slate-100 text-slate-700 border-slate-200', label: level };

  return (
    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold border ${config.bg}`}>
      {config.label}
    </span>
  );
};

export const StatusBadge: React.FC<{ status: SwapRequestStatus }> = ({ status }) => {
  const config = {
    PENDING: { bg: 'bg-amber-50 text-amber-700 border-amber-200', label: 'Pending' },
    ACCEPTED: { bg: 'bg-emerald-50 text-emerald-700 border-emerald-200', label: 'Accepted' },
    REJECTED: { bg: 'bg-rose-50 text-rose-700 border-rose-200', label: 'Rejected' },
    CANCELLED: { bg: 'bg-slate-100 text-slate-600 border-slate-200', label: 'Cancelled' },
    COMPLETED: { bg: 'bg-blue-50 text-blue-700 border-blue-200', label: 'Completed' },
  }[status] || { bg: 'bg-slate-100 text-slate-700 border-slate-200', label: status };

  return (
    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold border ${config.bg}`}>
      {config.label}
    </span>
  );
};

export const SkillTypeBadge: React.FC<{ type: SkillType }> = ({ type }) => {
  if (type === 'TEACH') {
    return (
      <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-teal-50 text-teal-700 border border-teal-200">
        Offering
      </span>
    );
  }
  return (
    <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-amber-50 text-amber-700 border border-amber-200">
      Seeking
    </span>
  );
};

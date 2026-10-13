import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../services/api';
import { OfferedSkillDiscoveryResponse, Page } from '../types/api';
import { ProficiencyBadge } from '../components/Badge';
import {
  Compass,
  Search,
  User as UserIcon,
  ChevronLeft,
  ChevronRight,
  Loader2,
  Calendar,
  Sparkles,
} from 'lucide-react';

export const DiscoveryPage: React.FC = () => {
  const [skillsPage, setSkillsPage] = useState<Page<OfferedSkillDiscoveryResponse> | null>(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [currentPage, setCurrentPage] = useState(0);
  const [loading, setLoading] = useState(true);

  const fetchDiscoverySkills = async (query: string, page: number) => {
    setLoading(true);
    try {
      const data = await api.discoverOfferedSkills(query.trim() || undefined, page, 9);
      setSkillsPage(data);
    } catch (err) {
      console.error('Error fetching discovery skills:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDiscoverySkills(searchQuery, currentPage);
  }, [currentPage]);

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setCurrentPage(0);
    fetchDiscoverySkills(searchQuery, 0);
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 pb-6 border-b border-slate-200">
        <div>
          <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-brand-50 border border-brand-200 text-brand-700 text-xs font-semibold mb-2">
            <Compass className="w-3.5 h-3.5 text-brand-600" />
            Marketplace Discovery
          </div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
            Explore Skills Offered by Peers
          </h1>
          <p className="mt-1 text-sm text-slate-500">
            Find community members sharing their craft and experience
          </p>
        </div>

        {/* Search Bar Form */}
        <form onSubmit={handleSearchSubmit} className="flex gap-2 max-w-md w-full">
          <div className="relative flex-1">
            <Search className="w-4 h-4 absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400" />
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Search by skill name (e.g. React, Python)..."
              className="w-full pl-10 pr-4 py-2.5 rounded-xl border border-slate-200 bg-white text-sm focus:outline-none focus:ring-2 focus:ring-brand-500/20 focus:border-brand-500 shadow-xs"
            />
          </div>
          <button
            type="submit"
            className="px-4 py-2.5 rounded-xl text-sm font-semibold text-white bg-brand-600 hover:bg-brand-700 transition-colors shadow-xs"
          >
            Search
          </button>
        </form>
      </div>

      {/* Reciprocal Matches Callout */}
      <div className="p-4 rounded-2xl bg-gradient-to-r from-brand-50 via-teal-50 to-indigo-50 border border-brand-100 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <div className="w-9 h-9 rounded-xl bg-brand-600 text-white flex items-center justify-center shrink-0 shadow-sm">
            <Sparkles className="w-5 h-5" />
          </div>
          <div>
            <h4 className="text-sm font-bold text-slate-900">Looking for mutual exchanges?</h4>
            <p className="text-xs text-slate-600">
              Check your Reciprocal Matches to view peers who want what you teach and teach what you want.
            </p>
          </div>
        </div>
        <Link
          to="/matches"
          className="shrink-0 px-4 py-2 rounded-xl text-xs font-bold text-white bg-slate-900 hover:bg-slate-800 transition-colors shadow-xs"
        >
          View Reciprocal Matches &rarr;
        </Link>
      </div>

      {/* Loading State */}
      {loading ? (
        <div className="min-h-[40vh] flex flex-col items-center justify-center gap-3">
          <Loader2 className="w-8 h-8 text-brand-600 animate-spin" />
          <p className="text-sm text-slate-500 font-medium">Discovering offered skills...</p>
        </div>
      ) : skillsPage && skillsPage.content.length > 0 ? (
        <>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {skillsPage.content.map((item) => (
              <div
                key={item.id}
                className="bg-white rounded-2xl border border-slate-200/80 p-6 shadow-xs hover:shadow-md transition-all flex flex-col justify-between"
              >
                <div>
                  <div className="flex items-start justify-between gap-3 mb-2">
                    <h3 className="text-lg font-bold text-slate-900 leading-snug">
                      {item.skillName}
                    </h3>
                    <ProficiencyBadge level={item.proficiencyLevel} />
                  </div>

                  {item.skillDescription && (
                    <p className="text-xs text-slate-400 line-clamp-2 mb-3">
                      {item.skillDescription}
                    </p>
                  )}

                  {item.description ? (
                    <div className="p-3 rounded-xl bg-slate-50 border border-slate-100 text-xs text-slate-700 italic">
                      "{item.description}"
                    </div>
                  ) : (
                    <div className="p-3 rounded-xl bg-slate-50/50 border border-slate-100 text-xs text-slate-400">
                      No specific notes provided by teacher.
                    </div>
                  )}
                </div>

                <div className="mt-6 pt-4 border-t border-slate-100 flex items-center justify-between">
                  <Link
                    to={`/profile/${item.userId}`}
                    className="flex items-center gap-2 group"
                  >
                    <div className="w-7 h-7 rounded-full bg-brand-100 text-brand-700 flex items-center justify-center text-xs font-bold">
                      <UserIcon className="w-3.5 h-3.5" />
                    </div>
                    <div>
                      <span className="text-xs font-semibold text-slate-800 group-hover:text-brand-600 transition-colors">
                        {item.userName}
                      </span>
                    </div>
                  </Link>

                  <div className="flex items-center gap-1 text-[11px] text-slate-400">
                    <Calendar className="w-3 h-3" />
                    <span>{new Date(item.createdAt).toLocaleDateString()}</span>
                  </div>
                </div>
              </div>
            ))}
          </div>

          {/* Pagination Controls */}
          {skillsPage.totalPages > 1 && (
            <div className="flex items-center justify-between border-t border-slate-200 pt-6">
              <p className="text-xs text-slate-500">
                Showing page <span className="font-semibold">{skillsPage.number + 1}</span> of{' '}
                <span className="font-semibold">{skillsPage.totalPages}</span> ({skillsPage.totalElements} skills)
              </p>

              <div className="flex gap-2">
                <button
                  disabled={skillsPage.first}
                  onClick={() => setCurrentPage((prev) => Math.max(0, prev - 1))}
                  className="px-3 py-1.5 rounded-lg border border-slate-200 text-xs font-medium text-slate-600 hover:bg-slate-50 disabled:opacity-40 disabled:cursor-not-allowed flex items-center gap-1"
                >
                  <ChevronLeft className="w-4 h-4" />
                  Previous
                </button>
                <button
                  disabled={skillsPage.last}
                  onClick={() => setCurrentPage((prev) => prev + 1)}
                  className="px-3 py-1.5 rounded-lg border border-slate-200 text-xs font-medium text-slate-600 hover:bg-slate-50 disabled:opacity-40 disabled:cursor-not-allowed flex items-center gap-1"
                >
                  Next
                  <ChevronRight className="w-4 h-4" />
                </button>
              </div>
            </div>
          )}
        </>
      ) : (
        <div className="text-center py-16 bg-white rounded-2xl border border-dashed border-slate-300 p-8">
          <div className="mx-auto w-12 h-12 rounded-2xl bg-slate-100 flex items-center justify-center text-slate-400 mb-3">
            <Search className="w-6 h-6" />
          </div>
          <h3 className="text-base font-bold text-slate-800">No skills found</h3>
          <p className="mt-1 text-sm text-slate-500 max-w-sm mx-auto">
            {searchQuery
              ? `No offered skills matched "${searchQuery}". Try a different keyword.`
              : 'No skills have been offered by other members yet.'}
          </p>
          {searchQuery && (
            <button
              onClick={() => {
                setSearchQuery('');
                setCurrentPage(0);
                fetchDiscoverySkills('', 0);
              }}
              className="mt-4 px-4 py-2 rounded-xl text-xs font-semibold text-brand-700 bg-brand-50 hover:bg-brand-100"
            >
              Clear Search
            </button>
          )}
        </div>
      )}
    </div>
  );
};

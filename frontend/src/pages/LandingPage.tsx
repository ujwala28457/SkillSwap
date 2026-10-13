import React from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  ArrowRight,
  BookOpen,
  Sparkles,
  Users,
  Repeat,
  ShieldCheck,
  Zap,
} from 'lucide-react';

export const LandingPage: React.FC = () => {
  const { isAuthenticated } = useAuth();

  return (
    <div className="space-y-24 py-8">
      {/* Hero Section */}
      <section className="relative overflow-hidden text-center pt-12 pb-16 px-4 sm:px-6 lg:px-8 max-w-5xl mx-auto">
        <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-brand-50 border border-brand-200/80 text-brand-700 text-xs font-semibold mb-6 shadow-xs">
          <Sparkles className="w-4 h-4 text-brand-500" />
          <span>Peer-to-Peer Knowledge Economy</span>
        </div>

        <h1 className="text-4xl sm:text-6xl font-extrabold text-slate-900 tracking-tight leading-[1.15]">
          Trade skills, not dollars. <br />
          <span className="bg-gradient-to-r from-brand-600 to-teal-500 bg-clip-text text-transparent">
            Learn anything from anyone.
          </span>
        </h1>

        <p className="mt-6 text-lg sm:text-xl text-slate-600 max-w-2xl mx-auto leading-relaxed">
          SkillSwap is a reciprocal exchange platform where you teach what you know and learn what you want — no money needed, just mutual growth.
        </p>

        <div className="mt-8 flex flex-col sm:flex-row items-center justify-center gap-4">
          {isAuthenticated ? (
            <Link
              to="/dashboard"
              className="w-full sm:w-auto inline-flex items-center justify-center gap-2 px-6 py-3.5 rounded-xl font-semibold text-white bg-brand-600 hover:bg-brand-700 shadow-md shadow-brand-600/25 transition-all text-base"
            >
              Go to Dashboard
              <ArrowRight className="w-4 h-4" />
            </Link>
          ) : (
            <>
              <Link
                to="/register"
                className="w-full sm:w-auto inline-flex items-center justify-center gap-2 px-6 py-3.5 rounded-xl font-semibold text-white bg-brand-600 hover:bg-brand-700 shadow-md shadow-brand-600/25 transition-all text-base"
              >
                Start Swapping for Free
                <ArrowRight className="w-4 h-4" />
              </Link>
              <Link
                to="/login"
                className="w-full sm:w-auto inline-flex items-center justify-center gap-2 px-6 py-3.5 rounded-xl font-semibold text-slate-700 bg-white border border-slate-200 hover:bg-slate-50 transition-all text-base shadow-xs"
              >
                Sign In
              </Link>
            </>
          )}
        </div>

        {/* Feature Highlights Grid */}
        <div className="mt-16 grid grid-cols-1 md:grid-cols-3 gap-6 text-left">
          <div className="p-6 rounded-2xl bg-white border border-slate-200/80 shadow-xs hover:shadow-md transition-shadow">
            <div className="w-12 h-12 rounded-xl bg-teal-50 text-teal-600 flex items-center justify-center mb-4">
              <BookOpen className="w-6 h-6" />
            </div>
            <h3 className="text-lg font-bold text-slate-900">1. List Your Skills</h3>
            <p className="mt-2 text-sm text-slate-600 leading-relaxed">
              Declare what you can teach and what you yearn to learn with accurate proficiency levels.
            </p>
          </div>

          <div className="p-6 rounded-2xl bg-white border border-slate-200/80 shadow-xs hover:shadow-md transition-shadow">
            <div className="w-12 h-12 rounded-xl bg-indigo-50 text-indigo-600 flex items-center justify-center mb-4">
              <Sparkles className="w-6 h-6" />
            </div>
            <h3 className="text-lg font-bold text-slate-900">2. Matchmaking Engine</h3>
            <p className="mt-2 text-sm text-slate-600 leading-relaxed">
              Our automated reciprocal matcher pairs you with peers whose learning goals mirror your expertise.
            </p>
          </div>

          <div className="p-6 rounded-2xl bg-white border border-slate-200/80 shadow-xs hover:shadow-md transition-shadow">
            <div className="w-12 h-12 rounded-xl bg-amber-50 text-amber-600 flex items-center justify-center mb-4">
              <Repeat className="w-6 h-6" />
            </div>
            <h3 className="text-lg font-bold text-slate-900">3. Exchange & Grow</h3>
            <p className="mt-2 text-sm text-slate-600 leading-relaxed">
              Send formal swap requests, agree on the mutual exchange, and level up your craft together.
            </p>
          </div>
        </div>
      </section>

      {/* How It Works Detailed Section */}
      <section className="bg-slate-900 text-white rounded-3xl p-8 sm:p-14 max-w-6xl mx-auto shadow-xl">
        <div className="max-w-3xl mx-auto text-center">
          <h2 className="text-3xl font-extrabold tracking-tight">
            How SkillSwap Works Behind the Scenes
          </h2>
          <p className="mt-4 text-slate-400 text-base">
            A transparent, verified peer-exchange workflow built on reciprocity.
          </p>
        </div>

        <div className="mt-12 grid grid-cols-1 md:grid-cols-3 gap-8">
          <div className="flex gap-4">
            <div className="shrink-0 w-8 h-8 rounded-full bg-brand-500 text-white font-bold flex items-center justify-center text-sm">
              1
            </div>
            <div>
              <h4 className="font-semibold text-lg text-white">Create Profile</h4>
              <p className="mt-1 text-sm text-slate-400 leading-relaxed">
                Add skills you can offer (e.g. Python, UI Design, Guitar) and skills you are seeking to learn.
              </p>
            </div>
          </div>

          <div className="flex gap-4">
            <div className="shrink-0 w-8 h-8 rounded-full bg-brand-500 text-white font-bold flex items-center justify-center text-sm">
              2
            </div>
            <div>
              <h4 className="font-semibold text-lg text-white">Reciprocal Discovery</h4>
              <p className="mt-1 text-sm text-slate-400 leading-relaxed">
                Find peers who want what you offer and offer what you want. No one-sided tutoring.
              </p>
            </div>
          </div>

          <div className="flex gap-4">
            <div className="shrink-0 w-8 h-8 rounded-full bg-brand-500 text-white font-bold flex items-center justify-center text-sm">
              3
            </div>
            <div>
              <h4 className="font-semibold text-lg text-white">Request & Confirm</h4>
              <p className="mt-1 text-sm text-slate-400 leading-relaxed">
                Submit a Swap Request. The recipient accepts or rejects. Track all active exchanges in one place.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* Values & Safety Section */}
      <section className="max-w-5xl mx-auto px-4 text-center">
        <h2 className="text-2xl sm:text-3xl font-bold text-slate-900">
          Designed for Genuine Collaboration
        </h2>
        <div className="mt-10 grid grid-cols-1 sm:grid-cols-3 gap-8">
          <div className="flex flex-col items-center text-center">
            <div className="w-12 h-12 rounded-2xl bg-brand-100 text-brand-700 flex items-center justify-center mb-3">
              <Zap className="w-6 h-6" />
            </div>
            <h4 className="font-bold text-slate-900">Zero Financial Barrier</h4>
            <p className="mt-1 text-sm text-slate-600">
              Knowledge is the only currency. Everyone has something valuable to teach.
            </p>
          </div>

          <div className="flex flex-col items-center text-center">
            <div className="w-12 h-12 rounded-2xl bg-brand-100 text-brand-700 flex items-center justify-center mb-3">
              <ShieldCheck className="w-6 h-6" />
            </div>
            <h4 className="font-bold text-slate-900">Strict Ownership Rules</h4>
            <p className="mt-1 text-sm text-slate-600">
              Swaps only initiate when both sides legitimately hold the corresponding skills in their profile.
            </p>
          </div>

          <div className="flex flex-col items-center text-center">
            <div className="w-12 h-12 rounded-2xl bg-brand-100 text-brand-700 flex items-center justify-center mb-3">
              <Users className="w-6 h-6" />
            </div>
            <h4 className="font-bold text-slate-900">Community Driven</h4>
            <p className="mt-1 text-sm text-slate-600">
              Build lasting peer connections and mentor networks across technology, arts, and languages.
            </p>
          </div>
        </div>
      </section>
    </div>
  );
};

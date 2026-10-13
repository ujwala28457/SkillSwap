import React from 'react';
import { Repeat } from 'lucide-react';

export const Footer: React.FC = () => {
  return (
    <footer className="bg-white border-t border-slate-200 mt-auto py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex flex-col sm:flex-row items-center justify-between gap-4">
        <div className="flex items-center gap-2">
          <div className="w-6 h-6 rounded-lg bg-brand-600 flex items-center justify-center text-white text-xs">
            <Repeat className="w-3.5 h-3.5" />
          </div>
          <span className="font-bold text-slate-800 tracking-tight text-sm">SkillSwap</span>
          <span className="text-slate-400 text-xs">— Peer-to-Peer Skill Exchange Network</span>
        </div>
        <p className="text-xs text-slate-400">
          Built with Spring Boot, React, TypeScript & Tailwind CSS.
        </p>
      </div>
    </footer>
  );
};

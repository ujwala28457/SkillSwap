import React, { useEffect, useState } from 'react';
import { api } from '../services/api';
import { UserSkillResponse, ProficiencyLevel, SkillType, ApiError } from '../types/api';
import { ProficiencyBadge } from '../components/Badge';
import { Alert } from '../components/Alert';
import { Modal } from '../components/Modal';
import {
  BookOpen,
  GraduationCap,
  Plus,
  Trash2,
  Loader2,
  CheckCircle2,
} from 'lucide-react';

export const MySkillsPage: React.FC = () => {
  const [activeTab, setActiveTab] = useState<SkillType>('TEACH');
  const [offeredSkills, setOfferedSkills] = useState<UserSkillResponse[]>([]);
  const [learningSkills, setLearningSkills] = useState<UserSkillResponse[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [actionError, setActionError] = useState<ApiError | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  // Modal State
  const [isAddModalOpen, setIsAddModalOpen] = useState(false);
  const [modalSkillType, setModalSkillType] = useState<SkillType>('TEACH');
  const [skillName, setSkillName] = useState('');
  const [proficiencyLevel, setProficiencyLevel] = useState<ProficiencyLevel>('INTERMEDIATE');
  const [description, setDescription] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [deletingId, setDeletingId] = useState<number | null>(null);

  const fetchSkills = async () => {
    try {
      const [offered, learning] = await Promise.all([
        api.getMyOfferedSkills(),
        api.getMyLearningSkills(),
      ]);
      setOfferedSkills(offered);
      setLearningSkills(learning);
    } catch (err: any) {
      setActionError(err as ApiError);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchSkills();
  }, []);

  const openAddModal = (type: SkillType) => {
    setModalSkillType(type);
    setSkillName('');
    setProficiencyLevel('INTERMEDIATE');
    setDescription('');
    setActionError(null);
    setSuccessMessage(null);
    setIsAddModalOpen(true);
  };

  const handleAddSkill = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!skillName.trim()) {
      setActionError({
        status: 400,
        error: 'Bad Request',
        message: 'Please provide a skill name.',
      });
      return;
    }

    setSubmitting(true);
    setActionError(null);

    try {
      if (modalSkillType === 'TEACH') {
        await api.addMyOfferedSkill({
          skillName: skillName.trim(),
          proficiencyLevel,
          description: description.trim() || undefined,
        });
        setSuccessMessage(`Skill "${skillName.trim()}" added to your offered skills!`);
      } else {
        await api.addMyLearningSkill({
          skillName: skillName.trim(),
          proficiencyLevel,
          description: description.trim() || undefined,
        });
        setSuccessMessage(`Skill "${skillName.trim()}" added to your learning goals!`);
      }

      setIsAddModalOpen(false);
      await fetchSkills();
    } catch (err: any) {
      setActionError(err as ApiError);
    } finally {
      setSubmitting(false);
    }
  };

  const handleDeleteSkill = async (skillId: number, type: SkillType) => {
    if (!window.confirm('Are you sure you want to remove this skill association?')) return;

    setDeletingId(skillId);
    setActionError(null);
    setSuccessMessage(null);

    try {
      if (type === 'TEACH') {
        await api.removeMyOfferedSkill(skillId);
        setSuccessMessage('Offered skill removed successfully.');
      } else {
        await api.removeMyLearningSkill(skillId);
        setSuccessMessage('Learning goal removed successfully.');
      }
      await fetchSkills();
    } catch (err: any) {
      setActionError(err as ApiError);
    } finally {
      setDeletingId(null);
    }
  };

  if (loading) {
    return (
      <div className="min-h-[60vh] flex flex-col items-center justify-center gap-3">
        <Loader2 className="w-8 h-8 text-brand-600 animate-spin" />
        <p className="text-sm text-slate-500 font-medium">Loading your skills portfolio...</p>
      </div>
    );
  }

  const currentSkills = activeTab === 'TEACH' ? offeredSkills : learningSkills;

  return (
    <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 pb-6 border-b border-slate-200">
        <div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
            My Skills Portfolio
          </h1>
          <p className="mt-1 text-sm text-slate-500">
            Define what you can teach peers and what skills you wish to learn
          </p>
        </div>

        <button
          onClick={() => openAddModal(activeTab)}
          className="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl text-sm font-semibold text-white bg-brand-600 hover:bg-brand-700 shadow-md shadow-brand-600/25 transition-all"
        >
          <Plus className="w-4 h-4" />
          {activeTab === 'TEACH' ? 'Add Skill to Teach' : 'Add Skill to Learn'}
        </button>
      </div>

      {/* Global Alerts */}
      {actionError && (
        <Alert
          type="error"
          message={actionError.message}
          validationErrors={actionError.validationErrors}
          onClose={() => setActionError(null)}
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
            setActiveTab('TEACH');
            setActionError(null);
          }}
          className={`pb-3 text-sm font-bold flex items-center gap-2 border-b-2 transition-all ${
            activeTab === 'TEACH'
              ? 'border-brand-600 text-brand-600'
              : 'border-transparent text-slate-500 hover:text-slate-800'
          }`}
        >
          <BookOpen className="w-4 h-4" />
          Skills I Can Teach ({offeredSkills.length})
        </button>
        <button
          onClick={() => {
            setActiveTab('LEARN');
            setActionError(null);
          }}
          className={`pb-3 text-sm font-bold flex items-center gap-2 border-b-2 transition-all ${
            activeTab === 'LEARN'
              ? 'border-indigo-600 text-indigo-600'
              : 'border-transparent text-slate-500 hover:text-slate-800'
          }`}
        >
          <GraduationCap className="w-4 h-4" />
          Skills I Want to Learn ({learningSkills.length})
        </button>
      </div>

      {/* Skills Card Grid */}
      {currentSkills.length === 0 ? (
        <div className="text-center py-16 bg-white rounded-2xl border border-dashed border-slate-300 p-8">
          <div className="mx-auto w-12 h-12 rounded-2xl bg-slate-100 flex items-center justify-center text-slate-400 mb-3">
            {activeTab === 'TEACH' ? <BookOpen className="w-6 h-6" /> : <GraduationCap className="w-6 h-6" />}
          </div>
          <h3 className="text-base font-bold text-slate-800">
            {activeTab === 'TEACH'
              ? 'No teaching skills registered yet'
              : 'No learning goals specified yet'}
          </h3>
          <p className="mt-1 text-sm text-slate-500 max-w-sm mx-auto">
            {activeTab === 'TEACH'
              ? 'Add skills you are proficient in so other learners can discover you.'
              : 'Add skills you want to learn so our reciprocal matchmaker can pair you with peers.'}
          </p>
          <button
            onClick={() => openAddModal(activeTab)}
            className="mt-5 inline-flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-semibold text-brand-700 bg-brand-50 hover:bg-brand-100 border border-brand-200 transition-colors"
          >
            <Plus className="w-3.5 h-3.5" />
            Add {activeTab === 'TEACH' ? 'a teaching skill' : 'a learning goal'}
          </button>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {currentSkills.map((userSkill) => (
            <div
              key={userSkill.id}
              className="bg-white p-5 rounded-2xl border border-slate-200/80 shadow-xs hover:shadow-md transition-shadow flex flex-col justify-between"
            >
              <div>
                <div className="flex items-start justify-between gap-3">
                  <h3 className="text-base font-bold text-slate-900 leading-snug">
                    {userSkill.skillName}
                  </h3>
                  <ProficiencyBadge level={userSkill.proficiencyLevel} />
                </div>

                {userSkill.skillDescription && (
                  <p className="mt-1.5 text-xs text-slate-400 line-clamp-2">
                    {userSkill.skillDescription}
                  </p>
                )}

                {userSkill.description && (
                  <div className="mt-3 p-2.5 rounded-lg bg-slate-50 text-xs text-slate-600 border border-slate-100 italic">
                    "{userSkill.description}"
                  </div>
                )}
              </div>

              <div className="mt-5 pt-3 border-t border-slate-100 flex items-center justify-between text-xs text-slate-400">
                <span>Added {new Date(userSkill.createdAt).toLocaleDateString()}</span>
                <button
                  onClick={() => handleDeleteSkill(userSkill.skillId, userSkill.skillType)}
                  disabled={deletingId === userSkill.skillId}
                  className="p-1.5 rounded-lg text-slate-400 hover:text-rose-600 hover:bg-rose-50 transition-colors"
                  title="Remove skill"
                >
                  {deletingId === userSkill.skillId ? (
                    <Loader2 className="w-4 h-4 animate-spin text-rose-500" />
                  ) : (
                    <Trash2 className="w-4 h-4" />
                  )}
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Add Skill Modal */}
      <Modal
        isOpen={isAddModalOpen}
        onClose={() => setIsAddModalOpen(false)}
        title={modalSkillType === 'TEACH' ? 'Add Skill You Can Teach' : 'Add Skill You Want to Learn'}
      >
        <form onSubmit={handleAddSkill} className="space-y-4">
          <div>
            <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-2">
              Skill Name *
            </label>
            <input
              type="text"
              required
              value={skillName}
              onChange={(e) => setSkillName(e.target.value)}
              placeholder="e.g. Python, Graphic Design, Spanish"
              className="w-full px-4 py-2.5 rounded-xl border border-slate-200 bg-slate-50/50 text-slate-900 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500/20 focus:border-brand-500"
            />
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-2">
              Proficiency Level *
            </label>
            <select
              value={proficiencyLevel}
              onChange={(e) => setProficiencyLevel(e.target.value as ProficiencyLevel)}
              className="w-full px-4 py-2.5 rounded-xl border border-slate-200 bg-slate-50/50 text-slate-900 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500/20 focus:border-brand-500"
            >
              <option value="BEGINNER">Beginner (Foundational understanding)</option>
              <option value="INTERMEDIATE">Intermediate (Practical project experience)</option>
              <option value="ADVANCED">Advanced (Deep expertise)</option>
              <option value="EXPERT">Expert (Industry authority / Master)</option>
            </select>
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-2">
              Personal Note / What You Expect (Optional)
            </label>
            <textarea
              rows={3}
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder={
                modalSkillType === 'TEACH'
                  ? 'e.g. Can teach React fundamentals and state management'
                  : 'e.g. Want to learn conversational speaking for travel'
              }
              className="w-full px-4 py-2.5 rounded-xl border border-slate-200 bg-slate-50/50 text-slate-900 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500/20 focus:border-brand-500"
            />
          </div>

          <div className="flex items-center justify-end gap-3 pt-4 border-t border-slate-100">
            <button
              type="button"
              onClick={() => setIsAddModalOpen(false)}
              className="px-4 py-2 rounded-xl text-sm font-medium text-slate-600 hover:bg-slate-100 transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={submitting}
              className="inline-flex items-center gap-2 px-5 py-2 rounded-xl text-sm font-semibold text-white bg-brand-600 hover:bg-brand-700 disabled:opacity-50 transition-all shadow-sm"
            >
              {submitting ? (
                <>
                  <Loader2 className="w-4 h-4 animate-spin" />
                  Saving...
                </>
              ) : (
                <>
                  <CheckCircle2 className="w-4 h-4" />
                  Save Skill
                </>
              )}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

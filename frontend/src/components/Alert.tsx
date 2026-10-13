import React from 'react';
import { AlertCircle, CheckCircle, Info, AlertTriangle, X } from 'lucide-react';

interface AlertProps {
  type: 'error' | 'success' | 'warning' | 'info';
  message: string;
  validationErrors?: Record<string, string>;
  onClose?: () => void;
  className?: string;
}

export const Alert: React.FC<AlertProps> = ({
  type,
  message,
  validationErrors,
  onClose,
  className = '',
}) => {
  const styles = {
    error: {
      bg: 'bg-rose-50 border-rose-200 text-rose-800',
      icon: <AlertCircle className="w-5 h-5 text-rose-500 shrink-0 mt-0.5" />,
    },
    success: {
      bg: 'bg-emerald-50 border-emerald-200 text-emerald-800',
      icon: <CheckCircle className="w-5 h-5 text-emerald-500 shrink-0 mt-0.5" />,
    },
    warning: {
      bg: 'bg-amber-50 border-amber-200 text-amber-800',
      icon: <AlertTriangle className="w-5 h-5 text-amber-500 shrink-0 mt-0.5" />,
    },
    info: {
      bg: 'bg-sky-50 border-sky-200 text-sky-800',
      icon: <Info className="w-5 h-5 text-sky-500 shrink-0 mt-0.5" />,
    },
  }[type];

  return (
    <div className={`p-4 rounded-xl border flex items-start gap-3 ${styles.bg} ${className}`} role="alert">
      {styles.icon}
      <div className="flex-1 text-sm">
        <p className="font-medium leading-relaxed">{message}</p>
        {validationErrors && Object.keys(validationErrors).length > 0 && (
          <ul className="mt-2 list-disc list-inside space-y-1 text-xs opacity-90">
            {Object.entries(validationErrors).map(([field, error]) => (
              <li key={field}>
                <span className="font-semibold capitalize">{field}:</span> {error}
              </li>
            ))}
          </ul>
        )}
      </div>
      {onClose && (
        <button
          onClick={onClose}
          className="text-slate-400 hover:text-slate-600 transition-colors p-1"
          aria-label="Dismiss alert"
        >
          <X className="w-4 h-4" />
        </button>
      )}
    </div>
  );
};

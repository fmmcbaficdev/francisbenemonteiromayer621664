// ==========================================
// LOADING COMPONENT
// ==========================================

interface LoadingProps {
  size?: 'sm' | 'md' | 'lg';
  fullScreen?: boolean;
  message?: string;
}

export function Loading({ 
  size = 'md', 
  fullScreen = false, 
  message 
}: LoadingProps) {
  const sizeClasses = {
    sm: 'w-6 h-6 border-2',
    md: 'w-10 h-10 border-3',
    lg: 'w-16 h-16 border-4',
  };

  const spinner = (
    <div className="flex flex-col items-center justify-center gap-3">
      <div 
        className={`
          ${sizeClasses[size]} 
          rounded-full 
          border-gray-200 
          border-t-primary-600 
          animate-spin
        `}
      />
      {message && (
        <p className="text-gray-600 text-sm">{message}</p>
      )}
    </div>
  );

  if (fullScreen) {
    return (
      <div className="fixed inset-0 bg-white/80 backdrop-blur-sm flex items-center justify-center z-50">
        {spinner}
      </div>
    );
  }

  return spinner;
}

export default Loading;
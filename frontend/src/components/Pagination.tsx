// ==========================================
// PAGINATION COMPONENT
// ==========================================

import React from 'react';
import { ChevronLeft, ChevronRight } from 'lucide-react';

interface PaginationProps {
  currentPage: number;
  totalPages: number;
  onPageChange: (page: number) => void;
  totalElements?: number;
  pageSize?: number;
}

export function Pagination({
  currentPage,
  totalPages,
  onPageChange,
  totalElements,
  pageSize,
}: PaginationProps) {
  if (totalPages <= 1) return null;

  const pages: (number | string)[] = [];
  const maxVisiblePages = 5;

  // Calcular páginas visíveis
  if (totalPages <= maxVisiblePages) {
    for (let i = 0; i < totalPages; i++) {
      pages.push(i);
    }
  } else {
    // Sempre mostrar primeira página
    pages.push(0);

    if (currentPage > 2) {
      pages.push('...');
    }

    // Páginas ao redor da atual
    for (let i = Math.max(1, currentPage - 1); i <= Math.min(totalPages - 2, currentPage + 1); i++) {
      if (!pages.includes(i)) {
        pages.push(i);
      }
    }

    if (currentPage < totalPages - 3) {
      pages.push('...');
    }

    // Sempre mostrar última página
    if (!pages.includes(totalPages - 1)) {
      pages.push(totalPages - 1);
    }
  }

  return (
    <div className="flex flex-col sm:flex-row items-center justify-between gap-4 mt-6">
      {/* Info */}
      {totalElements !== undefined && pageSize !== undefined && (
        <p className="text-sm text-gray-600">
          Mostrando {currentPage * pageSize + 1} - {Math.min((currentPage + 1) * pageSize, totalElements)} de {totalElements} resultados
        </p>
      )}

      {/* Navigation */}
      <div className="flex items-center gap-1">
        {/* Previous */}
        <button
          onClick={() => onPageChange(currentPage - 1)}
          disabled={currentPage === 0}
          className="p-2 rounded-lg border border-gray-300 text-gray-600 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
        >
          <ChevronLeft className="w-5 h-5" />
        </button>

        {/* Pages */}
        {pages.map((page, index) => (
          <React.Fragment key={index}>
            {page === '...' ? (
              <span className="px-3 py-2 text-gray-400">...</span>
            ) : (
              <button
                onClick={() => onPageChange(page as number)}
                className={`
                  min-w-[40px] px-3 py-2 rounded-lg font-medium transition-colors
                  ${currentPage === page
                    ? 'bg-primary-600 text-white'
                    : 'border border-gray-300 text-gray-600 hover:bg-gray-50'
                  }
                `}
              >
                {(page as number) + 1}
              </button>
            )}
          </React.Fragment>
        ))}

        {/* Next */}
        <button
          onClick={() => onPageChange(currentPage + 1)}
          disabled={currentPage === totalPages - 1}
          className="p-2 rounded-lg border border-gray-300 text-gray-600 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
        >
          <ChevronRight className="w-5 h-5" />
        </button>
      </div>
    </div>
  );
}

export default Pagination;
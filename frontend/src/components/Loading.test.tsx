import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { Loading } from './Loading';

describe('Loading', () => {
  it('renderiza a mensagem quando informada', () => {
    render(<Loading message="Carregando dados..." />);
    expect(screen.getByText('Carregando dados...')).toBeInTheDocument();
  });

  it('renderiza sem mensagem quando message não é passado', () => {
    const { container } = render(<Loading />);
    expect(container.querySelector('.spinner')).toBeInTheDocument();
  });

  it('aplica classe fullScreen quando fullScreen é true', () => {
    const { container } = render(<Loading fullScreen message="Aguarde" />);
    const wrapper = container.firstChild as HTMLElement;
    expect(wrapper).toHaveClass('fixed');
    expect(screen.getByText('Aguarde')).toBeInTheDocument();
  });
});

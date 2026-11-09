import { afterEach, describe, expect, it, vi } from 'vitest';

import { renovar } from './autenticacao';
import { request } from './cliente';

vi.mock('./cliente', () => ({ request: vi.fn() }));

describe('renovar sessão', () => {
  afterEach(() => vi.resetAllMocks());

  it('compartilha a chamada pendente e permite uma renovação posterior', async () => {
    const chamada = vi.mocked(request);
    chamada.mockResolvedValueOnce({ accessToken: 'primeiro' });
    chamada.mockResolvedValueOnce({ accessToken: 'segundo' });

    const primeira = renovar();
    const simultanea = renovar();

    expect(primeira).toBe(simultanea);
    await expect(primeira).resolves.toEqual({ accessToken: 'primeiro' });
    expect(chamada).toHaveBeenCalledTimes(1);

    await expect(renovar()).resolves.toEqual({ accessToken: 'segundo' });
    expect(chamada).toHaveBeenCalledTimes(2);
  });
});

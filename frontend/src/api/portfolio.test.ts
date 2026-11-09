import { afterEach, expect, it, vi } from 'vitest';
import { request } from './cliente';
import { enviarMidiaPortfolio, listarPortfolios } from './portfolio';

vi.mock('./cliente', () => ({ request: vi.fn() }));
afterEach(() => {
  vi.mocked(request).mockReset();
  vi.unstubAllGlobals();
});

it('preserva a cidade ao paginar a vitrine de profissionais', async () => {
  vi.mocked(request).mockResolvedValueOnce({
    conteudo: [], pagina: 1, tamanho: 20, totalElementos: 0, totalPaginas: 0,
  });
  await listarPortfolios(1, 20, 'Foz do Iguaçu');
  const [path] = vi.mocked(request).mock.lastCall!;
  const url = new URL(path, 'http://teste.local');
  expect(url.searchParams.get('pagina')).toBe('1');
  expect(url.searchParams.get('cidade')).toBe('Foz do Iguaçu');
});

it('envia um vídeo na URL assinada e confirma com a chave e MIME', async () => {
  const url = 'http://storage.teste.local/arquivo';
  const chave = 'arquivo.mp4';
  const midia = { id: 'c4b1cfef-a803-44ba-b385-5f5b52d34298', url, contentType: 'video/mp4' };
  const video = new File(['arquivo'], 'arquivo.mp4', { type: 'video/mp4' });
  const put = vi.fn().mockResolvedValue({ ok: true });
  vi.stubGlobal('fetch', put);
  vi.mocked(request).mockResolvedValueOnce({ url, chave }).mockResolvedValueOnce(midia);
  await expect(enviarMidiaPortfolio(3, video, 'token')).resolves.toEqual(midia);
  expect(put).toHaveBeenCalledWith(url, {
    method: 'PUT', headers: { 'Content-Type': 'video/mp4' }, body: video,
  });
  expect(request).toHaveBeenLastCalledWith(
    '/portfolio/3/midias/confirmar',
    { method: 'POST', body: JSON.stringify({ chave, contentType: 'video/mp4' }) },
    'token',
  );
});

it('não confirma uma imagem quando o armazenamento falha', async () => {
  const imagem = new File(['arquivo'], 'foto.png', { type: 'image/png' });
  vi.stubGlobal('fetch', vi.fn().mockResolvedValue({ ok: false }));
  vi.mocked(request).mockResolvedValueOnce({ url: 'http://storage.teste.local/foto.png', chave: 'foto.png' });
  await expect(enviarMidiaPortfolio(3, imagem, 'token')).rejects.toThrow('armazenamento');
  expect(request).toHaveBeenCalledTimes(1);
});

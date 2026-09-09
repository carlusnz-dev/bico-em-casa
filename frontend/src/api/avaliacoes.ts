import { apiFetch } from './client';

export async function criarAvaliacao(contratacaoId, dados) {
    return apiFetch(
        `/avaliacoes/${contratacaoId}`,
        {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(dados),
        }
    )
}
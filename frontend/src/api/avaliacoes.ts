import { apiFetch } from './client';

interface DadosAvaliacao {
    nota: number;
    comentario: string;
}

export async function criarAvaliacao(contratacaoId:string, dados: DadosAvaliacao) {
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

export async function buscarAvaliacao(avaliadoId: string) {
    return apiFetch(
        `/avaliacoes/avaliado/${avaliadoId}`,
        {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            }
        }
    );
}

export async function buscarAvaliacoesAutor(autorId: string){
    return apiFetch(
        `/avaliacoes/autor/${autorId}`,
        {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        }
    );
}

export async function alterarAvaliacao(id: string,dados: DadosAvaliacao) {
    return apiFetch(
        `/avaliacoes/${id}`,
        {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(dados),
        }
    );
}

export async function deletarAvaliacao(id:string){
    return apiFetch(
        `/avaliacoes/${id}`,
        {
            method: 'DELETE',
            headers: {}
        }
    );
}
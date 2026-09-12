'use client'

import {buscarAvaliacao, buscarAvaliacoesAutor} from "../../../../api/avaliacoes";
import {useState, useEffect} from 'react';
import './MostrarAvaliacoes.module.css'

interface Avaliacao {
    id: string;
    nota: number;
    comentario: string;
}


export default function MostrarAvaliacoes({ avaliadoId } : { avaliadoId: string }) {
    const [avaliacoes, setAvaliacoes] = useState<Avaliacao[]>([]);
    const [existe, setExiste] = useState(true);
    const [nota, setNota] = useState(0);
    const [comentario, setComentario] = useState("");

    useEffect(() => {
        const fetchAvaliacoes = async () => {
            try {
                const json = await buscarAvaliacoesAutor(avaliadoId);
                setAvaliacoes(json);
            } catch (error) {
                setExiste(false);
            }
        };

        fetchAvaliacoes();
    }, [avaliadoId]);

    if (avaliacoes.length > 0) {
        return (
            <div>
                {avaliacoes.map((avaliacao) => (
                    <div key={avaliacao.id}>
                        <p>Avaliação</p>
                        <div>
                            <p>Nota: </p>
                            {[1,2,3,4,5].map((estrela) =>(
                                <span key={estrela}>
                                    {avaliacao.nota >= estrela ? '★': '☆'}
                                </span>
                            ))}
                        </div>
                        <p>Comentário: {avaliacao.comentario}</p>
                    </div>
                ))}
            </div>
        );
    } else {
        return (
            <div>
                <h3>Nenhuma avaliação encontrada!</h3>
            </div>
        );
    }
}
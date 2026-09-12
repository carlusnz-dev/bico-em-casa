'use client'

import {alterarAvaliacao, buscarAvaliacoesAutor, deletarAvaliacao} from "@/api/avaliacoes";
import {useState, useEffect} from "react";
import './UsuarioAvaliacoes.module.css'

interface Avaliacao {
    id: string;
    nota: number;
    comentario: string;
}

export default function MostrarUsuarioAvaliacoes({autorId}: {autorId: string}) {
    const [avaliacoes, setAvaliacoes] = useState<Avaliacao[]>([]);
    const [existe, setExiste] = useState(true);
    const [editando, setEditando] = useState<Avaliacao | null>(null);
    const [nota, setNota] = useState(0);
    const [comentario, setComentario] = useState("");
    const[btnNota, setbtnNota] = useState(0);

    const handleEditar = (avaliacao: Avaliacao) => {

        setEditando(avaliacao);
        setNota(avaliacao.nota);
        setComentario(avaliacao.comentario);
    }

    const handleSalvar = async () => {
        // corrigir erro do intellij editando
        if (!editando) {
            return;
        }

        await alterarAvaliacao(editando.id, {
            nota : nota,
            comentario : comentario

        })

        setAvaliacoes(
            avaliacoes.map((avaliacao) => {
                if (avaliacao.id === editando.id) {
                    return  {
                        ...avaliacao,
                        nota : nota,
                        comentario : comentario,
                    };
                }

                return avaliacao;
            })
        )
        setEditando(null);
    }

    const handleDeletar = async (id: string) => {
        await deletarAvaliacao(id);



        setAvaliacoes(
            avaliacoes.filter((avaliacao) => {
                if (avaliacao.id !== id) {
                    return avaliacao;
                }
            })
        )
    }


    useEffect(() => {
        const fetchAvaliacoes = async () => {
            try {
                const json = await buscarAvaliacoesAutor(autorId);
                setAvaliacoes(json);
            } catch (error) {
                setExiste(false);
            }
        };

        fetchAvaliacoes();
    }, [autorId]);

    if (avaliacoes.length > 0) {
        return (
            <div>
                {avaliacoes.map((avaliacao) => (
                    <div key={avaliacao.id}>
                        {editando?.id === avaliacao.id ? (
                            <>
                                {/* edditando estrela */}
                            <div style={{ display: 'flex', gap: '4px', marginBottom: '12px' }}>
                                {[1, 2, 3, 4, 5].map((estrela) => (
                                    <button
                                        key={estrela}
                                        type="button"
                                        onClick={() => setNota(estrela)}
                                        onMouseEnter={() => setbtnNota(estrela)}
                                        onMouseLeave={() => setbtnNota(0)}
                                        style={{
                                            background: 'none',
                                            border: 'none',
                                            cursor: 'pointer',
                                            fontSize: '24px'
                                        }}
                                    >
                                        {(btnNota || nota) >= estrela ? '★' : '☆'}
                                    </button>
                                ))}
                            </div>
                                {/* editando comentario */}
                            <div>
                                <label htmlFor="comentario">Comentário:</label>
                                <br />
                                <textarea
                                    id="comentario"
                                    rows={4}
                                    value={comentario}
                                    onChange={(e) => setComentario(e.target.value)}
                                    placeholder="Deixe sua opinião"
                                />
                            </div>
                                {/* botões para salvar e cancelar */}
                                <button type={"button"}
                                    onClick={handleSalvar}
                                >Salvar alteração</button>
                                <button type={"button"}
                                    onClick={() => setEditando(null)}
                                >Cancelar</button>
                            </>
                        ) : (
                            // mostrando avaliação normal
                            <>
                            <p> Avaliação </p>

                            <div>
                                <p>Nota: </p>

                                {[1,2,3,4,5].map((estrela) =>(
                                    <span key={estrela}>
                                    {avaliacao.nota >= estrela ? '★': '☆'}
                                    </span>
                                ))}
                                <p>Comentário: {avaliacao.comentario}</p>

                                <button type="button"
                                        onClick={() =>    handleEditar(avaliacao)}>
                                    Alterar Avaliação</button>
                                <button type="button"
                                    onClick={() => handleDeletar(avaliacao.id)}
                                >Deletar Avaliação</button>

                            </div>

                            </>
                        )}

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

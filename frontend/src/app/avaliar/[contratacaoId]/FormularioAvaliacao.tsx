'use client';

import {criarAvaliacao} from "../../../api/avaliacoes";
import {useState} from 'react';

export default function FormularioAvaliacao({ contratacaoId }) {
    const [nota, setNota] = useState(0);
    const [btnNota, setbtnNota] = useState(0);
    const [comentario, setComentario] = useState('');
    const [enviado, setEnviado] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (nota === 0) {
            alert('Por favor, selecione pelo menos 1 estrela.');
            return;
        }

        await criarAvaliacao(contratacaoId, {
            nota: nota,
            comentario: comentario,
        });

        // enviar dados
        console.log({ contratacaoId, nota, comentario });
        setEnviado(true);
    };

    if (enviado) {
        return (
            <div>
                <h3>Avaliação enviada com sucesso!!</h3>
            </div>
        );
    }

    return (
        <form onSubmit={handleSubmit}>
            <h3>Como você avalia o serviço do profissional?</h3>

            {/*estrelas*/}
            <div>
                {[1, 2, 3, 4, 5].map((star) => (
                    <button
                        key={star}
                        type="button"
                        onClick={() => setNota(star)}
                        onMouseEnter={() => setbtnNota(star)}
                        onMouseLeave={() => setbtnNota(0)}
                        style={{
                            background: 'none',
                            border: 'none',
                            cursor: 'pointer',
                            fontSize: '24px'
                        }}
                    >
                        {(btnNota || nota) >= star ? '★' : '☆'}
                    </button>
                ))}
            </div>

            <br />

            {/*comentarios */}
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

            <br />
            {/*botão de envviar*/}
            <button type="submit">Enviar avaliação</button>
        </form>
    );
}
'use client';

import {criarAvaliacao} from "../../../api/avaliacoes";
import {useState} from 'react';

export default function FormularioAvaliacao({ contratacaoId }) {
    const [rating, setRating] = useState(0);
    const [hoverRating, setHoverRating] = useState(0);
    const [comentario, setComentario] = useState('');
    const [enviado, setEnviado] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (rating === 0) {
            alert('Por favor, selecione pelo menos 1 estrela.');
            return;
        }

        await criarAvaliacao(contratacaoId, {
            nota: rating,
            comentario: comentario,
        });

        // enviar dados
        console.log({ contratacaoId, rating, comentario });
        setEnviado(true);
    };

    if (enviado) {
        return (
            <div>
                <h3>Obrigado pela sua avaliação!</h3>
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
                        onClick={() => setRating(star)}
                        onMouseEnter={() => setHoverRating(star)}
                        onMouseLeave={() => setHoverRating(0)}
                        style={{
                            background: 'none',
                            border: 'none',
                            cursor: 'pointer',
                            fontSize: '24px'
                        }}
                    >
                        {(hoverRating || rating) >= star ? '★' : '☆'}
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
                    placeholder="Deixe sua opinião..."
                />
            </div>

            <br />

            <button type="submit">Enviar avaliação</button>
        </form>
    );
}
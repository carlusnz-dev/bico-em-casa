import FormularioAvaliacao from './FormularioAvaliacao';
import './FormularioAvaliacao.module.css'

export default async function AvaliarPag({params} : {
    params: Promise<{contratacaoId : string}>
}) {
    const dados = await params;

    return (
        <main>
            <h1>Avaliar Profissional</h1>

            <h3>
                Deixe sua avaliação sobre o serviço do profissional!

            </h3>

            <br />

            <FormularioAvaliacao contratacaoId={dados.contratacaoId} />
        </main>
    );
}
import FormularioAvaliacao from './FormularioAvaliacao';

export default async function AvaliarPag({params}) {
    const dados = await params;

    return (
        <main>
            <h1>Avaliar Profissional</h1>

            <p>
                Deixe sua avaliação sobre o serviço do profissional!

            </p>

            <br />

            <FormularioAvaliacao contratacaoId={dados.contratacaoId} />
        </main>
    );
}
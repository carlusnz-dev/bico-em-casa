import UsuarioAvaliacoes from './UsuarioAvaliacoes';
import './UsuarioAvaliacoes.module.css'

export default async function MostrarUsuarioAvaliacoes({params}: {
    params: Promise<{autorId : string}>
}) {
    const dados = await params;

    return (
        <main>
            <h1>Minhas avaliações</h1>


            <UsuarioAvaliacoes autorId={dados.autorId} />

        </main>
    )
}
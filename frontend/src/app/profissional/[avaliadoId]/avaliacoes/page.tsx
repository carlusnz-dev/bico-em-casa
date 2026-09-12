import MostrarAvaliacoes from './MostrarAvaliacoes';
import './MostrarAvaliacoes.module.css'

export default async function MostrarAvaliacoesPag({params}:{
    params: Promise<{avaliadoId : string}>
}){
    const dados = await params;

    return (
        <main>
            <h1>Avaliações do profissional</h1>


            <MostrarAvaliacoes avaliadoId={dados.avaliadoId} />
        </main>

    )

}
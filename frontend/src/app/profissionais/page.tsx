"use client";

import { useEffect, useState } from "react";
import PortfolioCard from "@/components/PortfolioCard";
import { apiFetch } from "@/api/client";

type Portfolio = {
    id: number;
    profissionalId: number;
    titulo: string;
    descricao: string;
};

export default function ProfissionaisPage() {
    const [portfolios, setPortfolios] = useState<Portfolio[]>([]);
    const [carregando, setCarregando] = useState(true);
    const [erro, setErro] = useState("");

    useEffect(() => {
        async function carregarPortfolios() {
            try {
                const dados = await apiFetch("/portfolios");

                setPortfolios(dados);
            } catch (error) {
                console.error(error);
                setErro("Não foi possível carregar os profissionais.");
            } finally {
                setCarregando(false);
            }
        }

        carregarPortfolios();
    }, []);

    if (carregando) {
        return (
            <main>
                <h1>Profissionais</h1>
                <p>Carregando profissionais...</p>
            </main>
        );
    }

    if (erro) {
        return (
            <main>
                <h1>Profissionais</h1>
                <p>{erro}</p>
            </main>
        );
    }

    return (
        <main>
            <h1>Profissionais</h1>

            <p>Encontre profissionais para realizar seus serviços.</p>

            <section>
                {portfolios.map((portfolio) => (
                    <PortfolioCard
                        key={portfolio.id}
                        titulo={portfolio.titulo}
                        descricao={portfolio.descricao}
                        profissionalId={portfolio.profissionalId}
                    />
                ))}
            </section>
        </main>
    );
}
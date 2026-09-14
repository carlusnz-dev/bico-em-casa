type PortfolioCardProps = {
    titulo: string;
    descricao: string;
    profissionalId: number;
};

export default function PortfolioCard({
                                          titulo,
                                          descricao,
                                          profissionalId,
                                      }: PortfolioCardProps) {
    return (
        <article
            style={{
                border: "1px solid #ddd",
                borderRadius: "12px",
                padding: "20px",
                marginBottom: "16px",
                maxWidth: "400px",
                boxShadow: "0 2px 8px rgba(0, 0, 0, 0.08)",
            }}
        >
            <h2>{titulo}</h2>

            <p>{descricao}</p>

            <p>
                <strong>Profissional:</strong> #{profissionalId}
            </p>

            <button
                style={{
                    padding: "10px 16px",
                    borderRadius: "8px",
                    border: "none",
                    cursor: "pointer",
                }}
            >
                Ver serviço
            </button>
        </article>
    );
}
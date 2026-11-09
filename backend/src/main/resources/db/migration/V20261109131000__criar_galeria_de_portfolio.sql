CREATE TABLE portfolio_midia (
    id UUID PRIMARY KEY,
    portfolio_id BIGINT NOT NULL REFERENCES portfolio(id) ON DELETE CASCADE,
    chave VARCHAR(120) NOT NULL UNIQUE,
    content_type VARCHAR(40) NOT NULL,
    criado_em TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_portfolio_midia_portfolio ON portfolio_midia(portfolio_id, criado_em);

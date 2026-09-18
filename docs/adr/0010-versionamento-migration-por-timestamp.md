# ADR-0010 — Versionamento de migration Flyway por timestamp

| Campo | Valor |
|---|---|
| **ADR** | `0010` |
| **Título** | Versionamento de migration Flyway por timestamp em vez de sequencial |
| **Autor** | Carlos Antunes |
| **Data** | `2026-09-01` |
| **Tópico** | Processo · Banco |
| **Status** | Aceito |
| **LLM utilizada** | `Claude Sonnet 5 (Claude Code)` |

---

## Contexto

O time trabalha em branches separadas por integrante, todas partindo de um ponto comum do
backend. O Flyway resolve a ordem de aplicação das migrations pelo número embutido no nome do
arquivo (`V<versão>__descricao.sql`) — não pela ordem de merge no Git. Com o padrão sequencial
(`V1`, `V2`, `V3`...), dois integrantes que criam a próxima migration em branches paralelas podem
escolher o mesmo número, ou números que não refletem a ordem real em que as mudanças vão ser
integradas. O Git não sinaliza esse conflito — são arquivos com nomes diferentes, então o merge
passa limpo; só o Flyway detecta o problema, e só quando alguém tenta aplicar as duas migrations
em sequência.

O Flyway agrava isso de propósito: uma migration já aplicada é imutável, e ele valida por
checksum do conteúdo do arquivo. Nesta mesma sessão, `./mvnw spring-boot:run` com o profile `dev`
falhou com exatamente esse sintoma — `Migration checksum mismatch for migration version 2` —
porque a `V2` já tinha sido aplicada localmente antes de ser corrigida (FK, cascade, nomenclatura)
em commits posteriores. Não foi um conflito de branch desta vez, mas ilustra ao vivo o mesmo
mecanismo que causa o problema entre branches: o Flyway não perdoa divergência entre o que foi
aplicado e o que está no arquivo.

## Decisão

Toda migration criada **a partir desta data** nasce com versão baseada em timestamp de criação,
no formato `V<yyyyMMddHHmmss>__descricao_em_snake_case.sql` — por exemplo,
`V20260901143000__criar_tabela_endereco_perfil.sql` — em vez do padrão sequencial `V1`, `V2`,
`V3`...

`V1__criar-tabela-usuarios.sql` e `V2__criar-tabelas-endereco-perfil.sql` **não são renomeadas**.
Elas já foram aplicadas em ambientes locais do time; renomear mudaria a versão que o Flyway usa
para conferência no `flyway_schema_history`, forçando `flyway repair` ou reset de banco em todo
ambiente que já rodou essas migrations. Ficam registradas aqui como exceção histórica ao padrão
novo.

## Alternativas Consideradas

| Opção | Prós | Contras | Por que foi recusada |
|---|---|---|---|
| A — mesma branch de fundação como base, coordenação manual (avisar o time antes de criar a próxima migration) | Simples, não muda nenhuma ferramenta | Não elimina a colisão, só reduz a chance dela; depende de disciplina humana constante, sem nada que force o alinhamento | A dependência de aviso manual entre pessoas é o tipo de coordenação que falha silenciosamente sob prazo |
| B — não criar migration em branch de feature; consolidar todas no merge | Elimina a colisão de versão por completo | Impede teste de integração com Testcontainers/PostgreSQL durante o desenvolvimento da feature (exigência do projeto); concentra a escrita de todas as migrations em uma pessoa só, na integração | Contradiz a Regra nº 3 do CLAUDE.md — migration é onde a equipe aprende banco; empurrar isso para uma pessoa no fim tira esse aprendizado dos demais |
| C — sequencial, reorganizar/renomear na integração com ajuda da LLM | Nenhuma mudança de ferramenta ou convenção | Exige um passo manual de renumeração a cada merge com mais de uma migration pendente; quem já aplicou a versão antiga localmente precisa rodar `repair`/reset — como aconteceu nesta sessão só por edição pós-aplicação, sem nem haver conflito de branch | O custo de coordenação é recorrente a cada ciclo de merge, e escala com o número de pessoas criando migration ao mesmo tempo |
| D — timestamp (`VyyyyMMddHHmmss`) | Colisão entre branches paralelas praticamente eliminada sem coordenação humana; ordem de aplicação = ordem cronológica de criação | Nome de arquivo mais longo; a versão deixa de indicar "a Nª migration do projeto" | — escolhida |

## Consequências

### Positivas

- Colisão de número de versão entre branches paralelas deixa de depender de coordenação manual
  entre os integrantes.
- A ordem de aplicação passa a ser a ordem cronológica de criação do arquivo, que já é a
  informação mais intuitiva de se raciocinar sobre.
- Reduz a necessidade de renumerar migrations na hora do merge.

### Negativas

- Nome de arquivo mais longo e menos fácil de memorizar de cabeça do que `V1`, `V2`, `V3`.
- A versão não corresponde mais à contagem de migrations do projeto; para saber quantas existem
  é preciso olhar `flyway info` ou o histórico, não o número do arquivo mais recente.

### Neutras

- `V1` e `V2` continuam sequenciais — convivem como exceção histórica registrada neste ADR; não
  há trabalho de migração retroativa.
- Migrations futuras seguem timestamp; nada impede reordenar manualmente em um caso pontual, mas
  deixa de ser a prática padrão.

## Impacto na Arquitetura

Esta decisão altera `docs/arquitetura-sistema.json`?

- [x] Sim — campos alterados: `database.migration_versioning` (novo), `database.migration_policy`,
  `backend.estrutura_pastas["src/main/resources"]["db/migration/"]`
- [ ] Não

## Referências

- [Documentação oficial do Flyway — Team-based development](https://documentation.red-gate.com/fd/team-based-development-184127470.html)
- Log de boot desta sessão: `Migration checksum mismatch for migration version 2` ao rodar
  `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev` contra o PostgreSQL local

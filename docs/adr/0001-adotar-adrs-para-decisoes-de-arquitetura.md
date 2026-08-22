# ADR-0001 — Adotar ADRs para registrar decisões de arquitetura

| Campo | Valor |
|---|---|
| **ADR** | `0001` |
| **Título** | Adotar ADRs para registrar decisões de arquitetura |
| **Autor** | Carlos Antunes |
| **Data** | 2026-08-22 |
| **Tópico** | Processo |
| **Status** | Aceito |
| **LLM utilizada** | `Claude Opus 5 (Claude Code)` |

---

## Contexto

O projeto está começando e já acumulou decisões estruturais na primeira sessão: provedor de
identidade, build tool, recorte de módulos, versões de framework. Nenhuma delas é óbvia a
partir do código, porque ainda não existe código.

O `arquitetura-sistema.json` registra **o que** foi decidido, mas não **por quê**. Sem esse
registro, o motivo de cada escolha vive apenas na memória de quem participou da conversa — e
some. O sintoma clássico aparece meses depois: alguém questiona uma escolha, ninguém lembra
das restrições que a produziram, e a discussão recomeça do zero.

O Bico em Casa é um projeto acadêmico (Experiência Criativa, PUCPR) com participação de LLMs
na tomada de decisão. Isso adiciona um requisito próprio: é preciso ser possível auditar
**qual decisão teve assistência de qual modelo**, tanto por honestidade acadêmica quanto para
calibrar o quanto confiar em cada registro.

## Decisão

Adotar **Architecture Decision Records** em `docs/adr/`, no formato definido por
`docs/adr/TEMPLATE.md`.

Todo ADR carrega um header em tabela com: número, título, autor, data, tópico, status e
**LLM utilizada**. O corpo segue Contexto → Decisão → Alternativas Consideradas →
Consequências → Impacto na Arquitetura → Referências.

Arquivos são nomeados `NNNN-titulo-em-kebab-case.md`, numerados sequencialmente a partir de
`0001`, e **nunca são apagados nem reescritos** após aceitos — a revisão vem por um novo ADR
que marca o anterior como *Substituído*.

## Alternativas Consideradas

| Opção | Prós | Contras | Por que foi recusada |
|---|---|---|---|
| Nenhum registro formal | Zero atrito | O porquê some junto com a memória de quem decidiu | É exatamente o problema que se quer resolver |
| Registrar tudo dentro do `arquitetura-sistema.json` | Um arquivo só | JSON não comporta narrativa; o arquivo viraria ilegível e o histórico se perderia em diffs gigantes | O JSON descreve o estado atual, não a trajetória |
| Wiki do GitHub | Edição fácil, não polui o repo | Sai do controle de versão do código; não aparece em code review; desincroniza | A decisão precisa ser revisada junto com o código que ela afeta |
| Issues do GitHub com label `decision` | Já integrado ao fluxo | Depende da plataforma; não sobrevive a migração; difícil de ler em sequência | Documentação de arquitetura deve viver no repositório |

## Consequências

### Positivas

- O porquê de cada decisão fica versionado, revisável em PR e legível em sequência
- Decisões rejeitadas ficam registradas, evitando rediscussão do zero
- A participação de LLMs vira rastreável e auditável, atendendo ao contexto acadêmico
- Novos integrantes leem a pasta em ordem e reconstroem o raciocínio do projeto

### Negativas

- Atrito adicional: toda decisão estrutural passa a exigir um documento
- Risco de ADRs de baixa qualidade, escritos por obrigação, com tabela de alternativas
  preenchida por teatro
- Mais um artefato que pode sair de sincronia com a realidade

### Neutras

- A pasta cresce indefinidamente por design — ADRs antigos são histórico, não lixo

## Impacto na Arquitetura

Esta decisão altera `docs/arquitetura-sistema.json`?

- [x] Sim — campos alterados: `documentation_governance.adr`
- [ ] Não

## Referências

- Michael Nygard, *Documenting Architecture Decisions* (2011)
- [adr.github.io](https://adr.github.io/)

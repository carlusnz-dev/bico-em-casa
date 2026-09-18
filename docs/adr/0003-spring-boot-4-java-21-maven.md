# ADR-0003 — Spring Boot 4.1 com Java 21 e Maven

| Campo | Valor |
|---|---|
| **ADR** | `0003` |
| **Título** | Spring Boot 4.1 com Java 21 e Maven |
| **Autor** | Carlos Antunes |
| **Data** | 2026-08-22 |
| **Tópico** | Backend |
| **Status** | Aceito |
| **LLM utilizada** | `Claude Opus 5 (Claude Code)` |

---

## Contexto

A versão 1.0.0 do `arquitetura-sistema.json` fixava **Spring Boot 3.3.x**. Duas coisas
estavam erradas nisso:

1. A linha **3.5.x encerrou o suporte OSS em 30/06/2026** (último patch: 3.5.16, em 25/06/2026).
   A 3.3.x já estava fora de suporte bem antes disso. Iniciar um projeto novo numa linha morta
   significa começar sem receber correção de segurança.
2. O mesmo arquivo listava o build tool como *"Gradle (Kotlin DSL) / Maven"* — uma ambiguidade
   que não é decisão. Alguém teria que escolher no momento de rodar `spring init`, e a escolha
   ficaria sem registro.

O Java 21 LTS foi definido pelo usuário na especificação da stack e não está em questão aqui.

## Decisão

**Spring Boot 4.1.x** (Spring Framework 7.0.x, Spring Security 7.1.x), rodando em **Java 21 LTS**,
com **Maven** como build tool.

O Spring Boot 4 tem baseline mínimo de Java 17, então o Java 21 é runtime plenamente suportado —
não há conflito entre a exigência do usuário e a linha de framework escolhida.

Ajustes de versão decorrentes: `springdoc-openapi` sobe para a linha **3.1.x** (a linha 2.x não
é compatível com Boot 4), Flyway para **13.3.x** e Testcontainers para **2.0.x**.

> [!NOTE]
> **Revisão de 2026-08-27 — a faixa do Flyway mudou.** Ao fundar o backend de fato, o
> desenvolvedor decidiu que **toda biblioteca gerenciada pelo BOM do Spring Boot herda a versão
> do BOM**, sem override no `pom.xml`. O BOM do Boot 4.1.1 gerencia o Flyway **12.4.0**, então é
> essa a versão vigente — não a 13.3.x prevista aqui. A 13.3.x existe e é mais nova; a escolha
> foi por não manter override de versão de uma biblioteca que o BOM já resolve, e assim não ter
> que revalidar a compatibilidade a cada bump do Boot.
>
> O restante desta decisão (Spring Boot 4.1, Java 21, Maven, springdoc 3.1.x, Testcontainers
> 2.0.x) continua valendo. A versão vigente está sempre em
> [`arquitetura-sistema.json`](../arquitetura-sistema.json), que é a fonte da verdade.
> Ver [o plano de fundação](../planos/2026-08-27-fundacao-backend-frontend-e-infra.md).

## Alternativas Consideradas

| Opção | Prós | Contras | Por que foi recusada |
|---|---|---|---|
| Manter Spring Boot 3.3.x | Nenhum | Fora de suporte OSS; sem correção de segurança | É o defeito que este ADR corrige |
| Spring Boot 3.5.x | Material e exemplos abundantes; ecossistema estável | EOL de suporte OSS em 30/06/2026 — já passou | Nascer sem suporte é dívida no dia zero |
| Spring Boot 4.1 com **Java 25 LTS** | LTS mais recente; ganhos de performance da JVM | Contraria o Java 21 definido pelo usuário; menos maduro no ferramental (Lombok, agents) | A stack foi especificada com Java 21 |
| **Gradle Kotlin DSL 9.7.x** | Build incremental mais rápido; tipado; melhor em multi-módulo | Curva maior para o time; menos material didático em português | O projeto é monolito modular de módulo único — o ganho do Gradle em multi-projeto não se aplica |

## Consequências

### Positivas

- Projeto nasce numa linha GA com suporte ativo e correções de segurança
- Spring Framework 7 e Security 7.1 trazem melhorias de configuração do Resource Server,
  que é exatamente o caminho usado no [ADR-0002](./0002-supabase-como-baas.md)
- Maven integra direto com o Spring Initializr, encurtando o bootstrap do projeto
- A ambiguidade *"Gradle / Maven"* deixa de existir

### Negativas

- Material da comunidade sobre Spring Boot 4 ainda é escasso comparado ao da linha 3.x —
  respostas de Stack Overflow e tutoriais frequentemente assumem 3.x, e a diferença de
  configuração entre Spring Security 6 e 7 é a fonte mais provável de confusão
- Bibliotecas de terceiros podem demorar a acompanhar o Framework 7
- `pom.xml` é mais verboso que `build.gradle.kts`

### Neutras

- Se o projeto crescer para multi-módulo Maven, a migração para Gradle vira um novo ADR

## Impacto na Arquitetura

Esta decisão altera `docs/arquitetura-sistema.json`?

- [x] Sim — campos alterados: `backend.framework`, `backend.spring_framework`,
  `backend.spring_security`, `backend.build_tool`, `backend.rationale`,
  `backend.dependencies.*`, `backend.testing_modules.containers_and_db_mock`
- [ ] Não

## Referências

- [Spring Boot — Support Policy](https://spring.io/projects/spring-boot#support)
- [endoflife.date/spring-boot](https://endoflife.date/spring-boot)
- [Spring Boot 4.0 Release Notes](https://github.com/spring-projects/spring-boot/wiki)

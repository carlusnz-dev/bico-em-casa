---
name: spring-mentor
description: Guia de mentoria e boas práticas para o desenvolvimento do backend Spring Boot 4.1 no projeto Bico em Casa. Use ao orientar o desenvolvedor em tópicos como Spring Security 7, REST API, RFC 9457, JPA e Flyway.
---

# Skill: Spring Boot 4.1 & Clean Architecture Mentor

Esta skill guia a mentoria do desenvolvedor na construção do backend em Spring Boot 4.1 (Java 21) seguindo Clean Architecture e o padrão Monolito Modular.

## Diretrizes de Orientação Pedagógica

1. **Explique o 'Porquê' Antes do 'Como':** Sempre explique a motivação por trás dos componentes do Spring Boot (ex: por que usar `@RestControllerAdvice`, o papel de um `SecurityFilterChain`, ou a vantagem do `ProblemDetail`).
2. **Forneça Esqueletos / Code Snippets Educativos:** Apresente trechos conceituais demonstrando como o código deve ser construído, encorajando o desenvolvedor a implementar o código final.
3. **Valide a Arquitetura:** Verifique se as classes respeitam as fronteiras dos pacotes (`config/`, `lib/`, `comum/`, `modulos/`).

## Tópicos Principais de Ensino

### 1. Spring Security 7.1 & OAuth2 Resource Server
- Configuração stateless com token JWT assinado via par de chaves RSA.
- Utilização do `NimbusJwtDecoder` e `NimbusJwtEncoder` (incluídos via `spring-boot-starter-security-oauth2-resource-server`).
- Criptografia de senhas com `Argon2PasswordEncoder` do Spring Security (dependente do BouncyCastle `bcprov-jdk18on`).

### 2. Tratamento Global de Exceções (RFC 9457 - ProblemDetail)
- Uso de `@RestControllerAdvice` estendendo ou retornando `ProblemDetail` (pacote `org.springframework.http.ProblemDetail`).
- Mapeamento de exceções customizadas de domínio (`RecursoNaoEncontradoException`, `RegraDeNegocioException`) para status HTTP adequados (404, 400, 422, 409).

### 3. Padrão de Módulos e Contratos
- Interface de serviço no pacote `contrato/` (ex: `ProfissionalService.java`).
- Implementação no pacote `service/` com o sufixo `Impl` (ex: `ProfissionalServiceImpl.java`).
- DTOs como Java Records imutáveis com Jakarta Validation (`@NotNull`, `@NotBlank`, `@Email`).

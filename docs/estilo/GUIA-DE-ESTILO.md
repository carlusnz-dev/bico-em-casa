# Guia de Estilo — Documentos da Equipe (Bico em Casa)

| Campo | Valor |
|---|---|
| **Versão** | 1.0.0 |
| **Criado em** | 2026-08-22 |
| **Origem da paleta** | `docs/PBB_bico-em-casa.png` (canvas PBB), amostrado por pixel |

Este guia documenta por escrito o estilo visual usado nos documentos `.docx` da equipe, para que
seja reaproveitável mesmo sem abrir o `reference-doc.docx` binário. O template pronto vive em
`docs/estilo/reference-doc.docx` — gerar um novo documento é rodar:

```bash
pandoc conteudo.md --reference-doc=docs/estilo/reference-doc.docx --resource-path=. -o saida.docx
```

## Paleta de cores

Extraída por amostragem de pixel do canvas PBB, não estimada visualmente.

| Papel | Hex | Uso |
|---|---|---|
| Cinza-escuro (primário) | `#595959` | Faixa de capa, cabeçalho/rodapé, `Heading 1` |
| Laranja (destaque) | `#FBAB53` | Título na capa, `Heading 2`, realces, header de tabela |
| Âmbar (acento pontual) | `#FFC000` | Uso moderado — marcações de pendência/atenção |
| Cinza-claro | `#F2F2F2` | Zebra de tabela, fundo de citações |
| Cinza-médio | `#9B9B9B` | Bordas de tabela, linhas divisórias |
| Branco | `#FFFFFF` | Fundo de página |
| Preto | `#000000` | Texto de corpo |

## Tipografia

Arial (sans-serif, disponível em qualquer instalação de Word/LibreOffice sem precisar embutir
fonte — escolha deliberada para portabilidade).

| Elemento | Tamanho | Peso |
|---|---|---|
| Título (capa) | ~28pt | Bold, laranja |
| Heading 1 | ~20pt | Bold, cinza-escuro, régua laranja abaixo |
| Heading 2 | ~16pt | Bold, laranja |
| Heading 3 | ~13pt | Bold, cinza-escuro |
| Corpo | 11pt | Regular, preto |
| Legenda de figura | 9pt | Itálico, cinza-médio |

## Estrutura do documento

1. **Capa**: faixa cinza-escura no topo com "BICO EM CASA" em branco; título do documento em
   laranja bold abaixo; subtítulo "Documento acadêmico — PUCPR" em cinza; mini-tabela de
   metadados (criado em / alterado em / autor / versão da doc-fonte); quebra de página.
2. **Cabeçalho** (da página 2 em diante): faixa fina cinza-escura, "Bico em Casa" à esquerda em
   branco, nome da seção à direita, régua laranja fina abaixo.
3. **Rodapé**: régua laranja fina, texto do documento à esquerda, "Página X de Y" à direita.
4. **Tabelas**: header row `#595959` com texto branco bold; zebra `#F2F2F2`; bordas `#9B9B9B`
   finas.
5. **Diagramas mermaid**: usam o `mermaid-theme.json` deste mesmo guia (laranja/cinza-escuro/
   cinza-claro) para não destoar visualmente do resto do documento.

## Diagramas — tema mermaid padrão

```json
{
  "theme": "base",
  "themeVariables": {
    "primaryColor": "#FBAB53",
    "primaryTextColor": "#000000",
    "primaryBorderColor": "#595959",
    "lineColor": "#595959",
    "secondaryColor": "#F2F2F2",
    "tertiaryColor": "#FFFFFF",
    "fontFamily": "Arial"
  }
}
```

Renderizar com `npx @mermaid-js/mermaid-cli -i arquivo.mmd -o arquivo.png -c mermaid-theme.json
-b white -w 1600 -s 2`. Sempre PNG — nunca diagrama em caracteres de texto/ASCII.

## Metadados do arquivo

Todo `.docx` gerado por este pipeline recebe `core_properties.created`, `.modified`, `.author` e
`.title` setados via `python-docx` no pós-processamento — além da tabela de metadados visível na
última seção do documento.

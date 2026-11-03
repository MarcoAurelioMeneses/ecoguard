# EcoGuard — Design System v0.2

Plataforma de inteligência urbana de São Paulo: risco ambiental + saúde + social por distrito, com o AI City Analyst.
Estilo: minimalista, direto, produto de dados sério. Tema **escuro é o padrão**; o claro é opcional (mesmos tokens, só trocam os valores).
Todos os mockups usam **dados de exemplo** (marcados na tela com o chip "Dados de exemplo"). Os valores não são reais.

**v0.2 (revisão ui-ux-pro-max):** contraste, tamanhos mínimos, risco além da cor (padrões + formas + rótulo), foco/hover, navegação agrupada, estados de interface e tela 00 menos densa. Detalhes e origem de cada recomendação em `uiux-review/auditoria.md`.

Arquivos de apoio: `tokens.css` (variáveis CSS prontas), `html/*.html` (mockups em HTML/CSS real, base para o front), `src/ui.py` (CSS e ícones usados).

## 1. Cores

### Base
| Token | Escuro | Claro | Uso |
|---|---|---|---|
| `--bg` | `#0B1014` | `#F4F7F9` | fundo da página |
| `--surface` | `#121A21` | `#FFFFFF` | cards, sidebar, inputs |
| `--elev` | `#19232C` | `#EEF3F6` | trilhas de barra, tooltips, blocos internos |
| `--border` | `#243039` | `#D5DEE5` | bordas 1px de cards e divisores (decorativas) |
| `--border-strong` | `#5A6C7A` | `#7C8D9A` | **bordas de controles** (chips, botões secundários, campos): ≥ 3:1 |
| `--ring` | `#38BDF8` | `#076E94` | anel de foco (2px, offset 2px) |
| `--text` | `#E6EDF3` | `#0E1A24` | texto principal |
| `--muted` | `#8B9AA7` | `#556574` | rótulos, legendas, texto secundário |
| `--accent` | `#38BDF8` | `#076E94` | item ativo, links, ícones de KPI, seleção |
| `--accent-bg` | `rgba(56,189,248,.12)` | `rgba(7,110,148,.10)` | fundo de item ativo / chip selecionado |

### Escala de risco
Classes (score 0–100): **Baixo 0–29 · Moderado 30–54 · Alto 55–74 · Crítico 75–100.**
Nunca usar só a cor: cada classe tem **cor + padrão/forma + texto**.

| Classe | Padrão no mapa/legenda | Forma (badge, marcador) |
|---|---|---|
| Baixo | liso | círculo ● |
| Moderado | pontos | losango ◆ |
| Alto | diagonal | triângulo ▲ |
| Crítico | hachura cruzada | octógono ⬣ |

Os padrões usam `--pat` (escuro `rgba(5,8,10,.45)`, claro `rgba(14,26,36,.5)`); definições SVG em `PATDEFS` (`src/ui.py`).

| Classe | Preenchimento (mapa, barras, linhas) | Texto/borda do badge — escuro | Texto/borda do badge — claro |
|---|---|---|---|
| Baixo | `#3FB68B` | `#3FB68B` | `#1B7A58` |
| Moderado | `#F2C94C` | `#F2C94C` | `#80620A` |
| Alto | `#F2994A` | `#F2994A` | `#AE5208` |
| Crítico | `#E5484D` | `#FF6B6F` | `#C0272D` |
| Sem dado | `--nodata` `#1F2B35` | | `#E3EAEF` (claro) |

Contraste medido (WCAG, razão sobre `--surface`): texto 14,9:1 (escuro) / 17,6:1 (claro); muted 6,1:1 / 6,0:1; **accent 8,2:1 / 5,7:1** (sobre `--bg` claro 5,3:1; antes 4,3:1 com `#0A7EA8`, abaixo de 4,5:1);
texto de risco 6,3–11,1:1 (escuro) e 5,2–5,9:1 (claro); botão primário claro (branco sobre `#076E94`) 5,7:1; `--border-strong` 3,2:1 / 3,4:1 (`--border` decorativo tem 1,3:1 e só separa blocos).
Preenchimentos de mapa/barras são elementos gráficos: no escuro 4,5–11:1 sobre a superfície; **no claro ficam abaixo de 3:1 contra o branco** (baixo 2,5 · moderado 1,6 · alto 2,2 · crítico 3,9). Por isso os distritos têm contorno branco e padrão, e a classe sempre aparece também em texto; se o produto exigir 3:1 estrito no claro, escurecer os preenchimentos (opção documentada em `uiux-review/auditoria.md`).

## 2. Tipografia
- **Inter** (UI): 400 / 500 / 600. **JetBrains Mono** (números, datas, tags técnicas): 400 / 600, com `font-variant-numeric: tabular-nums`. Mantidas na revisão (o banco do ui-ux-pro-max não trouxe par verificado melhor para dados).
- Canvas 1920×1080. **Escala: 12 · 14 · 16 · 18 · 24 · 28 · 32 · 40 · 48 px**; corpo 16. Em produção, escalar com `rem` (1rem = 16px).
- **Mínimo 14px** em todo o conteúdo (rótulos de eixo, legendas, tabelas, tooltips). Exceção: 12px apenas em rótulo de grupo da navegação, contador de alertas e tag "IA" (caixa-alta, 600).

| Papel | Tamanho / peso | Fonte |
|---|---|---|
| Título de página `h1` | 28 / 600, tracking −0,02em | Inter |
| Subtítulo e breadcrumb | 14 / 400, `--muted` (links do breadcrumb em `--accent`) | Inter |
| Valor de KPI | 48 / 600, tracking −0,03em (+ unidade 16 / 500 `--muted`) | JetBrains Mono |
| Título de card (`h2`, antes `h3`) | 14 / 600, CAIXA ALTA, tracking .06em, `--muted` | Inter |
| Corpo / linhas de tabela | 16 / 400–500 (resposta do chat 18) | Inter |
| Cabeçalho de tabela | 14 / 600, CAIXA ALTA, `--muted` | Inter |
| Badge / chip | 14 / 600 | Inter |
| Números em tabela, datas, tags de ferramenta | 14–16 | JetBrains Mono |
| Rótulos de eixo e tooltips de gráfico | 14 | JetBrains Mono, `--muted` |

## 3. Espaçamento, forma, layout
- Ritmo de 4/8px. Gap entre cards **24px**; padding do conteúdo **28px 36px**; padding de card **24px** (KPI/compacto 18–20px). Alvo de toque/clique **≥ 44px** (nav, botões, controles do mapa).
- Raios: card **14px**, controles (seg, botão) **10px**, chips/badges **999px**, item de navegação 10px, barras 4px.
- Bordas **1px `--border`** (cards) e **`--border-strong`** (controles); sem sombras (exceto tooltip do mapa: `0 8px 24px rgba(0,0,0,.25)`).
- Ícones: traço de 1,75px, 24×24, cantos arredondados, `currentColor` (conjunto estilo Lucide).
- Shell: **sidebar 292px** (fundo `--surface`, borda direita) + **topbar 92px** (breadcrumb quando há hierarquia, título, subtítulo, chips à direita) + área de conteúdo.
- Largura mínima de referência: desktop 1280. Abaixo disso, a sidebar vira ícones (não desenhado nos mockups).

## 4. Componentes
Todos estão implementados em `html/*.html` / `src/ui.py`; use como referência de CSS.

- **Sidebar / navegação**: 6 itens em **3 grupos** (PRODUTO: O que é o EcoGuard · ANÁLISE: Visão geral, Mapa de risco, Analista IA, Alertas · TRANSPARÊNCIA: Fontes e metodologia); logo + "EcoGuard"; itens 44px com ícone 22px, 16/500, `--muted`; hover `--elev`; ativo = `--accent-bg` + texto `--accent` + barra de 4px à esquerda (não só cor). Contador de alertas com `aria-label`. Landmarks `nav`/`header`/`main` e link "pular para o conteúdo". Rodapé: "São Paulo · 96 distritos / Granularidade diária e semanal".
- **Topbar chips**: seletor de período (ícone calendário), **"Dados de exemplo"** (borda e texto `--t-moderado`; remover quando houver dados reais), **"Atualizado há 2 h"** (ponto verde `--baixo`).
- **Card**: `--surface`, 1px `--border`, raio 14.
- **KPI card**: rótulo com ícone `--accent` (15/500 muted), valor mono 46/600, unidade, badge de classe, sparkline à direita.
- **Badge de risco**: pílula 1px, ponto de 8px + texto da classe, cores `--t-*`.
- **Chip / filtro**: pílula 1px `--border`; selecionado = `--accent-bg` + borda e texto `--accent`.
- **Segmented control (camadas)**: Ambiental · Saúde · Social · Geral; borda 1px, raio 10; ativo `--accent-bg` / `--accent` 600.
- **Tabela**: cabeçalho caps muted; linhas com borda superior 1px; nome do distrito 600; score em mono 600; mini-barras de 8px (preenchimento = cor da classe, trilha `--elev`); variação semanal com ícone SVG de seta + sinal (`+6`), coluna ordenada marcada com seta e `aria-sort`.
- **Barra de score**: 8px, raio 4, trilha `--elev`.
- **Legenda**: amostra 18px **com o mesmo padrão do mapa** + rótulo + faixa em mono (`legend_items()`).
- **Mapa coroplético**: 96 polígonos preenchidos com a cor de classe; traço `--mapstroke` 0,5–1px; distrito em foco com traço branco (escuro) / `--text` (claro) e 2px; "sem dado" = `--nodata`. Tooltip: card `--elev` com nome, badge, score grande e A/S/S.
- **Linha do tempo semanal**: segmentos de 12 semanas, ativo `--accent`; botões ‹ › (com `aria-label`) e "Hoje".
- **Controles do mapa**: "Ver como tabela" (alternativa textual ao mapa), zoom + / −, todos com 44px de altura.
- **Breadcrumb** (detalhe do distrito): Mapa de risco › Zona › Distrito.
- **Botões**: `.btn.p` (primário), `.btn.s` (secundário, borda `--accent`), `.btn.dis` (45% de opacidade); `:hover` com brilho/fundo, `:focus-visible` com anel `--ring`.
- **Estados**: skeleton (`.sk`, sem spinner, para com `prefers-reduced-motion`), vazio (texto + ação), erro (causa + tentar novamente), sem dado (`--nodata` tracejado). Folha em `mockups/extras/estados-*.png`.
- **Gráfico de linha com faixas de risco**: `role="img"` + `aria-label`, eixos mono 14px, **rótulo de cada faixa** (Baixo/Moderado…) à direita, **tooltip** com data e valor, grade 1px `--border`, faixas de fundo com 6% de opacidade nas cores das classes, linha 2px, ponto final com marcador.
- **Sparkline**: 2px com preenchimento 12% abaixo da linha.
- **Alerta**: card com **borda esquerda 4px na cor da severidade** + badge com forma e texto, ícone 40px em quadrado `--elev`, título 600 + badge, descrição, linha "Distritos · Fonte", horário em mono à direita. Filtros por tipo (chips) e por severidade (seg). Marcadores no mapa de alertas usam **a forma da classe** (círculo/losango/triângulo/octógono) e a legenda fica sob o mapa.
- **Chat (AI City Analyst)**: mensagem do usuário em balão `--accent-bg` com borda `--accent` (alinhado à direita); resposta com avatar do logo, **chips de ferramentas** (`get_risk(...)`, `sql_marts`, `search_docs`) em mono, texto com **citações numeradas** ①②③ (círculos 18px `--accent`), **card de resultado** (mini-mapa + tabela + barras horizontais) e painel lateral "Fontes citadas" com número, fonte, descrição e data. Linha de rodapé da resposta: tag "Resposta gerada por IA", aviso "Pode conter erros. Confira as fontes citadas.", 👍/👎 e "Regenerar" (ícones SVG). Sugestões de pergunta como chips acima do input; input com anel de foco e botão de enviar `--accent`.
- **Bloco de fórmula** (Fontes e metodologia): fundo `--elev`, raio 10, mono 20px.

## 5. Telas (arquivos)
A tela 0 é a porta de entrada, em 5 blocos (v0.2 removeu o cartão "Para quem é", que virou uma linha no hero): manchete + CTAs + "Para" (públicos), problema | solução, 4 passos, 3 frentes | fontes (só nomes em texto), CTAs ('Explorar o mapa' → tela 2, 'Perguntar ao analista' → tela 4) e aviso de projeto de portfólio/demonstração com dados de exemplo. Não contém métricas de impacto.

| # | Tela | PNG escuro | PNG claro |
|---|---|---|---|
| 0 | O que é o EcoGuard (apresentação / objetivo) | `mockups/00-o-que-e-escuro.png` | `mockups/00-o-que-e-claro.png` |
| 1 | Visão geral | `mockups/01-overview-escuro.png` | `…-claro.png` |
| 2 | Mapa de risco | `mockups/02-mapa-risco-escuro.png` | `…-claro.png` |
| 3 | Detalhe do distrito (Capão Redondo) | `mockups/03-detalhe-distrito-escuro.png` | `…-claro.png` |
| 4 | AI City Analyst (chat) | `mockups/04-analista-ia-escuro.png` | `…-claro.png` |
| 5 | Alertas | `mockups/05-alertas-escuro.png` | `…-claro.png` |
| 6 | Fontes e metodologia | `mockups/06-fontes-metodologia-escuro.png` | `…-claro.png` |

Extras: `mockups/extras/estados-escuro.png` e `estados-claro.png` (skeleton, vazio, erro, foco/hover/desativado, sem dado, risco cor+forma+rótulo).

A tela 7 (status/observabilidade, opcional no `mvp-scope.md`) não foi desenhada.

## 6. Dados, mapa e fórmula — observações para o dev
- **Dados de exemplo**: scores, séries e alertas são gerados de forma determinística em `src/data.py` (semana de exemplo 26 out – 1 nov 2026, SE 44). Nada é medição real.
- **Mapa**: polígonos dos 96 distritos vêm do GeoJSON `codigourbano/distritos-sp` (origem GeoSampa, 2016), simplificados (Douglas-Peucker). Em produção, usar a camada oficial atual do GeoSampa (os limites foram revisados) carregada no PostGIS e servida ao MapLibre.
- **Fórmula (proposta, a confirmar)**: `Geral = 0,40·Ambiental + 0,35·Saúde + 0,25·Social`, cada camada = média ponderada de indicadores normalizados 0–100 por distrito. O `mvp-scope.md` só diz "transparente"; os pesos são sugestão de design e devem ser validados com o time de dados antes de ir ao ar. A tela "Fontes e metodologia" deve ler os pesos da mesma configuração usada no cálculo.
- **Defasagens exibidas**: INMET/Open-Meteo ~1 h · CETESB ~2 h · INPE ~3 h · CEMADEN ~15 min · InfoDengue 1 semana epidemiológica · **DataSUS SIH ~2 meses (mensal)** · IBGE Censo 2022 estático · GeoSampa sob demanda. Mostrar sempre a defasagem real na UI e no chat.
- **Chat**: toda resposta deve citar fonte e data; mostrar as ferramentas usadas (`get_risk`, SQL em allowlist, `search_docs`); acesso somente leitura.
- **Acessibilidade**: foco visível (anel 2px `--ring`, offset 2px), risco nunca só por cor (padrão + forma + texto), alvo ≥ 44px, `aria-label` em gráficos/mapa/botões só com ícone, alternativa em tabela para o mapa, `prefers-reduced-motion` e `prefers-color-scheme` para o tema inicial. Em Next.js: `next/font` para Inter/JetBrains Mono, componentes de mapa/gráfico carregados sob demanda (`next/dynamic`).

## 7. Logo

O pacote final está em `logo/v2/` (a folha `logo-sheet.png` mostra as variantes). Escudo, pin de mapa e folha. O wordmark na UI é texto, não o SVG: **Eco** em `--accent` e **Guard** em `--text`, Inter 24px, peso 600, tracking −0,02em. Abaixo de 24px use `favicon.svg` (traço mais grosso, sem o pin). O ícone na interface fica em 32px, na variante transparente do tema. Área de respiro mínima = um quarto da altura do escudo.

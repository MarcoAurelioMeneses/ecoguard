# EcoGuard — Design System v0.1

Plataforma de inteligência urbana de São Paulo: risco ambiental + saúde + social por distrito, com o AI City Analyst.
Estilo: minimalista, direto, produto de dados sério. Tema **escuro é o padrão**; o claro é opcional (mesmos tokens, só trocam os valores).
Todos os mockups usam **dados de exemplo** (marcados na tela com o chip "Dados de exemplo"). Os valores não são reais.

Arquivos de apoio: `tokens.css` (variáveis CSS prontas), `html/*.html` (mockups em HTML/CSS real, base para o front), `src/ui.py` (CSS e ícones usados).

## 1. Cores

### Base
| Token | Escuro | Claro | Uso |
|---|---|---|---|
| `--bg` | `#0B1014` | `#F4F7F9` | fundo da página |
| `--surface` | `#121A21` | `#FFFFFF` | cards, sidebar, inputs |
| `--elev` | `#19232C` | `#EEF3F6` | trilhas de barra, tooltips, blocos internos |
| `--border` | `#243039` | `#D5DEE5` | bordas 1px |
| `--text` | `#E6EDF3` | `#0E1A24` | texto principal |
| `--muted` | `#8B9AA7` | `#556574` | rótulos, legendas, texto secundário |
| `--accent` | `#38BDF8` | `#0A7EA8` | item ativo, links, ícones de KPI, seleção |
| `--accent-bg` | `rgba(56,189,248,.12)` | `rgba(10,126,168,.10)` | fundo de item ativo / chip selecionado |

### Escala de risco
Classes (score 0–100): **Baixo 0–29 · Moderado 30–54 · Alto 55–74 · Crítico 75–100.**
Nunca usar só a cor: sempre acompanhar de texto (badge) ou número.

| Classe | Preenchimento (mapa, barras, linhas) | Texto/borda do badge — escuro | Texto/borda do badge — claro |
|---|---|---|---|
| Baixo | `#3FB68B` | `#3FB68B` | `#1B7A58` |
| Moderado | `#F2C94C` | `#F2C94C` | `#80620A` |
| Alto | `#F2994A` | `#F2994A` | `#AE5208` |
| Crítico | `#E5484D` | `#FF6B6F` | `#C0272D` |
| Sem dado | `--nodata` `#1F2B35` | | `#E3EAEF` (claro) |

Contraste medido (WCAG): texto principal 16:1 (escuro) e 17:1 (claro); muted ≥ 6:1 em ambos; texto de risco ≥ 4,5:1 sobre `--bg`/`--surface` nos dois temas (escuro 6,3–11,1; claro 4,9–5,9);
accent escuro 8,2:1; accent claro 4,6:1 sobre branco e 4,3:1 sobre `--bg` claro (usar em texto ≥ 15px semibold ou sobre `--surface`).
Os preenchimentos de mapa/barras são elementos gráficos (mínimo 3:1 contra o fundo escuro); no tema claro o crítico tem 3,9:1 sobre branco.

## 2. Tipografia
- **Inter** (UI): 400 / 500 / 600. **JetBrains Mono** (números, datas, tags técnicas): 400 / 600, com `font-variant-numeric: tabular-nums`.
- Canvas de referência 1920×1080; corpo 17px. Em produção, escalar com `rem` (1rem = 16px; os valores abaixo são px do mockup).

| Papel | Tamanho / peso | Fonte |
|---|---|---|
| Título de página `h1` | 28 / 600, tracking −0,02em | Inter |
| Subtítulo de página | 15 / 400, `--muted` | Inter |
| Valor de KPI | 46 / 600, tracking −0,03em (+ unidade 17 / 500 `--muted`) | JetBrains Mono |
| Título de card (`h3`) | 13,5 / 600, CAIXA ALTA, tracking .06em, `--muted` | Inter |
| Corpo / linhas de tabela | 16,5–17 / 400–500 | Inter |
| Cabeçalho de tabela | 13,5 / 600, CAIXA ALTA, `--muted` | Inter |
| Badge / chip | 14–15 / 600 | Inter |
| Números em tabela, datas, tags de ferramenta | 14–16 | JetBrains Mono |
| Rótulos de eixo | 11–12 | JetBrains Mono, `--muted` |

## 3. Espaçamento, forma, layout
- Gap entre cards **22px**; padding do conteúdo **28px 36px**; padding de card **22px** (KPI/compacto 18px).
- Raios: card **14px**, controles (seg, botão) **10px**, chips/badges **999px**, item de navegação 10px, barras 4px.
- Bordas **1px `--border`**; sem sombras (exceto tooltip do mapa: `0 8px 24px rgba(0,0,0,.25)`).
- Ícones: traço de 1,75px, 24×24, cantos arredondados, `currentColor` (conjunto estilo Lucide).
- Shell: **sidebar 292px** (fundo `--surface`, borda direita) + **topbar 88px** (título, subtítulo, chips à direita) + área de conteúdo.
- Largura mínima de referência: desktop 1280. Abaixo disso, a sidebar vira ícones (não desenhado nos mockups).

## 4. Componentes
Todos estão implementados em `html/*.html` / `src/ui.py`; use como referência de CSS.

- **Sidebar / navegação**: 6 itens (O que é o EcoGuard · Visão geral · Mapa de risco · Analista IA · Alertas · Fontes e metodologia); logo + "EcoGuard"; itens com ícone 22px, 17/500, `--muted`; ativo = `--accent-bg` + texto `--accent`. Rodapé: "São Paulo · 96 distritos / Granularidade diária e semanal".
- **Topbar chips**: seletor de período (ícone calendário), **"Dados de exemplo"** (borda e texto `--t-moderado`; remover quando houver dados reais), **"Atualizado há 2 h"** (ponto verde `--baixo`).
- **Card**: `--surface`, 1px `--border`, raio 14.
- **KPI card**: rótulo com ícone `--accent` (15/500 muted), valor mono 46/600, unidade, badge de classe, sparkline à direita.
- **Badge de risco**: pílula 1px, ponto de 8px + texto da classe, cores `--t-*`.
- **Chip / filtro**: pílula 1px `--border`; selecionado = `--accent-bg` + borda e texto `--accent`.
- **Segmented control (camadas)**: Ambiental · Saúde · Social · Geral; borda 1px, raio 10; ativo `--accent-bg` / `--accent` 600.
- **Tabela**: cabeçalho caps muted; linhas com borda superior 1px; nome do distrito 600; score em mono 600; mini-barras de 8px (preenchimento = cor da classe, trilha `--elev`); variação semanal com ▲/▼ colorido.
- **Barra de score**: 8px, raio 4, trilha `--elev`.
- **Legenda**: quadrados 16px raio 4 + rótulo + faixa em mono.
- **Mapa coroplético**: 96 polígonos preenchidos com a cor de classe; traço `--mapstroke` 0,5–1px; distrito em foco com traço branco (escuro) / `--text` (claro) e 2px; "sem dado" = `--nodata`. Tooltip: card `--elev` com nome, badge, score grande e A/S/S.
- **Linha do tempo semanal**: segmentos de 12 semanas, ativo `--accent`; botões ‹ › e "Hoje".
- **Gráfico de linha com faixas de risco**: eixos mono 11px, grade 1px `--border`, faixas de fundo com 6% de opacidade nas cores das classes, linha 2px, ponto final com marcador.
- **Sparkline**: 2px com preenchimento 12% abaixo da linha.
- **Alerta**: card com **borda esquerda 4px na cor da severidade**, ícone 40px em quadrado `--elev`, título 600 + badge, descrição, linha "Distritos · Fonte", horário em mono à direita. Filtros por tipo (chips) e por severidade (seg).
- **Chat (AI City Analyst)**: mensagem do usuário em balão `--accent-bg` com borda `--accent` (alinhado à direita); resposta com avatar do logo, **chips de ferramentas** (`get_risk(...)`, `sql_marts`, `search_docs`) em mono, texto com **citações numeradas** ①②③ (círculos 18px `--accent`), **card de resultado** (mini-mapa + tabela + barras horizontais) e painel lateral "Fontes citadas" com número, fonte, descrição e data. Sugestões de pergunta como chips acima do input; input com botão de enviar `--accent`.
- **Bloco de fórmula** (Fontes e metodologia): fundo `--elev`, raio 10, mono 20px.

## 5. Telas (arquivos)
A tela 0 é a porta de entrada: manchete, problema, solução, 4 passos, para quem é, 3 frentes, fontes (só nomes em texto), CTAs ('Explorar o mapa' → tela 2, 'Perguntar ao analista' → tela 4) e aviso de projeto de portfólio/demonstração com dados de exemplo. Não contém métricas de impacto.

| # | Tela | PNG escuro | PNG claro |
|---|---|---|---|
| 0 | O que é o EcoGuard (apresentação / objetivo) | `mockups/00-o-que-e-escuro.png` | `mockups/00-o-que-e-claro.png` |
| 1 | Visão geral | `mockups/01-overview-escuro.png` | `…-claro.png` |
| 2 | Mapa de risco | `mockups/02-mapa-risco-escuro.png` | `…-claro.png` |
| 3 | Detalhe do distrito (Capão Redondo) | `mockups/03-detalhe-distrito-escuro.png` | `…-claro.png` |
| 4 | AI City Analyst (chat) | `mockups/04-analista-ia-escuro.png` | `…-claro.png` |
| 5 | Alertas | `mockups/05-alertas-escuro.png` | `…-claro.png` |
| 6 | Fontes e metodologia | `mockups/06-fontes-metodologia-escuro.png` | `…-claro.png` |

A tela 7 (status/observabilidade, opcional no `mvp-scope.md`) não foi desenhada.

## 6. Dados, mapa e fórmula — observações para o dev
- **Dados de exemplo**: scores, séries e alertas são gerados de forma determinística em `src/data.py` (semana de exemplo 26 out – 1 nov 2026, SE 44). Nada é medição real.
- **Mapa**: polígonos dos 96 distritos vêm do GeoJSON `codigourbano/distritos-sp` (origem GeoSampa, 2016), simplificados (Douglas-Peucker). Em produção, usar a camada oficial atual do GeoSampa (os limites foram revisados) carregada no PostGIS e servida ao MapLibre.
- **Fórmula (proposta, a confirmar)**: `Geral = 0,40·Ambiental + 0,35·Saúde + 0,25·Social`, cada camada = média ponderada de indicadores normalizados 0–100 por distrito. O `mvp-scope.md` só diz "transparente"; os pesos são sugestão de design e devem ser validados com o time de dados antes de ir ao ar. A tela "Fontes e metodologia" deve ler os pesos da mesma configuração usada no cálculo.
- **Defasagens exibidas**: INMET/Open-Meteo ~1 h · CETESB ~2 h · INPE ~3 h · CEMADEN ~15 min · InfoDengue 1 semana epidemiológica · **DataSUS SIH ~2 meses (mensal)** · IBGE Censo 2022 estático · GeoSampa sob demanda. Mostrar sempre a defasagem real na UI e no chat.
- **Chat**: toda resposta deve citar fonte e data; mostrar as ferramentas usadas (`get_risk`, SQL em allowlist, `search_docs`); acesso somente leitura.
- **Acessibilidade**: foco visível (2px `--accent`), não depender só de cor (badge com texto), alvo de clique ≥ 36px, `prefers-color-scheme` para escolher o tema inicial.

## 7. Logo
- `logo/logo-icone.svg` / `logo-icone-claro.svg`: escudo (proteção) + 3 barras (dados) + folha (ambiente), traço 2,6 em `--accent`. É o ícone usado dentro da UI (vetor).
- `logo/logo-icone-ia.png`: versão gerada por IA, ícone de app em tile escuro (1:1), para favicon/app/redes.
- `logo/logo-wordmark-ia.png`: lockup "EcoGuard" + "Inteligência urbana para São Paulo" (16:9), gerado por IA. Wordmark na UI: **Eco** (600) + **Guard** (accent), Inter 24px, tracking −0,02em.
- Área de respiro mínima = altura da barra central do ícone. Tamanho mínimo do ícone: 24px.

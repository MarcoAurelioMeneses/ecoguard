# EcoGuard — Auditoria de UI/UX (ui-ux-pro-max) — v0.1 → v0.2

Data: 02/11/2026 (America/Sao_Paulo). Escopo: 7 telas (00–06) × 2 temas = 14 PNGs, mais a brand sheet.
Método: busca no banco do ui-ux-pro-max (`scripts/search.py`, sem `--persist`; saídas em `01…12-*.txt` nesta pasta), auditoria das telas contra as regras prioritárias do `quick-reference.md` e medição de contraste WCAG por cálculo.
As buscas são **recomendações**, não ordens: cada uma foi conferida (domínio e resultado de topo) antes de usar.

## 1. De onde veio cada recomendação

**Match verificado no banco** (a consulta devolveu o domínio certo e um resultado aplicável):
| Tema | Arquivo | O que o banco recomendou | Onde aplicei |
|---|---|---|---|
| Estilo/sistema | `01-design-system.txt` | Estilo "Data-Dense Dashboard", densidade 8, espaçamento 8–32 px | Ritmo 4/8, gap 24 px, escala de tipo |
| Mapa coroplético | `02-chart-choropleth.txt` | Não depender só de cor; padrões/textura; legenda; alternativa em tabela; tooltips | Padrões no mapa e na legenda, botão "Ver como tabela", `aria-label` |
| Série temporal | `03-chart-timeseries.txt` | Rótulos de eixo legíveis, tooltip no ponto, texto alternativo | Eixos 14 px, tooltip, `role="img"`, rótulos das faixas |
| Chat com IA | `09-ux-ai-chat.txt` | Indicar conteúdo de IA, mostrar fontes, feedback, regenerar | Tag "Resposta gerada por IA", aviso, 👍/👎, "Regenerar" |
| Breadcrumb | `07-ux-dashboard-nav.txt` | Usar a partir de 3 níveis | Só no detalhe do distrito |
| Next.js | `12-stack-nextjs.txt` | `next/font`, `next/dynamic` para mapa/gráfico | Nota no `design-system.md` (nada a desenhar) |

**Match fraco ou sem match** (a consulta devolveu produto/paleta de outro tipo; tratei como **padrão de fallback**, vindo do `quick-reference.md` e das minhas medições, não do banco):
- Escala de cor de risco (`05`): veio paleta de "Smart Home/IoT", sem relação. Mantive a escala do EcoGuard e apliquei as regras gerais de contraste e de "nunca só cor".
- Paleta modo escuro (`06`): sem match específico; ajustes por medição.
- Tabelas de dados (`08`) e feed de alertas (`10`): match genérico; usei regras do quick-reference (alvo 44 px, estado ordenado, hierarquia) e julgamento próprio.
- KPI (`04`): parcial; os cartões já cumpriam o padrão (valor grande + contexto + tendência), só troquei o glifo por ícone SVG.
- Tipografia (`11`): sugeriu Fira Code + Fira Sans. **Não troquei**: Inter + JetBrains Mono cumprem o mesmo papel (UI + números tabulares), a marca já foi aprovada, e o banco não trouxe razão de legibilidade ou contraste para trocar.

## 2. Problemas encontrados e correções

| # | Regra | Problema (antes) | Correção (agora) | Origem |
|---|---|---|---|---|
| 1 | Contraste 4,5:1 | Accent claro `#0A7EA8` com 4,3:1 sobre o fundo `#F4F7F9` (4,6:1 sobre branco) | `#076E94`: 5,3:1 sobre o fundo, 5,7:1 sobre branco; botão primário claro com texto branco 5,7:1 | fallback (medição) |
| 2 | Contraste de controles (3:1) | Bordas de chips e botões em `--border`: 1,3:1 | Novo `--border-strong` (3,2:1 escuro, 3,4:1 claro) em todos os controles | fallback |
| 3 | Texto < 12 px | Rótulos de eixo 11–12,5 px; textos de 13–13,5 px | Mínimo de 14 px; 12 px só em rótulo de grupo da nav, contador e tag "IA" | fallback + match (eixos) |
| 4 | Risco não só por cor | Mapa, legenda, barras e marcadores só com cor; setas ▲ como texto | Padrões (liso/pontos/diagonal/hachura) no mapa e legendas; formas (● ◆ ▲ ⬣) nos badges e marcadores; ícones SVG | match (coroplético) |
| 5 | Legenda e tooltip | Gráficos sem rótulo das faixas e sem tooltip; legenda sem padrão | Rótulos das faixas, tooltip com data/valor, legenda com a mesma amostra do mapa | match (série temporal) |
| 6 | Foco e hover | Sem `:focus-visible`, sem hover, sem `prefers-reduced-motion` | Anel de 2 px (`--ring`), hover em nav/chips/botões, animações desligadas sob reduced-motion; campo do chat mostrado em foco | fallback |
| 7 | Estados vazio/carregando/erro | Inexistentes | CSS de skeleton + folha `mockups/extras/estados-*.png` (skeleton, vazio, erro, foco/hover/desativado, sem dado) | fallback |
| 8 | Hierarquia de navegação | Lista plana de 6 itens, sem agrupamento nem breadcrumb | 3 grupos (Produto / Análise / Transparência), barra lateral no item ativo, breadcrumb no detalhe | match (breadcrumb) + fallback |
| 9 | Densidade da tela 00 | 6 blocos, textos de 14–15 px, cartão "Para quem é" separado | 5 blocos: "Para quem é" virou linha no hero, textos de 16 px, problema e solução mais curtos | densidade do banco (8) + julgamento |
| 10 | Ícones | Glifos de texto (▲ ▾ ›) como ícones | Ícones SVG (up, dn, down, plus, minus, table, thumbup/dn, refresh, inbox) | fallback |
| 11 | Consistência | Tamanhos soltos (13,5/15/15,5/17/17,5…), gaps de 22 px, `h3` como título de card | Escala 12·14·16·18·24·28·32·40·48; gap 24 px; `h2`; topbar 92 px | match (ritmo 8) |
| 12 | Chat com IA | Resposta sem marca de IA, sem aviso, sem feedback | Tag de IA, aviso, feedback e regenerar | match (chat) |
| 13 | Tabela do mapa | Mapa sem alternativa textual nem zoom | Botões "Ver como tabela", + e −, 44 px | match (coroplético) |
| 14 | Acessibilidade estrutural | Sem landmarks, sem `aria`, sem link de pular | `nav`/`header`/`main`, skip link, `aria-label` em gráficos, mapa e botões de ícone, `aria-sort` | fallback |
| 15 | Fontes: espaço vazio | Cartão da tabela com vazio embaixo; coluna da direita curta | Novo cartão "Limitações conhecidas"; barras de classe com padrão | julgamento |

## 3. Contraste medido (WCAG, sobre `--surface`)
| Item | Escuro | Claro |
|---|---|---|
| Texto | 14,9 | 17,6 |
| Muted | 6,1 | 6,0 |
| Accent | 8,2 | 5,7 (antes 4,6; 4,3 sobre o fundo) |
| Texto de risco (baixo / moderado / alto / crítico) | 6,9 / 11,1 / 7,9 / 6,3 | 5,3 / 5,7 / 5,2 / 5,9 |
| `--border-strong` (controles) | 3,2 | 3,4 |
| `--border` (decorativa) | 1,3 | 1,4 |

## 4. Mudanças de marca (e por quê)
- **Paleta**: só o accent do tema claro (`#0A7EA8` → `#076E94`), por falhar 4,5:1. Escala de risco, escuro, nome, logo e rótulos de dados de exemplo ficaram como estavam.
- **Fontes**: sem mudança (ver seção 1).
- **Novos tokens**: `--border-strong`, `--ring`, `--pat`.

## 5. Ressalvas
- **Mapa no tema claro**: os preenchimentos de risco ficam abaixo de 3:1 contra o branco (baixo 2,5; moderado 1,6; alto 2,2; crítico 3,9). A informação não depende da cor (padrão + rótulo), mas, se for exigido 3:1 estrito, será preciso escurecer os preenchimentos do tema claro, o que mudaria a aparência da escala.
- Os mockups são estáticos: foco, hover e carregamento estão no CSS e na folha de estados, não em cada tela.
- A folha de estados é um extra e não faz parte dos 14 PNGs.
- Todas as telas continuam com dados de exemplo; a validação com usuário e com leitor de tela não foi feita (só inspeção visual e cálculo).
- A tela 03 usa breadcrumb; as demais são de nível único e não ganharam.
- Arquivos `*.antes` e `antes/` guardam o estado anterior para comparação.

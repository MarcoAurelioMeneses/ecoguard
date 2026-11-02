import subprocess,os
from PIL import Image
from ui import *
def sw(name,hexv,txt,sub=''):
    return f'<div style="flex:1;min-width:0"><div style="height:74px;border-radius:10px;background:{hexv};border:1px solid rgba(128,128,128,.35);display:flex;align-items:flex-end;padding:8px 10px;color:{txt};font:600 12px Inter">{sub}</div><div style="margin-top:8px;font-size:14px;font-weight:600">{name}</div><div class="mono muted" style="font-size:13px">{hexv}</div></div>'
BARS=''.join(f'<div style="display:flex;align-items:center;gap:10px"><span class="mono muted" style="width:36px;font-size:13px">{n}</span><div class="bar"><i style="display:block;height:100%;width:{w}%;background:var(--{c})"></i></div><span class="mono" style="width:30px;font-size:14px;text-align:right">{v}</span></div>' for n,w,c,v in [('Amb',62,'alto',62),('Saú',38,'moderado',38),('Soc',21,'baixo',21)])
def block(th,P):
    rows=''.join(sw(*p) for p in P[0])
    risk=''.join(sw(*p) for p in P[1])
    return f'''<div class="{th}" style="flex:1;background:var(--bg);color:var(--text);border-radius:18px;padding:30px 32px;border:1px solid #243039;display:flex;flex-direction:column;gap:18px;justify-content:space-between">
<div style="display:flex;align-items:center;gap:12px">{logo(40)}<div style="font-size:30px;font-weight:600;letter-spacing:-.01em">Eco<b style="color:var(--accent)">Guard</b></div><span class="muted" style="margin-left:auto;font-size:14px;text-transform:uppercase;letter-spacing:.08em">Tema {'escuro' if th=='dark' else 'claro'}</span></div>
<div><div class="muted" style="font-size:13px;letter-spacing:.08em;text-transform:uppercase;margin-bottom:10px">Base</div><div style="display:flex;gap:12px">{rows}</div></div>
<div><div class="muted" style="font-size:13px;letter-spacing:.08em;text-transform:uppercase;margin-bottom:10px">Escala de risco</div><div style="display:flex;gap:12px">{risk}</div></div>
<div><div class="muted" style="font-size:13px;letter-spacing:.08em;text-transform:uppercase;margin-bottom:10px">Texto de risco (contraste AA)</div><div style="display:flex;gap:10px"><span class="badge b-baixo">Baixo</span><span class="badge b-moderado">Moderado</span><span class="badge b-alto">Alto</span><span class="badge b-critico">Crítico</span></div></div>
<div><div class="muted" style="font-size:13px;letter-spacing:.08em;text-transform:uppercase;margin-bottom:10px">Componentes</div><div style="display:flex;gap:16px;align-items:stretch">
<div class="card" style="padding:16px 18px;flex:1.2"><div class="muted" style="font-size:14px">Risco geral</div><div class="mono" style="font-size:40px;font-weight:600;line-height:1.2">37<span class="muted" style="font-size:18px"> /100</span></div><span class="badge b-moderado">Moderado</span></div>
<div class="card" style="padding:16px 18px;flex:1.6;display:flex;flex-direction:column;justify-content:center;gap:12px">{BARS}</div>
<div style="flex:1;display:flex;flex-direction:column;gap:10px;justify-content:center;align-items:flex-start"><span class="chip" style="border:1px solid var(--moderado);color:var(--t-moderado);border-radius:999px;padding:4px 14px;font-size:14px">Dados de exemplo</span><span class="chip" style="border:1px solid var(--border);border-radius:999px;padding:4px 14px;font-size:14px">Atualizado há 2 h</span><span style="background:var(--accent);color:var(--bg);border-radius:10px;padding:8px 16px;font-weight:600;font-size:15px">Ação primária</span></div></div></div><div style="display:flex;gap:24px;align-items:flex-end;margin-top:auto">
<div style="flex:1"><div class="muted" style="font-size:13px;letter-spacing:.08em;text-transform:uppercase;margin-bottom:6px">Inter — interface</div><div style="font-size:40px;font-weight:600;letter-spacing:-.01em">Risco por distrito</div><div class="muted" style="font-size:17px">Regular 400 · Medium 500 · Semibold 600</div></div>
<div style="flex:1"><div class="muted" style="font-size:13px;letter-spacing:.08em;text-transform:uppercase;margin-bottom:6px">JetBrains Mono — números</div><div class="mono" style="font-size:40px;font-weight:600">75<span class="muted" style="font-size:20px"> /100</span></div><div class="muted mono" style="font-size:15px">34,2 °C · 143/100 mil · IQAR 82</div></div></div></div>'''
dark=(
 [('Fundo','#0B1014','#E6EDF3','bg'),('Superfície','#121A21','#E6EDF3','surface'),('Elevado','#19232C','#E6EDF3','elevated'),('Borda','#243039','#E6EDF3','border'),('Texto','#E6EDF3','#0B1014','text'),('Muted','#8B9AA7','#0B1014','muted'),('Accent','#38BDF8','#0B1014','accent')],
 [('Baixo','#3FB68B','#0B1014','0–29'),('Moderado','#F2C94C','#0B1014','30–54'),('Alto','#F2994A','#0B1014','55–74'),('Crítico','#E5484D','#FFFFFF','75–100'),('Sem dado','#1F2B35','#8B9AA7','nodata')])
light=(
 [('Fundo','#F4F7F9','#0E1A24','bg'),('Superfície','#FFFFFF','#0E1A24','surface'),('Elevado','#EEF3F6','#0E1A24','elevated'),('Borda','#D5DEE5','#0E1A24','border'),('Texto','#0E1A24','#F4F7F9','text'),('Muted','#556574','#FFFFFF','muted'),('Accent','#0A7EA8','#FFFFFF','accent')],
 [('Baixo','#3FB68B','#0E1A24','0–29'),('Moderado','#F2C94C','#0E1A24','30–54'),('Alto','#F2994A','#0E1A24','55–74'),('Crítico','#E5484D','#FFFFFF','75–100'),('Sem dado','#E3EAEF','#556574','nodata')])
html=f'''<!doctype html><html lang="pt-BR"><head><meta charset="utf-8"><style>{CSS}
body{{display:block;background:#080C0F;padding:36px 40px}}.mono{{font-family:'JetBrains Mono',monospace}}</style></head><body class="dark">
<div style="display:flex;align-items:baseline;gap:18px;margin-bottom:22px;color:#E6EDF3"><div style="font-size:30px;font-weight:600">EcoGuard · Brand sheet</div><div style="color:#8B9AA7;font-size:16px">Inteligência urbana para São Paulo · v0.1</div></div>
<div style="display:flex;gap:24px;height:940px">{block('dark',dark)}{block('light',light)}</div></body></html>'''
os.makedirs('../html',exist_ok=True)
open('../html/brand-sheet.html','w').write(html)
subprocess.run(['google-chrome','--headless=new','--no-sandbox','--hide-scrollbars','--force-device-scale-factor=1','--window-size=1920,1167','--screenshot=/tmp/bs.png','file:///workspace/ecoguard-design/html/brand-sheet.html'],capture_output=True)
Image.open('/tmp/bs.png').crop((0,0,1920,1080)).save('../brand-sheet.png')

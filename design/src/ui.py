import math
from data import *

CSS = """
:root{--bg:#0B1014;--surface:#121A21;--elev:#19232C;--border:#243039;--text:#E6EDF3;--muted:#8B9AA7;--accent:#38BDF8;--accent-bg:rgba(56,189,248,.12);
--baixo:#3FB68B;--moderado:#F2C94C;--alto:#F2994A;--critico:#E5484D;
--t-baixo:#3FB68B;--t-moderado:#F2C94C;--t-alto:#F2994A;--t-critico:#FF6B6F;--mapstroke:#0B1014;--nodata:#1F2B35;}
.light{--bg:#F4F7F9;--surface:#FFFFFF;--elev:#EEF3F6;--border:#D5DEE5;--text:#0E1A24;--muted:#556574;--accent:#0A7EA8;--accent-bg:rgba(10,126,168,.10);
--t-baixo:#1B7A58;--t-moderado:#80620A;--t-alto:#AE5208;--t-critico:#C0272D;--mapstroke:#FFFFFF;--nodata:#E3EAEF;}
*{box-sizing:border-box;margin:0;padding:0}
html,body{width:1920px;height:1080px;overflow:hidden}
body{background:var(--bg);color:var(--text);font-family:'Inter',sans-serif;font-size:17px;line-height:1.4;display:flex}
.mono{font-family:'JetBrains Mono',monospace;font-variant-numeric:tabular-nums}
svg{display:block}
.side{width:292px;background:var(--surface);border-right:1px solid var(--border);padding:26px 18px;display:flex;flex-direction:column;gap:6px;flex:none}
.brand{display:flex;align-items:center;gap:12px;padding:0 10px 26px;font-size:24px;font-weight:600;letter-spacing:-.02em}
.brand b{font-weight:300}
.nav{display:flex;align-items:center;gap:13px;padding:12px 14px;border-radius:10px;color:var(--muted);font-weight:500;font-size:17px}
.nav.on{background:var(--accent-bg);color:var(--accent)}
.nav svg{width:22px;height:22px;stroke:currentColor;fill:none;stroke-width:1.75;stroke-linecap:round;stroke-linejoin:round}
.sidefoot{margin-top:auto;padding:14px;border:1px solid var(--border);border-radius:12px;color:var(--muted);font-size:14px;line-height:1.5}
.main{flex:1;display:flex;flex-direction:column;min-width:0}
.top{height:88px;padding:0 36px;display:flex;align-items:center;gap:16px;border-bottom:1px solid var(--border);flex:none}
.top h1{font-size:28px;font-weight:600;letter-spacing:-.02em}
.top .sub{color:var(--muted);font-size:15px;margin-top:2px}
.sp{flex:1}
.chip{display:inline-flex;align-items:center;gap:8px;padding:8px 14px;border:1px solid var(--border);border-radius:999px;font-size:15px;color:var(--text);background:var(--surface);white-space:nowrap}
.chip svg{width:18px;height:18px;stroke:currentColor;fill:none;stroke-width:1.75;stroke-linecap:round;stroke-linejoin:round;color:var(--muted)}
.chip.ex{border-color:var(--t-moderado);color:var(--t-moderado)}
.dot{width:8px;height:8px;border-radius:50%;background:var(--baixo)}
.content{flex:1;padding:28px 36px 30px;display:flex;flex-direction:column;gap:22px;min-height:0}
.card{background:var(--surface);border:1px solid var(--border);border-radius:14px;padding:22px}
.card h3{font-size:16px;font-weight:600;color:var(--muted);text-transform:uppercase;letter-spacing:.06em;font-size:13.5px;margin-bottom:14px}
.row{display:flex;gap:22px}
.badge{display:inline-flex;align-items:center;gap:7px;padding:4px 11px;border-radius:999px;font-size:14px;font-weight:600;border:1px solid}
.badge::before{content:'';width:8px;height:8px;border-radius:50%;background:currentColor}
.b-baixo{color:var(--t-baixo);border-color:var(--t-baixo)}.b-moderado{color:var(--t-moderado);border-color:var(--t-moderado)}.b-alto{color:var(--t-alto);border-color:var(--t-alto)}.b-critico{color:var(--t-critico);border-color:var(--t-critico)}
.t-baixo{color:var(--t-baixo)}.t-moderado{color:var(--t-moderado)}.t-alto{color:var(--t-alto)}.t-critico{color:var(--t-critico)}
.muted{color:var(--muted)}
.kpi .lab{display:flex;align-items:center;gap:9px;color:var(--muted);font-size:15px;font-weight:500}
.kpi .lab svg{width:20px;height:20px;stroke:var(--accent);fill:none;stroke-width:1.75;stroke-linecap:round;stroke-linejoin:round}
.kpi .val{font-size:46px;font-weight:600;letter-spacing:-.03em;margin:8px 0 2px;display:flex;align-items:baseline;gap:8px}
.kpi .val small{font-size:17px;font-weight:500;color:var(--muted);letter-spacing:0}
table{border-collapse:collapse;width:100%}
th{font-size:13.5px;text-transform:uppercase;letter-spacing:.06em;color:var(--muted);font-weight:600;text-align:left;padding:0 12px 12px}
td{padding:var(--tdp,14px) 12px;border-top:1px solid var(--border);font-size:16.5px}
.bar{height:8px;border-radius:4px;background:var(--elev);overflow:hidden;width:100%}
.bar i{display:block;height:100%;border-radius:4px}
.seg{display:inline-flex;border:1px solid var(--border);border-radius:10px;overflow:hidden;background:var(--surface)}
.seg span{padding:10px 20px;font-size:16px;font-weight:500;color:var(--muted);border-right:1px solid var(--border)}
.seg span:last-child{border:0}.seg span.on{background:var(--accent-bg);color:var(--accent);font-weight:600}
.legend{display:flex;flex-direction:column;gap:9px}
.legend div{display:flex;align-items:center;gap:10px;font-size:15px}
.legend i{width:16px;height:16px;border-radius:4px;display:block}
.sw{width:100%}
"""

ICONS = {
'info':'<circle cx="12" cy="12" r="9"/><path d="M12 11v5"/><path d="M12 7.5h.01"/>',
'users':'<path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M22 21v-2a4 4 0 0 0-3-3.9"/><path d="M16 3.1a4 4 0 0 1 0 7.8"/>',
'layout':'<rect x="3" y="3" width="7" height="9" rx="1"/><rect x="14" y="3" width="7" height="5" rx="1"/><rect x="14" y="12" width="7" height="9" rx="1"/><rect x="3" y="16" width="7" height="5" rx="1"/>',
'map':'<path d="M3 6l6-3 6 3 6-3v15l-6 3-6-3-6 3z"/><path d="M9 3v15M15 6v15"/>',
'chat':'<path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>',
'bell':'<path d="M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9"/><path d="M10.3 21a1.94 1.94 0 0 0 3.4 0"/>',
'book':'<path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/>',
'rain':'<path d="M20 16.2A4.5 4.5 0 0 0 17.5 8h-1.8A7 7 0 1 0 4 14.9"/><path d="M16 14v6M8 14v6M12 16v6"/>',
'heat':'<path d="M14 4v10.54a4 4 0 1 1-4 0V4a2 2 0 0 1 4 0z"/>',
'wind':'<path d="M9.6 4.6A2 2 0 1 1 11 8H2M12.6 19.4A2 2 0 1 0 14 16H2M17.7 7.7A2.5 2.5 0 1 1 19.5 12H2"/>',
'waves':'<path d="M2 6c.6.5 1.2 1 2.5 1C7 7 7 5 9.5 5c2.6 0 2.4 2 5 2 2.5 0 2.5-2 5-2 1.3 0 1.9.5 2.5 1M2 12c.6.5 1.2 1 2.5 1 2.5 0 2.5-2 5-2 2.6 0 2.4 2 5 2 2.5 0 2.5-2 5-2 1.3 0 1.9.5 2.5 1M2 18c.6.5 1.2 1 2.5 1 2.5 0 2.5-2 5-2 2.6 0 2.4 2 5 2 2.5 0 2.5-2 5-2 1.3 0 1.9.5 2.5 1"/>',
'flame':'<path d="M8.5 14.5A2.5 2.5 0 0 0 11 12c0-1.4-.5-2-1-3-1.1-2.1-.2-4 2-6 .5 2.500 2 4.900 4 6.500 2 1.600 3 3.500 3 5.500a7 7 0 1 1-14 0c0-1.150.43-2.290 1-3a2.500 2.500 0 0 0 2.500 2.500z"/>',
'pulse':'<path d="M19 14c1.5-1.500 3-3.200 3-5.500A5.500 5.500 0 0 0 16.500 3c-1.800 0-3 .5-4.500 2-1.500-1.500-2.700-2-4.500-2A5.500 5.500 0 0 0 2 8.500c0 2.300 1.500 4.050 3 5.500l7 7z"/><path d="M3.200 12h6.300l.5-1 2 4.500 2-7 1.500 3.500h5.300"/>',
'city':'<path d="M3 21h18M5 21V7l8-4v18M19 21V11l-6-4M9 9v.01M9 12v.01M9 15v.01"/>',
'cal':'<rect x="3" y="4" width="18" height="18" rx="2"/><path d="M16 2v4M8 2v4M3 10h18"/>',
'send':'<path d="M12 19V5M5 12l7-7 7 7"/>',
'spark':'<path d="M12 3l1.900 5.100L19 10l-5.100 1.900L12 17l-1.900-5.100L5 10l5.100-1.900z"/>',
'warn':'<path d="M10.300 3.900L1.800 18a2 2 0 0 0 1.700 3h17a2 2 0 0 0 1.700-3L13.700 3.900a2 2 0 0 0-3.400 0z"/><path d="M12 9v4M12 17h.01"/>',
'down':'<path d="M6 9l6 6 6-6"/>',
'left':'<path d="M15 6l-6 6 6 6"/>','right':'<path d="M9 6l6 6-6 6"/>',
'search':'<circle cx="11" cy="11" r="7"/><path d="M21 21l-4.300-4.300"/>',
'db':'<ellipse cx="12" cy="5" rx="8" ry="3"/><path d="M4 5v14c0 1.700 3.600 3 8 3s8-1.300 8-3V5M4 12c0 1.700 3.600 3 8 3s8-1.300 8-3"/>',
'clock':'<circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/>',
'tool':'<path d="M14.700 6.300a1 1 0 0 0 0 1.400l1.600 1.600a1 1 0 0 0 1.400 0l3.800-3.800a6 6 0 0 1-7.900 7.900l-6.900 6.900a2.100 2.100 0 0 1-3-3l6.900-6.900a6 6 0 0 1 7.900-7.900z"/>',
}
def ic(n,cls=''):
    return f'<svg viewBox="0 0 24 24" class="{cls}" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round">{ICONS[n]}</svg>'

LOGO = '''<svg width="{s}" height="{s}" viewBox="0 0 48 48" fill="none" stroke="var(--accent)" stroke-width="2.6" stroke-linecap="round" stroke-linejoin="round"><path d="M24 4.5L40 10.5V24c0 9-7 15.500-16 19.500C15 39.500 8 33 8 24V10.500z"/><path d="M17 30V25M24 30V16M31 30V21" stroke-width="3"/><path d="M26 38c0-5 3.500-8 9-8.500-.2 5-3.200 8.200-9 8.500z" stroke-width="2"/></svg>'''
def logo(s=36): return LOGO.format(s=s)

NAV=[('info','O que é o EcoGuard'),('layout','Visão geral'),('map','Mapa de risco'),('chat','Analista IA'),('bell','Alertas'),('book','Fontes e metodologia')]
def page(active, title, sub, body, light=False, extra_top=''):
    nav=''.join(f'<div class="nav {"on" if t==active else ""}">{ic(i)}{t}</div>' for i,t in NAV)
    return f'''<!doctype html><html lang="pt-BR"><head><meta charset="utf-8"><style>{CSS}</style></head><body class="{'light' if light else ''}">
<aside class="side"><div class="brand">{logo(38)}<span>Eco<b>Guard</b></span></div>{nav}
<div class="sidefoot"><b style="color:var(--text)">São Paulo · 96 distritos</b><br>Granularidade diária e semanal</div></aside>
<div class="main"><header class="top"><div><h1>{title}</h1><div class="sub">{sub}</div></div><div class="sp"></div>{extra_top}
<span class="chip ex">Dados de exemplo</span><span class="chip"><span class="dot"></span>Atualizado há 2 h</span></header>
<div class="content">{body}</div></div></body></html>'''

DATE_CHIP=f'<span class="chip">{ic("cal")}26 out – 1 nov 2026 {ic("down")}</span>'

def fill(v): return f'var(--{cls(v)})'
def map_svg(w,h,key=None,hl=None,hlmap=None,bbox=None,dim=False,labels=None,sw=0.8,vals=None,strokec=None):
    b=bbox or BBOX
    x0,y0,x1,y1=b; sc=min(w/(x1-x0),h/(y1-y0))
    ox=(w-(x1-x0)*sc)/2; oy=(h-(y1-y0)*sc)/2
    P=[]
    for d in dists:
        if key is None: f='var(--nodata)'
        elif hl is not None and d['name'] not in hl: f='var(--nodata)'
        else: f=fill(d[key])
        path=''
        for r in d['rings']:
            path+='M'+'L'.join(f'{(x-x0)*sc+ox:.1f},{(y-y0)*sc+oy:.1f}' for x,y in r)+'Z'
        op=''
        stroke=strokec or 'var(--mapstroke)'; swd=sw
        if (hlmap and d['name']==hlmap) or (labels and hl is not None and d['name'] in hl): stroke='var(--text)'; swd=3 if hlmap else 1.6
        P.append(f'<path d="{path}" fill="{f}" stroke="{stroke}" stroke-width="{swd}" stroke-linejoin="round"/>')
    # draw highlighted last
    if hlmap:
        pass
    if True:
        P.sort(key=lambda s: 1 if ('stroke-width="3"' in s or 'stroke-width="1.6"' in s) else 0)
    lab=''
    for (nm,txt,dx,dy) in (labels or []):
        d=BY[nm]; x=(d['cx']*K-x0)*sc+ox; y=(-d['cy']-y0)*sc+oy
        lab+=f'<circle cx="{x+dx:.1f}" cy="{y+dy:.1f}" r="10.5" fill="var(--bg)" stroke="var(--text)" stroke-width="1.5"/><text x="{x+dx:.1f}" y="{y+dy+4.5:.1f}" text-anchor="middle" font-size="12.5" font-weight="700" fill="var(--text)" font-family="Inter">{txt}</text>'
    return f'<svg width="{w}" height="{h}" viewBox="0 0 {w} {h}" style="overflow:{'visible' if lab else 'hidden'}">{"".join(P)}{lab}</svg>'

def spark(vals,w=200,h=44,color='var(--accent)',fillc=True):
    lo,hi=min(vals),max(vals); r=(hi-lo) or 1
    pts=[(i*(w-4)/(len(vals)-1)+2,h-4-(v-lo)/r*(h-8)) for i,v in enumerate(vals)]
    d='M'+'L'.join(f'{x:.1f},{y:.1f}' for x,y in pts)
    a=d+f'L{w-2},{h}L2,{h}Z'
    return f'<svg width="{w}" height="{h}" viewBox="0 0 {w} {h}">'+(f'<path d="{a}" fill="{color}" opacity=".12"/>' if fillc else '')+f'<path d="{d}" fill="none" stroke="{color}" stroke-width="2.2" stroke-linejoin="round" stroke-linecap="round"/><circle cx="{pts[-1][0]:.1f}" cy="{pts[-1][1]:.1f}" r="3.5" fill="{color}"/></svg>'

def linechart(series,w,h,xl,ylo,yhi,yticks,unit='',bands=False):
    pl,pr,pt,pb=46,12,10,28
    iw,ih=w-pl-pr,h-pt-pb
    o=''
    if bands:
        for lo,hi,c in [(0,30,'baixo'),(30,55,'moderado'),(55,75,'alto'),(75,100,'critico')]:
            y1=pt+ih-(min(hi,yhi)-ylo)/(yhi-ylo)*ih; y2=pt+ih-(max(lo,ylo)-ylo)/(yhi-ylo)*ih
            if hi>ylo and lo<yhi: o+=f'<rect x="{pl}" y="{y1:.1f}" width="{iw}" height="{y2-y1:.1f}" fill="var(--{c})" opacity=".07"/>'
    for t in yticks:
        y=pt+ih-(t-ylo)/(yhi-ylo)*ih
        o+=f'<line x1="{pl}" x2="{w-pr}" y1="{y:.1f}" y2="{y:.1f}" stroke="var(--border)"/><text x="{pl-8}" y="{y+4:.1f}" text-anchor="end" font-size="12.5" fill="var(--muted)" font-family="JetBrains Mono">{t}</text>'
    n=len(xl)
    for i,t in enumerate(xl):
        x=pl+i*iw/(n-1)
        o+=f'<text x="{x:.1f}" y="{h-6}" text-anchor="middle" font-size="12.5" fill="var(--muted)" font-family="JetBrains Mono">{t}</text>'
    for col,vals,dash in series:
        pts=[(pl+i*iw/(len(vals)-1),pt+ih-(v-ylo)/(yhi-ylo)*ih) for i,v in enumerate(vals)]
        d='M'+'L'.join(f'{x:.1f},{y:.1f}' for x,y in pts)
        o+=f'<path d="{d}" fill="none" stroke="{col}" stroke-width="2.6" stroke-linejoin="round" stroke-linecap="round" {"stroke-dasharray=\"6 5\"" if dash else ""}/><circle cx="{pts[-1][0]:.1f}" cy="{pts[-1][1]:.1f}" r="4" fill="{col}"/>'
    return f'<svg width="{w}" height="{h}" viewBox="0 0 {w} {h}">{o}</svg>'

def barchart(items,w,h,maxv=100,lw=170):
    # horizontal bars: items (label,value,key)
    rowh=h/len(items); o=''
    for i,(l,v,c) in enumerate(items):
        y=i*rowh
        o+=f'<text x="0" y="{y+rowh*0.62:.1f}" font-size="15" fill="var(--text)" font-family="Inter">{l}</text>'
        bw=(w-190)*v/maxv
        o+=f'<rect x="{lw}" y="{y+rowh*0.25:.1f}" width="{w-lw-20-40}" height="{rowh*0.5:.1f}" rx="4" fill="var(--border)"/><rect x="{lw}" y="{y+rowh*0.25:.1f}" width="{bw:.1f}" height="{rowh*0.5:.1f}" rx="4" fill="var(--{c})"/><text x="{w}" y="{y+rowh*0.62:.1f}" text-anchor="end" font-size="15" font-weight="600" fill="var(--text)" font-family="JetBrains Mono">{v}</text>'
    return f'<svg width="{w}" height="{h}" viewBox="0 0 {w} {h}">{o}</svg>'

def badge(v): c=cls(v); return f'<span class="badge b-{c}">{LAB[c]}</span>'

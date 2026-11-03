from ui import *
import numpy as np
fmt=lambda v:str(v).replace('.',',')
CITY=int(round(np.mean([d['geral'] for d in dists])))
TOP=rank('geral')[:5]
TOPN=[d['name'] for d in TOP]
def trend(d): return 2+int(rnd(d['name'],'t')*7)
def minibars(d):
    return ''.join(f'<div style="display:flex;align-items:center;gap:8px"><span class="mono" style="width:26px;font-size:15px">{d[k]}</span><div class="bar" style="width:70px"><i style="width:{d[k]}%;background:var(--{cls(d[k])})"></i></div></div>' for k in ('env','hl','soc'))

def overview(light):
    k=lambda icn,lab,val,unit,sp,extra,col='var(--accent)':f'<div class="card kpi" style="flex:1"><div class="lab">{ic(icn)}{lab}</div><div class="val mono">{val}<small>{unit}</small></div><div style="display:flex;align-items:center;justify-content:space-between;gap:10px">{extra}{spark(sp,120,40,col)}</div></div>'
    kp=''.join([
     k('layout','Risco geral',CITY,'/100',[38,40,39,42,41,44,43,46,45,CITY-1,CITY],f'<span class="badge b-{cls(CITY)}">{LAB[cls(CITY)]}</span>','var(--moderado)'),
     k('heat','Temp. máxima','34,2','°C',[29,30,31,33,32,35,34,36,34],'<span class="muted" style="font-size:14px">+2,1 °C vs. média</span>','var(--alto)'),
     k('wind','Qualidade do ar','82','IQAR',[60,70,66,80,92,85,78,88,82],'<span class="badge b-moderado">Moderada</span>','var(--moderado)'),
     k('pulse','Incidência de dengue','143','/100 mil',[90,98,105,112,118,125,133,140,143],'<span class="muted" style="font-size:14px">SE 43 · +2,1%</span>','var(--alto)'),
     k('bell','Alertas ativos','7','',[2,3,2,4,5,4,6,7,7],'<span class="badge b-alto">2 altos</span>','var(--alto)')])
    rows=''.join(f'<tr><td class="mono muted">{i+1}</td><td style="font-weight:600">{d["name"]}</td><td><div style="display:flex;gap:14px">{minibars(d)}</div></td><td class="mono" style="font-size:24px;font-weight:600">{d["geral"]}</td><td>{badge(d["geral"])}</td><td class="mono t-alto">▲ +{trend(d)}</td></tr>' for i,d in enumerate(TOP))
    # simplify minibars into separate columns
    rows=''
    for i,d in enumerate(TOP):
        cells=''.join(f'<td><div style="display:flex;align-items:center;gap:8px"><span class="mono" style="width:26px">{d[k_]}</span><div class="bar" style="width:84px"><i style="width:{d[k_]}%;background:var(--{cls(d[k_])})"></i></div></div></td>' for k_ in ('env','hl','soc'))
        rows+=f'<tr><td class="mono muted" style="width:40px">{i+1}</td><td style="font-weight:600;font-size:18px">{d["name"]}</td>{cells}<td class="mono" style="font-size:26px;font-weight:600">{d["geral"]}</td><td>{badge(d["geral"])}</td><td class="mono t-alto">▲ +{trend(d)}</td></tr>'
    top5=f'<div class="card" style="flex:none"><h3>Top 5 distritos de maior risco · semana 26 out – 1 nov</h3><table><tr><th></th><th>Distrito</th><th>Ambiental</th><th>Saúde</th><th>Social</th><th>Geral</th><th>Classe</th><th>vs. sem. ant.</th></tr>{rows}</table></div>'
    ch=linechart([('var(--moderado)',[38,39,41,40,43,42,44,43,45,CITY-2,CITY-1,CITY],0)],900,150,['ago','','','set','','','out','','','','','nov'],20,60,[20,30,40,50,60],bands=True)
    trendc=f'<div class="card" style="flex:1;min-height:0"><h3>Risco geral da cidade · últimas 12 semanas</h3>{ch}</div>'
    legend=''.join(f'<div><i style="background:var(--{c})"></i>{LAB[c]} <span class="muted mono" style="margin-left:auto;font-size:13px">{r}</span></div>' for c,r in [('baixo','0–29'),('moderado','30–54'),('alto','55–74'),('critico','75–100')])
    mp=f'<div class="card" style="width:560px;display:flex;flex-direction:column"><div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:6px"><h3 style="margin:0">Mapa de risco geral</h3><span class="chip" style="padding:5px 12px;font-size:14px">Geral {ic("down")}</span></div><div style="display:flex;gap:18px;align-items:center;flex:1"><div style="flex:1;display:flex;justify-content:center">{map_svg(360,660,"geral",sw=.7)}</div><div class="legend" style="width:130px">{legend}</div></div></div>'
    return f'<div class="row">{kp}</div><div class="row" style="flex:1;min-height:0"><div style="flex:1;display:flex;flex-direction:column;gap:22px;min-width:0">{top5}{trendc}</div>{mp}</div>'

def mapa(light,key='geral'):
    seg='<div class="seg">'+''.join(f'<span class="{"on" if k_==key else ""}">{t}</span>' for k_,t in [('amb','Ambiental'),('sau','Saúde'),('soc','Social'),('geral','Geral')])+'</div>'
    sel=BY['Capão Redondo']
    legend=''.join(f'<div><i style="background:var(--{c})"></i>{LAB[c]} <span class="muted mono" style="margin-left:auto;font-size:13px;padding-left:18px">{r}</span></div>' for c,r in [('baixo','0–29'),('moderado','30–54'),('alto','55–74'),('critico','75–100')])
    # tooltip
    tt=f'''<div style="position:absolute;left:14px;top:330px;width:280px;background:var(--elev);border:1px solid var(--border);border-radius:12px;padding:14px 16px;box-shadow:0 12px 30px rgba(0,0,0,.35)"><div style="display:flex;justify-content:space-between;align-items:center"><b style="font-size:18px">{sel['name']}</b>{badge(sel['geral'])}</div><div class="mono" style="font-size:34px;font-weight:600;margin:4px 0">{sel['geral']}<small style="font-size:15px;color:var(--muted)"> /100</small></div><div class="muted" style="font-size:14px;line-height:1.7">Ambiental <b class="mono" style="color:var(--text)">{sel['env']}</b> · Saúde <b class="mono" style="color:var(--text)">{sel['hl']}</b> · Social <b class="mono" style="color:var(--text)">{sel['soc']}</b></div></div>'''
    mapcard=f'<div class="card" style="flex:1;position:relative;padding:12px;display:flex;justify-content:center;overflow:hidden"><div style="position:absolute;left:24px;top:22px">{seg}</div>{map_svg(780,800,key,hlmap="Capão Redondo",sw=.8)}{tt}<div class="card" style="position:absolute;left:24px;bottom:22px;padding:16px 18px;background:var(--elev)"><div class="muted" style="font-size:13px;text-transform:uppercase;letter-spacing:.06em;margin-bottom:10px;font-weight:600">Risco · Geral</div><div class="legend">{legend}</div></div></div>'
    rk=''.join(f'<div style="display:flex;align-items:center;gap:12px;padding:10px 0;border-top:1px solid var(--border)"><span class="mono muted" style="width:24px">{i+1}</span><span style="flex:1;font-weight:500">{d["name"]}</span><span class="mono" style="font-weight:600">{d["geral"]}</span>{badge(d["geral"])}</div>' for i,d in enumerate(rank('geral')[:9]))
    right=f'<div class="card" style="width:430px;display:flex;flex-direction:column"><h3>Maiores riscos · Geral</h3>{rk}<div class="muted" style="font-size:14px;margin-top:auto;padding-top:14px">Clique em um distrito no mapa para abrir o detalhe.</div></div>'
    # timeline
    weeks=['10 ago','17 ago','24 ago','31 ago','7 set','14 set','21 set','28 set','5 out','12 out','19 out','26 out']
    tl=''.join(f'<div style="flex:1;text-align:center"><div style="height:10px;border-radius:5px;background:{"var(--accent)" if i==11 else "var(--elev)"};margin:0 3px"></div><div class="mono" style="font-size:13px;margin-top:8px;color:{"var(--accent)" if i==11 else "var(--muted)"}">{w}</div></div>' for i,w in enumerate(weeks))
    tlc=f'<div class="card" style="padding:16px 22px;display:flex;align-items:center;gap:18px;flex:none"><span class="chip">{ic("left")}</span><div style="flex:1;display:flex">{tl}</div><span class="chip">{ic("right")}</span><span class="chip" style="color:var(--accent);border-color:var(--accent)">Hoje</span></div>'
    return f'<div class="row" style="flex:1;min-height:0">{mapcard}{right}</div>{tlc}'

def detalhe(light):
    d=BY['Capão Redondo']
    sc=''.join(f'<div class="card" style="flex:1;padding:18px"><div class="muted" style="font-size:14px;font-weight:500">{t}</div><div class="mono" style="font-size:40px;font-weight:600;margin:4px 0;display:flex;align-items:baseline;gap:8px;color:{"var(--text)"}">{d[k_]}<span style="font-size:15px" class="t-{cls(d[k_])}">{LAB[cls(d[k_])]}</span></div><div class="bar"><i style="width:{d[k_]}%;background:var(--{cls(d[k_])})"></i></div></div>' for t,k_ in [('Risco geral','geral'),('Ambiental','env'),('Saúde','hl'),('Social','soc')])
    x=['ago','','','set','','','out','','','','','nov']
    def ind(icn,t,val,unit,series,ylo,yhi,tk,col,sub):
        return f'<div class="card" style="flex:1;padding:18px 20px"><div style="display:flex;justify-content:space-between;align-items:flex-start"><div class="kpi"><div class="lab">{ic(icn)}{t}</div><div class="val mono" style="font-size:34px;margin:4px 0 0">{val}<small>{unit}</small></div></div><span class="muted" style="font-size:13.5px;text-align:right;margin-top:4px">{sub}</span></div>{linechart([(col,series,0)],465,250,x,ylo,yhi,tk)}</div>'
    grid=f'''<div class="row" style="flex:1;min-height:0">{ind("rain","Chuva acumulada","48","mm / 7 dias",[10,22,15,30,18,25,40,22,35,28,52,48],0,60,[0,30,60],"var(--accent)","INMET / Open-Meteo")}{ind("wind","Qualidade do ar","91","IQAR",[60,72,70,85,78,95,88,100,92,86,94,91],40,120,[40,80,120],"var(--moderado)","CETESB QUALAR")}</div>
<div class="row" style="flex:1;min-height:0">{ind("pulse","Dengue","171","casos/100 mil",[70,80,92,105,110,124,130,138,150,158,165,171],40,200,[40,120,200],"var(--alto)","InfoDengue · SE 43")}{ind("pulse","Internações respiratórias","34","/100 mil hab.",[22,24,23,27,30,28,31,33,30,35,32,34],10,40,[10,25,40],"var(--critico)","DataSUS SIH · set/2026")}</div>'''
    left=f'<div style="flex:1;display:flex;flex-direction:column;gap:22px;min-width:0"><div class="row" style="flex:none">{sc}</div>{grid}</div>'
    fac=''.join(f'<div style="display:flex;align-items:center;gap:12px;padding:11px 0;border-top:1px solid var(--border)"><span style="flex:1">{t}</span><span class="mono t-{c}" style="font-weight:600">{v}</span></div>' for t,v,c in [('Alertas de alagamento (CEMADEN)','2 ativos','alto'),('Precipitação acima da média','+38%','alto'),('Incidência de dengue','171/100 mil','alto'),('Vulnerabilidade social (Censo 2022)','Alta','alto'),('Focos de queimadas (INPE)','0','baixo')])
    right=f'<div style="width:480px;display:flex;flex-direction:column;gap:22px"><div class="card" style="padding:14px;display:flex;justify-content:center">{map_svg(450,420,"geral",hl=[d["name"]],hlmap=d["name"],bbox=(d["bbox"][0]-.045,d["bbox"][1]-.04,d["bbox"][2]+.045,d["bbox"][3]+.04),strokec="var(--border)",sw=1)}</div><div class="card" style="flex:1"><h3>Principais fatores</h3>{fac}</div></div>'
    back=f'<div class="muted" style="display:flex;align-items:center;gap:6px;font-size:15px;margin-bottom:-8px;margin-top:-6px">{ic("left")[:-6]+"</svg>"}</div>'
    return f'<div class="row" style="flex:1;min-height:0">{left}{right}</div>'

def chat(light):
    comb=sorted(dists,key=lambda d:-(d['env']+d['hl']))[:5]
    off={1:(-26,0),2:(26,10),3:(0,0),4:(26,-8),5:(0,0)}
    labs=[(d['name'],str(i+1))+off[i+1] for i,d in enumerate(comb)]
    mp=map_svg(250,420,None,sw=.6)
    mp=map_svg(205,345,'geral',hl=[d['name'] for d in comb],labels=labs,sw=.5)
    rows=''.join(f'<tr><td class="mono muted" style="width:30px;padding:9px 8px">{i+1}</td><td style="padding:9px 8px;font-weight:600">{d["name"]}</td><td class="mono" style="padding:9px 8px">{d["env"]}</td><td class="mono" style="padding:9px 8px">{d["hl"]}</td><td class="mono" style="padding:9px 8px;font-weight:600">{(d["env"]+d["hl"])//2}</td></tr>' for i,d in enumerate(comb))
    tbl=f'<table style="font-size:15.5px"><tr><th style="padding:0 8px 8px"></th><th style="padding:0 8px 8px">Distrito</th><th style="padding:0 8px 8px">Amb.</th><th style="padding:0 8px 8px">Saúde</th><th style="padding:0 8px 8px">Comb.</th></tr>{rows}</table>'
    bars=barchart([(d['name'],(d['env']+d['hl'])//2,cls((d['env']+d['hl'])//2)) for d in comb],330,230,lw=132)
    cite=lambda n:f'<span class="chip mono" style="padding:1px 8px;font-size:13px;color:var(--accent);border-color:var(--accent);margin:0 2px">{n}</span>'
    user=f'<div style="align-self:flex-end;max-width:760px;background:var(--accent-bg);border:1px solid var(--accent);border-radius:16px 16px 4px 16px;padding:14px 20px;font-size:18px">Quais regiões de São Paulo estão apresentando maior risco ambiental e de saúde esta semana?</div>'
    tools=''.join(f'<span class="chip mono" style="font-size:13px;padding:5px 11px">{ic("tool")}{t}</span>' for t in ['get_risk(semana=2026-W44)','sql_marts · ranking por distrito','search_docs · metodologia'])
    ans=f'''<div style="display:flex;gap:14px;align-items:flex-start"><div style="flex:none;margin-top:2px">{logo(34)}</div><div style="flex:1;min-width:0;display:flex;flex-direction:column;gap:14px">
<div style="display:flex;gap:8px;flex-wrap:wrap">{tools}</div>
<div style="font-size:18px;line-height:1.55">Na semana de <b>26 out a 1 nov de 2026</b>, os distritos com maior risco combinado (ambiental + saúde) estão concentrados nas zonas <b>Sul</b> e <b>Leste</b>. <b>{comb[0]['name']}</b> lidera ({(comb[0]['env']+comb[0]['hl'])//2}), puxado por chuva acima da média e alertas de alagamento{cite(1)}{cite(3)} e por dengue em alta{cite(4)}.</div>
<div class="card" style="padding:16px 30px 16px 40px;background:var(--elev);display:flex;gap:30px"><div style="flex:none">{mp}</div><div style="flex:none"><h3 style="margin-bottom:8px">Top 5 · ambiental + saúde</h3>{tbl}</div><div style="flex:none;width:330px"><h3 style="margin-bottom:8px">Índice combinado</h3>{bars}</div></div>
<div style="font-size:17px;line-height:1.55;color:var(--text)">Atenção: as internações respiratórias (DataSUS) têm defasagem mensal{cite(5)}; o índice de saúde usa a competência de setembro. Pontuação conforme a metodologia v0.1{cite(6)}.</div>
</div></div>'''
    conv=f'<div style="flex:1;display:flex;flex-direction:column;gap:20px;overflow:hidden;min-height:0">{user}{ans}</div>'
    sug=''.join(f'<span class="chip" style="font-size:15px">{t}</span>' for t in ['Como o risco social é calculado?','Compare Grajaú e Itaquera','Há alertas de alagamento hoje?'])
    inp=f'<div style="display:flex;flex-direction:column;gap:12px;flex:none"><div style="display:flex;gap:10px">{sug}</div><div class="card" style="padding:14px 18px;display:flex;align-items:center;gap:14px;border-radius:14px"><span style="flex:1;color:var(--muted);font-size:18px">Pergunte sobre risco ambiental, saúde ou social em São Paulo…</span><span style="width:42px;height:42px;border-radius:10px;background:var(--accent);display:flex;align-items:center;justify-content:center;color:var(--bg)"><svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">{ICONS["send"]}</svg></span></div></div>'
    srcs=[('1','INMET / Open-Meteo','Chuva e temperatura · 02/11, 08:00'),('2','CETESB QUALAR','IQAR por estação · 02/11, 07:00'),('3','CEMADEN','Alertas de alagamento · 02/11, 09:10'),('4','InfoDengue','Semana epidemiológica 43 · 31/10'),('5','DataSUS SIH','Internações respiratórias · set/2026'),('6','Metodologia EcoGuard v0.1','Documento interno · fórmula do score')]
    sl=''.join(f'<div style="display:flex;gap:12px;padding:12px 0;border-top:1px solid var(--border)"><span class="chip mono" style="padding:1px 9px;font-size:13px;color:var(--accent);border-color:var(--accent);height:26px">{n}</span><div><div style="font-weight:600;font-size:16px">{t}</div><div class="muted" style="font-size:14px">{s}</div></div></div>' for n,t,s in srcs)
    side=f'<div class="card" style="width:400px;display:flex;flex-direction:column"><h3>Fontes citadas</h3>{sl}<div class="muted" style="font-size:14px;margin-top:auto;padding-top:12px;line-height:1.5">As respostas citam fonte e data. O analista só lê dados (acesso somente leitura).</div></div>'
    return f'<div class="row" style="flex:1;min-height:0"><div style="flex:1;display:flex;flex-direction:column;gap:18px;min-width:0">{conv}{inp}</div>{side}</div>'

def alertas(light):
    al=[('critico','Alagamento','waves','Capão Redondo · Jardim Ângela','CEMADEN','Alerta de alagamento · risco muito alto de transbordo de córregos','há 25 min · válido até 18:00'),
        ('alto','Chuva forte','rain','Zona Sul · 7 distritos','INMET','Previsão de chuva > 50 mm em 24 h','há 1 h · válido até 03/11, 06:00'),
        ('alto','Deslizamento','warn','Brasilândia · Jaraguá','CEMADEN','Alerta de deslizamento em áreas de encosta','há 2 h · válido até 18:00'),
        ('moderado','Calor','heat','Cidade inteira','INMET','Temperatura máxima prevista de 36 °C · baixa umidade','há 3 h · válido até 03/11'),
        ('moderado','Qualidade do ar','wind','Pinheiros · Santo Amaro','CETESB QUALAR','IQAR 112 (ruim) em estação · material particulado','há 4 h'),
        ('moderado','Queimadas','flame','Parelheiros · Marsilac','INPE BDQueimadas','3 focos de calor detectados nas últimas 24 h','há 6 h'),
        ('baixo','Vento forte','wind','Zona Norte','INMET','Rajadas de até 45 km/h previstas à noite','há 8 h')]
    items=''
    for c,t,i,reg,src,desc,when in al:
        items+=f'<div class="card" style="padding:16px 20px;display:flex;gap:18px;align-items:center;border-left:5px solid var(--{c})"><span style="width:46px;height:46px;border-radius:12px;background:var(--elev);display:flex;align-items:center;justify-content:center;color:var(--t-{c});flex:none"><svg viewBox="0 0 24 24" width="26" height="26" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round">{ICONS[i]}</svg></span><div style="flex:1;min-width:0"><div style="display:flex;gap:10px;align-items:center"><b style="font-size:18px">{t}</b><span class="badge b-{c}">{LAB[c]}</span></div><div style="font-size:16px;margin-top:2px">{desc}</div><div class="muted" style="font-size:14px;margin-top:2px">{reg} · Fonte: {src}</div></div><div class="muted mono" style="font-size:13.5px;text-align:right;flex:none">{when}</div></div>'
    flt=''.join(f'<span class="chip" style="{"color:var(--accent);border-color:var(--accent);background:var(--accent-bg)" if i==0 else ""}">{t}</span>' for i,t in enumerate(['Todos (7)','Chuva','Calor','Qualidade do ar','Enchente','Queimadas']))
    sev=f'<div class="seg">'+''.join(f'<span class="{"on" if i==0 else ""}">{t}</span>' for i,t in enumerate(['Todas severidades','Crítico','Alto','Moderado']))+'</div>'
    # map with markers
    mk=''
    b=BBOX; x0,y0,x1,y1=b; w,h=400,640; sc=min(w/(x1-x0),h/(y1-y0)); ox=(w-(x1-x0)*sc)/2; oy=(h-(y1-y0)*sc)/2
    pts=[('Capão Redondo','critico'),('Jardim Ângela','critico'),('Grajaú','alto'),('Brasilândia','alto'),('Jaraguá','alto'),('Pinheiros','moderado'),('Santo Amaro','moderado'),('Parelheiros','moderado'),('Marsilac','moderado'),('Itaquera','baixo')]
    for n,c in pts:
        d=BY[n]; x=(d['cx']*K-x0)*sc+ox; y=(-d['cy']-y0)*sc+oy
        mk+=f'<circle cx="{x:.1f}" cy="{y:.1f}" r="15" fill="var(--{c})" opacity=".25"/><circle cx="{x:.1f}" cy="{y:.1f}" r="7" fill="var(--{c})" stroke="var(--bg)" stroke-width="2"/>'
    base=map_svg(w,h,None,sw=.6,strokec='var(--border)').replace('</svg>',mk+'</svg>')
    mp=f'<div class="card" style="width:470px;display:flex;flex-direction:column;align-items:center"><h3 style="align-self:flex-start">Localização dos alertas</h3>{base}</div>'
    return f'<div style="display:flex;gap:12px;align-items:center;flex:none">{flt}<div class="sp"></div>{sev}</div><div class="row" style="flex:1;min-height:0"><div style="flex:1;display:flex;flex-direction:column;gap:12px;min-width:0">{items}</div>{mp}</div>'

def fontes(light):
    S=[('INMET / Open-Meteo','Temperatura, chuva e previsão','amb','Horária','~1 h','02/11 08:00'),
       ('CETESB QUALAR','Índice de qualidade do ar (IQAR), PM2,5, O₃','amb','Horária','~2 h','02/11 07:00'),
       ('INPE BDQueimadas','Focos de calor (satélites)','amb','A cada 3 h','~3 h','02/11 06:30'),
       ('CEMADEN','Alertas de alagamento e deslizamento','amb','Quase em tempo real','~15 min','02/11 09:10'),
       ('InfoDengue','Casos e incidência de dengue','sau','Semanal','1 semana epid.','31/10 (SE 43)'),
       ('DataSUS SIH','Internações por doenças respiratórias','sau','Mensal','~2 meses','set/2026'),
       ('IBGE Censo 2022','População, renda e domicílios','soc','Estática','—','Censo 2022'),
       ('GeoSampa','Limites dos distritos e infraestrutura','soc','Sob demanda','—','rev. 2026')]
    tag={'amb':('Ambiental','accent'),'sau':('Saúde','accent'),'soc':('Social','accent')}
    rows=''.join(f'<tr><td style="font-weight:600">{a}</td><td class="muted" style="font-size:15.5px">{b}</td><td><span class="chip" style="padding:3px 12px;font-size:14px">{tag[c][0]}</span></td><td style="font-size:15.5px">{d}</td><td class="mono muted" style="font-size:14.5px">{e}</td><td class="mono" style="font-size:14.5px"><span class="dot" style="display:inline-block;margin-right:8px"></span>{f}</td></tr>' for a,b,c,d,e,f in S)
    tb=f'<div class="card" style="flex:1.45;min-width:0;padding-bottom:10px;--tdp:19px"><h3>Fontes de dados</h3><table><tr><th>Fonte</th><th>Dados</th><th>Camada</th><th>Frequência</th><th>Defasagem</th><th>Última atualização</th></tr>{rows}</table></div>'
    form=f'''<div class="card"><h3>Como calculamos o risco</h3><div class="mono" style="font-size:20px;padding:16px 18px;background:var(--elev);border-radius:10px;border:1px solid var(--border);line-height:1.8"><span style="color:var(--accent)">Geral</span> = 0,40 × Ambiental<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;+ 0,35 × Saúde<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;+ 0,25 × Social</div><div class="muted" style="font-size:15.5px;margin-top:14px;line-height:1.55">Cada camada é a média ponderada de indicadores normalizados de 0 a 100 por distrito. Os pesos são parâmetros da versão 0.1 e podem ser ajustados.</div></div>'''
    comp=''.join(f'<div style="display:flex;gap:10px;padding:8px 0;border-top:1px solid var(--border);font-size:15.5px"><b style="width:100px;color:var(--accent)">{a}</b><span class="muted">{b}</span></div>' for a,b in [('Ambiental','chuva, calor, qualidade do ar, queimadas, alertas'),('Saúde','dengue e internações respiratórias'),('Social','população vulnerável, renda, infraestrutura')])
    cl=''.join(f'<div style="flex:1"><div style="height:12px;border-radius:6px;background:var(--{c})"></div><div style="font-weight:600;margin-top:8px;font-size:16px" class="t-{c}">{LAB[c]}</div><div class="mono muted" style="font-size:13.5px">{r}</div></div>' for c,r in [('baixo','0–29'),('moderado','30–54'),('alto','55–74'),('critico','75–100')])
    right=f'<div style="width:620px;display:flex;flex-direction:column;gap:22px">{form}<div class="card"><h3>Composição das camadas</h3>{comp}</div><div class="card"><h3>Classes de risco</h3><div style="display:flex;gap:12px">{cl}</div></div></div>'
    return f'<div class="row" style="flex:1;min-height:0">{tb}{right}</div>'


def oque(light):
    def step(n,ic_,t,d):
        return f'<div style="flex:1;display:flex;gap:14px;align-items:flex-start"><div style="flex:none;width:44px;height:44px;border-radius:12px;background:var(--accent-bg);color:var(--accent);display:flex;align-items:center;justify-content:center"><span style="width:24px;display:block">{ic(ic_)}</span></div><div><div class="mono muted" style="font-size:13px">PASSO {n}</div><div style="font-weight:600;font-size:18px;margin:2px 0 4px">{t}</div><div class="muted" style="font-size:15px;line-height:1.45">{d}</div></div></div>'
    arrow=f'<div style="flex:none;align-self:center;color:var(--muted);width:24px">{ic("right")}</div>'
    steps=arrow.join([
      step(1,'db','Coleta de fontes públicas','Clima, qualidade do ar, queimadas, alertas, dengue, internações e Censo.'),
      step(2,'pulse','Score por distrito','Indicadores normalizados e combinados numa fórmula aberta, para os 96 distritos.'),
      step(3,'map','Mapa e alertas','Risco ambiental, de saúde e social visível por distrito, semana a semana.'),
      step(4,'chat','Analista de IA','Responde perguntas em linguagem natural e cita fonte e data de cada resposta.')])
    def pill(t): return f'<span class="chip" style="font-size:15.5px;padding:8px 15px;background:var(--elev)">{t}</span>'
    who=''.join(pill(t) for t in ['Gestores públicos','Defesa civil','Saúde','Pesquisadores','Cidadãos'])
    def front(c_,icn,t,d):
        return f'<div style="flex:1;background:var(--elev);border-radius:12px;padding:14px 16px;border-top:3px solid var(--accent)"><div style="display:flex;align-items:center;gap:9px;font-weight:600;font-size:17px"><span style="width:20px;color:var(--accent)">{ic(icn)}</span>{t}</div><div class="muted" style="font-size:14.5px;margin-top:6px;line-height:1.4">{d}</div></div>'
    fronts=''.join([front('','rain','Ambiental','Chuva, calor, qualidade do ar, queimadas e alertas de alagamento.'),front('','pulse','Saúde','Dengue e internações por doenças respiratórias.'),front('','users','Social','População vulnerável, renda e infraestrutura.')])
    srcs=''.join(f'<span class="mono" style="font-size:14px;padding:6px 10px;border:1px solid var(--border);border-radius:8px;background:var(--elev)">{s}</span>' for s in ['INMET','Open-Meteo','CETESB QUALAR','INPE BDQueimadas','CEMADEN','InfoDengue','DataSUS SIH','IBGE Censo 2022','GeoSampa'])
    h3='font-size:13.5px'
    hero=f'''<div class="card" style="display:flex;gap:40px;align-items:center;padding:20px 36px;height:240px;flex:none;overflow:hidden">
<div style="flex:1"><div class="mono" style="font-size:14px;color:var(--accent);letter-spacing:.08em;text-transform:uppercase;margin-bottom:8px">Inteligência urbana para São Paulo</div>
<div style="font-size:41px;font-weight:600;letter-spacing:-.03em;line-height:1.1;white-space:nowrap">O risco de cada distrito de São Paulo, num só lugar.</div>
<div class="muted" style="font-size:17.5px;margin-top:8px;max-width:900px;line-height:1.45">O EcoGuard junta dados públicos ambientais, de saúde e sociais em um score de risco transparente por distrito, e um analista de IA que responde com as fontes.</div>
<div style="display:flex;gap:14px;margin-top:16px"><span style="background:var(--accent);color:var(--bg);border-radius:10px;padding:10px 20px;font-weight:600;font-size:17px;display:inline-flex;gap:10px;align-items:center"><span style="width:20px">{ic('map')}</span>Explorar o mapa</span><span style="border:1px solid var(--accent);color:var(--accent);border-radius:10px;padding:10px 20px;font-weight:600;font-size:17px;display:inline-flex;gap:10px;align-items:center"><span style="width:20px">{ic('chat')}</span>Perguntar ao analista</span></div></div>
<div style="flex:none">{map_svg(200,196,'geral',sw=.5)}</div></div>'''
    prob=f'''<div class="card" style="flex:1"><h3>O problema</h3><div style="font-size:21px;font-weight:600;letter-spacing:-.01em;line-height:1.3">Os riscos da cidade estão espalhados em lugares diferentes.</div><div class="muted" style="font-size:15.5px;margin-top:8px;line-height:1.45">Clima, qualidade do ar, saúde e vulnerabilidade social são acompanhados em sistemas separados. Quem decide precisa abrir várias fontes para juntar o quadro, e os sinais de risco chegam tarde.</div></div>'''
    does=f'''<div class="card" style="flex:1;border-color:var(--accent)"><h3 style="color:var(--accent)">O que o EcoGuard faz</h3><div style="font-size:21px;font-weight:600;letter-spacing:-.01em;line-height:1.3">Une os dados por distrito e explica o resultado.</div><div class="muted" style="font-size:15.5px;margin-top:8px;line-height:1.45">Reúne dados públicos num score de risco transparente, com a fórmula e as fontes à vista. O AI City Analyst responde perguntas sobre a cidade e cita a fonte de cada resposta.</div></div>'''
    how=f'<div class="card" style="flex:none;padding:16px 24px 18px"><h3 style="margin-bottom:12px">Como funciona</h3><div style="display:flex;gap:14px">{steps}</div></div>'
    bottom=f'''<div style="display:flex;gap:22px;flex:1;min-height:0">
<div class="card" style="width:430px;flex:none"><h3>Para quem é</h3><div style="display:flex;flex-wrap:wrap;gap:10px">{who}</div></div>
<div class="card" style="flex:1.5"><h3>Três frentes</h3><div style="display:flex;gap:14px">{fronts}</div></div>
<div class="card" style="flex:1"><h3>Fontes públicas</h3><div style="display:flex;flex-wrap:wrap;gap:8px">{srcs}</div></div></div>'''
    note=f'<div style="flex:none;display:flex;align-items:center;gap:12px;padding:9px 18px;border:1px dashed var(--t-moderado);border-radius:12px;color:var(--text);font-size:15px"><span style="width:20px;color:var(--t-moderado)">{ic("info")}</span><span><b>Projeto de portfólio / demonstração.</b> <span class="muted">Todos os números, séries e alertas mostrados são dados de exemplo, não medições reais.</span></span></div>'
    return hero+f'<div class="row" style="flex:none">{prob}{does}</div>'+how+bottom+note

SCREENS=[('00-o-que-e',oque,'O que é o EcoGuard','Plataforma de inteligência urbana para São Paulo',None,'O que é o EcoGuard'),
('01-overview',overview,'Visão geral','Panorama de risco da cidade de São Paulo',None,'Visão geral'),
('02-mapa-risco',mapa,'Mapa de risco','Risco por distrito · camadas e período',DATE_CHIP,'Mapa de risco'),
('03-detalhe-distrito',detalhe,'Capão Redondo','Subprefeitura M\'Boi Mirim · Zona Sul · risco crítico (exemplo)',DATE_CHIP,'Mapa de risco'),
('04-analista-ia',chat,'AI City Analyst','Pergunte aos dados da cidade, com fontes citadas',None,'Analista IA'),
('05-alertas',alertas,'Alertas ativos','Alertas meteorológicos, de enchente e de queimadas',None,'Alertas'),
('06-fontes-metodologia',fontes,'Fontes e metodologia','De onde vêm os dados e como o risco é calculado',None,'Fontes e metodologia')]
if __name__=='__main__':
    import os,subprocess
    os.makedirs('../html',exist_ok=True); os.makedirs('../mockups',exist_ok=True)
    only=os.environ.get('ONLY')
    for name,fn,title,sub,extra,act in SCREENS:
        if only and only not in name: continue
        for light in (False,True):
            if light and os.environ.get('NOLIGHT'): continue
            h=page(act,title,sub,fn(light),light,extra or '')
            suffix='claro' if light else 'escuro'
            hp=f'/workspace/ecoguard-design/html/{name}-{suffix}.html'; open(hp,'w').write(h)
            out=f'/workspace/ecoguard-design/mockups/{name}-{suffix}.png'
            subprocess.run(['google-chrome','--headless=new','--no-sandbox','--disable-gpu','--hide-scrollbars','--force-device-scale-factor=1','--window-size=1920,1167',f'--screenshot={out}','--virtual-time-budget=2000',f'file://{hp}'],capture_output=True)
            from PIL import Image
            im=Image.open(out); im=im.crop((0,0,1920,1080)); im.save(out); print(out,im.size)

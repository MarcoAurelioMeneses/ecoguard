import json, math, hashlib
import numpy as np

ACC = {'AGUA RASA':'Água Rasa','ALTO DE PINHEIROS':'Alto de Pinheiros','BELEM':'Belém','BRAS':'Brás','BRASILANDIA':'Brasilândia','BUTANTA':'Butantã','CANGAIBA':'Cangaíba','CAPAO REDONDO':'Capão Redondo','CARRAO':'Carrão','CIDADE LIDER':'Cidade Líder','CONSOLACAO':'Consolação','FREGUESIA DO O':'Freguesia do Ó','GRAJAU':'Grajaú','JACANA':'Jaçanã','JAGUARE':'Jaguaré','JARAGUA':'Jaraguá','JARDIM ANGELA':'Jardim Ângela','JARDIM SAO LUIS':'Jardim São Luís','JOSE BONIFACIO':'José Bonifácio','LIMAO':'Limão','REPUBLICA':'República','SACOMA':'Sacomã','SANTA CECILIA':'Santa Cecília','SAO DOMINGOS':'São Domingos','SAO LUCAS':'São Lucas','SAO MATEUS':'São Mateus','SAO MIGUEL':'São Miguel','SAO RAFAEL':'São Rafael','SAUDE':'Saúde','SE':'Sé','TATUAPE':'Tatuapé','TREMEMBE':'Tremembé','VILA CURUCA':'Vila Curuçá','VILA JACUI':'Vila Jacuí','VILA SONIA':'Vila Sônia'}
def nice(n):
    if n in ACC: return ACC[n]
    return ' '.join(w.capitalize() if w not in ('DE','DO') else w.lower() for w in n.split())

def rdp(pts, eps):
    if len(pts)<3: return pts
    a,b=pts[0],pts[-1]
    v=b-a; w=pts-a
    d=np.abs(v[0]*w[:,1]-v[1]*w[:,0])/(np.linalg.norm(v)+1e-12)
    if np.linalg.norm(v)<1e-9: d=np.linalg.norm(w,axis=1)
    i=int(np.argmax(d))
    if d[i]>eps:
        l=rdp(pts[:i+1],eps); r=rdp(pts[i:],eps); return np.vstack([l[:-1],r])
    return np.vstack([a,b])

raw=json.load(open('/workspace/ecoguard-design/src/distritos-sp.geojson'))
LAT0=-23.7
K=math.cos(math.radians(23.7))
def proj(c): return np.array([[x*K, -y] for x,y in c])
dists=[]
for f in raw['features']:
    p=f['properties']; g=f['geometry']
    polys=g['coordinates'] if g['type']=='MultiPolygon' else [g['coordinates']]
    rings=[]
    allp=[]
    for poly in polys:
        for ring in poly[:1]:
            P=proj(ring); allp.append(P)
            rings.append(rdp(P,0.00045))
    A=np.vstack(allp)
    cen=A.mean(axis=0)
    dists.append(dict(code=p['ds_codigo'],raw=p['ds_nome'],name=nice(p['ds_nome']),rings=rings,cx=cen[0]/K,cy=-cen[1],bbox=(A[:,0].min(),A[:,1].min(),A[:,0].max(),A[:,1].max())))
allb=np.array([d['bbox'] for d in dists])
BBOX=(allb[:,0].min(),allb[:,1].min(),allb[:,2].max(),allb[:,3].max())
BY={d['name']:d for d in dists}

# ---- example data (deterministic) ----
def rnd(name,salt):
    h=hashlib.md5((name+salt).encode()).hexdigest()
    return int(h[:8],16)/0xffffffff
ENVB={'Grajaú':26,'Parelheiros':22,'Marsilac':24,'Capão Redondo':18,'Jardim Ângela':20,'Cidade Dutra':14,'Brasilândia':22,'Perus':16,'Jaraguá':16,'Sapopemba':12,'Itaim Paulista':14,'São Miguel':10,'Vila Curuçá':14,'Pirituba':8,'Jaguara':10,'Cidade Tiradentes':12,'Anhanguera':14,'Vila Jacuí':10,'Jardim Helena':10,'Tremembé':14,'Jaçanã':8}
HLB={'Cidade Tiradentes':20,'Guaianases':16,'Itaim Paulista':16,'Grajaú':14,'Brasilândia':18,'Capão Redondo':14,'Jardim Ângela':12,'Lajeado':14,'Sapopemba':10,'José Bonifácio':8,'Vila Curuçá':8,'Iguatemi':8,'São Mateus':6,'Jardim Helena':10,'Perus':8}
for d in dists:
    dist=math.hypot((d['cx']+46.633)*K,(d['cy']+23.55))
    n=d['name']
    soc=22+105*dist+ (rnd(n,'s')-.5)*16
    soc+= {'Marsilac':-18,'Parelheiros':-4}.get(n,0)
    hl =24+58*dist+ENVB.get(n,0)*0+HLB.get(n,0)+ (rnd(n,'h')-.5)*20
    env=30+(rnd(n,'e')-.5)*26+ENVB.get(n,0)+ (8 if d['cy']<-23.7 else 0)*rnd(n,'e2')
    cl=lambda v:int(round(max(4,min(96,v))))
    st=lambda v:(v-17)*1.85+8
    d['env'],d['hl'],d['soc']=cl(st(env)),cl(st(hl)),cl(st(soc))
    d['geral']=cl(.40*d['env']+.35*d['hl']+.25*d['soc'])
def cls(v): return 'baixo' if v<30 else 'moderado' if v<55 else 'alto' if v<75 else 'critico'
LAB={'baixo':'Baixo','moderado':'Moderado','alto':'Alto','critico':'Crítico'}
def rank(key): return sorted(dists,key=lambda d:-d[key])
if __name__=='__main__':
    import collections
    for k in ('env','hl','soc','geral'):
        print(k,collections.Counter(cls(d[k]) for d in dists), round(np.mean([d[k] for d in dists])))
    for d in rank('geral')[:8]: print(d['name'],d['env'],d['hl'],d['soc'],d['geral'])

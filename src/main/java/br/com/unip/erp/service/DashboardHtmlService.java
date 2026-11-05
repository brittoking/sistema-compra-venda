package br.com.unip.erp.service;

import br.com.unip.erp.service.RelatorioService.Dashboard;
import br.com.unip.erp.service.RelatorioService.MesResumo;
import br.com.unip.erp.service.RelatorioService.MovimentoRecente;
import br.com.unip.erp.service.RelatorioService.PagamentoResumo;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/** Gera um dashboard HTML autônomo com os dados reais do banco e abre-o no navegador. */
public final class DashboardHtmlService {
    private final RelatorioService relatorios = new RelatorioService();
    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    public void abrirDashboard() throws Exception {
        Dashboard dados = relatorios.carregarDashboard();
        Path pasta = pastaRelatorios();
        Files.createDirectories(pasta);
        String carimbo = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        Path arquivo = pasta.resolve("dashboard-vendas-compras-" + carimbo + ".html");
        Files.writeString(arquivo, gerarHtml(dados), StandardCharsets.UTF_8);
        NavegadorUtil.abrir(arquivo);
    }

    private Path pastaRelatorios() {
        String home = System.getProperty("user.home", ".");
        return Path.of(home, "BrittoERP", "Relatorios");
    }

    private String gerarHtml(Dashboard d) {
        var i = d.indicadores();
        StringBuilder meses = new StringBuilder("[");
        for (int n = 0; n < d.meses().size(); n++) {
            MesResumo m = d.meses().get(n);
            if (n > 0) meses.append(',');
            meses.append("{label:").append(js(m.mes()))
                .append(",vendas:").append(numero(m.vendas()))
                .append(",compras:").append(numero(m.compras())).append('}');
        }
        meses.append(']');

        StringBuilder pagamentos = new StringBuilder("[");
        for (int n = 0; n < d.pagamentos().size(); n++) {
            PagamentoResumo p = d.pagamentos().get(n);
            if (n > 0) pagamentos.append(',');
            pagamentos.append("{nome:").append(js(p.nome()))
                .append(",total:").append(numero(p.total())).append('}');
        }
        pagamentos.append(']');

        StringBuilder movimentos = new StringBuilder();
        if (d.movimentos().isEmpty()) {
            movimentos.append("<tr><td colspan=\"5\" class=\"empty\">Nenhuma movimentação encontrada.</td></tr>");
        } else {
            for (MovimentoRecente m : d.movimentos()) {
                String data = formatarData(m.data());
                movimentos.append("<tr><td><span class=\"pill ")
                    .append(m.tipo().equalsIgnoreCase("Venda") ? "sale" : "purchase")
                    .append("\">").append(html(m.tipo())).append("</span></td><td>")
                    .append(html(data)).append("</td><td>#").append(m.codigo())
                    .append("</td><td class=\"person\">").append(html(m.pessoa()))
                    .append("</td><td class=\"money\">").append(html(dinheiro(m.total())))
                    .append("</td></tr>");
            }
        }

        String geradoEm = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm", PT_BR));
        return """
            <!doctype html>
            <html lang="pt-BR">
            <head>
              <meta charset="utf-8">
              <meta name="viewport" content="width=device-width, initial-scale=1">
              <title>Painel Analítico Comercial | Britto ERP</title>
              <style>
                :root{color-scheme:dark;--bg:#0b0d17;--panel:#141724;--panel2:#191d2d;--line:#272c40;--muted:#9ca5bf;--text:#f5f6fb;--purple:#7167ff;--green:#14b889;--orange:#f5a623;--red:#fb7185}
                *{box-sizing:border-box}body{margin:0;background:radial-gradient(circle at 20% -20%,#23234c 0,transparent 34%),var(--bg);color:var(--text);font:14px/1.45 Inter,Segoe UI,Arial,sans-serif}
                .wrap{max-width:1600px;margin:auto;padding:28px 30px 36px}.top{display:flex;justify-content:space-between;gap:20px;align-items:flex-start;margin-bottom:24px}.brand{font-size:11px;text-transform:uppercase;letter-spacing:2px;color:#a7a2ff;font-weight:800}.title{font-size:27px;font-weight:750;letter-spacing:-.6px;margin:5px 0}.sub{color:var(--muted)}.date{color:var(--muted);font-size:12px;text-align:right}.btn{border:1px solid var(--line);background:#1b2032;color:var(--text);padding:10px 14px;border-radius:9px;font-weight:650;cursor:pointer}.btn:hover{background:#292e47}
                .cards{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:15px;margin-bottom:17px}.card,.panel{background:linear-gradient(145deg,rgba(26,30,48,.98),rgba(18,21,34,.98));border:1px solid var(--line);border-radius:14px;box-shadow:0 12px 32px #0002}.card{padding:18px 19px;min-height:122px;position:relative;overflow:hidden}.card:after{content:"";position:absolute;right:-20px;top:-25px;width:90px;height:90px;border-radius:50%;background:var(--accent);opacity:.09}.eyebrow{color:#a9b1c9;font-size:11px;text-transform:uppercase;letter-spacing:.8px}.value{font-size:clamp(19px,2vw,27px);font-weight:750;margin:12px 0 7px;letter-spacing:-.5px;overflow-wrap:anywhere}.detail{color:var(--muted);font-size:12px}.accent{display:inline-block;width:7px;height:7px;border-radius:50%;margin-right:7px;background:var(--accent)}
                .charts{display:grid;grid-template-columns:minmax(0,1.6fr) minmax(330px,1fr);gap:16px;margin-bottom:17px}.panel{padding:18px 19px;min-width:0}.panelhead{display:flex;justify-content:space-between;align-items:center;gap:12px;margin-bottom:14px}.paneltitle{font-size:15px;font-weight:750}.panelhint{font-size:11px;color:var(--muted)}canvas{display:block;width:100%;height:255px}.paybody{display:grid;grid-template-columns:minmax(120px,.8fr) minmax(130px,1fr);gap:15px;align-items:center;min-height:255px}.paybody canvas{height:220px}.legend{display:flex;flex-direction:column;gap:12px;min-width:0}.legendrow{display:grid;grid-template-columns:9px minmax(0,1fr);gap:9px;align-items:start}.dot{width:9px;height:9px;border-radius:3px;margin-top:4px}.legendname{overflow-wrap:anywhere;color:#e6e8f1;font-size:12px}.legendamount{color:var(--muted);font-size:11px;margin-top:2px}
                .tablewrap{overflow-x:auto}table{width:100%;border-collapse:collapse;min-width:720px}th{text-align:left;color:#9ca5bf;font-weight:650;text-transform:uppercase;font-size:10px;letter-spacing:.8px;padding:12px 12px;border-bottom:1px solid var(--line)}td{padding:13px 12px;border-bottom:1px solid #24283a;color:#dfe3f1;vertical-align:top}tbody tr:last-child td{border-bottom:0}tbody tr:hover{background:#ffffff06}.person{white-space:normal;overflow-wrap:anywhere;min-width:200px;max-width:520px}.money{text-align:right;font-weight:700;white-space:nowrap}.pill{padding:4px 8px;border-radius:6px;font-size:10px;font-weight:750}.pill.sale{background:#25234c;color:#aaa5ff}.pill.purchase{background:#113a33;color:#58e2be}.empty{text-align:center;color:var(--muted);padding:30px}.footer{margin-top:18px;color:#7c849d;font-size:11px;display:flex;justify-content:space-between;gap:12px;flex-wrap:wrap}
                @media(max-width:1000px){.cards{grid-template-columns:repeat(2,minmax(0,1fr))}.charts{grid-template-columns:1fr}.paybody{grid-template-columns:minmax(140px,.6fr) minmax(150px,1fr)}}@media(max-width:560px){.wrap{padding:18px 12px}.top{flex-direction:column}.date{text-align:left}.cards{grid-template-columns:1fr}.paybody{grid-template-columns:1fr}.paybody canvas{height:200px}}
                @media print{@page{size:A4 landscape;margin:10mm}body{background:#fff;color:#171923;-webkit-print-color-adjust:exact;print-color-adjust:exact}.wrap{max-width:none;padding:0}.top{margin-bottom:12px}.sub,.date,.panelhint,.footer{color:#5b6070}.cards{gap:8px;margin-bottom:8px}.card,.panel{box-shadow:none;break-inside:avoid}.card{min-height:auto;padding:11px}.value{font-size:18px;margin:6px 0}.charts{gap:8px;margin-bottom:8px}.panel{padding:11px}.paneltitle{font-size:12px}canvas{height:190px}.paybody{min-height:190px}.paybody canvas{height:180px}th{font-size:8px;padding:8px 7px}td{font-size:9px;padding:7px}.person{min-width:150px}.btn{display:none}.footer{margin-top:8px}}
              </style>
            </head>
            <body>
            <main class="wrap">
              <header class="top">
                <div><div class="brand">Britto ERP · Analytics</div><div class="title">Painel Analítico Comercial</div><div class="sub">Visão integrada de vendas, compras, estoque e recebimentos.</div></div>
                <div><div class="date">Dados consultados em __GERADO__</div><div style="text-align:right;margin-top:10px"><button class="btn" onclick="window.print()">Imprimir / Salvar em PDF</button></div></div>
              </header>
              <section class="cards">
                <article class="card" style="--accent:var(--purple)"><div class="eyebrow"><span class="accent"></span>Faturamento bruto · vendas</div><div class="value">__VENDAS__</div><div class="detail">__QTDVENDAS__ venda(s) registrada(s)</div></article>
                <article class="card" style="--accent:var(--green)"><div class="eyebrow"><span class="accent"></span>Custo operacional · compras</div><div class="value">__COMPRAS__</div><div class="detail">__QTDCOMPRAS__ compra(s) registrada(s)</div></article>
                <article class="card" style="--accent:__COR_SALDO__"><div class="eyebrow"><span class="accent"></span>Saldo operacional</div><div class="value">__SALDO__</div><div class="detail">Vendas menos compras · total acumulado</div></article>
                <article class="card" style="--accent:var(--orange)"><div class="eyebrow"><span class="accent"></span>Estoque baixo</div><div class="value">__ESTOQUE__</div><div class="detail">de __PRODUTOS__ produto(s) ativo(s)</div></article>
              </section>
              <section class="charts">
                <article class="panel"><div class="panelhead"><div class="paneltitle">Fluxo histórico: vendas × compras</div><div class="panelhint">Últimos 12 meses</div></div><canvas id="fluxo" aria-label="Gráfico de vendas e compras"></canvas></article>
                <article class="panel"><div class="panelhead"><div class="paneltitle">Mix de recebimento</div><div class="panelhint">Por forma de pagamento</div></div><div class="paybody"><canvas id="pagamentos" aria-label="Gráfico de formas de recebimento"></canvas><div class="legend" id="legendaPagamentos"></div></div></article>
              </section>
              <section class="panel"><div class="panelhead"><div class="paneltitle">Movimentações recentes</div><div class="panelhint">Últimas 10 compras e vendas</div></div><div class="tablewrap"><table><thead><tr><th>Tipo</th><th>Data</th><th>Código</th><th>Cliente / fornecedor</th><th style="text-align:right">Total</th></tr></thead><tbody>__MOVIMENTOS__</tbody></table></div></section>
              <footer class="footer"><span>Britto ERP · Relatório gerado a partir dos dados do PostgreSQL.</span><span>Para atualizar, gere novamente o dashboard pelo sistema.</span></footer>
            </main>
            <script>
              const meses = __MESES__;
              const pagamentos = __PAGAMENTOS__;
              const cores = ['#7167ff','#14b889','#f5a623','#3888ef','#a78bfa','#f472b6','#6b7280','#22d3ee'];
              const moeda = n => new Intl.NumberFormat('pt-BR',{style:'currency',currency:'BRL',maximumFractionDigits:2}).format(n||0);
              const compacto = n => {const v=n||0;if(Math.abs(v)>=1000000)return 'R$ '+(v/1000000).toLocaleString('pt-BR',{maximumFractionDigits:1})+' mi';if(Math.abs(v)>=1000)return 'R$ '+(v/1000).toLocaleString('pt-BR',{maximumFractionDigits:0})+' mil';return 'R$ '+v.toLocaleString('pt-BR',{maximumFractionDigits:0});};
              function canvasPronto(id){const c=document.getElementById(id);const r=c.getBoundingClientRect();const d=window.devicePixelRatio||1;c.width=Math.max(1,r.width*d);c.height=Math.max(1,r.height*d);const x=c.getContext('2d');x.scale(d,d);return {c,x,w:r.width,h:r.height};}
              function graficoFluxo(){const {x,w,h}=canvasPronto('fluxo');x.clearRect(0,0,w,h);const l=64,r=14,t=14,b=51,pw=w-l-r,ph=h-t-b;const max=Math.max(1,...meses.map(m=>Math.max(m.vendas,m.compras)))*1.15;x.font='10px Segoe UI,Arial';for(let k=0;k<=4;k++){const y=t+ph-k*ph/4;x.strokeStyle='#2b3044';x.beginPath();x.moveTo(l,y);x.lineTo(w-r,y);x.stroke();x.fillStyle='#929bb5';x.fillText(compacto(max*k/4),2,y+3);}const xx=i=>meses.length<=1?l+pw/2:l+i*pw/(meses.length-1);for(let i=0;i<meses.length;i++){if(meses.length>8&&i%2===1&&i!==meses.length-1)continue;x.fillStyle='#929bb5';x.fillText(meses[i].label,xx(i)-10,h-24);}function linha(campo,cor){x.strokeStyle=cor;x.lineWidth=2.5;x.beginPath();meses.forEach((m,i)=>{const y=t+ph-(m[campo]/max)*ph;if(i===0)x.moveTo(xx(i),y);else x.lineTo(xx(i),y)});x.stroke();meses.forEach((m,i)=>{const y=t+ph-(m[campo]/max)*ph;x.fillStyle=cor;x.beginPath();x.arc(xx(i),y,2.6,0,Math.PI*2);x.fill()})}linha('vendas','#7167ff');linha('compras','#14b889');x.fillStyle='#7167ff';x.beginPath();x.arc(l+5,h-4,4,0,Math.PI*2);x.fill();x.fillStyle='#e5e7ef';x.fillText('Vendas',l+14,h-1);x.fillStyle='#14b889';x.beginPath();x.arc(l+80,h-4,4,0,Math.PI*2);x.fill();x.fillStyle='#e5e7ef';x.fillText('Compras',l+89,h-1);}
              function graficoPagamentos(){const {x,w,h}=canvasPronto('pagamentos');x.clearRect(0,0,w,h);const total=pagamentos.reduce((s,p)=>s+p.total,0);const size=Math.min(w-12,h-18,190),cx=w/2,cy=h/2,ro=size/2,ri=ro*.63;if(total<=0){x.strokeStyle='#30364a';x.lineWidth=ro-ri;x.beginPath();x.arc(cx,cy,(ro+ri)/2,0,Math.PI*2);x.stroke()}else{let a=-Math.PI/2;pagamentos.forEach((p,i)=>{const arc=p.total/total*Math.PI*2;x.beginPath();x.arc(cx,cy,ro,a,a+arc);x.arc(cx,cy,ri,a+arc,a,true);x.closePath();x.fillStyle=cores[i%cores.length];x.fill();a+=arc})}x.textAlign='center';x.fillStyle='#e9ebf4';x.font='bold 12px Segoe UI,Arial';x.fillText(total>0?'Total recebido':'Sem dados',cx,cy-3);x.font='11px Segoe UI,Arial';x.fillStyle='#a5acc1';x.fillText(total>0?compacto(total):'de pagamentos',cx,cy+15);x.textAlign='left';const legenda=document.getElementById('legendaPagamentos');legenda.replaceChildren();if(!pagamentos.length){const p=document.createElement('div');p.className='legendamount';p.textContent='Nenhum pagamento registrado.';legenda.append(p);return;}pagamentos.forEach((p,i)=>{const row=document.createElement('div');row.className='legendrow';const dot=document.createElement('span');dot.className='dot';dot.style.background=cores[i%cores.length];const body=document.createElement('div');const name=document.createElement('div');name.className='legendname';name.textContent=p.nome;const amount=document.createElement('div');amount.className='legendamount';amount.textContent=moeda(p.total)+(total>0?' · '+Math.round(p.total*100/total)+'%':'');body.append(name,amount);row.append(dot,body);legenda.append(row);});}
              function desenhar(){graficoFluxo();graficoPagamentos()}window.addEventListener('resize',desenhar);desenhar();
            </script>
            </body></html>
            """
            .replace("__GERADO__", html(geradoEm))
            .replace("__VENDAS__", html(dinheiro(i.totalVendas())))
            .replace("__COMPRAS__", html(dinheiro(i.totalCompras())))
            .replace("__SALDO__", html(dinheiro(i.saldoOperacional())))
            .replace("__COR_SALDO__", i.saldoOperacional().signum() >= 0 ? "var(--green)" : "var(--red)")
            .replace("__QTDVENDAS__", Long.toString(i.quantidadeVendas()))
            .replace("__QTDCOMPRAS__", Long.toString(i.quantidadeCompras()))
            .replace("__ESTOQUE__", Long.toString(i.estoqueBaixo()))
            .replace("__PRODUTOS__", Long.toString(i.produtosAtivos()))
            .replace("__MESES__", meses.toString())
            .replace("__PAGAMENTOS__", pagamentos.toString())
            .replace("__MOVIMENTOS__", movimentos.toString());
    }

    private static String formatarData(Object valor) {
        if (valor == null) return "—";
        if (valor instanceof java.sql.Date data) return data.toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        if (valor instanceof java.time.LocalDate data) return data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        return valor.toString();
    }

    private static String dinheiro(BigDecimal valor) {
        return NumberFormat.getCurrencyInstance(PT_BR).format(valor == null ? BigDecimal.ZERO : valor);
    }

    private static String numero(BigDecimal valor) {
        return (valor == null ? BigDecimal.ZERO : valor).stripTrailingZeros().toPlainString();
    }

    private static String js(String valor) {
        if (valor == null) return "\"\"";
        StringBuilder b = new StringBuilder("\"");
        for (char c : valor.toCharArray()) {
            switch (c) {
                case '\\' -> b.append("\\\\");
                case '"' -> b.append("\\\"");
                case '\n' -> b.append("\\n");
                case '\r' -> b.append("\\r");
                case '\t' -> b.append("\\t");
                case '<' -> b.append("\\u003c");
                case '>' -> b.append("\\u003e");
                case '&' -> b.append("\\u0026");
                default -> { if (c == '\u2028') b.append("\\u2028"); else if (c == '\u2029') b.append("\\u2029"); else b.append(c); }
            }
        }
        return b.append('"').toString();
    }

    private static String html(String valor) {
        if (valor == null) return "";
        return valor.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
            .replace("\"", "&quot;").replace("'", "&#39;");
    }
}

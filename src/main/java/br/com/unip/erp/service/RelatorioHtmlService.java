package br.com.unip.erp.service;

import javax.swing.JTable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Exporta a visualização atual da tabela para HTML responsivo e adequado à impressão, sem truncar texto. */
public final class RelatorioHtmlService {
    public void abrirRelatorio(JTable tabela, String titulo) throws IOException {
        Path pasta = Path.of(System.getProperty("user.home", "."), "BrittoERP", "Relatorios");
        Files.createDirectories(pasta);
        String carimbo = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        String slug = titulo.toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
        Path arquivo = pasta.resolve("" + (slug.isBlank() ? "relatorio" : slug) + "-" + carimbo + ".html");
        Files.writeString(arquivo, gerarHtml(tabela, titulo), StandardCharsets.UTF_8);
        NavegadorUtil.abrir(arquivo);
    }

    private String gerarHtml(JTable tabela, String titulo) {
        StringBuilder cabecalho = new StringBuilder();
        for (int coluna = 0; coluna < tabela.getColumnCount(); coluna++) {
            cabecalho.append("<th>").append(escapar(tabela.getColumnName(coluna))).append("</th>");
        }
        StringBuilder linhas = new StringBuilder();
        for (int linha = 0; linha < tabela.getRowCount(); linha++) {
            linhas.append("<tr>");
            for (int coluna = 0; coluna < tabela.getColumnCount(); coluna++) {
                Object valor = tabela.getValueAt(linha, coluna);
                String texto = valor == null ? "" : valor.toString();
                linhas.append("<td>").append(escapar(texto)).append("</td>");
            }
            linhas.append("</tr>");
        }
        String gerado = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm"));
        return """
            <!doctype html><html lang="pt-BR"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
            <title>__TITULO__</title><style>
            *{box-sizing:border-box}body{font:14px/1.45 Arial,Helvetica,sans-serif;color:#202532;margin:0;background:#f4f6fa}.wrap{max-width:1500px;margin:0 auto;padding:28px 26px 34px}.top{display:flex;justify-content:space-between;align-items:flex-start;gap:16px;margin-bottom:22px}.brand{font-size:11px;letter-spacing:1.7px;text-transform:uppercase;font-weight:bold;color:#5950c8}.title{font-size:27px;margin:5px 0 7px}.meta{color:#606879;font-size:12px}.actions{display:flex;gap:8px}.btn{background:#554bd2;color:white;border:0;border-radius:7px;padding:10px 14px;cursor:pointer;font-weight:700}.btn.secondary{background:#e2e5ef;color:#202532}.paper{background:#fff;border:1px solid #e0e4ed;border-radius:10px;padding:15px;box-shadow:0 5px 20px #18203a0a}.summary{font-size:12px;color:#636b7c;margin:0 0 10px}.tablewrap{width:100%;overflow-x:auto}table{width:100%;border-collapse:collapse;table-layout:auto}thead{display:table-header-group}th{background:#f0f2f8;color:#434a5b;text-align:left;font-size:11px;text-transform:uppercase;letter-spacing:.35px;padding:11px 10px;border-bottom:1px solid #dce1ec;white-space:normal;overflow-wrap:anywhere}td{padding:10px;border-bottom:1px solid #e8ebf2;vertical-align:top;white-space:normal;overflow-wrap:anywhere;word-break:normal;max-width:420px}tbody tr:nth-child(even){background:#fafbfe}tbody tr{break-inside:avoid;page-break-inside:avoid}tbody tr:hover{background:#f4f5ff}.foot{margin-top:13px;font-size:11px;color:#757c8c;display:flex;justify-content:space-between;gap:10px}.empty{text-align:center;padding:28px;color:#666}
            @media(max-width:700px){.wrap{padding:14px 8px}.top{display:block}.actions{margin-top:12px}.title{font-size:22px}.paper{padding:7px}th,td{padding:8px 7px;font-size:12px}}
            @media print{@page{size:A4 landscape;margin:12mm}body{background:white;font-size:10px}.wrap{max-width:none;margin:0;padding:0}.top{margin-bottom:12px}.title{font-size:19px}.actions{display:none}.paper{border:0;border-radius:0;padding:0;box-shadow:none}.summary{margin-bottom:8px}table{width:100%;table-layout:auto}th{background:#e9edf5!important;-webkit-print-color-adjust:exact;print-color-adjust:exact;font-size:8px;padding:6px 5px}td{font-size:9px;padding:6px 5px;max-width:none;overflow-wrap:anywhere;white-space:normal}tbody tr:nth-child(even){background:#f8f9fc!important;-webkit-print-color-adjust:exact;print-color-adjust:exact}.foot{margin-top:8px}}
            </style></head><body><main class="wrap"><header class="top"><div><div class="brand">Britto ERP · Relatórios</div><h1 class="title">__TITULO__</h1><div class="meta">Gerado em __GERADO__ · __QTD__ registro(s)</div></div><div class="actions"><button class="btn" onclick="window.print()">Imprimir / Salvar PDF</button><button class="btn secondary" onclick="window.close()">Fechar</button></div></header><section class="paper"><p class="summary">Todos os campos de texto são exibidos completos; nomes longos quebram linha automaticamente na tela e na impressão.</p><div class="tablewrap"><table><thead><tr>__COLUNAS__</tr></thead><tbody>__LINHAS__</tbody></table></div></section><footer class="foot"><span>Britto ERP · Sistema de compra e venda</span><span>Documento gerado a partir dos dados consultados no PostgreSQL.</span></footer></main></body></html>
            """
            .replace("__TITULO__", escapar(titulo))
            .replace("__GERADO__", escapar(gerado))
            .replace("__QTD__", Integer.toString(tabela.getRowCount()))
            .replace("__COLUNAS__", cabecalho.toString())
            .replace("__LINHAS__", linhas.isEmpty() ? "<tr><td colspan=\"" + Math.max(1, tabela.getColumnCount()) + "\" class=\"empty\">Nenhum registro encontrado.</td></tr>" : linhas.toString());
    }

    private static String escapar(String valor) {
        if (valor == null) return "";
        return valor.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
            .replace("\"", "&quot;").replace("'", "&#39;");
    }
}

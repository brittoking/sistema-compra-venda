package br.com.unip.erp.service;

import br.com.unip.erp.util.connectionFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;

/** Consultas de relatórios e indicadores calculados a partir dos dados reais do PostgreSQL. */
public final class RelatorioService {
    public record Definicao(String nome, String sql) {}
    public record Resultado(List<String> colunas, List<List<Object>> linhas) {}
    public record Indicadores(BigDecimal totalVendas, BigDecimal totalCompras,
                              long quantidadeVendas, long quantidadeCompras,
                              long produtosAtivos, long estoqueBaixo,
                              BigDecimal ticketMedio) {
        public BigDecimal saldoOperacional() { return totalVendas.subtract(totalCompras); }
    }
    public record MesResumo(String mes, BigDecimal vendas, BigDecimal compras) {}
    public record PagamentoResumo(String nome, BigDecimal total) {}
    public record MovimentoRecente(String tipo, Object data, long codigo, String pessoa, BigDecimal total) {}
    public record Dashboard(Indicadores indicadores, List<MesResumo> meses,
                            List<PagamentoResumo> pagamentos, List<MovimentoRecente> movimentos) {}

    private static final List<Definicao> RELATORIOS = List.of(
        new Definicao("Usuários",
            "SELECT usu_codigo AS \"Código\", COALESCE(usu_nome,'') AS \"Nome\", usu_login AS \"Login\", CASE WHEN usu_ativo='S' THEN 'Ativo' ELSE 'Inativo' END AS \"Status\" FROM usuario ORDER BY usu_codigo"),
        new Definicao("Pessoas",
            "SELECT pes_codigo AS \"Código\", pes_nome AS \"Nome\", COALESCE(pes_fantasia,'') AS \"Nome fantasia\", CASE WHEN pes_fisica='S' THEN 'Pessoa física' ELSE 'Pessoa jurídica' END AS \"Tipo\", pes_cpfcnpj AS \"CPF/CNPJ\", COALESCE(pes_cidade,'') AS \"Cidade\", CASE WHEN pes_ativo='S' THEN 'Ativo' ELSE 'Inativo' END AS \"Status\" FROM pessoa ORDER BY pes_nome"),
        new Definicao("Clientes",
            "SELECT c.cli_codigo AS \"Código\", p.pes_nome AS \"Nome\", p.pes_cpfcnpj AS \"CPF/CNPJ\", c.cli_limitecred AS \"Limite de crédito\", COALESCE(p.pes_cidade,'') AS \"Cidade\", CASE WHEN p.pes_ativo='S' THEN 'Ativo' ELSE 'Inativo' END AS \"Status\" FROM cliente c JOIN pessoa p ON p.pes_codigo=c.pes_codigo ORDER BY p.pes_nome"),
        new Definicao("Fornecedores",
            "SELECT f.for_codigo AS \"Código\", p.pes_nome AS \"Nome\", COALESCE(f.for_contato,'') AS \"Contato\", p.pes_cpfcnpj AS \"CPF/CNPJ\", COALESCE(p.pes_cidade,'') AS \"Cidade\", CASE WHEN p.pes_ativo='S' THEN 'Ativo' ELSE 'Inativo' END AS \"Status\" FROM fornecedor f JOIN pessoa p ON p.pes_codigo=f.pes_codigo ORDER BY p.pes_nome"),
        new Definicao("Produtos",
            "SELECT pro_codigo AS \"Código\", pro_nome AS \"Produto\", pro_estoque AS \"Estoque\", pro_unidade AS \"Unidade\", pro_preco AS \"Preço de venda\", pro_custo AS \"Custo\", CASE WHEN pro_ativo='S' THEN 'Ativo' ELSE 'Inativo' END AS \"Status\" FROM produto ORDER BY pro_nome"),
        new Definicao("Formas de pagamento",
            "SELECT fpg_codigo AS \"Código\", fpg_nome AS \"Forma de pagamento\", CASE WHEN fpg_ativo='S' THEN 'Ativo' ELSE 'Inativo' END AS \"Status\" FROM formapagto ORDER BY fpg_nome"),
        new Definicao("Vendas",
            "SELECT v.vda_codigo AS \"Código\", v.vda_data AS \"Data\", COALESCE(p.pes_nome,'') AS \"Cliente\", COALESCE(u.usu_nome,'') AS \"Usuário\", v.vda_valor AS \"Valor\", v.vda_desconto AS \"Desconto\", v.vda_total AS \"Total\" FROM venda v LEFT JOIN cliente c ON c.cli_codigo=v.cli_codigo LEFT JOIN pessoa p ON p.pes_codigo=c.pes_codigo LEFT JOIN usuario u ON u.usu_codigo=v.usu_codigo ORDER BY v.vda_data DESC, v.vda_codigo DESC"),
        new Definicao("Compras",
            "SELECT c.cpr_codigo AS \"Código\", c.cpr_emissao AS \"Emissão\", c.cpr_dtentrada AS \"Entrada\", COALESCE(p.pes_nome,'') AS \"Fornecedor\", COALESCE(u.usu_nome,'') AS \"Usuário\", c.cpr_valor AS \"Valor\", c.cpr_desconto AS \"Desconto\", c.cpr_total AS \"Total\" FROM compra c LEFT JOIN fornecedor f ON f.for_codigo=c.for_codigo LEFT JOIN pessoa p ON p.pes_codigo=f.pes_codigo LEFT JOIN usuario u ON u.usu_codigo=c.usu_codigo ORDER BY c.cpr_emissao DESC, c.cpr_codigo DESC"),
        new Definicao("Itens das vendas",
            "SELECT vp.vda_codigo AS \"Venda\", vp.vep_codigo AS \"Código do item\", p.pro_nome AS \"Produto\", vp.vep_qtde AS \"Quantidade\", vp.vep_preco AS \"Preço\", vp.vep_desconto AS \"Desconto\", vp.vep_total AS \"Total\" FROM venda_produto vp JOIN produto p ON p.pro_codigo=vp.pro_codigo ORDER BY vp.vda_codigo DESC, vp.vep_codigo"),
        new Definicao("Itens das compras",
            "SELECT cp.cpr_codigo AS \"Compra\", cp.cpp_codigo AS \"Código do item\", p.pro_nome AS \"Produto\", cp.cpr_qtde AS \"Quantidade\", cp.cpr_preco AS \"Preço\", cp.cpr_desconto AS \"Desconto\", cp.cpr_total AS \"Total\" FROM compra_produto cp JOIN produto p ON p.pro_codigo=cp.pro_codigo ORDER BY cp.cpr_codigo DESC, cp.cpp_codigo"),
        new Definicao("Pagamentos das vendas",
            "SELECT vp.vda_codigo AS \"Venda\", fp.fpg_nome AS \"Forma de pagamento\", vp.vdp_valor AS \"Valor\" FROM venda_pagto vp JOIN formapagto fp ON fp.fpg_codigo=vp.fpg_codigo ORDER BY vp.vda_codigo DESC, fp.fpg_nome")
    );

    public List<String> nomesRelatorios() { return RELATORIOS.stream().map(Definicao::nome).toList(); }

    public Resultado consultarRelatorio(String nome) throws Exception {
        Definicao definicao = RELATORIOS.stream().filter(r -> r.nome().equals(nome)).findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Tipo de relatório não reconhecido."));
        return connectionFactory.usar(c -> executarConsulta(c, definicao.sql()));
    }

    private Resultado executarConsulta(Connection c, String sql) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            ResultSetMetaData meta = rs.getMetaData();
            List<String> colunas = new ArrayList<>();
            for (int i = 1; i <= meta.getColumnCount(); i++) colunas.add(meta.getColumnLabel(i));
            List<List<Object>> linhas = new ArrayList<>();
            while (rs.next()) {
                List<Object> linha = new ArrayList<>();
                for (int i = 1; i <= meta.getColumnCount(); i++) linha.add(rs.getObject(i));
                linhas.add(linha);
            }
            return new Resultado(List.copyOf(colunas), List.copyOf(linhas));
        }
    }

    public Dashboard carregarDashboard() throws Exception {
        return connectionFactory.usar(c -> {
            Indicadores indicadores;
            String sqlIndicadores = "SELECT " +
                "(SELECT COALESCE(SUM(vda_total),0) FROM venda) AS total_vendas," +
                "(SELECT COALESCE(SUM(cpr_total),0) FROM compra) AS total_compras," +
                "(SELECT COUNT(*) FROM venda) AS qtd_vendas," +
                "(SELECT COUNT(*) FROM compra) AS qtd_compras," +
                "(SELECT COUNT(*) FROM produto WHERE pro_ativo='S') AS produtos_ativos," +
                "(SELECT COUNT(*) FROM produto WHERE pro_ativo='S' AND COALESCE(pro_estoque,0) <= COALESCE(pro_min,0)) AS estoque_baixo," +
                "(SELECT COALESCE(AVG(vda_total),0) FROM venda) AS ticket_medio";
            try (PreparedStatement ps = c.prepareStatement(sqlIndicadores); ResultSet rs = ps.executeQuery()) {
                rs.next();
                indicadores = new Indicadores(valor(rs.getBigDecimal("total_vendas")), valor(rs.getBigDecimal("total_compras")),
                    rs.getLong("qtd_vendas"), rs.getLong("qtd_compras"), rs.getLong("produtos_ativos"),
                    rs.getLong("estoque_baixo"), valor(rs.getBigDecimal("ticket_medio")));
            }

            List<MesResumo> meses = new ArrayList<>();
            String sqlMeses = "WITH meses AS (" +
                " SELECT generate_series(date_trunc('month', CURRENT_DATE) - interval '11 months', date_trunc('month', CURRENT_DATE), interval '1 month')::date AS mes" +
                "), v AS (SELECT date_trunc('month', vda_data)::date AS mes, SUM(vda_total) AS total FROM venda WHERE vda_data >= date_trunc('month', CURRENT_DATE) - interval '11 months' GROUP BY 1" +
                "), c AS (SELECT date_trunc('month', cpr_emissao)::date AS mes, SUM(cpr_total) AS total FROM compra WHERE cpr_emissao >= date_trunc('month', CURRENT_DATE) - interval '11 months' GROUP BY 1) " +
                "SELECT m.mes, COALESCE(v.total,0) AS vendas, COALESCE(c.total,0) AS compras FROM meses m LEFT JOIN v ON v.mes=m.mes LEFT JOIN c ON c.mes=m.mes ORDER BY m.mes";
            try (PreparedStatement ps = c.prepareStatement(sqlMeses); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    java.sql.Date data = rs.getDate("mes");
                    LocalDate local = data.toLocalDate();
                    meses.add(new MesResumo(rotuloMes(local.getMonth()), valor(rs.getBigDecimal("vendas")), valor(rs.getBigDecimal("compras"))));
                }
            }

            List<PagamentoResumo> pagamentos = new ArrayList<>();
            String sqlPagamentos = "SELECT COALESCE(fp.fpg_nome,'Não informado') AS nome, SUM(vp.vdp_valor) AS total FROM venda_pagto vp LEFT JOIN formapagto fp ON fp.fpg_codigo=vp.fpg_codigo GROUP BY fp.fpg_nome ORDER BY total DESC";
            try (PreparedStatement ps = c.prepareStatement(sqlPagamentos); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) pagamentos.add(new PagamentoResumo(rs.getString("nome"), valor(rs.getBigDecimal("total"))));
            }

            List<MovimentoRecente> movimentos = new ArrayList<>();
            String sqlMovimentos = "SELECT * FROM (" +
                "SELECT 'Venda' AS tipo, v.vda_data AS data, v.vda_codigo AS codigo, COALESCE(p.pes_nome,'Cliente') AS pessoa, COALESCE(v.vda_total,0) AS total FROM venda v LEFT JOIN cliente cl ON cl.cli_codigo=v.cli_codigo LEFT JOIN pessoa p ON p.pes_codigo=cl.pes_codigo " +
                "UNION ALL SELECT 'Compra' AS tipo, c.cpr_emissao AS data, c.cpr_codigo AS codigo, COALESCE(p.pes_nome,'Fornecedor') AS pessoa, COALESCE(c.cpr_total,0) AS total FROM compra c LEFT JOIN fornecedor f ON f.for_codigo=c.for_codigo LEFT JOIN pessoa p ON p.pes_codigo=f.pes_codigo" +
                ") movimentos ORDER BY data DESC NULLS LAST, codigo DESC LIMIT 10";
            try (PreparedStatement ps = c.prepareStatement(sqlMovimentos); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) movimentos.add(new MovimentoRecente(rs.getString("tipo"), rs.getObject("data"), rs.getLong("codigo"), rs.getString("pessoa"), valor(rs.getBigDecimal("total"))));
            }
            return new Dashboard(indicadores, List.copyOf(meses), List.copyOf(pagamentos), List.copyOf(movimentos));
        });
    }

    private static BigDecimal valor(BigDecimal value) { return value == null ? BigDecimal.ZERO : value; }
    private static String rotuloMes(Month mes) {
        return switch (mes) {
            case JANUARY -> "Jan"; case FEBRUARY -> "Fev"; case MARCH -> "Mar";
            case APRIL -> "Abr"; case MAY -> "Mai"; case JUNE -> "Jun";
            case JULY -> "Jul"; case AUGUST -> "Ago"; case SEPTEMBER -> "Set";
            case OCTOBER -> "Out"; case NOVEMBER -> "Nov"; case DECEMBER -> "Dez";
        };
    }
}

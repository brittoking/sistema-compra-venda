package br.com.unip.erp.view;

import br.com.unip.erp.service.RelatorioService;
import br.com.unip.erp.service.RelatorioService.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;

/** Dashboard com indicadores e gráficos de vendas e compras dos dados do banco. */
public final class DashboardPanel extends JPanel {
    private final RelatorioService service = new RelatorioService();
    private final JPanel cards = new JPanel(new GridLayout(1, 4, 14, 0));
    private final GraficoMovimento grafico = new GraficoMovimento();
    private final GraficoPagamentos graficoPagamentos = new GraficoPagamentos();
    private final DefaultTableModel tabelaModelo = new DefaultTableModel(
        new Object[]{"Tipo", "Data", "Código", "Cliente / Fornecedor", "Total"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

    public DashboardPanel() {
        super(new BorderLayout(0, 18));
        setBackground(Tema.BG);
        setBorder(BorderFactory.createEmptyBorder(30, 32, 28, 32));

        JPanel cabecalho = new JPanel(new BorderLayout(12, 0));
        cabecalho.setOpaque(false);
        JPanel titulos = Tema.coluna();
        titulos.add(Tema.titulo("Dashboard de vendas e compras", 27));
        titulos.add(Box.createVerticalStrut(7));
        JLabel subtitulo = new JLabel("Indicadores gerais, evolução mensal e formas de recebimento.");
        subtitulo.setForeground(Tema.MUTED);
        titulos.add(subtitulo);
        cabecalho.add(titulos, BorderLayout.CENTER);
        cabecalho.add(Tema.primario("↻ Atualizar", this::atualizar), BorderLayout.EAST);
        add(cabecalho, BorderLayout.NORTH);

        JPanel conteudo = new JPanel();
        conteudo.setOpaque(false);
        conteudo.setLayout(new BoxLayout(conteudo, BoxLayout.Y_AXIS));
        cards.setOpaque(false);
        cards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 124));
        conteudo.add(cards);
        conteudo.add(Box.createVerticalStrut(18));

        JPanel graficos = new JPanel(new GridLayout(1, 2, 16, 0));
        graficos.setOpaque(false);
        graficos.add(cartaoGrafico("Fluxo histórico: vendas x compras", grafico));
        graficos.add(cartaoGrafico("Mix de recebimentos", graficoPagamentos));
        graficos.setPreferredSize(new Dimension(850, 350));
        graficos.setMinimumSize(new Dimension(550, 320));
        graficos.setMaximumSize(new Dimension(Integer.MAX_VALUE, 370));
        conteudo.add(graficos);
        conteudo.add(Box.createVerticalStrut(18));

        JPanel recentes = new JPanel(new BorderLayout(0, 10));
        recentes.setOpaque(false);
        recentes.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
        recentes.add(Tema.titulo("Movimentações recentes", 18), BorderLayout.NORTH);
        JTable tabela = new JTable(tabelaModelo);
        Tema.tabela(tabela);
        tabela.setAutoCreateRowSorter(true);
        JScrollPane rolagem = Tema.rolagem(tabela);
        rolagem.setBorder(BorderFactory.createLineBorder(Tema.LINE));
        rolagem.setPreferredSize(new Dimension(850, 240));
        recentes.add(rolagem, BorderLayout.CENTER);
        recentes.setPreferredSize(new Dimension(850, 275));
        recentes.setMinimumSize(new Dimension(400, 230));
        recentes.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        conteudo.add(recentes);

        JScrollPane scrollConteudo = new JScrollPane(conteudo);
        scrollConteudo.setBorder(null);
        scrollConteudo.setOpaque(false);
        scrollConteudo.getViewport().setOpaque(false);
        scrollConteudo.getVerticalScrollBar().setUnitIncrement(18);
        add(scrollConteudo, BorderLayout.CENTER);
        atualizar();
    }

    private JPanel cartaoGrafico(String titulo, JComponent grafico) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Tema.LINE), BorderFactory.createEmptyBorder(14, 15, 12, 15)));
        JLabel label = Tema.titulo(titulo, 15);
        card.add(label, BorderLayout.NORTH);
        card.add(grafico, BorderLayout.CENTER);
        return card;
    }

    private void atualizar() {
        try {
            Dashboard dashboard = service.carregarDashboard();
            cards.removeAll();
            Indicadores i = dashboard.indicadores();
            cards.add(cartaoIndicador("Faturamento em vendas", Tema.dinheiro(i.totalVendas()), i.quantidadeVendas() + " venda(s)", new Color(89, 82, 232)));
            cards.add(cartaoIndicador("Total em compras", Tema.dinheiro(i.totalCompras()), i.quantidadeCompras() + " compra(s)", new Color(16, 166, 128)));
            cards.add(cartaoIndicador("Saldo operacional", Tema.dinheiro(i.saldoOperacional()), "Vendas menos compras", i.saldoOperacional().signum() >= 0 ? new Color(16, 166, 128) : new Color(220, 75, 75)));
            cards.add(cartaoIndicador("Estoque baixo", String.valueOf(i.estoqueBaixo()), i.produtosAtivos() + " produto(s) ativo(s)", new Color(235, 157, 36)));
            cards.revalidate(); cards.repaint();

            grafico.setDados(dashboard.meses());
            graficoPagamentos.setDados(dashboard.pagamentos());
            tabelaModelo.setRowCount(0);
            for (MovimentoRecente m : dashboard.movimentos()) {
                Object data = m.data();
                if (data instanceof java.sql.Date date) data = date.toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                tabelaModelo.addRow(new Object[]{m.tipo(), data == null ? "—" : data, m.codigo(), m.pessoa(), Tema.dinheiro(m.total())});
            }
        } catch (Exception ex) {
            Tema.erro(this, ex);
        }
    }

    private JPanel cartaoIndicador(String titulo, String valor, String detalhe, Color destaque) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Tema.LINE), BorderFactory.createEmptyBorder(15, 16, 14, 14)));
        JLabel t = new JLabel(titulo); t.setForeground(Tema.MUTED); t.setFont(new Font("SansSerif", Font.PLAIN, 12));
        JLabel v = Tema.titulo(valor, 20); v.setForeground(destaque);
        JLabel d = new JLabel(detalhe); d.setForeground(Tema.MUTED); d.setFont(new Font("SansSerif", Font.PLAIN, 11));
        card.add(t, BorderLayout.NORTH); card.add(v, BorderLayout.CENTER); card.add(d, BorderLayout.SOUTH);
        return card;
    }

    private static String compacto(BigDecimal n) {
        double v = n == null ? 0d : n.doubleValue();
        if (Math.abs(v) >= 1_000_000) return String.format(Locale.forLanguageTag("pt-BR"), "R$ %.1f mi", v / 1_000_000);
        if (Math.abs(v) >= 1_000) return String.format(Locale.forLanguageTag("pt-BR"), "R$ %.0f mil", v / 1_000);
        return String.format(Locale.forLanguageTag("pt-BR"), "R$ %.0f", v);
    }

    private static final class GraficoMovimento extends JPanel {
        private List<MesResumo> dados = List.of();
        private final Color corVendas = new Color(91, 82, 235);
        private final Color corCompras = new Color(18, 174, 133);
        GraficoMovimento() { setBackground(Color.WHITE); setPreferredSize(new Dimension(430, 260)); }
        void setDados(List<MesResumo> dados) { this.dados = List.copyOf(dados); repaint(); }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            int left = 64, right = 14, top = 22, bottom = 42;
            int pw = Math.max(1, w - left - right), ph = Math.max(1, h - top - bottom);
            BigDecimal maxB = BigDecimal.ZERO;
            for (MesResumo m : dados) { maxB = maxB.max(m.vendas()).max(m.compras()); }
            double max = maxB.doubleValue(); if (max <= 0) max = 100;
            max = max * 1.15;
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            for (int i = 0; i <= 4; i++) {
                int y = top + ph - i * ph / 4;
                g2.setColor(new Color(231, 235, 243)); g2.drawLine(left, y, w - right, y);
                g2.setColor(Tema.MUTED);
                g2.drawString(compacto(BigDecimal.valueOf(max * i / 4)), 1, y + 4);
            }
            if (dados.isEmpty()) {
                g2.setColor(Tema.MUTED); g2.drawString("Sem dados de movimentações no período.", left + 10, top + ph / 2);
            } else {
                desenharLinha(g2, dados.stream().mapToDouble(m -> m.vendas().doubleValue()).toArray(), left, top, pw, ph, max, corVendas);
                desenharLinha(g2, dados.stream().mapToDouble(m -> m.compras().doubleValue()).toArray(), left, top, pw, ph, max, corCompras);
                for (int i = 0; i < dados.size(); i++) {
                    if (dados.size() > 8 && i % 2 != 0 && i != dados.size() - 1) continue;
                    int x = x(i, dados.size(), left, pw);
                    g2.setColor(Tema.MUTED); g2.drawString(dados.get(i).mes(), x - 9, h - 22);
                }
            }
            int legendY = h - 5;
            g2.setColor(corVendas); g2.fillOval(left, legendY - 8, 7, 7);
            g2.setColor(Tema.INK); g2.drawString("Vendas", left + 11, legendY - 1);
            g2.setColor(corCompras); g2.fillOval(left + 72, legendY - 8, 7, 7);
            g2.setColor(Tema.INK); g2.drawString("Compras", left + 83, legendY - 1);
            g2.dispose();
        }

        private void desenharLinha(Graphics2D g, double[] valores, int left, int top, int pw, int ph, double max, Color cor) {
            if (valores.length == 0) return;
            g.setColor(cor); g.setStroke(new BasicStroke(2.2f));
            for (int i = 0; i < valores.length; i++) {
                int x = x(i, valores.length, left, pw);
                int y = top + ph - (int) Math.round((valores[i] / max) * ph);
                if (i > 0) {
                    int px = x(i - 1, valores.length, left, pw);
                    int py = top + ph - (int) Math.round((valores[i - 1] / max) * ph);
                    g.drawLine(px, py, x, y);
                }
                g.fillOval(x - 3, y - 3, 6, 6);
            }
        }
        private int x(int index, int count, int left, int width) { return count <= 1 ? left + width / 2 : left + (int) Math.round(index * width / (double) (count - 1)); }
    }

    private static final class GraficoPagamentos extends JPanel {
        private List<PagamentoResumo> dados = List.of();
        private static final Color[] CORES = {new Color(91,82,235), new Color(18,174,133), new Color(246,163,30), new Color(46,130,220), new Color(139,92,246), new Color(111,124,145)};
        GraficoPagamentos() { setBackground(Color.WHITE); setPreferredSize(new Dimension(380, 260)); }
        void setDados(List<PagamentoResumo> dados) { this.dados = List.copyOf(dados); repaint(); }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int size = Math.min(170, Math.max(90, getHeight() - 54));
            int x = 10, y = Math.max(8, (getHeight() - size) / 2 - 5);
            BigDecimal total = dados.stream().map(PagamentoResumo::total).reduce(BigDecimal.ZERO, BigDecimal::add);
            if (dados.isEmpty() || total.signum() <= 0) {
                g2.setColor(new Color(229, 233, 242)); g2.fillOval(x, y, size, size);
            } else {
                double inicio = 90;
                for (int i = 0; i < dados.size(); i++) {
                    double arco = dados.get(i).total().doubleValue() / total.doubleValue() * 360d;
                    g2.setColor(CORES[i % CORES.length]);
                    g2.fill(new Arc2D.Double(x, y, size, size, inicio, -arco, Arc2D.PIE));
                    inicio -= arco;
                }
            }
            int inner = (int) (size * .61); int ix = x + (size - inner) / 2; int iy = y + (size - inner) / 2;
            g2.setColor(Color.WHITE); g2.fillOval(ix, iy, inner, inner);
            g2.setColor(Tema.INK); g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            String[] centro = total.signum() > 0 ? new String[]{"Total recebido", compacto(total)} : new String[]{"Sem dados", "de pagamentos"};
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(centro[0], ix + (inner - fm.stringWidth(centro[0])) / 2, iy + inner / 2 - 3);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 11)); fm = g2.getFontMetrics();
            g2.drawString(centro[1], ix + (inner - fm.stringWidth(centro[1])) / 2, iy + inner / 2 + 14);

            int legendX = x + size + 16, legendY = Math.max(24, y + 15);
            int disponivel = Math.max(1, (getHeight() - legendY - 5) / 24);
            int maxItens = Math.min(dados.size(), Math.min(6, disponivel));
            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            for (int i = 0; i < maxItens; i++) {
                PagamentoResumo p = dados.get(i);
                int yy = legendY + i * 24;
                g2.setColor(CORES[i % CORES.length]); g2.fillRoundRect(legendX, yy - 9, 9, 9, 3, 3);
                g2.setColor(Tema.INK);
                String nome = p.nome().length() > 16 ? p.nome().substring(0, 15) + "…" : p.nome();
                g2.drawString(nome, legendX + 15, yy);
                if (total.signum() > 0) {
                    String pct = String.format(Locale.forLanguageTag("pt-BR"), "%.0f%%", p.total().doubleValue() * 100 / total.doubleValue());
                    g2.setColor(Tema.MUTED); g2.drawString(pct, legendX + 15, yy + 12);
                }
            }
            if (dados.size() > maxItens && maxItens > 0) {
                g2.setColor(Tema.MUTED); g2.drawString("+ " + (dados.size() - maxItens) + " outra(s)", legendX + 15, legendY + maxItens * 24);
            }
            g2.dispose();
        }
    }
}

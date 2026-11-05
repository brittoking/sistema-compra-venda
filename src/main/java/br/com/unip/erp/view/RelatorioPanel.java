package br.com.unip.erp.view;

import br.com.unip.erp.service.RelatorioService;
import br.com.unip.erp.service.RelatorioHtmlService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import javax.swing.table.TableCellRenderer;
import javax.swing.border.EmptyBorder;

/** Tela de geração, visualização, impressão e exportação dos relatórios dos formulários. */
public final class RelatorioPanel extends JPanel {
    private final RelatorioService service = new RelatorioService();
    private final JComboBox<String> tipo = new JComboBox<>(service.nomesRelatorios().toArray(String[]::new));
    private final JTable tabela = new JTable();
    private final JLabel resumo = new JLabel("Selecione o relatório e clique em Gerar relatório.");
    private final JButton exportar = Tema.botao("Exportar CSV", this::exportarCsv);
    private final JButton imprimir = Tema.botao("Imprimir no navegador", this::imprimir);
    private String relatorioAtual = "Relatório";

    public RelatorioPanel() {
        super(new BorderLayout(0, 18));
        setBackground(Tema.BG);
        setBorder(BorderFactory.createEmptyBorder(30, 32, 28, 32));

        JPanel cabecalho = Tema.coluna();
        cabecalho.add(Tema.titulo("Relatórios", 28));
        cabecalho.add(Box.createVerticalStrut(7));
        JLabel descricao = new JLabel("Gere relatórios dos cadastros, compras, vendas e pagamentos.");
        descricao.setForeground(Tema.MUTED);
        cabecalho.add(descricao);
        add(cabecalho, BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout(0, 12));
        centro.setOpaque(false);
        JPanel filtros = new JPanel(new BorderLayout(10, 0));
        filtros.setOpaque(false);
        JPanel seletor = new JPanel(new BorderLayout(0, 5));
        seletor.setOpaque(false);
        JLabel label = new JLabel("Tipo de relatório");
        label.setForeground(Tema.MUTED);
        seletor.add(label, BorderLayout.NORTH);
        tipo.setPreferredSize(new Dimension(250, 38));
        seletor.add(tipo, BorderLayout.CENTER);
        filtros.add(seletor, BorderLayout.WEST);

        JPanel botoes = Tema.linha(
            Tema.primario("Gerar relatório", this::gerar),
            exportar,
            imprimir
        );
        botoes.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        filtros.add(botoes, BorderLayout.CENTER);
        centro.add(filtros, BorderLayout.NORTH);

        tabela.setModel(modeloVazio());
        Tema.tabela(tabela);
        tabela.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        configurarRendererTextoCompleto();
        JScrollPane scroll = Tema.rolagem(tabela);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(BorderFactory.createLineBorder(Tema.LINE));
        centro.add(scroll, BorderLayout.CENTER);

        JPanel rodape = new JPanel(new BorderLayout());
        rodape.setOpaque(false);
        resumo.setForeground(Tema.MUTED);
        rodape.add(resumo, BorderLayout.WEST);
        centro.add(rodape, BorderLayout.SOUTH);
        add(centro, BorderLayout.CENTER);

        exportar.setEnabled(false);
        imprimir.setEnabled(false);
        gerar();
    }

    private DefaultTableModel modeloVazio() {
        return new DefaultTableModel(new Object[]{"Relatório"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
    }

    private void gerar() {
        String nome = (String) tipo.getSelectedItem();
        if (nome == null) return;
        Tema.tentar(this, () -> {
            RelatorioService.Resultado resultado = service.consultarRelatorio(nome);
            String[] colunas = resultado.colunas().toArray(String[]::new);
            DefaultTableModel modelo = new DefaultTableModel(colunas, 0) {
                @Override public boolean isCellEditable(int row, int column) { return false; }
            };
            for (List<Object> linha : resultado.linhas()) modelo.addRow(linha.toArray());
            tabela.setModel(modelo);
            Tema.tabela(tabela);
            tabela.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            configurarRendererTextoCompleto();
            ajustarLargurasColunas();
            ajustarAlturaLinhas();
            relatorioAtual = "Relatório de " + nome;
            resumo.setText(resultado.linhas().size() + (resultado.linhas().size() == 1 ? " registro encontrado." : " registros encontrados."));
            exportar.setEnabled(true);
            imprimir.setEnabled(true);
        });
    }

    private void exportarCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Salvar relatório como CSV");
        chooser.setSelectedFile(new java.io.File(nomeArquivo() + ".csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        java.io.File arquivo = chooser.getSelectedFile();
        if (!arquivo.getName().toLowerCase(java.util.Locale.ROOT).endsWith(".csv")) {
            arquivo = new java.io.File(arquivo.getParentFile(), arquivo.getName() + ".csv");
        }
        try (BufferedWriter writer = Files.newBufferedWriter(arquivo.toPath(), StandardCharsets.UTF_8)) {
            writer.write('\uFEFF'); // BOM para que o Excel reconheça UTF-8 e acentos.
            for (int c = 0; c < tabela.getColumnCount(); c++) {
                if (c > 0) writer.write(';');
                writer.write(csv(tabela.getColumnName(c)));
            }
            writer.newLine();
            for (int r = 0; r < tabela.getRowCount(); r++) {
                for (int c = 0; c < tabela.getColumnCount(); c++) {
                    if (c > 0) writer.write(';');
                    Object valor = tabela.getValueAt(r, c);
                    writer.write(csv(valor == null ? "" : valor.toString()));
                }
                writer.newLine();
            }
            JOptionPane.showMessageDialog(this, "Relatório exportado com sucesso:\n" + arquivo.getAbsolutePath(), "Relatório", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Não foi possível salvar o arquivo.\n" + ex.getMessage(), "Erro ao exportar", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String csv(String valor) {
        String protegido = valor == null ? "" : valor.replace("\"", "\"\"");
        return "\"" + protegido + "\"";
    }

    private String nomeArquivo() {
        return relatorioAtual.toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
    }

    private void imprimir() {
        try {
            new RelatorioHtmlService().abrirRelatorio(tabela, relatorioAtual);
            JOptionPane.showMessageDialog(this,
                "O relatório foi aberto no navegador. Use o botão ‘Imprimir / Salvar PDF’ para imprimir ou salvar o documento.\n\nOs nomes completos são mantidos e quebram linha quando necessário.",
                "Relatório pronto para impressão", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Não foi possível abrir o relatório no navegador.\n" + ex.getMessage(),
                "Erro ao imprimir", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Faz os nomes longos quebrarem linha em vez de serem escondidos dentro da célula. */
    private void configurarRendererTextoCompleto() {
        tabela.setDefaultRenderer(Object.class, new TableCellRenderer() {
            private final JTextArea area = new JTextArea();
            {
                area.setLineWrap(true);
                area.setWrapStyleWord(true);
                area.setOpaque(true);
                area.setBorder(new EmptyBorder(6, 8, 6, 8));
                area.setFont(tabela.getFont());
            }
            @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                    boolean focus, int row, int column) {
                area.setText(value == null ? "" : value.toString());
                area.setBackground(selected ? table.getSelectionBackground() : Color.WHITE);
                area.setForeground(selected ? table.getSelectionForeground() : new Color(36, 42, 56));
                area.setSize(Math.max(40, table.getColumnModel().getColumn(column).getWidth()), Short.MAX_VALUE);
                return area;
            }
        });
    }

    /** Calcula a largura inicial pelo conteúdo e reserva mais espaço para nomes e descrições. */
    private void ajustarLargurasColunas() {
        java.awt.FontMetrics fm = tabela.getFontMetrics(tabela.getFont());
        for (int coluna = 0; coluna < tabela.getColumnCount(); coluna++) {
            String cabecalho = tabela.getColumnName(coluna) == null ? "" : tabela.getColumnName(coluna);
            String chave = cabecalho.toLowerCase(java.util.Locale.ROOT);
            int largura = fm.stringWidth(cabecalho) + 34;
            for (int linha = 0; linha < tabela.getRowCount(); linha++) {
                Object valor = tabela.getValueAt(linha, coluna);
                if (valor != null) {
                    String[] partes = valor.toString().split("\\R");
                    for (String parte : partes) largura = Math.max(largura, fm.stringWidth(parte) + 28);
                }
            }
            int minimo = 80;
            int maximo = 230;
            if (chave.contains("nome") || chave.contains("cliente") || chave.contains("fornecedor")
                    || chave.contains("produto") || chave.contains("usuário") || chave.contains("usuario")
                    || chave.contains("contato") || chave.contains("pagamento")) {
                minimo = 220;
                maximo = 380;
            }
            if (chave.contains("cidade") || chave.contains("fantasia")) { minimo = 150; maximo = 280; }
            if (chave.contains("código") || chave.contains("codigo") || chave.contains("quantidade") || chave.contains("status")) {
                minimo = 85;
                maximo = 150;
            }
            tabela.getColumnModel().getColumn(coluna).setPreferredWidth(Math.min(maximo, Math.max(minimo, largura)));
        }
    }

    private void ajustarAlturaLinhas() {
        for (int linha = 0; linha < tabela.getRowCount(); linha++) {
            int altura = tabela.getRowHeight();
            for (int coluna = 0; coluna < tabela.getColumnCount(); coluna++) {
                Component componente = tabela.prepareRenderer(tabela.getCellRenderer(linha, coluna), linha, coluna);
                altura = Math.max(altura, componente.getPreferredSize().height + 2);
            }
            tabela.setRowHeight(linha, Math.min(altura, 120));
        }
    }

}

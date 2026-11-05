package br.com.unip.erp.view;
import br.com.unip.erp.controller.*;
import br.com.unip.erp.model.*;
import br.com.unip.erp.dao.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.awt.*;
import java.math.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.List;
public class MovimentoPanel extends JPanel  {
    private record Opcao(int id,String nome,Registro registro) {
        public String toString() {
            return nome;
        }
    }
    private final boolean venda;
    private final CadastroController cadastro;
    private final MovimentoController movimento;
    private final Registro usuario;
    private final JComboBox<Opcao> pessoa=new JComboBox<>(),produto=new JComboBox<>(),forma=new JComboBox<>();
    private final JTextField data=new JTextField(10),entrada=new JTextField(10),qtd=new JTextField("1",6),preco=new JTextField("0,00",8),descontoItem=new JTextField("0,00",7),desconto=new JTextField("0,00",8),valorPago=new JTextField("0,00",8),obs=new JTextField();
    private final List<ItemMovimento> itens=new ArrayList<>();
    private final List<Pagamento> pagamentos=new ArrayList<>();
    private final DefaultTableModel itensModel=Tema.modelo("Produto","Quantidade","Preço unitário","Desconto","Total"),pgModel=Tema.modelo("Forma de pagamento","Valor");
    private final JLabel subtotal=new JLabel(),total=new JLabel(),pendente=new JLabel();
    public MovimentoPanel(boolean venda,CadastroController cadastro,Registro usuario) {
        super(new BorderLayout(0,14));
        this.venda=venda;
        this.cadastro=cadastro;
        this.movimento=new MovimentoController(cadastro.banco);
        this.usuario=usuario;
        setBorder(BorderFactory.createEmptyBorder(22,26,22,26));
        String hoje=LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        data.setText(hoje);
        entrada.setText(hoje);
        JPanel header=new JPanel(new BorderLayout());
        header.add(Tema.titulo(venda?"Nova venda":"Nova compra",27),BorderLayout.WEST);
        header.add(Tema.botao("Histórico",this::historico),BorderLayout.EAST);
        JPanel dados=new JPanel(new GridLayout(1,venda?3:4,14,0));
        dados.add(Tema.campo(venda?"Cliente *":"Fornecedor *",pessoa));
        dados.add(Tema.campo(venda?"Data *":"Emissão *",data));
        if(!venda)dados.add(Tema.campo("Data de entrada *",entrada));
        JTextField user=new JTextField(usuario.text("usu_nome"));
        user.setEditable(false);
        dados.add(Tema.campo("Usuário responsável",user));
        JPanel top=Tema.coluna();
        top.add(header);
        top.add(Box.createVerticalStrut(18));
        top.add(dados);
        add(top,BorderLayout.NORTH);
        JPanel body=new JPanel(new BorderLayout(0,10));
        JPanel itemEntry=new JPanel(new GridBagLayout());
        GridBagConstraints g=new GridBagConstraints();
        g.gridy=0;
        g.fill=GridBagConstraints.HORIZONTAL;
        g.insets=new Insets(0,4,4,4);
        g.weightx=3;
        itemEntry.add(Tema.campo("Produto *",produto),g);
        g.weightx=1;
        itemEntry.add(Tema.campo("Quantidade",qtd),g);
        itemEntry.add(Tema.campo("Preço unitário",preco),g);
        itemEntry.add(Tema.campo("Desconto do item",descontoItem),g);
        g.weightx=0;
        itemEntry.add(Tema.primario("Adicionar",this::adicionarItem),g);
        JTable itemTable=new JTable(itensModel);
        Tema.tabela(itemTable);
        JPanel items=new JPanel(new BorderLayout(0,8));
        items.add(itemEntry,BorderLayout.NORTH);
        items.add(Tema.rolagem(itemTable),BorderLayout.CENTER);
        items.add(Tema.linha(Tema.botao("Remover item selecionado",()-> {
            int n=itemTable.getSelectedRow();if(n>=0) {
                itens.remove(itemTable.convertRowIndexToModel(n));atualizarTotais();
            }
        }
        )),BorderLayout.SOUTH);
        JTabbedPane tabs=new JTabbedPane();
        tabs.addTab("Produtos",items);
        if(venda) {
            JTable pgTable=new JTable(pgModel);
            Tema.tabela(pgTable);
            JPanel pgPanel=new JPanel(new BorderLayout(0,10));
            pgPanel.add(Tema.linha(new JLabel("Forma"),forma,new JLabel("Valor"),valorPago,Tema.primario("Adicionar pagamento",this::adicionarPagamento),Tema.botao("Usar saldo restante",()->Tema.tentar(this,()->valorPago.setText(saldo().max(BigDecimal.ZERO).toPlainString())))),BorderLayout.NORTH);
            pgPanel.add(Tema.rolagem(pgTable),BorderLayout.CENTER);
            pgPanel.add(Tema.linha(Tema.botao("Remover pagamento",()-> {
                int n=pgTable.getSelectedRow();if(n>=0) {
                    pagamentos.remove(pgTable.convertRowIndexToModel(n));atualizarTotais();
                }
            }
            ),pendente),BorderLayout.SOUTH);
            tabs.addTab("Pagamentos",pgPanel);
        }
        body.add(tabs,BorderLayout.CENTER);
        body.add(Tema.campo("Observações",obs),BorderLayout.SOUTH);
        add(body,BorderLayout.CENTER);
        JPanel footer=new JPanel(new BorderLayout());
        total.setFont(new Font("SansSerif",Font.BOLD,23));
        total.setForeground(Tema.PRIMARY);
        footer.add(Tema.linha(subtotal,new JLabel("Desconto geral (R$)"),desconto,total),BorderLayout.CENTER);
        footer.add(Tema.primario(venda?"Finalizar venda":"Registrar compra",this::salvar),BorderLayout.EAST);
        add(footer,BorderLayout.SOUTH);
        produto.addActionListener(e-> {
            Opcao op=(Opcao)produto.getSelectedItem();if(op!=null)preco.setText(op.registro.number(venda?"pro_preco":"pro_custo").toPlainString());
        }
        );
        desconto.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                resumo();
            }
            public void removeUpdate(DocumentEvent e) {
                resumo();
            }
            public void changedUpdate(DocumentEvent e) {
                resumo();
            }
        }
        );
        carregarOpcoes();
        atualizarTotais();
    }
    public boolean temRascunho() {
        return !itens.isEmpty()||!pagamentos.isEmpty()||!obs.getText().isBlank();
    }
    private void preencher(JComboBox<Opcao> combo,DaoJdbc<?> e)throws Exception {
        combo.removeAllItems();
        for(Registro r:cadastro.listar(e)) {
            String ativo=e.pessoa()?"pes_ativo":e==cadastro.banco.produto?"pro_ativo":"fpg_ativo";
            if(r.text(ativo).equals("S"))combo.addItem(new Opcao(r.id(e.chave),r.text(e.nome()),r));
        }
        combo.setPreferredSize(new Dimension(220,33));
    }
    private void carregarOpcoes() {
        Tema.tentar(this,()-> {
            preencher(pessoa,venda?cadastro.banco.cliente:cadastro.banco.fornecedor);preencher(produto,cadastro.banco.produto);preencher(forma,cadastro.banco.formaPagto);
        }
        );
    }
    private BigDecimal dinheiro(JTextField f) {
        return CadastroController.decimal(f.getText()).setScale(2,RoundingMode.UNNECESSARY);
    }
    private BigDecimal saldo() {
        return MovimentoController.subtotal(itens).subtract(dinheiro(desconto)).subtract(pagamentos.stream().map(Pagamento::valor).reduce(BigDecimal.ZERO,BigDecimal::add));
    }
    private void adicionarItem() {
        Tema.tentar(this,()-> {
            Opcao op=(Opcao)produto.getSelectedItem();if(op==null)throw new IllegalArgumentException("Cadastre um produto ativo primeiro.");ItemMovimento it=new ItemMovimento(op.id,op.nome,CadastroController.decimal(qtd.getText()).setScale(4,RoundingMode.UNNECESSARY),dinheiro(preco),dinheiro(descontoItem));if(it.quantidade().signum()<=0||it.preco().signum()<=0||it.desconto().signum()<0||it.total().signum()<=0)throw new IllegalArgumentException("Use quantidade e preço positivos; desconto menor que o valor do item.");itens.add(it);atualizarTotais();
        }
        );
    }
    private void adicionarPagamento() {
        Tema.tentar(this,()-> {
            Opcao op=(Opcao)forma.getSelectedItem();if(op==null)throw new IllegalArgumentException("Cadastre uma forma de pagamento ativa.");BigDecimal n=dinheiro(valorPago);if(n.signum()<=0||n.compareTo(saldo())>0)throw new IllegalArgumentException("Informe um valor positivo de até "+Tema.dinheiro(saldo())+".");pagamentos.add(new Pagamento(op.id,op.nome,n));atualizarTotais();
        }
        );
    }
    private void resumo() {
        subtotal.setText("Subtotal: "+Tema.dinheiro(MovimentoController.subtotal(itens)));
        try {
            total.setText("Total: "+Tema.dinheiro(MovimentoController.subtotal(itens).subtract(dinheiro(desconto))));
            pendente.setText("Saldo a distribuir: "+Tema.dinheiro(saldo()));
        }
        catch(Exception ex) {
            total.setText("Confira o desconto");
        }
    }
    private void atualizarTotais() {
        itensModel.setRowCount(0);
        for(ItemMovimento i:itens)itensModel.addRow(new Object[] {
            i.nome(),i.quantidade(),Tema.dinheiro(i.preco()),Tema.dinheiro(i.desconto()),Tema.dinheiro(i.total())
        }
        );
        pgModel.setRowCount(0);
        for(Pagamento p:pagamentos)pgModel.addRow(new Object[] {
            p.nome(),Tema.dinheiro(p.valor())
        }
        );
        resumo();
    }
    private void salvar() {
        Tema.tentar(this,()-> {
            Opcao op=(Opcao)pessoa.getSelectedItem();int id=movimento.salvar(venda,usuario.id("usu_codigo"),op==null?0:op.id,CadastroController.data(data.getText()),CadastroController.data(entrada.getText()),obs.getText(),dinheiro(desconto),itens,pagamentos);JOptionPane.showMessageDialog(this,(venda?"Venda":"Compra")+" nº "+id+" registrada com sucesso.");itens.clear();pagamentos.clear();obs.setText("");desconto.setText("0,00");carregarOpcoes();atualizarTotais();
        }
        );
    }
    private void historico() {
        Tema.tentar(this,()-> {
            DaoJdbc<?> e=venda?cadastro.banco.venda:cadastro.banco.compra;String p=venda?"vda_":"cpr_";List<Registro> rows=new ArrayList<>(e.listar());DefaultTableModel m=Tema.modelo("Código","Data",venda?"Cliente":"Fornecedor","Total");for(Registro r:rows) {
                int personId=r.id(venda?"cli_codigo":"for_codigo");String nome=cadastro.listar(venda?cadastro.banco.cliente:cadastro.banco.fornecedor).stream().filter(x->x.id(venda?"cli_codigo":"for_codigo")==personId).map(x->x.text("pes_nome")).findFirst().orElse("#"+personId);m.addRow(new Object[] {
                    r.id(e.chave),r.get(p+(venda?"data":"emissao")),nome,Tema.dinheiro(r.number(p+"total"))
                }
                );
            }
            JTable t=new JTable(m);Tema.tabela(t);JDialog d=new JDialog(SwingUtilities.getWindowAncestor(this),"Histórico de "+e.titulo,Dialog.ModalityType.APPLICATION_MODAL);d.add(Tema.rolagem(t));d.add(Tema.linha(new JLabel("Movimentos finalizados são somente leitura."),Tema.botao("Ver detalhes",()-> {
                int sel=t.getSelectedRow();if(sel>=0)detalhes(rows.get(t.convertRowIndexToModel(sel)));
            }
            )),BorderLayout.SOUTH);d.setSize(820,520);d.setLocationRelativeTo(this);d.setVisible(true);
        }
        );
    }
    private void detalhes(Registro r) {
        Tema.tentar(this,()-> {
            String p=venda?"vda_":"cpr_",q=venda?"vep_":"cpr_";StringBuilder text=new StringBuilder((venda?"VENDA":"COMPRA")+" #"+r.id(p+"codigo")+"\n\n");for(Registro item:(venda?cadastro.banco.vendaProduto:cadastro.banco.compraProduto).listarPor(p+"codigo",r.id(p+"codigo")))if(item.id(p+"codigo")==r.id(p+"codigo")) {
                Registro pro=cadastro.banco.produto.buscarPorId(item.id("pro_codigo"));text.append(pro==null?"Produto #"+item.id("pro_codigo"):pro.text("pro_nome")).append(" | qtd: ").append(item.text(q+"qtde")).append(" | ").append(Tema.dinheiro(item.number(q+"total"))).append("\n");
            }
            text.append("\nDesconto geral: ").append(Tema.dinheiro(r.number(p+"desconto"))).append("\nTotal: ").append(Tema.dinheiro(r.number(p+"total")));if(venda) {
                text.append("\n\nPAGAMENTOS\n");for(Registro pg:cadastro.banco.vendaPagto.listarPorMovimento(r.id("vda_codigo")))if(pg.id("vda_codigo")==r.id("vda_codigo"))text.append(cadastro.banco.formaPagto.buscarPorId(pg.id("fpg_codigo")).text("fpg_nome")).append(": ").append(Tema.dinheiro(pg.number("vdp_valor"))).append("\n");
            }
            text.append("\nObservações: ").append(r.text(p+"obs"));JTextArea area=new JTextArea(text.toString(),18,55);area.setEditable(false);area.setLineWrap(true);area.setWrapStyleWord(true);JOptionPane.showMessageDialog(this,new JScrollPane(area),"Detalhes",JOptionPane.PLAIN_MESSAGE);
        }
        );
    }
}

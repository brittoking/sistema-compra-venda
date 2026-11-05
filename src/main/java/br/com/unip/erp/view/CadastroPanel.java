package br.com.unip.erp.view;
import br.com.unip.erp.controller.*;
import br.com.unip.erp.model.*;
import br.com.unip.erp.dao.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
public class CadastroPanel extends JPanel  {
    private final CadastroController controller;
    private final DaoJdbc<?> dao;
    private final int usuario;
    private final DefaultTableModel model;
    private final JTable table;
    private java.util.List<Registro> rows=new ArrayList<>();
    public CadastroPanel(CadastroController controller,DaoJdbc<?> dao,int usuario) {
        super(new BorderLayout(0,18));
        this.controller=controller;
        this.dao=dao;
        this.usuario=usuario;
        setBorder(BorderFactory.createEmptyBorder(24,28,24,28));
        model=Tema.modelo("Código",dao==controller.banco.produto?"Produto":"Nome / descrição",dao==controller.banco.produto?"Preço de venda":"Identificação",dao==controller.banco.produto?"Estoque":"Ativo");
        table=new JTable(model);
        Tema.tabela(table);
        JPanel top=new JPanel(new BorderLayout());
        top.add(Tema.titulo(dao.titulo,27),BorderLayout.NORTH);
        JTextField busca=new JTextField(23);
        busca.setToolTipText("Buscar em todas as colunas");
        busca.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                filtrar();
            }
            public void removeUpdate(DocumentEvent e) {
                filtrar();
            }
            public void changedUpdate(DocumentEvent e) {
                filtrar();
            }
            void filtrar() {
                ((TableRowSorter<?>)table.getRowSorter()).setRowFilter(RowFilter.regexFilter("(?i)"+java.util.regex.Pattern.quote(busca.getText())));
            }
        }
        );
        top.add(Tema.linha(new JLabel("Pesquisar"),busca,Tema.primario("+ Novo",()->editar(null)),Tema.botao("Editar",()->selecionar(this::editar)),Tema.botao("Excluir",()->selecionar(this::excluir)),Tema.botao("Atualizar",this::atualizar)),BorderLayout.SOUTH);
        add(top,BorderLayout.NORTH);
        add(Tema.rolagem(table),BorderLayout.CENTER);
        add(new JLabel("Selecione uma linha para editar • Duplo clique também abre o cadastro"),BorderLayout.SOUTH);
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount()==2)selecionar(CadastroPanel.this::editar);
            }
        }
        );
        atualizar();
    }
    private void selecionar(java.util.function.Consumer<Registro> action) {
        int row=table.getSelectedRow();
        if(row<0) {
            JOptionPane.showMessageDialog(this,"Selecione um registro na tabela.");
            return;
        }
        action.accept(rows.get(table.convertRowIndexToModel(row)));
    }
    public void atualizar() {
        Tema.tentar(this,()-> {
            rows=controller.listar(dao);model.setRowCount(0);for(Registro r:rows) {
                String extra=dao==controller.banco.usuario?r.text("usu_login"):dao.pessoa()?r.text("pes_cpfcnpj"):"—";String ativo=r.text(dao.pessoa()?"pes_ativo":dao==controller.banco.usuario?"usu_ativo":"fpg_ativo");model.addRow(new Object[] {
                    r.id(dao.chave),r.text(dao.nome()),dao==controller.banco.produto?Tema.dinheiro(r.number("pro_preco")):extra,dao==controller.banco.produto?r.number("pro_estoque"):ativo.equals("S")?"Sim":"Não"
                }
                );
            }
        }
        );
    }
    private void editar(Registro r) {
        JDialog d=new JDialog(SwingUtilities.getWindowAncestor(this),(r==null?"Novo cadastro • ":"Editar • ")+dao.titulo,Dialog.ModalityType.APPLICATION_MODAL);
        FormularioPanel form=new FormularioPanel(dao,r);
        d.add(new JScrollPane(form),BorderLayout.CENTER);
        d.add(Tema.linha(new JLabel("* Campos obrigatórios"),Tema.botao("Cancelar",d::dispose),Tema.primario("Salvar cadastro",()->Tema.tentar(d,()-> {
            Registro data=form.ler();if(dao==controller.banco.usuario&&data.id(dao.chave)==usuario&&data.text("usu_ativo").equals("N"))throw new IllegalArgumentException("Não é possível desativar o usuário conectado.");controller.salvar(dao,data);d.dispose();atualizar();
        }
        ))),BorderLayout.SOUTH);
        d.setSize(760,dao.pessoa()?710:580);
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }
    private void excluir(Registro r) {
        if(JOptionPane.showConfirmDialog(this,"Excluir "+r.text(dao.nome())+"?","Confirmar exclusão",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION)Tema.tentar(this,()-> {
            controller.excluir(dao,r.id(dao.chave),usuario);atualizar();
        }
        );
    }
}

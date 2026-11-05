package br.com.unip.erp.view;
import br.com.unip.erp.controller.*;
import br.com.unip.erp.model.*;
import br.com.unip.erp.dao.*;
import javax.swing.*;
import java.awt.*;
public class Menu extends JPanel  {
    private final CadastroController controller;
    private final Registro usuario;
    private final JPanel content=new JPanel(new BorderLayout());
    public final JMenuBar barra=new JMenuBar();
    public Menu(CadastroController controller,Registro usuario,Runnable sair) {
        super(new BorderLayout());
        this.controller=controller;
        this.usuario=usuario;
        JPanel nav=new JPanel();
        nav.setLayout(new BoxLayout(nav,BoxLayout.Y_AXIS));
        nav.setBackground(Tema.NAV);
        nav.setBorder(BorderFactory.createEmptyBorder(27,16,20,16));
        nav.setPreferredSize(new Dimension(210,0));
        JLabel brand=Tema.titulo("BRITTO / ERP",22);
        brand.setForeground(Color.WHITE);
        brand.setAlignmentX(0);
        nav.add(brand);
        nav.add(Box.createVerticalStrut(8));
        JLabel sub=new JLabel("Compra e venda");
        sub.setForeground(new Color(158,171,202));
        nav.add(sub);
        nav.add(Box.createVerticalStrut(35));
        nav.add(link("Visão geral",this::inicio));
        nav.add(Box.createVerticalStrut(18));
        JLabel label=new JLabel("CADASTROS");
        label.setForeground(new Color(158,171,202));
        nav.add(label);
        nav.add(Box.createVerticalStrut(8));
        JMenu cad=new JMenu("Cadastros"),mov=new JMenu("Movimentos"),analises=new JMenu("Análises"),session=new JMenu("Sair");
        barra.add(cad);
        barra.add(mov);
        barra.add(analises);
        barra.add(session);
        JMenuItem menuRelatorios = new JMenuItem("Gerar relatório");
        menuRelatorios.addActionListener(a -> abrir(new RelatorioPanel()));
        analises.add(menuRelatorios);
        JMenuItem menuDashboard = new JMenuItem("Dashboard de vendas e compras");
        menuDashboard.addActionListener(a -> abrirDashboard());
        analises.add(menuDashboard);
        for(DaoJdbc<?> e:new DaoJdbc<?>[] {
            controller.banco.cliente,controller.banco.fornecedor,controller.banco.produto,controller.banco.formaPagto,controller.banco.usuario
        }
        ) {
            nav.add(link(e.titulo,()->abrir(new CadastroPanel(controller,e,usuario.id("usu_codigo")))));
            JMenuItem item=new JMenuItem(e.titulo);
            item.addActionListener(a->abrir(new CadastroPanel(controller,e,usuario.id("usu_codigo"))));
            cad.add(item);
        }
        nav.add(Box.createVerticalStrut(20));
        JLabel label2=new JLabel("MOVIMENTOS");
        label2.setForeground(new Color(158,171,202));
        nav.add(label2);
        nav.add(Box.createVerticalStrut(8));
        nav.add(link("Nova venda",()->nova(true)));
        nav.add(link("Nova compra",()->nova(false)));
        nav.add(Box.createVerticalStrut(20));
        JLabel label3=new JLabel("ANÁLISES");
        label3.setForeground(new Color(158,171,202));
        nav.add(label3);
        nav.add(Box.createVerticalStrut(8));
        nav.add(link("Gerar relatório",()->abrir(new RelatorioPanel())));
        nav.add(link("Dashboard", this::abrirDashboard));
        for(boolean venda:new boolean[] {
            true,false
        }
        ) {
            JMenuItem item=new JMenuItem(venda?"Venda":"Compra");
            item.addActionListener(a->nova(venda));
            mov.add(item);
        }
        JMenuItem exit=new JMenuItem("Encerrar sessão");
        exit.addActionListener(a->sair.run());
        session.add(exit);
        nav.add(Box.createVerticalGlue());
        nav.add(link("Encerrar sessão",sair));
        add(nav,BorderLayout.WEST);
        add(content,BorderLayout.CENTER);
        JLabel status=new JLabel("  Conectado: "+usuario.text("usu_nome")+" • PostgreSQL");
        status.setBorder(BorderFactory.createEmptyBorder(9,8,9,8));
        status.setForeground(Tema.MUTED);
        add(status,BorderLayout.SOUTH);
        inicio();
    }
    private JButton link(String label,Runnable action) {
        JButton b=Tema.botao(label,action);
        b.setBackground(Tema.NAV);
        b.setForeground(Color.WHITE);
        b.setBorder(BorderFactory.createEmptyBorder(11,4,11,4));
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setMaximumSize(new Dimension(200,42));
        b.setAlignmentX(0);
        return b;
    }
    private void abrir(JPanel panel) {
        if(content.getComponentCount()>0 && content.getComponent(0) instanceof MovimentoPanel m && m.temRascunho() && JOptionPane.showConfirmDialog(this,"Sair desta tela e descartar o movimento não salvo?","Movimento em andamento",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
        content.removeAll();
        content.add(panel);
        content.revalidate();
        content.repaint();
    }
    public void nova(boolean venda) {
        abrir(new MovimentoPanel(venda,controller,usuario));
    }
    private void abrirDashboard() {
        abrir(new DashboardPanel());
    }

    private void inicio() {
        Tema.tentar(this,()-> {
            JPanel home=new JPanel(new BorderLayout(0,25));home.setBorder(BorderFactory.createEmptyBorder(35,32,30,32));JPanel heading=Tema.coluna();heading.add(Tema.titulo("Visão geral",29));heading.add(Box.createVerticalStrut(9));heading.add(new JLabel("Organize os cadastros e acompanhe as movimentações do seu negócio."));home.add(heading,BorderLayout.NORTH);
            JPanel body=Tema.coluna();JPanel cards=new JPanel(new GridLayout(1,3,16,0));for(DaoJdbc<?> e:new DaoJdbc<?>[] {
                controller.banco.cliente,controller.banco.produto,controller.banco.venda
            }
            ) {
                JPanel card=new JPanel(new BorderLayout(0,12));card.setBackground(Color.WHITE);card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Tema.LINE),BorderFactory.createEmptyBorder(22,22,22,22)));card.add(new JLabel(e.titulo+" • registros"),BorderLayout.NORTH);card.add(Tema.titulo(String.valueOf(controller.listar(e).size()),36),BorderLayout.CENTER);cards.add(card);
            }
            cards.setMaximumSize(new Dimension(Integer.MAX_VALUE,135));
            body.add(cards);
            body.add(Box.createVerticalStrut(28));
            JPanel actions=new JPanel(new BorderLayout(0,15));
            actions.setOpaque(false);
            actions.add(Tema.titulo("O que vamos fazer hoje?",20),BorderLayout.NORTH);
            actions.add(Tema.linha(Tema.primario("+ Registrar venda",()->nova(true)),Tema.botao("+ Registrar compra",()->nova(false)),
                    Tema.botao("Cadastrar produto",()->abrir(new CadastroPanel(controller, controller.banco.produto, usuario.id("usu_codigo"))))),
                    BorderLayout.CENTER);
            actions.setMaximumSize(new Dimension(Integer.MAX_VALUE,105));
            body.add(actions);
            body.add(Box.createVerticalStrut(20));
            JPanel analitica = new JPanel(new BorderLayout(0, 10));
            analitica.setOpaque(false);
            analitica.add(Tema.titulo("Relatórios e análise", 20), BorderLayout.NORTH);
            analitica.add(Tema.linha(
                Tema.primario("Gerar relatório", () -> abrir(new RelatorioPanel())),
                Tema.botao("Abrir dashboard", this::abrirDashboard)
            ), BorderLayout.CENTER);
            analitica.setMaximumSize(new Dimension(Integer.MAX_VALUE, 105));
            body.add(analitica);
            body.add(Box.createVerticalStrut(25));
            JTextArea info=new JTextArea("FLUXO DE TRABALHO\n\n1. Cadastre clientes, fornecedores e produtos.\n2. Defina as formas de pagamento.\n3." +
                    " Registre uma compra para entrada de estoque.\n4. Registre a venda e distribua o total entre os pagamentos.");
            info.setEditable(false);
            info.setOpaque(false);
            info.setForeground(Tema.MUTED);
            info.setFont(new Font("SansSerif",Font.PLAIN,14));
            body.add(info);
            home.add(body,BorderLayout.CENTER);
            abrir(home);
        }
        );
    }
}

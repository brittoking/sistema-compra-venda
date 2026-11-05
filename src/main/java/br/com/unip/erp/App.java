package br.com.unip.erp;

import br.com.unip.erp.dao.Banco;
import br.com.unip.erp.controller.CadastroController;
import br.com.unip.erp.model.*;
import br.com.unip.erp.util.HibernateUtil;
import br.com.unip.erp.view.*;
import br.com.unip.erp.view.Menu;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.util.Arrays;

public class App {
    private final CadastroController controller;
    private final JFrame frame=new JFrame("Britto ERP • Sistema de compra e venda");
    public App(Banco banco) {
        controller=new CadastroController(banco);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent event) {
                if(JOptionPane.showConfirmDialog(frame,"Fechar o sistema? Movimentos não salvos serão perdidos.","Sair",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION) { HibernateUtil.fechar(); frame.dispose(); }
            }
        });
        login();
    }
    private void login() {
        frame.setJMenuBar(null);
        frame.setMinimumSize(new Dimension(900,520));
        LoginPanel panel=new LoginPanel(() -> {});
        for(ActionListener listener:panel.entrar.getActionListeners()) panel.entrar.removeActionListener(listener);
        panel.entrar.addActionListener(event -> autenticar(panel));
        panel.senha.addActionListener(event -> panel.entrar.doClick());
        frame.setContentPane(panel);
        frame.getRootPane().setDefaultButton(panel.entrar);
        frame.setSize(930,560);
        frame.setLocationRelativeTo(null);
        frame.revalidate();frame.repaint();frame.setVisible(true);
    }
    private void autenticar(LoginPanel panel) {
        panel.entrar.setEnabled(false);
        String login=panel.login.getText();char[] senha=panel.senha.getPassword();
        new SwingWorker<Usuario,Void>() {
            @Override protected Usuario doInBackground() throws Exception {
                try { return controller.autenticar(login,senha); }
                finally { Arrays.fill(senha,'\0'); }
            }
            @Override protected void done() {
                panel.entrar.setEnabled(true);panel.senha.setText("");
                try {
                    Usuario user=get();
                    if(user==null) { JOptionPane.showMessageDialog(frame,"Login ou senha inválidos, ou usuário inativo.");return; }
                    Menu menu=new Menu(controller,user,() -> {
                        if(JOptionPane.showConfirmDialog(frame,"Encerrar a sessão? Movimentos não salvos serão perdidos.","Sair",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION) login();
                    });
                    frame.getRootPane().setDefaultButton(null);
                    frame.setContentPane(menu);frame.setJMenuBar(menu.barra);
                    frame.setMinimumSize(new Dimension(1120,720));frame.setSize(1250,800);
                    frame.setLocationRelativeTo(null);frame.revalidate();frame.repaint();
                } catch(Exception ex) { Tema.erro(frame,ex.getCause() instanceof Exception cause?cause:ex); }
            }
        }.execute();
    }
    private static boolean primeiroAcesso(Banco banco) throws Exception {
        if(!banco.usuario.listar().isEmpty()) return true;
        JTextField nome=new JTextField("Administrador"),login=new JTextField("admin");
        JPasswordField senha=new JPasswordField();
        JPanel form=new JPanel(new GridLayout(0,1,0,8));
        form.add(new JLabel("Banco sem usuários: crie seu primeiro acesso."));
        form.add(Tema.campo("Nome",nome));form.add(Tema.campo("Login",login));form.add(Tema.campo("Senha (mínimo 6 caracteres)",senha));
        if(JOptionPane.showConfirmDialog(null,form,"Primeiro acesso",JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION) return false;
        char[] segredo=senha.getPassword();
        try {
            if(segredo.length<6) throw new IllegalArgumentException("Use uma senha com pelo menos 6 caracteres.");
            Registro user=new Registro().set("usu_nome",nome.getText()).set("usu_login",login.getText()).set("usu_senha",new String(segredo))
                .set("usu_cadastro",LocalDate.now()).set("usu_ativo","S");
            new CadastroController(banco).salvar(banco.usuario,user);
        } finally { Arrays.fill(segredo,'\0');senha.setText(""); }
        return true;
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Tema.instalar();
            try { Banco banco=new Banco();if(primeiroAcesso(banco)) new App(banco); }
            catch(Exception ex) { Tema.erro(null,ex); }
        });
    }
}

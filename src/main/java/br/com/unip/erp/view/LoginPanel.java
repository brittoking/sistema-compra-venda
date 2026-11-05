package br.com.unip.erp.view;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.Random;
public class LoginPanel extends JPanel  {
    public final JTextField login=new JTextField("admin");
    public final JPasswordField senha=new JPasswordField();
    public final JButton entrar;
    public LoginPanel(Runnable action) {
        super(new GridLayout(1,2));
        setPreferredSize(new Dimension(900,520));
        JPanel form=new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        JPanel content=new JPanel();
        content.setLayout(new BoxLayout(content,BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);
        content.setPreferredSize(new Dimension(305,430));
        JLabel brand=Tema.titulo("BRITTO / ERP",14);
        brand.setForeground(Tema.PRIMARY);
        content.add(brand);
        content.add(Tema.titulo("Bem-vindo de volta",24));
        content.add(new JLabel("Entre para gerenciar compras e vendas."));
        content.add(Tema.campo("Login",login));
        content.add(Tema.campo("Senha",senha));
        entrar=Tema.primario("Entrar no sistema",action);
        content.add(entrar);
        JLabel hint=new JLabel("PostgreSQL • acesso por usuário cadastrado");
        hint.setFont(new Font("SansSerif",Font.PLAIN,11));
        hint.setForeground(Tema.MUTED);
        content.add(hint);
        for(Component child:content.getComponents()) {
            JComponent jc=(JComponent)child;
            jc.setAlignmentX(0);
            int height=jc instanceof JPanel?66:jc instanceof JButton?44:jc==hint?30:40;
            jc.setMaximumSize(new Dimension(305,height));
            jc.setPreferredSize(new Dimension(305,height));
        }
        content.add(Box.createVerticalStrut(14),5);
        content.add(Box.createVerticalStrut(10),4);
        form.add(content);
        add(form);
        add(new JPanel() {
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);Graphics2D g=(Graphics2D)graphics.create();g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);int w=getWidth(),h=getHeight();g.setPaint(new GradientPaint(0,0,new Color(27,30,67),w,h,new Color(84,61,157)));g.fillRect(0,0,w,h);Random random=new Random(10);for(int i=0;i<80;i++) {
                    g.setColor(new Color(255,255,255,35+random.nextInt(110)));g.fillOval(random.nextInt(Math.max(1,w)),random.nextInt(Math.max(1,h)),2,2);
                }
                g.setColor(new Color(157,139,254,40));g.setStroke(new BasicStroke(1.4f));for(int i=0;i<5;i++)g.draw(new Ellipse2D.Double(w*.42-i*26,h*.1+i*28,350,350));g.setColor(Color.WHITE);g.setFont(new Font("SansSerif",Font.BOLD,33));g.drawString("Seu negócio.",35,h-145);g.drawString("Tudo conectado.",35,h-102);g.setFont(new Font("SansSerif",Font.PLAIN,13));g.setColor(new Color(211,210,237));g.drawString("CADASTROS  /  COMPRAS  /  VENDAS",35,h-53);g.dispose();
            }
        }
        );
    }
}

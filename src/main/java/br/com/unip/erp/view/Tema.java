package br.com.unip.erp.view;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.text.NumberFormat;
import java.util.Locale;
public final class Tema  {
    public static final Color BG=new Color(243,246,251),INK=new Color(29,40,64),MUTED=new Color(105,117,139),PRIMARY=new Color(83,72,218),LINE=new Color(222,228,238),NAV=new Color(24,32,55);
    public static void instalar() {
        UIManager.put("defaultFont",new Font("SansSerif",Font.PLAIN,13));
        for(Object key:UIManager.getDefaults().keySet().toArray())if(key.toString().endsWith(".font"))UIManager.put(key,new Font("SansSerif",Font.PLAIN,13));
        UIManager.put("Panel.background",BG);
        UIManager.put("Label.foreground",INK);
        UIManager.put("TextField.background",Color.WHITE);
        UIManager.put("Table.selectionBackground",new Color(226,224,253));
        UIManager.put("Table.selectionForeground",INK);
    }
    public static JLabel titulo(String s,int size) {
        JLabel l=new JLabel(s);
        l.setFont(new Font("SansSerif",Font.BOLD,size));
        l.setForeground(INK);
        return l;
    }
    public static JButton botao(String s,Runnable action) {
        JButton b=new JButton(s);
        b.setFocusPainted(false);
        b.setBackground(Color.WHITE);
        b.setForeground(INK);
        b.setBorder(new CompoundBorder(new LineBorder(LINE),new EmptyBorder(9,15,9,15)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(e->action.run());
        return b;
    }
    public static JButton primario(String s,Runnable action) {
        JButton b=botao(s,action);
        b.setBackground(PRIMARY);
        b.setForeground(Color.WHITE);
        b.setOpaque(true);
        b.setBorder(new EmptyBorder(10,16,10,16));
        return b;
    }
    public static JPanel linha(Component... comps) {
        JPanel p=new JPanel(new FlowLayout(FlowLayout.LEFT,10,7));
        p.setOpaque(false);
        for(Component c:comps)p.add(c);
        return p;
    }
    public static JPanel coluna() {
        JPanel p=new JPanel();
        p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));
        p.setOpaque(false);
        return p;
    }
    public static JPanel campo(String label,JComponent control) {
        JPanel p=new JPanel(new BorderLayout(0,6));
        p.setOpaque(false);
        JLabel l=new JLabel(label);
        l.setForeground(MUTED);
        p.add(l,BorderLayout.NORTH);
        control.setPreferredSize(new Dimension(200,34));
        p.add(control,BorderLayout.CENTER);
        return p;
    }
    public static void tabela(JTable t) {
        t.setRowHeight(35);
        t.setShowVerticalLines(false);
        t.setGridColor(LINE);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.getTableHeader().setPreferredSize(new Dimension(0,36));
        t.getTableHeader().setFont(new Font("SansSerif",Font.BOLD,12));
        t.setFillsViewportHeight(true);
        t.setBackground(Color.WHITE);
        t.setAutoCreateRowSorter(true);
    }
    public static DefaultTableModel modelo(String... cols) {
        return new DefaultTableModel(cols,0) {
            public boolean isCellEditable(int r,int c) {
                return false;
            }
        }
        ;
    }
    public static JScrollPane rolagem(JTable t) {
        JScrollPane p=new JScrollPane(t);
        p.setColumnHeaderView(t.getTableHeader());
        return p;
    }
    public static String dinheiro(Object n) {
        return NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).format(n);
    }
    public static void erro(Component parent,Exception ex) {
        String msg=ex.getMessage();
        if(ex instanceof java.sql.SQLException)msg="Não foi possível concluir a operação no banco. Confira conexão, esquema e vínculos do registro.\nDetalhe: "+msg;
        JOptionPane.showMessageDialog(parent,msg,"Confira os dados",JOptionPane.WARNING_MESSAGE);
    }
    public static void tentar(Component parent,Tarefa r) {
        try {
            r.executar();
        }
        catch(Exception e) {
            erro(parent,e);
        }
    }
    @FunctionalInterface public interface Tarefa {
        void executar()throws Exception;
    }
}

package br.com.unip.erp.view;
import br.com.unip.erp.model.*;
import br.com.unip.erp.dao.*;
import javax.swing.*;
import java.awt.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
/** Campos derivados dos metadados do DER, sem SQL na camada de apresentação. */
public class FormularioPanel extends JPanel  {
    private final Map<String,JComponent> controls=new LinkedHashMap<>();
    private final DaoJdbc<?> dao;
    private final Registro original;
    public FormularioPanel(DaoJdbc<?> e,Registro row) {
        super(new GridBagLayout());
        dao=e;
        original=row==null?new Registro():row;
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(18,18,18,18));
        int i=0;
        for(Campo f:e.formulario()) {
            Object value=original.get(f.coluna());
            JComponent c;
            if(f.tipo().equals("active")||f.tipo().equals("person")) {
                JComboBox<String> combo=new JComboBox<>(f.tipo().equals("active")?new String[] {
                    "Sim","Não"
                }
                :new String[] {
                    "Física","Jurídica"
                }
                );
                combo.setSelectedIndex("N".equals(value)?1:0);
                c=combo;
            }
            else  {
                JTextField t=f.tipo().equals("password")?new JPasswordField():new JTextField();
                String v=value==null?"":value.toString();
                if(f.tipo().equals("date"))v=value instanceof LocalDate d?d.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")):row==null?LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")):"";
                if(f.tipo().equals("password"))v="";
                if(value==null&&Set.of("money","decimal","int").contains(f.tipo()))v="0";
                t.setText(v);
                t.setMargin(new Insets(5,8,5,8));
                c=t;
            }
            controls.put(f.coluna(),c);
            GridBagConstraints gc=new GridBagConstraints();
            gc.gridx=i%2;
            gc.gridy=i/2;
            gc.weightx=1;
            gc.fill=GridBagConstraints.HORIZONTAL;
            gc.insets=new Insets(7,9,7,9);
            String label=f.titulo()+(f.obrigatorio()?" *":"");
            if(f.tipo().equals("password")&&original.id(e.chave)>0)label="Nova senha (vazia mantém a atual)";
            add(Tema.campo(label,c),gc);
            i++;
        }
    }
    public Registro ler() {
        Registro r=new Registro(original);
        for(Campo f:dao.formulario()) {
            JComponent c=controls.get(f.coluna());
            Object v=c instanceof JComboBox<?> box?(box.getSelectedIndex()==0?"S":"N"):((JTextField)c).getText();
            r.put(f.coluna(),v);
        }
        return r;
    }
}

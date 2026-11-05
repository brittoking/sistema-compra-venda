package br.com.unip.erp.dao;

import br.com.unip.erp.model.*;
import java.util.List;

/** DAO de venda com operações comuns persistidas pelo Hibernate. */
public class VendaDaoImpl extends DaoJdbc<Venda> {
    public VendaDaoImpl() {
        super("venda", "vda_codigo", "vda_codigo", "Vendas", Venda::new, Venda.class,
            List.of(new Campo("usu_codigo","Usuário","int",true,0),
                new Campo("cli_codigo","Cliente","int",true,0),
                new Campo("vda_data","Data","date",true,0),
                new Campo("vda_valor","Valor","money",false,0),
                new Campo("vda_desconto","Desconto","money",false,0),
                new Campo("vda_total","Total","money",false,0),
                new Campo("vda_obs","Observações","text",false,0)));
    }
}

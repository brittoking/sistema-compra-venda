package br.com.unip.erp.dao;

import br.com.unip.erp.model.*;
import java.util.List;

/** DAO de compra com operações comuns persistidas pelo Hibernate. */
public class CompraDaoImpl extends DaoJdbc<Compra> {
    public CompraDaoImpl() {
        super("compra", "cpr_codigo", "cpr_codigo", "Compras", Compra::new, Compra.class,
            List.of(new Campo("usu_codigo","Usuário","int",true,0),
                new Campo("for_codigo","Fornecedor","int",true,0),
                new Campo("cpr_emissao","Emissão","date",false,0),
                new Campo("cpr_valor","Valor","money",false,0),
                new Campo("cpr_desconto","Desconto","money",false,0),
                new Campo("cpr_total","Total","money",false,0),
                new Campo("cpr_dtentrada","Entrada","date",false,0),
                new Campo("cpr_obs","Observações","text",false,0)));
    }
}

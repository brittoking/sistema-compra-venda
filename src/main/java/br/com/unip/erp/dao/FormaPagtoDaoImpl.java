package br.com.unip.erp.dao;

import br.com.unip.erp.model.*;
import java.util.List;

/** DAO de formapagto com operações comuns persistidas pelo Hibernate. */
public class FormaPagtoDaoImpl extends DaoJdbc<FormaPagto> {
    public FormaPagtoDaoImpl() {
        super("formapagto", "fpg_codigo", "fpg_nome", "Formas de pagamento", FormaPagto::new, FormaPagto.class,
            List.of(new Campo("fpg_nome","Nome","text",true,80),
                new Campo("fpg_ativo","Ativo","active",true,1)));
    }
}

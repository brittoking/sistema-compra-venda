package br.com.unip.erp.dao;

import br.com.unip.erp.model.*;
import java.util.List;

/** DAO de venda_pagto com operações comuns persistidas pelo Hibernate. */
public class VendaPagtoDaoImpl extends DaoJdbc<VendaPagto> {
    public VendaPagtoDaoImpl() {
        super("venda_pagto", "vdp_codigo", "vdp_codigo", "Pagamentos da venda", VendaPagto::new, VendaPagto.class,
            List.of(new Campo("vda_codigo","Venda","int",true,0),
                new Campo("fpg_codigo","Forma de pagamento","int",true,0),
                new Campo("vdp_valor","Valor","money",false,0)));
    }
    public java.util.List<VendaPagto> listarPorMovimento(int id) throws Exception { return listarPor("vda_codigo", id); }
}

package br.com.unip.erp.dao;

import br.com.unip.erp.model.*;
import java.util.List;

/** DAO de venda_produto com operações comuns persistidas pelo Hibernate. */
public class VendaProdutoDaoImpl extends DaoJdbc<VendaProduto> {
    public VendaProdutoDaoImpl() {
        super("venda_produto", "vep_codigo", "vep_codigo", "Itens da venda", VendaProduto::new, VendaProduto.class,
            List.of(new Campo("vda_codigo","Movimento","int",true,0),
                new Campo("pro_codigo","Produto","int",true,0),
                new Campo("vep_qtde","Quantidade","decimal",false,0),
                new Campo("vep_preco","Preco","money",false,0),
                new Campo("vep_desconto","Desconto","money",false,0),
                new Campo("vep_total","Total","money",false,0)));
    }
    public java.util.List<VendaProduto> listarPorMovimento(int id) throws Exception { return listarPor("vda_codigo", id); }
}

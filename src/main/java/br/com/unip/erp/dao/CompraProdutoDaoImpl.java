package br.com.unip.erp.dao;

import br.com.unip.erp.model.*;
import java.util.List;

/** DAO de compra_produto com operações comuns persistidas pelo Hibernate. */
public class CompraProdutoDaoImpl extends DaoJdbc<CompraProduto> {
    public CompraProdutoDaoImpl() {
        super("compra_produto", "cpp_codigo", "cpp_codigo", "Itens da compra", CompraProduto::new, CompraProduto.class,
            List.of(new Campo("cpr_codigo","Movimento","int",true,0),
                new Campo("pro_codigo","Produto","int",true,0),
                new Campo("cpr_qtde","Quantidade","decimal",false,0),
                new Campo("cpr_preco","Preco","money",false,0),
                new Campo("cpr_desconto","Desconto","money",false,0),
                new Campo("cpr_total","Total","money",false,0)));
    }
    public java.util.List<CompraProduto> listarPorMovimento(int id) throws Exception { return listarPor("cpr_codigo", id); }
}

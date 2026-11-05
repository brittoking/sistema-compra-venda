package br.com.unip.erp.dao;

import br.com.unip.erp.model.*;
import java.util.List;

/** DAO de produto com operações comuns persistidas pelo Hibernate. */
public class ProdutoDaoImpl extends DaoJdbc<Produto> {
    public ProdutoDaoImpl() {
        super("produto", "pro_codigo", "pro_nome", "Produtos", Produto::new, Produto.class,
            List.of(new Campo("pro_nome","Nome","text",true,80),
                new Campo("pro_estoque","Estoque","decimal",false,0),
                new Campo("pro_unidade","Unidade","text",false,5),
                new Campo("pro_preco","Preço","money",false,0),
                new Campo("pro_custo","Custo","money",false,0),
                new Campo("pro_atacado","Atacado","money",false,0),
                new Campo("pro_min","Estoque mínimo","decimal",false,0),
                new Campo("pro_max","Estoque máximo","decimal",false,0),
                new Campo("pro_embalagem","Embalagem","int",false,0),
                new Campo("pro_peso","Peso","decimal",false,0),
                new Campo("pro_cadastro","Cadastro","date",false,0),
                new Campo("pro_obs","Observações","text",false,0),
                new Campo("pro_ativo","Ativo","active",true,1)));
    }

    public Produto buscarParaAtualizar(int id) throws Exception { return buscarBloqueado(id); }
    public void atualizarEstoque(int id, java.math.BigDecimal estoque) throws Exception {
        if (estoque.signum() < 0) throw new IllegalArgumentException("Estoque não pode ser negativo.");
        atualizarColuna(id, "pro_estoque", estoque.setScale(4, java.math.RoundingMode.UNNECESSARY));
    }
}

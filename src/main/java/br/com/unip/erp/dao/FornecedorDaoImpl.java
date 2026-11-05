package br.com.unip.erp.dao;

import br.com.unip.erp.model.*;
import java.util.List;

/** DAO de fornecedor com operações comuns persistidas pelo Hibernate. */
public class FornecedorDaoImpl extends DaoJdbc<Fornecedor> {
    public FornecedorDaoImpl() {
        super("fornecedor", "for_codigo", "pes_nome", "Fornecedores", Fornecedor::new, Fornecedor.class,
            List.of(new Campo("pes_codigo","Pessoa","int",true,0),
                new Campo("for_contato","Contato","text",false,80)));
    }

    @Override public boolean pessoa() { return true; }
    @Override public java.util.List<Campo> formulario() {
        var lista = new java.util.ArrayList<>(new PessoaDaoImpl().campos);
        campos.stream().filter(c -> !c.coluna().equals("pes_codigo")).forEach(lista::add);
        return java.util.List.copyOf(lista);
    }
}

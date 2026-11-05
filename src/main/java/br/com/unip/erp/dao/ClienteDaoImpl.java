package br.com.unip.erp.dao;

import br.com.unip.erp.model.*;
import java.util.List;

/** DAO de cliente com operações comuns persistidas pelo Hibernate. */
public class ClienteDaoImpl extends DaoJdbc<Cliente> {
    public ClienteDaoImpl() {
        super("cliente", "cli_codigo", "pes_nome", "Clientes", Cliente::new, Cliente.class,
            List.of(new Campo("pes_codigo","Pessoa","int",true,0),
                new Campo("cli_limitecred","Limite de crédito","money",false,0)));
    }

    @Override public boolean pessoa() { return true; }
    @Override public java.util.List<Campo> formulario() {
        var lista = new java.util.ArrayList<>(new PessoaDaoImpl().campos);
        campos.stream().filter(c -> !c.coluna().equals("pes_codigo")).forEach(lista::add);
        return java.util.List.copyOf(lista);
    }
}

package br.com.unip.erp.dao;

import br.com.unip.erp.model.*;
import java.util.List;

/** DAO de usuario com operações comuns persistidas pelo Hibernate. */
public class UsuarioDaoImpl extends DaoJdbc<Usuario> {
    public UsuarioDaoImpl() {
        super("usuario", "usu_codigo", "usu_nome", "Usuários", Usuario::new, Usuario.class,
            List.of(new Campo("usu_nome","Nome","text",false,80),
                new Campo("usu_login","Login","text",true,20),
                new Campo("usu_senha","Senha","password",true,128),
                new Campo("usu_cadastro","Cadastro","date",false,0),
                new Campo("usu_ativo","Ativo","active",true,1)));
    }

    public Usuario buscarPorLogin(String login) throws Exception {
        var encontrados = listarPor("usu_login", login);
        if (encontrados.size() > 1) throw new IllegalStateException("Há logins duplicados no banco. Corrija o cadastro de usuários.");
        return encontrados.isEmpty() ? null : encontrados.get(0);
    }
}

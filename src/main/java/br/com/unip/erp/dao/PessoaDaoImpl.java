package br.com.unip.erp.dao;

import br.com.unip.erp.model.*;
import java.util.List;

/** DAO de pessoa com operações comuns persistidas pelo Hibernate. */
public class PessoaDaoImpl extends DaoJdbc<Pessoa> {
    public PessoaDaoImpl() {
        super("pessoa", "pes_codigo", "pes_nome", "Pessoas", Pessoa::new, Pessoa.class,
            List.of(new Campo("pes_nome","Nome","text",true,80),
                new Campo("pes_fantasia","Nome fantasia","text",false,80),
                new Campo("pes_fisica","Tipo de pessoa","person",true,1),
                new Campo("pes_cpfcnpj","CPF / CNPJ","text",true,20),
                new Campo("pes_rgie","RG / IE","text",false,20),
                new Campo("pes_cadastro","Cadastro","date",false,0),
                new Campo("pes_endereco","Endereço","text",false,120),
                new Campo("pes_numero","Número","text",false,10),
                new Campo("pes_complemento","Complemento","text",false,30),
                new Campo("pes_bairro","Bairro","text",false,50),
                new Campo("pes_cidade","Cidade","text",false,80),
                new Campo("pes_uf","UF","text",false,2),
                new Campo("pes_cep","CEP","text",false,9),
                new Campo("pes_fone1","Telefone 1","text",false,16),
                new Campo("pes_fone2","Telefone 2","text",false,16),
                new Campo("pes_celular","Celular","text",false,16),
                new Campo("pes_site","Site","text",false,200),
                new Campo("pes_email","E-mail","text",false,200),
                new Campo("pes_ativo","Ativo","active",true,1)));
    }
}

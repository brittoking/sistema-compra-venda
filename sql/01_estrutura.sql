-- Estrutura do SQL enviado pelo usuário. Executar somente em schema vazio.
-- Não execute este arquivo sobre um banco que já contenha estas tabelas.

CREATE TABLE pessoa (
    pes_codigo serial NOT NULL PRIMARY KEY,
    pes_nome varchar(80) NOT NULL,
    pes_fantasia varchar(80),
    pes_fisica char(1) NOT NULL,
    pes_cpfcnpj varchar(20) NOT NULL,
    pes_rgie varchar(20),
    pes_cadastro date,
    pes_endereco varchar(120),
    pes_numero varchar(10),
    pes_complemento varchar(30),
    pes_bairro varchar(50),
    pes_cidade varchar(80),
    pes_uf char(2),
    pes_cep varchar(9),
    pes_fone1 varchar(16),
    pes_fone2 varchar(16),
    pes_celular varchar(16),
    pes_site varchar(200),
    pes_email varchar(200),
    pes_ativo char(1)
);

CREATE TABLE cliente (
    cli_codigo serial NOT NULL PRIMARY KEY,
    pes_codigo integer NOT NULL,
    cli_limitecred numeric(18,2)
);

CREATE TABLE fornecedor (
    for_codigo serial NOT NULL PRIMARY KEY,
    pes_codigo integer NOT NULL,
    for_contato varchar(80)
);

CREATE TABLE produto (
    pro_codigo serial NOT NULL PRIMARY KEY,
    pro_nome varchar(80) NOT NULL,
    pro_estoque numeric(14,4),
    pro_unidade varchar(5),
    pro_preco numeric(18,2),
    pro_custo numeric(18,2),
    pro_atacado numeric(18,2),
    pro_min numeric(14,4),
    pro_max numeric(14,4),
    pro_embalagem decimal(9,0),
    pro_peso numeric(14,4),
    pro_cadastro date,
    pro_obs text,
    pro_ativo char(1)
);

CREATE TABLE usuario (
    usu_codigo serial NOT NULL PRIMARY KEY,
    usu_nome varchar(80),
    usu_login varchar(20) NOT NULL,
    usu_senha varchar(20),
    usu_cadastro date,
    usu_ativo char(1)
);

CREATE TABLE formapagto (
    fpg_codigo serial NOT NULL PRIMARY KEY,
    fpg_nome varchar(80),
    fpg_ativo char(1)
);

CREATE TABLE venda (
    vda_codigo serial NOT NULL PRIMARY KEY,
    usu_codigo integer NOT NULL,
    cli_codigo integer NOT NULL,
    vda_data date NOT NULL,
    vda_valor numeric(18,2),
    vda_desconto numeric(18,2),
    vda_total numeric(18,2),
    vda_obs text
);

CREATE TABLE compra (
    cpr_codigo serial NOT NULL PRIMARY KEY,
    usu_codigo integer NOT NULL,
    for_codigo integer NOT NULL,
    cpr_emissao date,
    cpr_valor numeric(18,2),
    cpr_desconto numeric(18,2),
    cpr_total numeric(18,2),
    cpr_dtentrada date,
    cpr_obs text
);

CREATE TABLE venda_produto (
    vep_codigo serial NOT NULL PRIMARY KEY,
    vda_codigo integer NOT NULL,
    pro_codigo integer NOT NULL,
    vep_qtde numeric(14,4),
    vep_preco numeric(18,2),
    vep_desconto numeric(18,2),
    vep_total numeric(18,2)
);

CREATE TABLE compra_produto (
    cpp_codigo serial NOT NULL PRIMARY KEY,
    cpr_codigo integer NOT NULL,
    pro_codigo integer NOT NULL,
    cpr_qtde numeric(14,4),
    cpr_preco numeric(18,2),
    cpr_desconto numeric(18,2),
    cpr_total numeric(18,2)
);

CREATE TABLE venda_pagto (
    vdp_codigo serial NOT NULL PRIMARY KEY,
    vda_codigo integer NOT NULL,
    fpg_codigo integer NOT NULL,
    vdp_valor numeric(18,2)
);

ALTER TABLE cliente ADD CONSTRAINT fk_cliente_pessoa FOREIGN KEY (pes_codigo) REFERENCES pessoa (pes_codigo);
ALTER TABLE fornecedor ADD CONSTRAINT fk_fornecedor_pessoa FOREIGN KEY (pes_codigo) REFERENCES pessoa (pes_codigo);
ALTER TABLE venda ADD CONSTRAINT fk_venda_usuario FOREIGN KEY (usu_codigo) REFERENCES usuario (usu_codigo);
ALTER TABLE venda ADD CONSTRAINT fk_venda_cliente FOREIGN KEY (cli_codigo) REFERENCES cliente (cli_codigo);
ALTER TABLE compra ADD CONSTRAINT fk_compra_usuario FOREIGN KEY (usu_codigo) REFERENCES usuario (usu_codigo);
ALTER TABLE compra ADD CONSTRAINT fk_compra_fornecedor FOREIGN KEY (for_codigo) REFERENCES fornecedor (for_codigo);
ALTER TABLE venda_produto ADD CONSTRAINT fk_venda_produto_venda FOREIGN KEY (vda_codigo) REFERENCES venda (vda_codigo);
ALTER TABLE venda_produto ADD CONSTRAINT fk_venda_produto_produto FOREIGN KEY (pro_codigo) REFERENCES produto (pro_codigo);
ALTER TABLE compra_produto ADD CONSTRAINT fk_compra_produto_compra FOREIGN KEY (cpr_codigo) REFERENCES compra (cpr_codigo);
ALTER TABLE compra_produto ADD CONSTRAINT fk_compra_produto_produto FOREIGN KEY (pro_codigo) REFERENCES produto (pro_codigo);
ALTER TABLE venda_pagto ADD CONSTRAINT fk_venda_pagto_venda FOREIGN KEY (vda_codigo) REFERENCES venda (vda_codigo);
ALTER TABLE venda_pagto ADD CONSTRAINT fk_venda_pagto_formapagto FOREIGN KEY (fpg_codigo) REFERENCES formapagto (fpg_codigo);

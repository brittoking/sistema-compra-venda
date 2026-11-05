# Sistema de compra e venda — Swing, MVC, Hibernate e JDBC

Projeto adaptado ao SQL fornecido, com um modelo e um DAO para cada uma das 11 tabelas. O Hibernate executa o CRUD e as consultas comuns usando diretamente as classes existentes de `br.com.unip.erp.model`, anotadas como entidades JPA; `hibernate.cfg.xml` concentra sua configuração. Não há um pacote paralelo de entidades. As operações JDBC específicas de bloqueio de estoque e exclusão permanecem integradas à mesma transação. As telas usam o PostgreSQL configurado em `db.properties`. Não há dados de demonstração nem fallback de persistência em memória.

## Como executar

1. Abra a pasta que contém `pom.xml` no IntelliJ e selecione um **JDK 17 ou superior**. Recarregue as dependências Maven.
2. Se suas tabelas **já existem**, mantenha os dados e execute somente `sql/02_senhas_seguras.sql` no mesmo schema do sistema. Ele amplia `usu_senha` de 20 para 255 caracteres e cria um índice único para login. Se houver logins duplicados, corrija-os antes de criar o índice. O script não apaga registros.
3. Apenas para um **schema vazio**, execute primeiro `sql/01_estrutura.sql`, depois `sql/02_senhas_seguras.sql`. O primeiro arquivo reproduz as tabelas, tipos, chaves primárias e estrangeiras do SQL enviado, sem comandos `DROP`.
4. Copie `db.properties.example` para `db.properties` e preencha a senha, o nome do banco e o schema. Use como diretório de trabalho a pasta do `pom.xml`. Sem esse arquivo, a `connectionFactory` usa o acesso que já estava no projeto original.
5. Execute `br.com.unip.erp.App` no IntelliJ. Alternativamente, na raiz do projeto:

```bash
mvn compile exec:java
```

Se o banco estiver sem usuários, o sistema solicita a criação do primeiro acesso. Se já houver usuários, utilize um login ativo existente. Não há uma senha padrão de demonstração.

A URL de exemplo é:

```properties
url=jdbc:postgresql://localhost:5432/sistemaCV?currentSchema=public&connectTimeout=8&socketTimeout=30
user=postgres
password=SUA_SENHA
```

Se suas tabelas estiverem em outro schema, substitua `public` pelo nome correto. Execute os scripts SQL nesse mesmo schema. Ter outros schemas no banco não impede a conexão.

## DAOs e modelos

Todos os arquivos ficam em `src/main/java/br/com/unip/erp/`.

| Tabela | Modelo | DAO | Chave primária |
| --- | --- | --- | --- |
| pessoa | Pessoa | PessoaDaoImpl | pes_codigo |
| cliente | Cliente | ClienteDaoImpl | cli_codigo |
| fornecedor | Fornecedor | FornecedorDaoImpl | for_codigo |
| produto | Produto | ProdutoDaoImpl | pro_codigo |
| usuario | Usuario | UsuarioDaoImpl | usu_codigo |
| formapagto | FormaPagto | FormaPagtoDaoImpl | fpg_codigo |
| compra | Compra | CompraDaoImpl | cpr_codigo |
| compra_produto | CompraProduto | CompraProdutoDaoImpl | cpp_codigo |
| venda | Venda | VendaDaoImpl | vda_codigo |
| venda_produto | VendaProduto | VendaProdutoDaoImpl | vep_codigo |
| venda_pagto | VendaPagto | VendaPagtoDaoImpl | vdp_codigo |

`DaoInt<T>` define `inserir`, `atualizar`, `excluir`, `buscarPorId` e `listar`. Cada DAO concreto herda o acesso compartilhado de `DaoJdbc<T>` e informa a classe do próprio modelo JPA, tabela, chave e colunas. Inserções, atualizações, buscas por código e listagens usam `Session`/HQL do Hibernate. As exclusões e as consultas `SELECT ... FOR UPDATE` continuam usando JDBC onde é importante preservar o comportamento atual e a proteção de estoque. `Banco` apenas reúne as instâncias dos DAOs; não armazena registros.

Os modelos persistentes existentes (`Pessoa`, `Produto`, `Cliente`, `Fornecedor`, `Usuario`, `FormaPagto`, `Compra`, `CompraProduto`, `Venda`, `VendaProduto` e `VendaPagto`) agora são as próprias entidades JPA e recebem as anotações `@Entity`, `@Table`, `@Id`, `@GeneratedValue` e `@Column`. O `Registro` genérico continua disponível para os formulários e operações de movimento; quando necessário, o DAO converte esse mapa para o modelo correto antes de persistir.

Exemplo de uso do DAO:

```java
ProdutoDaoImpl dao = new ProdutoDaoImpl();
Produto produto = dao.buscarPorId(1);
if (produto != null) {
    produto.setNome("Produto atualizado");
    dao.atualizar(produto);
}
List<Produto> produtos = dao.listar();
```

Para salvar com as validações de cadastro, use `ProdutoController.salvar(produto)`. Chamadas diretas ao DAO executam o CRUD; as regras de negócio e o hash das senhas ficam nos controllers. Compras e vendas devem ser finalizadas pelo `MovimentoController`, para garantir a transação completa e a movimentação de estoque.

## O que foi corrigido

- Removidas todas as referências à classe ausente `Entidade` e ao modo de demonstração.
- Reconstituídos o início da aplicação, o login e os links de cadastro que estavam incompletos.
- Produto agora usa `pro_codigo`, `pro_nome` e as demais colunas reais, em vez de `id`, `nome` etc.
- A busca por código faz `SELECT`. Atualizações e exclusões sempre filtram pela chave primária.
- Cada inserção recupera o código gerado pelo banco.
- Quantidades e valores usam `BigDecimal`; `pro_unidade` é `String`; datas são `LocalDate`.
- Erros SQL são propagados para a tela, sem retornar uma falsa lista vazia ou sucesso.
- Cliente e fornecedor salvam os dados de pessoa e o vínculo dentro de uma única transação.
- Compras e vendas gravam cabeçalho, itens, estoque e pagamentos com commit/rollback na mesma conexão.
- Os produtos são bloqueados com `SELECT ... FOR UPDATE` durante a finalização e em ordem de código. Itens repetidos também são considerados no estoque disponível.
- As chaves estrangeiras impedem excluir cadastros vinculados. Excluir apenas um cliente/fornecedor mantém a pessoa, pois ela pode ter outro papel.
- Senhas novas e alteradas usam PBKDF2. As antigas, em texto, continuam sendo reconhecidas; para convertê-las, altere a senha no cadastro. Deixar a senha vazia ao editar mantém a atual.
- Os históricos consultam compras, vendas, itens e pagamentos persistidos.

O SQL recebido contém tabelas e constraints, sem funções armazenadas. O CRUD comum é feito com Hibernate/HQL; comandos JDBC preparados permanecem apenas nas operações específicas em que são necessários, como exclusões com erro SQL explícito e bloqueio de estoque.

## Verificação

Os testes de integração em `src/test/java/br/com/unip/erp/IntegracaoDaoTest.java` utilizam H2 em modo PostgreSQL e validam o CRUD, os movimentos, o rollback e a concorrência de estoque. Como o CRUD passou a usar Hibernate, execute-os novamente para validar esta versão atualizada:

```bash
mvn test
```

Este ambiente de edição não possui o comando Maven instalado, por isso não foi possível reexecutar a suíte após a integração. Use JDK 17 ou superior e Maven; o `pom.xml` baixa o Hibernate ORM e as demais dependências. A URL e as credenciais usadas pelo Hibernate são lidas de `db.properties` ou das propriedades de sistema `db.url`, `db.user` e `db.password`.

## Relatórios e dashboard

Após entrar no sistema, use os botões **Gerar relatório** e **Dashboard de vendas e compras** na tela inicial ou na navegação lateral. As mesmas opções também ficam no menu superior **Análises**.

- **Gerar relatório:** permite selecionar usuários, pessoas, clientes, fornecedores, produtos, formas de pagamento, vendas, compras, itens das vendas, itens das compras ou pagamentos das vendas. O resultado é consultado no PostgreSQL e exibido em tabela com quebra de linha para nomes e descrições longas. O CSV pode ser aberto no Excel. O botão **Imprimir no navegador** abre uma página HTML no Google Chrome (quando instalado) com nomes completos, quebra de linha, layout de impressão e opção de imprimir ou salvar em PDF.
- **Dashboard dentro do sistema:** o botão **Dashboard** abre o painel nativo Swing dentro da janela principal, sem abrir o Google Chrome. Ele apresenta faturamento de vendas, total de compras, saldo operacional, produtos com estoque baixo, gráfico comparativo dos últimos 12 meses, composição das formas de recebimento e as 10 movimentações mais recentes. Use **Atualizar** dentro do painel para consultar novamente os dados atuais do PostgreSQL.

Os relatórios e o dashboard não usam dados fictícios: se uma tabela não possuir registros, o resultado correspondente fica vazio e os indicadores permanecem em zero.

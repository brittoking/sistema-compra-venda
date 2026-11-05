package br.com.unip.erp;

import br.com.unip.erp.dao.*;
import br.com.unip.erp.controller.*;
import br.com.unip.erp.model.*;
import br.com.unip.erp.util.connectionFactory;
import br.com.unip.erp.view.*;
import java.math.BigDecimal;
import java.nio.file.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;
import javax.swing.*;
import org.junit.*;
import static org.junit.Assert.*;

/** Integração em H2 isolado, em modo PostgreSQL. Não acessa o banco real. */
public class IntegracaoDaoTest {
    private Banco b;
    private CadastroController cadastro;
    private int user,cliente,fornecedor,produto,forma;
    private String url;
    private static BigDecimal n(String s) { return new BigDecimal(s); }
    @Before public void preparar() throws Exception {
        url="jdbc:h2:mem:erp_"+UUID.randomUUID()+";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000";
        System.setProperty("db.url",url);System.setProperty("db.user","sa");System.setProperty("db.password","");
        for(String arquivo:List.of("sql/01_estrutura.sql","sql/02_senhas_seguras.sql")) {
            String sql=Files.readString(Path.of(arquivo)).replaceAll("(?m)^--.*$","");
            try(Connection c=connectionFactory.getConnection();Statement st=c.createStatement()) {
                for(String statement:sql.split(";")) if(!statement.isBlank()) st.execute(statement);
            }
        }
        b=new Banco();cadastro=new CadastroController(b);
        Registro u=new Registro().set("usu_nome","Teste").set("usu_login","teste").set("usu_senha","senha123").set("usu_cadastro",LocalDate.now()).set("usu_ativo","S");
        cadastro.salvar(b.usuario,u);user=u.id("usu_codigo");
        Registro cli=pessoa("Cliente").set("cli_limitecred",n("1000"));cadastro.salvar(b.cliente,cli);cliente=cli.id("cli_codigo");
        Registro forn=pessoa("Fornecedor").set("for_contato","Contato");cadastro.salvar(b.fornecedor,forn);fornecedor=forn.id("for_codigo");
        Produto p=new Produto();p.setNome("Produto A");p.setUnidade("KG");p.setEstoque(n("10.5000"));p.setPreco(n("5.00"));p.setCusto(n("2.00"));p.setCadastro(LocalDate.now());p.setAtivo("S");
        new ProdutoController(b).salvar(p);produto=p.getId();
        FormaPagto pg=new FormaPagto();pg.setNome("Pix");pg.setAtivo("S");b.formaPagto.inserir(pg);forma=pg.getId();
    }
    @After public void encerrar() throws Exception {
        try(Connection c=DriverManager.getConnection(url,"sa","");Statement st=c.createStatement()) { st.execute("SHUTDOWN"); }
        finally { System.clearProperty("db.url");System.clearProperty("db.user");System.clearProperty("db.password"); }
    }
    private Registro pessoa(String nome) {
        return new Registro().set("pes_nome",nome).set("pes_fisica","N").set("pes_cpfcnpj",nome+"-documento")
            .set("pes_cadastro",LocalDate.now()).set("pes_ativo","S");
    }
    private ItemMovimento item(String qtd,String preco) { return new ItemMovimento(produto,"Produto A",n(qtd),n(preco),n("0")); }
    private int movimento(boolean venda,List<ItemMovimento> itens,List<Pagamento> pagamentos) throws Exception {
        return new MovimentoController(b).salvar(venda,user,venda?cliente:fornecedor,LocalDate.now(),LocalDate.now(),"Teste",n("0"),itens,pagamentos);
    }
    private void estoque(String esperado) throws Exception { assertEquals(0,n(esperado).compareTo(b.produto.buscarPorId(produto).getEstoque())); }

    @Test public void produtoBuscaSemExcluirEAtualizaSomenteUm() throws Exception {
        Produto p2=new Produto(b.produto.buscarPorId(produto));p2.setId(0);p2.setNome("Produto B");b.produto.inserir(p2);
        Produto p1=b.produto.buscarPorId(produto);assertNotNull(p1);assertEquals(2,b.produto.listar().size());
        p1.setNome("Produto alterado");b.produto.atualizar(p1);
        assertEquals("Produto alterado",b.produto.buscarPorId(produto).getNome());
        assertEquals("Produto B",b.produto.buscarPorId(p2.getId()).getNome());
        estoque("10.5000");assertEquals("KG",p1.getUnidade());
        assertTrue(b.produto.excluir(p2.getId()));assertNull(b.produto.buscarPorId(p2.getId()));
    }
    @Test public void compraVendaItensEPagamentosPersistem() throws Exception {
        int compra=movimento(false,List.of(item("1.2500","2.00")),List.of());estoque("11.7500");
        assertEquals(1,b.compraProduto.listarPorMovimento(compra).size());
        assertEquals(0,n("2.50").compareTo(b.compra.buscarPorId(compra).getTotal()));
        int venda=movimento(true,List.of(item("0.5000","5.00")),List.of(new Pagamento(forma,"Pix",n("2.50"))));
        estoque("11.2500");assertEquals(1,b.vendaProduto.listarPorMovimento(venda).size());assertEquals(1,b.vendaPagto.listarPorMovimento(venda).size());
        assertNotNull(b.venda.buscarPorId(venda));
    }
    @Test public void pagamentoInvalidoDesfazCabecalhoItensEEstoque() throws Exception {
        assertThrows(IllegalArgumentException.class,() -> movimento(true,List.of(item("1","5")),List.of(new Pagamento(999,"Inválido",n("5")))));
        estoque("10.5000");assertTrue(b.venda.listar().isEmpty());assertTrue(b.vendaProduto.listar().isEmpty());assertTrue(b.vendaPagto.listar().isEmpty());
        assertFalse(connectionFactory.emTransacao());
        movimento(true,List.of(item("1","5")),List.of(new Pagamento(forma,"Pix",n("5"))));estoque("9.5000");
    }
    @Test public void estoqueInsuficienteComProdutoRepetidoFazRollback() throws Exception {
        assertThrows(IllegalArgumentException.class,() -> movimento(true,List.of(item("6","5"),item("6","5")),List.of(new Pagamento(forma,"Pix",n("60")))));
        estoque("10.5000");assertTrue(b.venda.listar().isEmpty());assertTrue(b.vendaProduto.listar().isEmpty());
    }
    @Test public void duasVendasConcorrentesNaoDeixamEstoqueNegativo() throws Exception {
        ExecutorService pool=Executors.newFixedThreadPool(2);CountDownLatch iniciar=new CountDownLatch(1);
        Callable<Boolean> tarefa=() -> { iniciar.await();try { movimento(true,List.of(item("7","5")),List.of(new Pagamento(forma,"Pix",n("35"))));return true; } catch(IllegalArgumentException esperado) { return false; } };
        try {
            Future<Boolean> a=pool.submit(tarefa),c=pool.submit(tarefa);iniciar.countDown();
            int sucessos=(a.get(15,TimeUnit.SECONDS)?1:0)+(c.get(15,TimeUnit.SECONDS)?1:0);
            assertEquals(1,sucessos);estoque("3.5000");assertEquals(1,b.venda.listar().size());
        } finally { pool.shutdownNow(); }
    }
    @Test public void loginHashLegadoSenhaVaziaEDuplicidade() throws Exception {
        assertNotNull(cadastro.autenticar("teste","senha123".toCharArray()));assertNull(cadastro.autenticar("teste","errada".toCharArray()));
        Usuario u=b.usuario.buscarPorId(user);assertTrue(u.getSenha().startsWith("pbkdf2$"));
        u.setSenha("");cadastro.salvar(b.usuario,u);assertNotNull(cadastro.autenticar("teste","senha123".toCharArray()));
        u=b.usuario.buscarPorId(user);u.setSenha("legada");b.usuario.atualizar(u);assertNotNull(cadastro.autenticar("teste","legada".toCharArray()));
        Usuario clone=new Usuario(u);clone.setId(0);clone.setSenha("outra123");assertThrows(IllegalArgumentException.class,() -> cadastro.salvar(b.usuario,clone));
        u.setAtivo("N");b.usuario.atualizar(u);assertNull(cadastro.autenticar("teste","legada".toCharArray()));
    }
    @Test public void clienteFornecedorEditamPessoaSemDuplicar() throws Exception {
        Registro cli=cadastro.listar(b.cliente).get(0);int pessoaId=cli.id("pes_codigo");
        cli.set("pes_nome","Cliente editado");cadastro.salvar(b.cliente,cli);
        assertEquals(pessoaId,b.cliente.buscarPorId(cliente).getPessoaId());assertEquals("Cliente editado",b.pessoa.buscarPorId(pessoaId).getNome());assertEquals(2,b.pessoa.listar().size());
        Registro fornecedorRow=cadastro.listar(b.fornecedor).get(0);fornecedorRow.set("for_contato","Contato novo");cadastro.salvar(b.fornecedor,fornecedorRow);
        assertEquals("Contato novo",b.fornecedor.buscarPorId(fornecedor).getContato());
    }
    @Test public void chavesEstrangeirasBloqueiamExclusao() throws Exception {
        movimento(false,List.of(item("1","2")),List.of());
        assertThrows(SQLException.class,() -> b.produto.excluir(produto));
        assertThrows(SQLException.class,() -> b.fornecedor.excluir(fornecedor));
        assertNotNull(b.produto.buscarPorId(produto));
        assertThrows(IllegalArgumentException.class,() -> cadastro.excluir(b.usuario,user,user));
    }
    @Test public void rollbackPessoaSeCadastroRelacionadoFalhar() throws Exception {
        int total=b.pessoa.listar().size();Registro invalido=pessoa("Nova").set("cli_limitecred",n("100"));
        // Simula erro de atualização do vínculo depois de inserir a pessoa.
        invalido.set("cli_codigo",99999);
        assertThrows(IllegalArgumentException.class,() -> cadastro.salvar(b.cliente,invalido));
        assertEquals(total,b.pessoa.listar().size());assertEquals(0,invalido.id("pes_codigo"));
    }
    @Test public void todosOsDaosPossuemCrud() throws Exception {
        movimento(false,List.of(item("1","2")),List.of());
        movimento(true,List.of(item("1","5")),List.of(new Pagamento(forma,"Pix",n("5"))));
        // CRUD herdado pelos 11 DAOs, incluindo filhos com chaves cpp/vep/vdp distintas.
        List<DaoJdbc<?>> daos=List.of(b.pessoa,b.cliente,b.fornecedor,b.produto,b.usuario,b.formaPagto,b.compra,b.venda,b.compraProduto,b.vendaProduto,b.vendaPagto);
        for(DaoJdbc<?> dao:daos) {
            Registro original=dao.listar().get(0);int id=original.id(dao.chave);
            dao.salvarRegistro(original);assertNotNull(dao.buscarPorId(id));
            Registro copia=new Registro(original);copia.remove(dao.chave);
            if(dao==b.usuario)copia.set("usu_login","copia");
            int novo=dao.salvarRegistro(copia);assertTrue(novo>0);assertNotNull(dao.buscarPorId(novo));
            assertTrue(dao.excluir(novo));assertNull(dao.buscarPorId(novo));assertNotNull(dao.buscarPorId(id));
        }
    }
    @Test public void telasCarregamDadosJdbcEMantemPessoaJuridica() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                Usuario u=b.usuario.buscarPorId(user);
                for(DaoJdbc<?> dao:List.of(b.cliente,b.fornecedor,b.produto,b.formaPagto,b.usuario)) new CadastroPanel(cadastro,dao,user);
                Registro cli=cadastro.listar(b.cliente).get(0);
                FormularioPanel form=new FormularioPanel(b.cliente,cli);
                assertEquals("N",form.ler().text("pes_fisica"));
                new MovimentoPanel(true,cadastro,u);new MovimentoPanel(false,cadastro,u);
                new br.com.unip.erp.view.Menu(cadastro,u,() -> {});new LoginPanel(() -> {});
            } catch(Exception ex) { throw new RuntimeException(ex); }
        });
    }
}

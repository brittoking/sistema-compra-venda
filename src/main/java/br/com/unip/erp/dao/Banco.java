package br.com.unip.erp.dao;

/** DAOs usados pelos controllers. Nenhum cadastro é armazenado nesta classe. */
public final class Banco {
    public final PessoaDaoImpl pessoa = new PessoaDaoImpl();
    public final ClienteDaoImpl cliente = new ClienteDaoImpl();
    public final FornecedorDaoImpl fornecedor = new FornecedorDaoImpl();
    public final ProdutoDaoImpl produto = new ProdutoDaoImpl();
    public final UsuarioDaoImpl usuario = new UsuarioDaoImpl();
    public final FormaPagtoDaoImpl formaPagto = new FormaPagtoDaoImpl();
    public final VendaDaoImpl venda = new VendaDaoImpl();
    public final CompraDaoImpl compra = new CompraDaoImpl();
    public final VendaProdutoDaoImpl vendaProduto = new VendaProdutoDaoImpl();
    public final CompraProdutoDaoImpl compraProduto = new CompraProdutoDaoImpl();
    public final VendaPagtoDaoImpl vendaPagto = new VendaPagtoDaoImpl();
}

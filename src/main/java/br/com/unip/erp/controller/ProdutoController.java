package br.com.unip.erp.controller;
import br.com.unip.erp.dao.*;
import br.com.unip.erp.model.Produto;
import java.util.List;

public class ProdutoController {
    private final Banco banco;
    public ProdutoController() { this(new Banco()); }
    public ProdutoController(Banco banco) { this.banco=banco; }
    public boolean salvar(Produto produto) throws Exception {
        new CadastroController(banco).salvar(banco.produto,produto);
        return true;
    }
    public List<Produto> listar() throws Exception { return banco.produto.listar(); }
    public Produto buscarPorId(int id) throws Exception { return banco.produto.buscarPorId(id); }
    public boolean excluir(int id) throws Exception { return banco.produto.excluir(id); }
}

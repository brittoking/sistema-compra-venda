package br.com.unip.erp.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/** Dados da tabela compra_produto; a persistência fica no DAO. */
@Entity
@Table(name = "compra_produto")
@Access(AccessType.PROPERTY)
public class CompraProduto extends Registro {
    public CompraProduto() {}
    public CompraProduto(Map<String,Object> valores) { super(valores); }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cpp_codigo")
    public int getId() { return id("cpp_codigo"); }
    public void setId(int id) { put("cpp_codigo", id); }
    @Column(name = "cpr_codigo")
    public int getCompraId() { return id("cpr_codigo"); }
    public void setCompraId(int valor) { put("cpr_codigo", valor); }
    @Column(name = "pro_codigo")
    public int getProdutoId() { return id("pro_codigo"); }
    public void setProdutoId(int valor) { put("pro_codigo", valor); }
    @Column(name = "cpr_qtde")
    public BigDecimal getQtde() { return number("cpr_qtde"); }
    public void setQtde(BigDecimal valor) { put("cpr_qtde", valor); }
    @Column(name = "cpr_preco")
    public BigDecimal getPreco() { return number("cpr_preco"); }
    public void setPreco(BigDecimal valor) { put("cpr_preco", valor); }
    @Column(name = "cpr_desconto")
    public BigDecimal getDesconto() { return number("cpr_desconto"); }
    public void setDesconto(BigDecimal valor) { put("cpr_desconto", valor); }
    @Column(name = "cpr_total")
    public BigDecimal getTotal() { return number("cpr_total"); }
    public void setTotal(BigDecimal valor) { put("cpr_total", valor); }
}

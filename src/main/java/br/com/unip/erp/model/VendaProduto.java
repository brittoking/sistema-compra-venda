package br.com.unip.erp.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/** Dados da tabela venda_produto; a persistência fica no DAO. */
@Entity
@Table(name = "venda_produto")
@Access(AccessType.PROPERTY)
public class VendaProduto extends Registro {
    public VendaProduto() {}
    public VendaProduto(Map<String,Object> valores) { super(valores); }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vep_codigo")
    public int getId() { return id("vep_codigo"); }
    public void setId(int id) { put("vep_codigo", id); }
    @Column(name = "vda_codigo")
    public int getVendaId() { return id("vda_codigo"); }
    public void setVendaId(int valor) { put("vda_codigo", valor); }
    @Column(name = "pro_codigo")
    public int getProdutoId() { return id("pro_codigo"); }
    public void setProdutoId(int valor) { put("pro_codigo", valor); }
    @Column(name = "vep_qtde")
    public BigDecimal getQtde() { return number("vep_qtde"); }
    public void setQtde(BigDecimal valor) { put("vep_qtde", valor); }
    @Column(name = "vep_preco")
    public BigDecimal getPreco() { return number("vep_preco"); }
    public void setPreco(BigDecimal valor) { put("vep_preco", valor); }
    @Column(name = "vep_desconto")
    public BigDecimal getDesconto() { return number("vep_desconto"); }
    public void setDesconto(BigDecimal valor) { put("vep_desconto", valor); }
    @Column(name = "vep_total")
    public BigDecimal getTotal() { return number("vep_total"); }
    public void setTotal(BigDecimal valor) { put("vep_total", valor); }
}

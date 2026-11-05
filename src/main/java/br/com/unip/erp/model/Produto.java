package br.com.unip.erp.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/** Dados da tabela produto; a persistência fica no DAO. */
@Entity
@Table(name = "produto")
@Access(AccessType.PROPERTY)
public class Produto extends Registro {
    public Produto() {}
    public Produto(Map<String,Object> valores) { super(valores); }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pro_codigo")
    public int getId() { return id("pro_codigo"); }
    public void setId(int id) { put("pro_codigo", id); }
    @Column(name = "pro_nome")
    public String getNome() { return text("pro_nome"); }
    public void setNome(String valor) { put("pro_nome", valor); }
    @Column(name = "pro_estoque")
    public BigDecimal getEstoque() { return number("pro_estoque"); }
    public void setEstoque(BigDecimal valor) { put("pro_estoque", valor); }
    @Column(name = "pro_unidade")
    public String getUnidade() { return text("pro_unidade"); }
    public void setUnidade(String valor) { put("pro_unidade", valor); }
    @Column(name = "pro_preco")
    public BigDecimal getPreco() { return number("pro_preco"); }
    public void setPreco(BigDecimal valor) { put("pro_preco", valor); }
    @Column(name = "pro_custo")
    public BigDecimal getCusto() { return number("pro_custo"); }
    public void setCusto(BigDecimal valor) { put("pro_custo", valor); }
    @Column(name = "pro_atacado")
    public BigDecimal getAtacado() { return number("pro_atacado"); }
    public void setAtacado(BigDecimal valor) { put("pro_atacado", valor); }
    @Column(name = "pro_min")
    public BigDecimal getMin() { return number("pro_min"); }
    public void setMin(BigDecimal valor) { put("pro_min", valor); }
    @Column(name = "pro_max")
    public BigDecimal getMax() { return number("pro_max"); }
    public void setMax(BigDecimal valor) { put("pro_max", valor); }
    @Column(name = "pro_embalagem")
    public BigDecimal getEmbalagem() { return number("pro_embalagem"); }
    public void setEmbalagem(BigDecimal valor) { put("pro_embalagem", valor); }
    @Column(name = "pro_peso")
    public BigDecimal getPeso() { return number("pro_peso"); }
    public void setPeso(BigDecimal valor) { put("pro_peso", valor); }
    @Column(name = "pro_cadastro")
    public LocalDate getCadastro() { return (LocalDate) get("pro_cadastro"); }
    public void setCadastro(LocalDate valor) { put("pro_cadastro", valor); }
    @Column(name = "pro_obs")
    public String getObs() { return text("pro_obs"); }
    public void setObs(String valor) { put("pro_obs", valor); }
    @Column(name = "pro_ativo")
    public String getAtivo() { return text("pro_ativo"); }
    public void setAtivo(String valor) { put("pro_ativo", valor); }
}

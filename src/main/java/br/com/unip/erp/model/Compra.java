package br.com.unip.erp.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/** Dados da tabela compra; a persistência fica no DAO. */
@Entity
@Table(name = "compra")
@Access(AccessType.PROPERTY)
public class Compra extends Registro {
    public Compra() {}
    public Compra(Map<String,Object> valores) { super(valores); }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cpr_codigo")
    public int getId() { return id("cpr_codigo"); }
    public void setId(int id) { put("cpr_codigo", id); }
    @Column(name = "usu_codigo")
    public int getUsuarioId() { return id("usu_codigo"); }
    public void setUsuarioId(int valor) { put("usu_codigo", valor); }
    @Column(name = "for_codigo")
    public int getFornecedorId() { return id("for_codigo"); }
    public void setFornecedorId(int valor) { put("for_codigo", valor); }
    @Column(name = "cpr_emissao")
    public LocalDate getEmissao() { return (LocalDate) get("cpr_emissao"); }
    public void setEmissao(LocalDate valor) { put("cpr_emissao", valor); }
    @Column(name = "cpr_valor")
    public BigDecimal getValor() { return number("cpr_valor"); }
    public void setValor(BigDecimal valor) { put("cpr_valor", valor); }
    @Column(name = "cpr_desconto")
    public BigDecimal getDesconto() { return number("cpr_desconto"); }
    public void setDesconto(BigDecimal valor) { put("cpr_desconto", valor); }
    @Column(name = "cpr_total")
    public BigDecimal getTotal() { return number("cpr_total"); }
    public void setTotal(BigDecimal valor) { put("cpr_total", valor); }
    @Column(name = "cpr_dtentrada")
    public LocalDate getDtentrada() { return (LocalDate) get("cpr_dtentrada"); }
    public void setDtentrada(LocalDate valor) { put("cpr_dtentrada", valor); }
    @Column(name = "cpr_obs")
    public String getObs() { return text("cpr_obs"); }
    public void setObs(String valor) { put("cpr_obs", valor); }
}

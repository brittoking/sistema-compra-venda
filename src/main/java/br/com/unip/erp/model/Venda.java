package br.com.unip.erp.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/** Dados da tabela venda; a persistência fica no DAO. */
@Entity
@Table(name = "venda")
@Access(AccessType.PROPERTY)
public class Venda extends Registro {
    public Venda() {}
    public Venda(Map<String,Object> valores) { super(valores); }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vda_codigo")
    public int getId() { return id("vda_codigo"); }
    public void setId(int id) { put("vda_codigo", id); }
    @Column(name = "usu_codigo")
    public int getUsuarioId() { return id("usu_codigo"); }
    public void setUsuarioId(int valor) { put("usu_codigo", valor); }
    @Column(name = "cli_codigo")
    public int getClienteId() { return id("cli_codigo"); }
    public void setClienteId(int valor) { put("cli_codigo", valor); }
    @Column(name = "vda_data")
    public LocalDate getData() { return (LocalDate) get("vda_data"); }
    public void setData(LocalDate valor) { put("vda_data", valor); }
    @Column(name = "vda_valor")
    public BigDecimal getValor() { return number("vda_valor"); }
    public void setValor(BigDecimal valor) { put("vda_valor", valor); }
    @Column(name = "vda_desconto")
    public BigDecimal getDesconto() { return number("vda_desconto"); }
    public void setDesconto(BigDecimal valor) { put("vda_desconto", valor); }
    @Column(name = "vda_total")
    public BigDecimal getTotal() { return number("vda_total"); }
    public void setTotal(BigDecimal valor) { put("vda_total", valor); }
    @Column(name = "vda_obs")
    public String getObs() { return text("vda_obs"); }
    public void setObs(String valor) { put("vda_obs", valor); }
}

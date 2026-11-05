package br.com.unip.erp.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/** Dados da tabela venda_pagto; a persistência fica no DAO. */
@Entity
@Table(name = "venda_pagto")
@Access(AccessType.PROPERTY)
public class VendaPagto extends Registro {
    public VendaPagto() {}
    public VendaPagto(Map<String,Object> valores) { super(valores); }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vdp_codigo")
    public int getId() { return id("vdp_codigo"); }
    public void setId(int id) { put("vdp_codigo", id); }
    @Column(name = "vda_codigo")
    public int getVendaId() { return id("vda_codigo"); }
    public void setVendaId(int valor) { put("vda_codigo", valor); }
    @Column(name = "fpg_codigo")
    public int getFormaPagtoId() { return id("fpg_codigo"); }
    public void setFormaPagtoId(int valor) { put("fpg_codigo", valor); }
    @Column(name = "vdp_valor")
    public BigDecimal getValor() { return number("vdp_valor"); }
    public void setValor(BigDecimal valor) { put("vdp_valor", valor); }
}

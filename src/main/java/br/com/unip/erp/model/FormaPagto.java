package br.com.unip.erp.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/** Dados da tabela formapagto; a persistência fica no DAO. */
@Entity
@Table(name = "formapagto")
@Access(AccessType.PROPERTY)
public class FormaPagto extends Registro {
    public FormaPagto() {}
    public FormaPagto(Map<String,Object> valores) { super(valores); }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fpg_codigo")
    public int getId() { return id("fpg_codigo"); }
    public void setId(int id) { put("fpg_codigo", id); }
    @Column(name = "fpg_nome")
    public String getNome() { return text("fpg_nome"); }
    public void setNome(String valor) { put("fpg_nome", valor); }
    @Column(name = "fpg_ativo")
    public String getAtivo() { return text("fpg_ativo"); }
    public void setAtivo(String valor) { put("fpg_ativo", valor); }
}

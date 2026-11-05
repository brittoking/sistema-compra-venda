package br.com.unip.erp.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/** Dados da tabela cliente; a persistência fica no DAO. */
@Entity
@Table(name = "cliente")
@Access(AccessType.PROPERTY)
public class Cliente extends Registro {
    public Cliente() {}
    public Cliente(Map<String,Object> valores) { super(valores); }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cli_codigo")
    public int getId() { return id("cli_codigo"); }
    public void setId(int id) { put("cli_codigo", id); }
    @Column(name = "pes_codigo")
    public int getPessoaId() { return id("pes_codigo"); }
    public void setPessoaId(int valor) { put("pes_codigo", valor); }
    @Column(name = "cli_limitecred")
    public BigDecimal getLimitecred() { return number("cli_limitecred"); }
    public void setLimitecred(BigDecimal valor) { put("cli_limitecred", valor); }
}

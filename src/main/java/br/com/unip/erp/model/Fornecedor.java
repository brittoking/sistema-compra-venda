package br.com.unip.erp.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/** Dados da tabela fornecedor; a persistência fica no DAO. */
@Entity
@Table(name = "fornecedor")
@Access(AccessType.PROPERTY)
public class Fornecedor extends Registro {
    public Fornecedor() {}
    public Fornecedor(Map<String,Object> valores) { super(valores); }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "for_codigo")
    public int getId() { return id("for_codigo"); }
    public void setId(int id) { put("for_codigo", id); }
    @Column(name = "pes_codigo")
    public int getPessoaId() { return id("pes_codigo"); }
    public void setPessoaId(int valor) { put("pes_codigo", valor); }
    @Column(name = "for_contato")
    public String getContato() { return text("for_contato"); }
    public void setContato(String valor) { put("for_contato", valor); }
}

package br.com.unip.erp.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/** Dados da tabela usuario; a persistência fica no DAO. */
@Entity
@Table(name = "usuario")
@Access(AccessType.PROPERTY)
public class Usuario extends Registro {
    public Usuario() {}
    public Usuario(Map<String,Object> valores) { super(valores); }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usu_codigo")
    public int getId() { return id("usu_codigo"); }
    public void setId(int id) { put("usu_codigo", id); }
    @Column(name = "usu_nome")
    public String getNome() { return text("usu_nome"); }
    public void setNome(String valor) { put("usu_nome", valor); }
    @Column(name = "usu_login")
    public String getLogin() { return text("usu_login"); }
    public void setLogin(String valor) { put("usu_login", valor); }
    @Column(name = "usu_senha")
    public String getSenha() { return text("usu_senha"); }
    public void setSenha(String valor) { put("usu_senha", valor); }
    @Column(name = "usu_cadastro")
    public LocalDate getCadastro() { return (LocalDate) get("usu_cadastro"); }
    public void setCadastro(LocalDate valor) { put("usu_cadastro", valor); }
    @Column(name = "usu_ativo")
    public String getAtivo() { return text("usu_ativo"); }
    public void setAtivo(String valor) { put("usu_ativo", valor); }
}

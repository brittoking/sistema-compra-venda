package br.com.unip.erp.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/** Dados da tabela pessoa; a persistência fica no DAO. */
@Entity
@Table(name = "pessoa")
@Access(AccessType.PROPERTY)
public class Pessoa extends Registro {
    public Pessoa() {}
    public Pessoa(Map<String,Object> valores) { super(valores); }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pes_codigo")
    public int getId() { return id("pes_codigo"); }
    public void setId(int id) { put("pes_codigo", id); }
    @Column(name = "pes_nome")
    public String getNome() { return text("pes_nome"); }
    public void setNome(String valor) { put("pes_nome", valor); }
    @Column(name = "pes_fantasia")
    public String getFantasia() { return text("pes_fantasia"); }
    public void setFantasia(String valor) { put("pes_fantasia", valor); }
    @Column(name = "pes_fisica")
    public String getFisica() { return text("pes_fisica"); }
    public void setFisica(String valor) { put("pes_fisica", valor); }
    @Column(name = "pes_cpfcnpj")
    public String getCpfcnpj() { return text("pes_cpfcnpj"); }
    public void setCpfcnpj(String valor) { put("pes_cpfcnpj", valor); }
    @Column(name = "pes_rgie")
    public String getRgie() { return text("pes_rgie"); }
    public void setRgie(String valor) { put("pes_rgie", valor); }
    @Column(name = "pes_cadastro")
    public LocalDate getCadastro() { return (LocalDate) get("pes_cadastro"); }
    public void setCadastro(LocalDate valor) { put("pes_cadastro", valor); }
    @Column(name = "pes_endereco")
    public String getEndereco() { return text("pes_endereco"); }
    public void setEndereco(String valor) { put("pes_endereco", valor); }
    @Column(name = "pes_numero")
    public String getNumero() { return text("pes_numero"); }
    public void setNumero(String valor) { put("pes_numero", valor); }
    @Column(name = "pes_complemento")
    public String getComplemento() { return text("pes_complemento"); }
    public void setComplemento(String valor) { put("pes_complemento", valor); }
    @Column(name = "pes_bairro")
    public String getBairro() { return text("pes_bairro"); }
    public void setBairro(String valor) { put("pes_bairro", valor); }
    @Column(name = "pes_cidade")
    public String getCidade() { return text("pes_cidade"); }
    public void setCidade(String valor) { put("pes_cidade", valor); }
    @Column(name = "pes_uf")
    public String getUf() { return text("pes_uf"); }
    public void setUf(String valor) { put("pes_uf", valor); }
    @Column(name = "pes_cep")
    public String getCep() { return text("pes_cep"); }
    public void setCep(String valor) { put("pes_cep", valor); }
    @Column(name = "pes_fone1")
    public String getFone1() { return text("pes_fone1"); }
    public void setFone1(String valor) { put("pes_fone1", valor); }
    @Column(name = "pes_fone2")
    public String getFone2() { return text("pes_fone2"); }
    public void setFone2(String valor) { put("pes_fone2", valor); }
    @Column(name = "pes_celular")
    public String getCelular() { return text("pes_celular"); }
    public void setCelular(String valor) { put("pes_celular", valor); }
    @Column(name = "pes_site")
    public String getSite() { return text("pes_site"); }
    public void setSite(String valor) { put("pes_site", valor); }
    @Column(name = "pes_email")
    public String getEmail() { return text("pes_email"); }
    public void setEmail(String valor) { put("pes_email", valor); }
    @Column(name = "pes_ativo")
    public String getAtivo() { return text("pes_ativo"); }
    public void setAtivo(String valor) { put("pes_ativo", valor); }
}

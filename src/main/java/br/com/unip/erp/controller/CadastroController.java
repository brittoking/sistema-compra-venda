package br.com.unip.erp.controller;

import br.com.unip.erp.model.*;
import br.com.unip.erp.dao.*;
import br.com.unip.erp.util.*;
import java.util.*;
import java.math.*;
import java.time.*;
import java.time.format.*;

public class CadastroController {
    public final Banco banco;
    public CadastroController(Banco banco) { this.banco=banco; }

    public List<Registro> listar(DaoJdbc<?> dao) throws Exception {
        List<Registro> lista=new ArrayList<>(dao.listar());
        if(dao.pessoa()) for(Registro r:lista) {
            Pessoa pessoa=banco.pessoa.buscarPorId(r.id("pes_codigo"));
            if(pessoa!=null) r.putAll(pessoa);
        }
        return lista;
    }
    public Usuario autenticar(String login, char[] senha) throws Exception {
        Usuario user=banco.usuario.buscarPorLogin(login.trim());
        return user!=null && "S".equals(user.getAtivo()) && Senhas.confere(senha,user.getSenha())?user:null;
    }
    public static BigDecimal decimal(String valor) {
        try {
            String n=valor.trim();
            if(n.contains(",")) n=n.replace(".","").replace(",",".");
            return new BigDecimal(n);
        } catch(Exception ex) { throw new IllegalArgumentException("Informe um número válido (ex.: 12,50)."); }
    }
    public static LocalDate data(String valor) {
        try { return LocalDate.parse(valor,DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT)); }
        catch(Exception ex) { throw new IllegalArgumentException("Data inválida. Use dd/mm/aaaa."); }
    }
    public void salvar(DaoJdbc<?> dao, Registro original) throws Exception {
        // Só devolve os IDs ao formulário depois do commit.
        Registro r=new Registro(original);
        for(Campo f:dao.formulario()) {
            String v=r.text(f.coluna());
            if(!f.tipo().equals("password")) v=v.trim();
            if(f.tipo().equals("password") && v.isEmpty() && r.id(dao.chave)>0) {
                Registro atual=dao.buscarPorId(r.id(dao.chave));
                if(atual==null) throw new IllegalArgumentException("Usuário não encontrado.");
                r.put(f.coluna(),atual.get(f.coluna()));continue;
            }
            if(f.obrigatorio() && v.isBlank()) throw new IllegalArgumentException("Preencha: "+f.titulo());
            if(f.tamanho()>0 && v.length()>f.tamanho()) throw new IllegalArgumentException(f.titulo()+": máximo de "+f.tamanho()+" caracteres.");
            switch(f.tipo()) {
                case "money", "decimal", "int" -> {
                    BigDecimal n=decimal(v.isBlank()?"0":v);
                    if(n.signum()<0) throw new IllegalArgumentException(f.titulo()+" não pode ser negativo.");
                    int escala=f.tipo().equals("money")?2:f.tipo().equals("decimal")?4:0;
                    try { r.put(f.coluna(),n.setScale(escala,RoundingMode.UNNECESSARY)); }
                    catch(ArithmeticException ex) { throw new IllegalArgumentException(f.titulo()+": use até "+escala+" casas decimais."); }
                }
                case "date" -> r.put(f.coluna(),v.isBlank()?null:r.get(f.coluna()) instanceof LocalDate?r.get(f.coluna()):data(v));
                case "password" -> r.put(f.coluna(),Senhas.hash(v));
                case "active", "person" -> {
                    if(!Set.of("S","N").contains(v)) throw new IllegalArgumentException("Valor inválido: "+f.titulo());
                    r.put(f.coluna(),v);
                }
                default -> r.put(f.coluna(),v);
            }
        }
        if(dao==banco.produto) {
            if(r.number("pro_preco").signum()<=0) throw new IllegalArgumentException("O preço do produto deve ser positivo.");
            if(r.number("pro_max").signum()>0 && r.number("pro_min").compareTo(r.number("pro_max"))>0)
                throw new IllegalArgumentException("Estoque máximo deve ser maior ou igual ao mínimo.");
        }
        connectionFactory.transacao(() -> {
            if(dao==banco.usuario) {
                Usuario atual=banco.usuario.buscarPorLogin(r.text("usu_login"));
                if(atual!=null && atual.getId()!=r.id(dao.chave)) throw new IllegalArgumentException("Este login já está cadastrado.");
            }
            if(dao.pessoa()) {
                Registro pessoa=new Registro(r);
                int id=banco.pessoa.salvarRegistro(pessoa);
                r.put("pes_codigo",id);
            }
            dao.salvarRegistro(r);
        });
        original.putAll(r);
    }
    public void excluir(DaoJdbc<?> dao,int id,int usuarioAtual) throws Exception {
        if(dao==banco.usuario && id==usuarioAtual) throw new IllegalArgumentException("Você não pode excluir o usuário conectado.");
        // O PostgreSQL impede a exclusão de cadastros vinculados pelas FKs.
        if(!dao.excluir(id)) throw new IllegalArgumentException("Registro já removido. Atualize a tela.");
    }
}

package br.com.unip.erp.dao;

import br.com.unip.erp.model.*;
import br.com.unip.erp.util.HibernateUtil;
import br.com.unip.erp.util.connectionFactory;
import java.sql.*;
import jakarta.persistence.Column;
import java.beans.Introspector;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;

/**
 * CRUD compartilhado. As classes de br.com.unip.erp.model são as próprias entidades JPA.
 * JDBC é mantido somente para excluir com SQLException explícita e bloquear estoque.
 */
public abstract class DaoJdbc<T extends Registro> implements DaoInt<T> {
    public final String tabela, chave, titulo;
    private final String nome;
    public final List<Campo> campos;
    private final Function<Map<String,Object>,T> criar;
    private final Class<T> entidade;

    protected DaoJdbc(String tabela, String chave, String nome, String titulo,
                      Function<Map<String,Object>,T> criar, Class<T> entidade, List<Campo> campos) {
        this.tabela=tabela;
        this.chave=chave;
        this.nome=nome;
        this.titulo=titulo;
        this.criar=criar;
        this.entidade=entidade;
        this.campos=List.copyOf(campos);
    }

    public String nome() { return nome; }
    public boolean pessoa() { return false; }
    public List<Campo> formulario() { return campos; }

    private void conferirColuna(String coluna) {
        if (!chave.equals(coluna) && campos.stream().noneMatch(c -> c.coluna().equals(coluna))) {
            throw new IllegalArgumentException("Coluna inválida para " + tabela);
        }
        propriedadeDaColuna(coluna);
    }

    /** Resolve uma coluna SQL para a propriedade Java anotada no próprio modelo JPA. */
    private String propriedadeDaColuna(String coluna) {
        for (Method metodo : entidade.getMethods()) {
            Column anotacao = metodo.getAnnotation(Column.class);
            if (anotacao == null || !anotacao.name().equalsIgnoreCase(coluna)) continue;
            String nome = metodo.getName();
            if (nome.startsWith("get") && nome.length() > 3) {
                return Introspector.decapitalize(nome.substring(3));
            }
            if (nome.startsWith("is") && nome.length() > 2) {
                return Introspector.decapitalize(nome.substring(2));
            }
        }
        throw new IllegalArgumentException("Coluna não mapeada no Hibernate: " + coluna + " (" + entidade.getSimpleName() + ")");
    }

    private T comoEntidade(Registro objeto) {
        if (entidade.isInstance(objeto)) return entidade.cast(objeto);
        return criar.apply(new LinkedHashMap<>(objeto));
    }

    private T montar(ResultSet rs) throws SQLException {
        Registro registro = new Registro();
        registro.put(chave, rs.getInt(chave));
        for (Campo campo : campos) {
            Object valor = rs.getObject(campo.coluna());
            if (valor instanceof java.sql.Date data) valor = data.toLocalDate();
            registro.put(campo.coluna(), valor);
        }
        return criar.apply(registro);
    }

    @Override public boolean inserir(T objeto) throws Exception {
        inserirRegistro(objeto);
        return true;
    }

    private int inserirRegistro(Registro objeto) throws Exception {
        T entidadeNova = comoEntidade(objeto);
        return HibernateUtil.executar(sessao -> {
            sessao.persist(entidadeNova);
            sessao.flush();
            int id = entidadeNova.id(chave);
            objeto.put(chave, id);
            return id;
        });
    }

    @Override public boolean atualizar(T objeto) throws Exception { return atualizarRegistro(objeto); }

    private boolean atualizarRegistro(Registro objeto) throws Exception {
        if (objeto.id(chave) <= 0) throw new IllegalArgumentException("Código inválido para atualização.");
        T entidadeAtualizada = comoEntidade(objeto);
        return HibernateUtil.executar(sessao -> {
            T atual = sessao.find(entidade, objeto.id(chave));
            if (atual == null) return false;
            sessao.merge(entidadeAtualizada);
            sessao.flush();
            return true;
        });
    }

    public int salvarRegistro(Registro objeto) throws Exception {
        if (objeto.id(chave) == 0) return inserirRegistro(objeto);
        if (!atualizarRegistro(objeto)) {
            throw new IllegalArgumentException("Registro removido por outro usuário. Atualize a tela.");
        }
        return objeto.id(chave);
    }

    /** Mantém a exceção SQL explícita quando uma chave estrangeira impede a exclusão. */
    @Override public boolean excluir(int id) throws Exception {
        return connectionFactory.usar(c -> {
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM " + tabela + " WHERE " + chave + "=?")) {
                ps.setInt(1, id);
                return ps.executeUpdate() == 1;
            }
        });
    }

    @Override public T buscarPorId(int id) throws Exception {
        if (id <= 0) return null;
        return HibernateUtil.executar(sessao -> sessao.find(entidade, id));
    }

    protected T buscarBloqueado(int id) throws Exception {
        if (!connectionFactory.emTransacao()) {
            throw new IllegalStateException("Bloqueio de registro exige uma transação.");
        }
        return buscar(id, true);
    }

    /** Consulta JDBC especial para SELECT ... FOR UPDATE no fluxo transacional de estoque. */
    private T buscar(int id, boolean bloquear) throws Exception {
        return connectionFactory.usar(c -> {
            String sql = "SELECT * FROM " + tabela + " WHERE " + chave + "=?" + (bloquear ? " FOR UPDATE" : "");
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? montar(rs) : null;
                }
            }
        });
    }

    @Override public List<T> listar() throws Exception { return consultar(null, null); }

    public List<T> listarPor(String coluna, Object valor) throws Exception {
        conferirColuna(coluna);
        return consultar(coluna, valor);
    }

    private List<T> consultar(String coluna, Object valor) throws Exception {
        return HibernateUtil.executar(sessao -> {
            String hql = "from " + entidade.getSimpleName() + " e";
            if (coluna != null) {
                String propriedade = propriedadeDaColuna(coluna);
                hql += " where e." + propriedade + " = :valor";
            }
            hql += " order by e.id";
            org.hibernate.query.Query<T> query = sessao.createQuery(hql, entidade);
            if (coluna != null) query.setParameter("valor", valor);
            return query.getResultList();
        });
    }

    protected void atualizarColuna(int id, String coluna, Object valor) throws Exception {
        conferirColuna(coluna);
        connectionFactory.usar(c -> {
            try (PreparedStatement ps = c.prepareStatement("UPDATE " + tabela + " SET " + coluna + "=? WHERE " + chave + "=?")) {
                ps.setObject(1, valor);
                ps.setInt(2, id);
                if (ps.executeUpdate() != 1) throw new SQLException("Registro não encontrado em " + tabela);
                return null;
            }
        });
    }
}

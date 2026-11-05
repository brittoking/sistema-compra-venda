package br.com.unip.erp.util;

import org.hibernate.Session;

import java.sql.*;
import java.nio.file.*;
import java.util.Properties;

/** Configuração de conexão; transações usam a mesma conexão disponibilizada pela Session do Hibernate. */
public final class connectionFactory {
    private connectionFactory() {}

    public static Properties getDatabaseProperties() throws SQLException {
        Properties arquivo = new Properties();
        Path caminho = Path.of("db.properties");
        if (Files.exists(caminho)) {
            try (var in = Files.newInputStream(caminho)) { arquivo.load(in); }
            catch (Exception ex) { throw new SQLException("Não foi possível ler db.properties.", ex); }
        }
        Properties resolvidas = new Properties();
        resolvidas.setProperty("url", System.getProperty("db.url", arquivo.getProperty("url", "sua url")));
        resolvidas.setProperty("user", System.getProperty("db.user", arquivo.getProperty("user", "seu usuario")));
        resolvidas.setProperty("password", System.getProperty("db.password", arquivo.getProperty("password", "sua senha")));
        return resolvidas;
    }

    public static Connection getConnection() throws SQLException {
        Properties p = getDatabaseProperties();
        DriverManager.setLoginTimeout(8);
        return DriverManager.getConnection(p.getProperty("url"), p.getProperty("user"), p.getProperty("password"));
    }

    @FunctionalInterface public interface Operacao<T> { T executar(Connection c) throws Exception; }
    @FunctionalInterface public interface Tarefa { void executar() throws Exception; }

    public static <T> T usar(Operacao<T> operacao) throws Exception {
        Session sessao = HibernateUtil.sessaoAtual();
        if (sessao != null) {
            Object[] resultado = new Object[1];
            Exception[] erro = new Exception[1];
            sessao.doWork(conexao -> {
                try { resultado[0] = operacao.executar(conexao); }
                catch (Exception ex) { erro[0] = ex; }
            });
            if (erro[0] != null) throw erro[0];
            @SuppressWarnings("unchecked") T valor = (T) resultado[0];
            return valor;
        }
        try (Connection nova = getConnection()) { return operacao.executar(nova); }
    }

    public static boolean emTransacao() { return HibernateUtil.emTransacao(); }

    public static void transacao(Tarefa tarefa) throws Exception {
        if (emTransacao()) throw new IllegalStateException("Transações aninhadas não são permitidas.");
        HibernateUtil.transacionar(tarefa);
    }
}

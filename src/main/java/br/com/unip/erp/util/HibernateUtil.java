package br.com.unip.erp.util;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;

/** Ponto central do Hibernate: SessionFactory compartilhada e Session por operação/transação. */
public final class HibernateUtil {
    @FunctionalInterface public interface Trabalho<T> { T executar(Session sessao) throws Exception; }

    private static volatile SessionFactory sessionFactory;
    private static volatile String configuracaoAtual;
    private static final ThreadLocal<Session> SESSAO_ATUAL = new ThreadLocal<>();

    private HibernateUtil() {}

    public static Session sessaoAtual() { return SESSAO_ATUAL.get(); }
    public static boolean emTransacao() {
        Session sessao = SESSAO_ATUAL.get();
        return sessao != null && sessao.getTransaction().isActive();
    }

    public static SessionFactory getSessionFactory() throws SQLException {
        Properties db = connectionFactory.getDatabaseProperties();
        String url = db.getProperty("url");
        String usuario = db.getProperty("user");
        String senha = db.getProperty("password");
        String driver = url.startsWith("jdbc:h2:") ? "org.h2.Driver" : "org.postgresql.Driver";
        String dialect = url.startsWith("jdbc:h2:")
                ? "org.hibernate.dialect.H2Dialect"
                : "org.hibernate.dialect.PostgreSQLDialect";
        String chave = url + "|" + usuario + "|" + senha + "|" + driver;
        SessionFactory atual = sessionFactory;
        if (atual != null && atual.isOpen() && Objects.equals(chave, configuracaoAtual)) return atual;
        synchronized (HibernateUtil.class) {
            atual = sessionFactory;
            if (atual != null && atual.isOpen() && Objects.equals(chave, configuracaoAtual)) return atual;
            if (atual != null && atual.isOpen()) atual.close();
            Configuration cfg = new Configuration().configure("hibernate.cfg.xml");
            cfg.setProperty("hibernate.connection.driver_class", driver);
            cfg.setProperty("hibernate.connection.url", url);
            cfg.setProperty("hibernate.connection.username", usuario);
            cfg.setProperty("hibernate.connection.password", senha);
            cfg.setProperty("hibernate.dialect", dialect);
            cfg.setProperty("hibernate.hbm2ddl.auto", "none");
            sessionFactory = cfg.buildSessionFactory();
            configuracaoAtual = chave;
            return sessionFactory;
        }
    }

    public static <T> T executar(Trabalho<T> trabalho) throws Exception {
        Session existente = SESSAO_ATUAL.get();
        if (existente != null) return trabalho.executar(existente);

        try (Session sessao = getSessionFactory().openSession()) {
            Transaction transacao = sessao.beginTransaction();
            SESSAO_ATUAL.set(sessao);
            try {
                T resultado = trabalho.executar(sessao);
                transacao.commit();
                return resultado;
            } catch (Exception | Error ex) {
                if (transacao.isActive()) {
                    try { transacao.rollback(); }
                    catch (RuntimeException rollback) { ex.addSuppressed(rollback); }
                }
                throw ex;
            } finally {
                SESSAO_ATUAL.remove();
            }
        }
    }

    public static void transacionar(connectionFactory.Tarefa tarefa) throws Exception {
        executar(sessao -> { tarefa.executar(); return null; });
    }

    public static void fechar() {
        synchronized (HibernateUtil.class) {
            if (sessionFactory != null && sessionFactory.isOpen()) sessionFactory.close();
            sessionFactory = null;
            configuracaoAtual = null;
        }
    }
}

package br.com.unip.erp.util;

import br.com.unip.erp.model.Registro;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/** Converte os modelos da aplicação para entidades JPA sem mudar as telas legadas. */
public final class HibernateMapper {
    private HibernateMapper() {}

    public static Object paraEntidade(Registro registro, Class<?> tipo, String colunaId) throws Exception {
        Object entidade = tipo.getDeclaredConstructor().newInstance();
        for (Field campo : tipo.getDeclaredFields()) {
            Column anotacao = campo.getAnnotation(Column.class);
            if (anotacao == null) continue;
            String coluna = anotacao.name();
            Object valor = registro.get(coluna);
            if (campo.isAnnotationPresent(Id.class) && (valor == null || (valor instanceof Number n && n.intValue() <= 0))) {
                continue; // a chave serial/identity será gerada pelo banco
            }
            campo.setAccessible(true);
            campo.set(entidade, converter(valor, campo.getType()));
        }
        return entidade;
    }

    public static <T extends Registro> T paraRegistro(Object entidade, Class<?> tipo, Function<Map<String, Object>, T> criar) throws Exception {
        Map<String, Object> valores = new LinkedHashMap<>();
        for (Field campo : tipo.getDeclaredFields()) {
            Column anotacao = campo.getAnnotation(Column.class);
            if (anotacao == null) continue;
            campo.setAccessible(true);
            Object valor = campo.get(entidade);
            if (valor instanceof Date data) valor = data.toLocalDate();
            valores.put(anotacao.name(), valor);
        }
        return criar.apply(valores);
    }

    public static int idDaEntidade(Object entidade, Class<?> tipo, String colunaId) throws Exception {
        for (Field campo : tipo.getDeclaredFields()) {
            Column anotacao = campo.getAnnotation(Column.class);
            if (anotacao != null && anotacao.name().equalsIgnoreCase(colunaId)) {
                campo.setAccessible(true);
                Object valor = campo.get(entidade);
                return valor == null ? 0 : ((Number) valor).intValue();
            }
        }
        throw new IllegalArgumentException("Coluna de chave não mapeada: " + colunaId);
    }

    public static String propriedadeDaColuna(Class<?> tipo, String coluna) {
        for (Field campo : tipo.getDeclaredFields()) {
            Column anotacao = campo.getAnnotation(Column.class);
            if (anotacao != null && anotacao.name().equalsIgnoreCase(coluna)) return campo.getName();
        }
        throw new IllegalArgumentException("Coluna não mapeada no Hibernate: " + coluna);
    }

    private static Object converter(Object valor, Class<?> destino) {
        if (valor == null) return null;
        if (destino.isInstance(valor)) return valor;
        if (destino == Integer.class || destino == int.class) {
            if (valor instanceof Number n) return n.intValue();
            return Integer.valueOf(valor.toString());
        }
        if (destino == Long.class || destino == long.class) {
            if (valor instanceof Number n) return n.longValue();
            return Long.valueOf(valor.toString());
        }
        if (destino == BigDecimal.class && valor instanceof Number) return new BigDecimal(valor.toString());
        if (destino == LocalDate.class && valor instanceof Date data) return data.toLocalDate();
        if (destino == LocalDate.class && valor instanceof java.util.Date data) return new Date(data.getTime()).toLocalDate();
        if (destino == String.class) return valor.toString();
        return valor;
    }
}

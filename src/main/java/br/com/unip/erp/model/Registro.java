package br.com.unip.erp.model;
import java.util.*;
import java.math.BigDecimal;
/** Registro de uma entidade do DER; nomes das propriedades correspondem às colunas. */
public class Registro extends LinkedHashMap<String,Object>  {
    public Registro()  {
    }
    public Registro(Map<String,Object> source)  {
        super(source);
    }
    public Registro set(String k,Object v)  {
        put(k,v);
        return this;
    }
    public String text(String k)  {
        return get(k)==null ? "" : get(k).toString();
    }
    public int id(String k)  {
        return get(k)==null?0:((Number)get(k)).intValue();
    }
    public BigDecimal number(String k)  {
        return text(k).isBlank()?BigDecimal.ZERO:new BigDecimal(text(k));
    }
}

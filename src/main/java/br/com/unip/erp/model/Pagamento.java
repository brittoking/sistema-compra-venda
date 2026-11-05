package br.com.unip.erp.model;
import java.math.BigDecimal;
public record Pagamento(int forma,String nome,BigDecimal valor)  {
}

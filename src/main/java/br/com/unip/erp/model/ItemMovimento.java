package br.com.unip.erp.model;
import java.math.*;
public record ItemMovimento(int produto,String nome,BigDecimal quantidade,BigDecimal preco,BigDecimal desconto) {
    public BigDecimal total() {
        return quantidade.multiply(preco).subtract(desconto).setScale(2,RoundingMode.HALF_UP);
    }
}

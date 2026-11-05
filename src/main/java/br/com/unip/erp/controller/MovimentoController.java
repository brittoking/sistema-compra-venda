package br.com.unip.erp.controller;

import br.com.unip.erp.dao.*;
import br.com.unip.erp.model.*;
import br.com.unip.erp.util.connectionFactory;
import java.math.*;
import java.time.LocalDate;
import java.util.*;

public class MovimentoController {
    private final Banco banco;
    public MovimentoController(Banco banco) { this.banco=banco; }
    public static BigDecimal subtotal(List<ItemMovimento> itens) {
        return itens.stream().map(ItemMovimento::total).reduce(BigDecimal.ZERO,BigDecimal::add);
    }
    public int salvar(boolean venda,int usuario,int pessoa,LocalDate data,LocalDate entrada,
                      String obs,BigDecimal desconto,List<ItemMovimento> itens,List<Pagamento> pagamentos) throws Exception {
        if(pessoa<=0) throw new IllegalArgumentException("Selecione "+(venda?"um cliente.":"um fornecedor."));
        if(data==null || (!venda && entrada==null)) throw new IllegalArgumentException("Informe as datas.");
        if(!venda && entrada.isBefore(data)) throw new IllegalArgumentException("A entrada não pode ser anterior à emissão.");
        List<ItemMovimento> linhas=List.copyOf(itens);
        List<Pagamento> parcelas=List.copyOf(pagamentos);
        if(linhas.isEmpty()) throw new IllegalArgumentException("Adicione pelo menos um produto.");
        desconto=desconto.setScale(2,RoundingMode.UNNECESSARY);
        for(ItemMovimento item:linhas) {
            item.quantidade().setScale(4,RoundingMode.UNNECESSARY);
            item.preco().setScale(2,RoundingMode.UNNECESSARY);
            item.desconto().setScale(2,RoundingMode.UNNECESSARY);
            if(item.quantidade().signum()<=0 || item.preco().signum()<=0 || item.desconto().signum()<0 || item.total().signum()<=0)
                throw new IllegalArgumentException("Confira quantidade, preço e desconto dos itens.");
        }
        BigDecimal sub=subtotal(linhas),total=sub.subtract(desconto),abatimento=desconto;
        if(desconto.signum()<0 || total.signum()<=0) throw new IllegalArgumentException("O total deve ser positivo e o desconto não pode superar o subtotal.");
        if(venda && parcelas.stream().map(Pagamento::valor).reduce(BigDecimal.ZERO,BigDecimal::add).compareTo(total)!=0)
            throw new IllegalArgumentException("A soma dos pagamentos deve ser igual ao total da venda.");
        int[] codigo={0};
        connectionFactory.transacao(() -> {
            Usuario user=banco.usuario.buscarPorId(usuario);
            if(user==null || !"S".equals(user.getAtivo())) throw new IllegalArgumentException("Usuário inativo ou removido.");
            Registro papel=(venda?banco.cliente:banco.fornecedor).buscarPorId(pessoa);
            Pessoa pessoaRow=papel==null?null:banco.pessoa.buscarPorId(papel.id("pes_codigo"));
            if(pessoaRow==null || !"S".equals(pessoaRow.getAtivo())) throw new IllegalArgumentException("Cadastro inativo ou removido.");
            String p=venda?"vda_":"cpr_";
            Registro head=new Registro().set("usu_codigo",usuario).set(venda?"cli_codigo":"for_codigo",pessoa)
                .set(p+(venda?"data":"emissao"),data).set(p+"valor",sub).set(p+"desconto",abatimento)
                .set(p+"total",total).set(p+"obs",obs);
            if(!venda) head.set("cpr_dtentrada",entrada);
            codigo[0]=(venda?banco.venda:banco.compra).salvarRegistro(head);
            // FOR UPDATE protege o estoque de vendas simultâneas. A ordem reduz deadlocks.
            for(ItemMovimento item:linhas.stream().sorted(Comparator.comparingInt(ItemMovimento::produto)).toList()) {
                Produto prod=banco.produto.buscarParaAtualizar(item.produto());
                if(prod==null || !"S".equals(prod.getAtivo())) throw new IllegalArgumentException("Produto inativo ou removido: "+item.nome());
                BigDecimal estoque=prod.getEstoque().add(venda?item.quantidade().negate():item.quantidade());
                if(estoque.signum()<0) throw new IllegalArgumentException("Estoque insuficiente: "+item.nome());
                banco.produto.atualizarEstoque(prod.getId(),estoque);
                String q=venda?"vep_":"cpr_";
                Registro row=new Registro().set(p+"codigo",codigo[0]).set("pro_codigo",item.produto())
                    .set(q+"qtde",item.quantidade()).set(q+"preco",item.preco()).set(q+"desconto",item.desconto()).set(q+"total",item.total());
                (venda?banco.vendaProduto:banco.compraProduto).salvarRegistro(row);
            }
            if(venda) for(Pagamento pg:parcelas) {
                pg.valor().setScale(2,RoundingMode.UNNECESSARY);
                FormaPagto forma=banco.formaPagto.buscarPorId(pg.forma());
                if(pg.valor().signum()<=0 || forma==null || !"S".equals(forma.getAtivo())) throw new IllegalArgumentException("Pagamento inválido ou forma inativa.");
                banco.vendaPagto.salvarRegistro(new Registro().set("vda_codigo",codigo[0]).set("fpg_codigo",pg.forma()).set("vdp_valor",pg.valor()));
            }
        });
        return codigo[0];
    }
}

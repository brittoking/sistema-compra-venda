package br.com.unip.erp.dao;
import br.com.unip.erp.model.Registro;
import java.util.List;

public interface DaoInt<T extends Registro> {
    boolean inserir(T objeto) throws Exception;
    boolean atualizar(T objeto) throws Exception;
    boolean excluir(int id) throws Exception;
    T buscarPorId(int id) throws Exception;
    List<T> listar() throws Exception;
}

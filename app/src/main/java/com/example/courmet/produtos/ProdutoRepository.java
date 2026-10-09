package com.example.courmet.produtos;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Camada entre as telas e o banco. As telas usam SÓ esta classe (nunca o DAO direto).
 *
 * Por que não acessar o banco na thread principal?
 * A thread principal (UI thread) é a que desenha a tela e responde aos toques.
 * Ler/gravar no SQLite acessa o disco e pode demorar; se isso acontecer nela,
 * a tela "congela" e, se travar por alguns segundos, o Android mostra o erro
 * "O app não está respondendo" (ANR). Por isso:
 *   1. o trabalho com o banco roda em uma thread de segundo plano (ExecutorService);
 *   2. o resultado volta para a thread principal (Handler), onde é permitido mexer na tela.
 */
public class ProdutoRepository {

    private static final String TAG = "ProdutoRepository";

    /** Recebe o resultado de uma operação, sempre na thread principal (pode mexer na tela). */
    public interface Callback<T> {
        void onResult(T resultado);
    }

    /**
     * Uma única thread de segundo plano, compartilhada por todo o app (por isso é static).
     * Ter só uma thread garante que as operações rodem em fila, uma depois da outra
     * (ex.: um "salvar" sempre termina antes do "listar" pedido logo em seguida).
     */
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    /** Handler ligado ao Looper principal: usado para "postar" código na thread da tela. */
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    private final ProdutoDao dao;

    public ProdutoRepository(Context context) {
        this(new ProdutoDao(context));
    }

    /** Construtor para testes: permite usar um DAO com banco em memória. */
    ProdutoRepository(ProdutoDao dao) {
        this.dao = dao;
    }

    /** Lista produtos. categoria == null -> todos; senão Produto.CATEGORIA_COMIDA / CATEGORIA_BEBIDA. */
    public void listar(String categoria, Callback<List<Produto>> callback) {
        EXECUTOR.execute(() -> {
            List<Produto> lista;
            try {
                lista = categoria == null ? dao.listarTodos() : dao.listarPorCategoria(categoria);
            } catch (Exception e) {
                Log.e(TAG, "Erro ao listar produtos", e);
                lista = new ArrayList<>(); // em caso de erro, a tela recebe uma lista vazia
            }
            entregar(callback, lista);
        });
    }

    /** Busca por id; devolve null se não existir. */
    public void buscarPorId(long id, Callback<Produto> callback) {
        EXECUTOR.execute(() -> {
            Produto produto;
            try {
                produto = dao.buscarPorId(id);
            } catch (Exception e) {
                Log.e(TAG, "Erro ao buscar produto " + id, e);
                produto = null;
            }
            entregar(callback, produto);
        });
    }

    /** Insere se produto.getId() == 0, senão atualiza. Devolve true se deu certo. */
    public void salvar(Produto produto, Callback<Boolean> callback) {
        EXECUTOR.execute(() -> {
            boolean ok;
            try {
                ok = produto.getId() == 0 ? dao.inserir(produto) > 0 : dao.atualizar(produto) > 0;
            } catch (Exception e) {
                Log.e(TAG, "Erro ao salvar produto", e);
                ok = false;
            }
            entregar(callback, ok);
        });
    }

    /** Exclui pelo id. Devolve true se deu certo. */
    public void excluir(long id, Callback<Boolean> callback) {
        EXECUTOR.execute(() -> {
            boolean ok;
            try {
                ok = dao.excluir(id) > 0;
            } catch (Exception e) {
                Log.e(TAG, "Erro ao excluir produto " + id, e);
                ok = false;
            }
            entregar(callback, ok);
        });
    }

    /** Envia o resultado para a thread principal e chama o callback lá. */
    private static <T> void entregar(Callback<T> callback, T resultado) {
        if (callback == null) {
            return; // quem chamou não quer saber o resultado
        }
        MAIN_HANDLER.post(() -> callback.onResult(resultado));
    }
}

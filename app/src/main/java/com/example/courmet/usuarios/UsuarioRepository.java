package com.example.courmet.usuarios;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Camada entre as telas e o banco de usuários. As telas usam SÓ esta classe.
 * Mesmo padrão do ProdutoRepository: banco em segundo plano, resultado na thread principal.
 */
public class UsuarioRepository {

    private static final String TAG = "UsuarioRepository";

    /** Resultado do salvar: além de "deu certo", a tela precisa saber se o e-mail já existe. */
    public enum ResultadoSalvar { OK, EMAIL_EM_USO, ERRO }

    /** Recebe o resultado de uma operação, sempre na thread principal (pode mexer na tela). */
    public interface Callback<T> {
        void onResult(T resultado);
    }

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    private final UsuarioDao dao;

    public UsuarioRepository(Context context) {
        this(new UsuarioDao(context));
    }

    /** Construtor para testes: permite usar um DAO com banco em memória. */
    UsuarioRepository(UsuarioDao dao) {
        this.dao = dao;
    }

    /** Lista todos os usuários (sem senha). */
    public void listar(Callback<List<Usuario>> callback) {
        EXECUTOR.execute(() -> {
            List<Usuario> lista;
            try {
                lista = dao.listarTodos();
            } catch (Exception e) {
                Log.e(TAG, "Erro ao listar usuários", e);
                lista = new ArrayList<>();
            }
            entregar(callback, lista);
        });
    }

    /** Busca por id; devolve null se não existir. */
    public void buscarPorId(long id, Callback<Usuario> callback) {
        EXECUTOR.execute(() -> {
            Usuario usuario;
            try {
                usuario = dao.buscarPorId(id);
            } catch (Exception e) {
                Log.e(TAG, "Erro ao buscar usuário " + id, e);
                usuario = null;
            }
            entregar(callback, usuario);
        });
    }

    /**
     * Insere (usuario.getId() == 0) ou atualiza.
     * senha: obrigatória ao inserir; ao atualizar, null/vazia = manter a senha atual.
     */
    public void salvar(Usuario usuario, String senha, Callback<ResultadoSalvar> callback) {
        EXECUTOR.execute(() -> {
            ResultadoSalvar resultado;
            try {
                if (dao.emailEmUso(usuario.getEmail(), usuario.getId())) {
                    resultado = ResultadoSalvar.EMAIL_EM_USO;
                } else if (usuario.getId() == 0) {
                    resultado = dao.inserir(usuario, senha) > 0 ? ResultadoSalvar.OK : ResultadoSalvar.ERRO;
                } else {
                    resultado = dao.atualizar(usuario, senha) > 0 ? ResultadoSalvar.OK : ResultadoSalvar.ERRO;
                }
            } catch (Exception e) {
                Log.e(TAG, "Erro ao salvar usuário", e);
                resultado = ResultadoSalvar.ERRO;
            }
            entregar(callback, resultado);
        });
    }

    /** Exclui pelo id. Devolve true se deu certo. */
    public void excluir(long id, Callback<Boolean> callback) {
        EXECUTOR.execute(() -> {
            boolean ok;
            try {
                ok = dao.excluir(id) > 0;
            } catch (Exception e) {
                Log.e(TAG, "Erro ao excluir usuário " + id, e);
                ok = false;
            }
            entregar(callback, ok);
        });
    }

    /** Login: devolve o usuário se e-mail e senha conferem, senão null. */
    public void autenticar(String email, String senha, Callback<Usuario> callback) {
        EXECUTOR.execute(() -> {
            Usuario usuario;
            try {
                usuario = dao.autenticar(email, senha);
            } catch (Exception e) {
                Log.e(TAG, "Erro ao autenticar", e);
                usuario = null;
            }
            entregar(callback, usuario);
        });
    }

    private static <T> void entregar(Callback<T> callback, T resultado) {
        if (callback != null) {
            MAIN_HANDLER.post(() -> callback.onResult(resultado));
        }
    }
}

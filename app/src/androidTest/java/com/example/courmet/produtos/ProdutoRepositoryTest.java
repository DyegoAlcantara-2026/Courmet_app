package com.example.courmet.produtos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.os.Looper;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Testa o ProdutoRepository, que é assíncrono: o resultado chega "depois", no callback.
 * Para o teste esperar a resposta, usamos um CountDownLatch (uma "trava" que libera
 * quando o callback chama countDown()).
 */
@RunWith(AndroidJUnit4.class)
public class ProdutoRepositoryTest {

    private ProdutoRepository repository;

    @Before
    public void setUp() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        repository = new ProdutoRepository(new ProdutoDao(context, null)); // banco em memória
    }

    /** Uma chamada ao repositório que recebe um callback. */
    private interface Operacao<T> {
        void executar(ProdutoRepository.Callback<T> callback);
    }

    /** Executa a operação, espera o callback (no máximo 5 s) e devolve o resultado recebido. */
    private <T> T aguardar(Operacao<T> operacao) throws InterruptedException {
        CountDownLatch trava = new CountDownLatch(1);
        AtomicReference<T> resultado = new AtomicReference<>();
        AtomicReference<Looper> looperDoCallback = new AtomicReference<>();
        operacao.executar(valor -> {
            resultado.set(valor);
            looperDoCallback.set(Looper.myLooper());
            trava.countDown();
        });
        assertTrue("O callback não foi chamado a tempo", trava.await(5, TimeUnit.SECONDS));
        // O callback precisa chegar na thread principal (onde se pode mexer na tela)
        assertSame(Looper.getMainLooper(), looperDoCallback.get());
        return resultado.get();
    }

    @Test
    public void listar_todos_e_porCategoria() throws InterruptedException {
        List<Produto> todos = aguardar(cb -> repository.listar(null, cb));
        List<Produto> bebidas = aguardar(cb -> repository.listar(Produto.CATEGORIA_BEBIDA, cb));
        assertEquals(5, todos.size());
        assertEquals(2, bebidas.size());
    }

    @Test
    public void salvar_insereAtualizaEExclui() throws InterruptedException {
        Produto p = new Produto("Chá", "Verde", Produto.CATEGORIA_BEBIDA, 8.0);

        // id == 0 -> insere
        assertTrue(aguardar(cb -> repository.salvar(p, cb)));
        assertTrue(p.getId() > 0);

        // id != 0 -> atualiza
        p.setPreco(9.0);
        assertTrue(aguardar(cb -> repository.salvar(p, cb)));
        Produto lido = aguardar(cb -> repository.buscarPorId(p.getId(), cb));
        assertNotNull(lido);
        assertEquals(9.0, lido.getPreco(), 0.0001);

        // exclui
        assertTrue(aguardar(cb -> repository.excluir(p.getId(), cb)));
        Produto excluido = aguardar(cb -> repository.buscarPorId(p.getId(), cb));
        assertNull(excluido);
    }

    @Test
    public void erros_naoQuebramOApp() throws InterruptedException {
        // Nome é NOT NULL no banco: a inserção falha e o callback recebe false
        Produto invalido = new Produto(null, null, Produto.CATEGORIA_COMIDA, 1.0);
        Boolean salvou = aguardar(cb -> repository.salvar(invalido, cb));
        assertEquals(Boolean.FALSE, salvou);

        // Excluir um id que não existe também devolve false
        Boolean excluiu = aguardar(cb -> repository.excluir(999_999, cb));
        assertEquals(Boolean.FALSE, excluiu);
    }
}

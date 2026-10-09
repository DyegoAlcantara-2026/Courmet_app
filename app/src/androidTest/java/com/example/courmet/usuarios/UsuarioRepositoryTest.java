package com.example.courmet.usuarios;

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
 * Testa o UsuarioRepository, que é assíncrono: o resultado chega "depois", no callback.
 * O CountDownLatch é uma "trava" que segura o teste até o callback chamar countDown().
 */
@RunWith(AndroidJUnit4.class)
public class UsuarioRepositoryTest {

    private UsuarioRepository repository;

    @Before
    public void setUp() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        repository = new UsuarioRepository(new UsuarioDao(context, null)); // banco em memória
    }

    /** Uma chamada ao repositório que recebe um callback. */
    private interface Operacao<T> {
        void executar(UsuarioRepository.Callback<T> callback);
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

    // Atalhos com o tipo do resultado já definido (deixam os testes mais fáceis de ler)

    private UsuarioRepository.ResultadoSalvar salvar(Usuario u, String senha) throws InterruptedException {
        return aguardar(cb -> repository.salvar(u, senha, cb));
    }

    private Usuario autenticar(String email, String senha) throws InterruptedException {
        return aguardar(cb -> repository.autenticar(email, senha, cb));
    }

    private Boolean excluir(long id) throws InterruptedException {
        return aguardar(cb -> repository.excluir(id, cb));
    }

    @Test
    public void salvar_novoUsuario_devolveOK() throws InterruptedException {
        Usuario u = new Usuario("Gabi", "gabi@courmet.com", "(11) 90000-0000");
        assertEquals(UsuarioRepository.ResultadoSalvar.OK, salvar(u, "abc"));
        assertTrue(u.getId() > 0);

        List<Usuario> lista = aguardar(cb -> repository.listar(cb));
        assertEquals(2, lista.size()); // exemplo + Gabi

        Usuario lido = aguardar(cb -> repository.buscarPorId(u.getId(), cb));
        assertNotNull(lido);
        assertEquals("Gabi", lido.getNome());
    }

    @Test
    public void salvar_emailRepetido_devolveEMAIL_EM_USO() throws InterruptedException {
        Usuario copia = new Usuario("Cópia", "Aluno@Courmet.com", null);
        assertEquals(UsuarioRepository.ResultadoSalvar.EMAIL_EM_USO, salvar(copia, "abc"));
        assertEquals(0, copia.getId()); // não foi inserido

        // Na edição, trocar para o e-mail de outro usuário também é EMAIL_EM_USO
        Usuario h = new Usuario("Heitor", "heitor@courmet.com", null);
        assertEquals(UsuarioRepository.ResultadoSalvar.OK, salvar(h, "abc"));
        h.setEmail("aluno@courmet.com");
        assertEquals(UsuarioRepository.ResultadoSalvar.EMAIL_EM_USO, salvar(h, null));
    }

    @Test
    public void salvar_edicao_semSenhaMantemESenhaNovaTroca() throws InterruptedException {
        Usuario u = new Usuario("Iris", "iris@courmet.com", null);
        assertEquals(UsuarioRepository.ResultadoSalvar.OK, salvar(u, "antiga"));

        // Mantendo o próprio e-mail não é "em uso"; senha null = manter a atual
        u.setNome("Iris Costa");
        assertEquals(UsuarioRepository.ResultadoSalvar.OK, salvar(u, null));
        assertNotNull(autenticar("iris@courmet.com", "antiga"));

        assertEquals(UsuarioRepository.ResultadoSalvar.OK, salvar(u, "nova"));
        assertNull(autenticar("iris@courmet.com", "antiga"));
        assertNotNull(autenticar("iris@courmet.com", "nova"));
    }

    @Test
    public void salvar_novoSemSenha_devolveERRO() throws InterruptedException {
        Usuario u = new Usuario("Sem senha", "semsenha@courmet.com", null);
        assertEquals(UsuarioRepository.ResultadoSalvar.ERRO, salvar(u, ""));
    }

    @Test
    public void autenticar_certoEErrado() throws InterruptedException {
        Usuario ok = autenticar("aluno@courmet.com", "123456");
        assertNotNull(ok);
        assertEquals("Aluno Courmet", ok.getNome());

        assertNull(autenticar("aluno@courmet.com", "000000"));
    }

    @Test
    public void excluir_existenteEInexistente() throws InterruptedException {
        Usuario u = new Usuario("Júlia", "julia@courmet.com", null);
        assertEquals(UsuarioRepository.ResultadoSalvar.OK, salvar(u, "abc"));

        assertEquals(Boolean.TRUE, excluir(u.getId()));
        assertEquals(Boolean.FALSE, excluir(u.getId())); // já não existe
    }
}

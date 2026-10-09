package com.example.courmet.usuarios;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

/**
 * Teste instrumentado do UsuarioDao (usa o SQLite do Android, então roda no emulador).
 * Cada teste usa um banco NOVO em memória (nome null): os dados reais do app não mudam.
 * O banco já começa com o usuário de exemplo aluno@courmet.com / 123456.
 */
@RunWith(AndroidJUnit4.class)
public class UsuarioDaoTest {

    private static final String EMAIL_EXEMPLO = "aluno@courmet.com";
    private static final String SENHA_EXEMPLO = "123456";

    private UsuarioDao dao;

    @Before
    public void setUp() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        dao = new UsuarioDao(context, null);
    }

    @Test
    public void usuarioDeExemplo_existeELogaCom123456() {
        List<Usuario> lista = dao.listarTodos();
        assertEquals(1, lista.size());
        assertEquals("Aluno Courmet", lista.get(0).getNome());
        assertEquals(EMAIL_EXEMPLO, lista.get(0).getEmail());

        Usuario logado = dao.autenticar(EMAIL_EXEMPLO, SENHA_EXEMPLO);
        assertNotNull(logado);
        assertEquals("Aluno Courmet", logado.getNome());
    }

    @Test
    public void autenticar_emailComMaiusculasEEspacos_funciona() {
        assertNotNull(dao.autenticar("  Aluno@Courmet.COM ", SENHA_EXEMPLO));
    }

    @Test
    public void autenticar_senhaErradaOuEmailDesconhecido_devolveNull() {
        assertNull(dao.autenticar(EMAIL_EXEMPLO, "654321"));
        assertNull(dao.autenticar(EMAIL_EXEMPLO, ""));
        assertNull(dao.autenticar("ninguem@courmet.com", SENHA_EXEMPLO));
        assertNull(dao.autenticar(null, SENHA_EXEMPLO));
        assertNull(dao.autenticar(EMAIL_EXEMPLO, null));
    }

    @Test
    public void inserir_listarEBuscarPorId() {
        Usuario novo = new Usuario("Beatriz Lima", "bia@courmet.com", "(21) 98888-7777");
        long id = dao.inserir(novo, "segredo");

        assertTrue(id > 0);
        assertEquals(id, novo.getId()); // inserir() preenche o id no objeto

        Usuario lido = dao.buscarPorId(id);
        assertNotNull(lido);
        assertEquals("Beatriz Lima", lido.getNome());
        assertEquals("bia@courmet.com", lido.getEmail());
        assertEquals("(21) 98888-7777", lido.getTelefone());

        // Lista em ordem alfabética: "Aluno Courmet" vem antes de "Beatriz Lima"
        List<Usuario> lista = dao.listarTodos();
        assertEquals(2, lista.size());
        assertEquals("Aluno Courmet", lista.get(0).getNome());
        assertEquals("Beatriz Lima", lista.get(1).getNome());

        assertNotNull(dao.autenticar("bia@courmet.com", "segredo"));
    }

    @Test
    public void inserir_guardaEmailPadronizado() {
        Usuario novo = new Usuario("Carlos", "  Carlos@Courmet.COM  ", null);
        long id = dao.inserir(novo, "abc");
        assertTrue(id > 0);
        assertEquals("carlos@courmet.com", dao.buscarPorId(id).getEmail());
        assertEquals("carlos@courmet.com", novo.getEmail());
    }

    @Test
    public void inserir_semSenhaOuSemEmail_falha() {
        assertEquals(-1, dao.inserir(new Usuario("Sem senha", "a@courmet.com", null), null));
        assertEquals(-1, dao.inserir(new Usuario("Sem senha", "a@courmet.com", null), ""));
        assertEquals(-1, dao.inserir(new Usuario("Sem e-mail", null, null), "abc"));
        assertEquals(-1, dao.inserir(new Usuario("Sem e-mail", "   ", null), "abc"));
        assertEquals(1, dao.listarTodos().size()); // só o usuário de exemplo
    }

    @Test
    public void emailRepetido_mesmoComOutraCaixa_naoPodeSerInserido() {
        assertTrue(dao.emailEmUso(EMAIL_EXEMPLO, 0));
        assertTrue(dao.emailEmUso("ALUNO@Courmet.com", 0));
        assertTrue(dao.emailEmUso(" aluno@courmet.com ", 0));
        assertFalse(dao.emailEmUso("outro@courmet.com", 0));
        assertFalse(dao.emailEmUso(null, 0));

        assertEquals(-1, dao.inserir(new Usuario("Cópia", EMAIL_EXEMPLO, null), "x"));
        assertEquals(-1, dao.inserir(new Usuario("Cópia", "Aluno@Courmet.Com", null), "x"));
        assertEquals(1, dao.listarTodos().size());
    }

    @Test
    public void emailEmUso_ignoraOProprioUsuario() {
        Usuario exemplo = dao.listarTodos().get(0);
        // Editando o próprio cadastro, manter o mesmo e-mail não é "e-mail em uso"
        assertFalse(dao.emailEmUso(EMAIL_EXEMPLO, exemplo.getId()));
        assertFalse(dao.emailEmUso("ALUNO@courmet.com", exemplo.getId()));
    }

    @Test
    public void atualizar_semSenha_mantemASenhaAntiga() {
        Usuario u = new Usuario("Davi", "davi@courmet.com", null);
        dao.inserir(u, "antiga");

        u.setNome("Davi Rocha");
        u.setTelefone("(31) 97777-6666");
        assertEquals(1, dao.atualizar(u, null));
        assertEquals(1, dao.atualizar(u, "")); // vazia também mantém

        Usuario lido = dao.buscarPorId(u.getId());
        assertEquals("Davi Rocha", lido.getNome());
        assertEquals("(31) 97777-6666", lido.getTelefone());
        assertNotNull(dao.autenticar("davi@courmet.com", "antiga"));
    }

    @Test
    public void atualizar_comNovaSenha_trocaASenha() {
        Usuario u = new Usuario("Eva", "eva@courmet.com", null);
        dao.inserir(u, "antiga");

        assertEquals(1, dao.atualizar(u, "nova"));
        assertNull(dao.autenticar("eva@courmet.com", "antiga"));
        assertNotNull(dao.autenticar("eva@courmet.com", "nova"));
    }

    @Test
    public void atualizar_paraEmailDeOutroUsuario_naoAltera() {
        Usuario u = new Usuario("Fábio", "fabio@courmet.com", null);
        dao.inserir(u, "abc");

        u.setEmail("ALUNO@courmet.com"); // já é do usuário de exemplo
        assertEquals(0, dao.atualizar(u, null));
        assertEquals("fabio@courmet.com", dao.buscarPorId(u.getId()).getEmail());
    }

    @Test
    public void excluir_removeOUsuario() {
        Usuario u = new Usuario("Temporário", "tmp@courmet.com", null);
        dao.inserir(u, "abc");

        assertEquals(1, dao.excluir(u.getId()));
        assertNull(dao.buscarPorId(u.getId()));
        assertNull(dao.autenticar("tmp@courmet.com", "abc"));
        assertEquals(0, dao.excluir(u.getId())); // excluir de novo não apaga nada
    }

    @Test
    public void buscarPorId_inexistente_devolveNull() {
        assertNull(dao.buscarPorId(999_999));
    }
}

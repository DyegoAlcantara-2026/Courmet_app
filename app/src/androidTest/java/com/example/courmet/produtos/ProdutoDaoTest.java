package com.example.courmet.produtos;

import static org.junit.Assert.assertEquals;
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
 * Teste instrumentado (roda no emulador/celular, pois usa o SQLite do Android).
 * Cada teste usa um banco NOVO em memória (nome null), então os dados reais do app
 * não são alterados. O banco já começa com os 5 produtos de exemplo do DatabaseHelper.
 */
@RunWith(AndroidJUnit4.class)
public class ProdutoDaoTest {

    private ProdutoDao dao;

    @Before
    public void setUp() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        dao = new ProdutoDao(context, null);
    }

    @Test
    public void inserir_depoisBuscarPorId_devolveOMesmoProduto() {
        Produto novo = new Produto("Pudim", "De leite", Produto.CATEGORIA_COMIDA, 15.0);
        long id = dao.inserir(novo);

        assertTrue(id > 0);
        assertEquals(id, novo.getId()); // inserir() preenche o id no objeto

        Produto lido = dao.buscarPorId(id);
        assertNotNull(lido);
        assertEquals("Pudim", lido.getNome());
        assertEquals("De leite", lido.getDescricao());
        assertEquals(Produto.CATEGORIA_COMIDA, lido.getCategoria());
        assertEquals(15.0, lido.getPreco(), 0.0001);
    }

    @Test
    public void listarTodos_trazOsExemplosEmOrdemAlfabetica() {
        dao.inserir(new Produto("abacaxi", null, Produto.CATEGORIA_BEBIDA, 1.0));
        List<Produto> lista = dao.listarTodos();

        // 5 exemplos + 1 inserido, em ordem alfabética ignorando maiúsculas e acentos
        String[] esperado = {
                "abacaxi",
                "Água com gás",
                "Guioza de Wagyu",
                "Omakase Premium (12 pçs)",
                "Risoto de Funghi Porcini",
                "Saquê Junmai Daiginjo"
        };
        assertEquals(esperado.length, lista.size());
        for (int i = 0; i < esperado.length; i++) {
            assertEquals(esperado[i], lista.get(i).getNome());
        }
    }

    @Test
    public void listarPorCategoria_trazSoACategoriaPedida() {
        List<Produto> bebidas = dao.listarPorCategoria(Produto.CATEGORIA_BEBIDA);
        List<Produto> comidas = dao.listarPorCategoria(Produto.CATEGORIA_COMIDA);

        assertEquals(2, bebidas.size());
        assertEquals(3, comidas.size());
        for (Produto p : bebidas) {
            assertTrue(p.isBebida());
        }
        for (Produto p : comidas) {
            assertEquals(Produto.CATEGORIA_COMIDA, p.getCategoria());
        }
    }

    @Test
    public void atualizar_alteraOsDados() {
        Produto p = new Produto("Café", "Expresso", Produto.CATEGORIA_BEBIDA, 7.0);
        dao.inserir(p);

        p.setNome("Café duplo");
        p.setPreco(12.0);
        assertEquals(1, dao.atualizar(p));

        Produto lido = dao.buscarPorId(p.getId());
        assertEquals("Café duplo", lido.getNome());
        assertEquals(12.0, lido.getPreco(), 0.0001);
    }

    @Test
    public void excluir_removeOProduto() {
        Produto p = new Produto("Temporário", null, Produto.CATEGORIA_COMIDA, 1.0);
        dao.inserir(p);

        assertEquals(1, dao.excluir(p.getId()));
        assertNull(dao.buscarPorId(p.getId()));
        assertEquals(0, dao.excluir(p.getId())); // excluir de novo não apaga nada
    }

    @Test
    public void buscarPorId_inexistente_devolveNull() {
        assertNull(dao.buscarPorId(999_999));
    }
}

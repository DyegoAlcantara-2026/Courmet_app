package com.example.courmet.produtos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Teste de unidade "puro" (roda no computador, sem emulador): Produto é uma classe
 * Java comum, que não depende do Android.
 */
public class ProdutoTest {

    @Test
    public void produtoNovo_comecaComIdZero() {
        Produto p = new Produto("Suco", "Laranja", Produto.CATEGORIA_BEBIDA, 10.0);
        assertEquals(0, p.getId());
    }

    @Test
    public void isBebida_verdadeiroSoParaBebida() {
        assertTrue(new Produto("Suco", null, Produto.CATEGORIA_BEBIDA, 10.0).isBebida());
        assertFalse(new Produto("Bolo", null, Produto.CATEGORIA_COMIDA, 10.0).isBebida());
        assertFalse(new Produto("Sem categoria", null, null, 10.0).isBebida());
    }

    @Test
    public void setters_alteramOsCampos() {
        Produto p = new Produto(5, "A", "B", Produto.CATEGORIA_COMIDA, 1.0);
        p.setNome("Novo");
        p.setPreco(2.5);
        p.setCategoria(Produto.CATEGORIA_BEBIDA);
        assertEquals(5, p.getId());
        assertEquals("Novo", p.getNome());
        assertEquals(2.5, p.getPreco(), 0.0001);
        assertTrue(p.isBebida());
    }
}

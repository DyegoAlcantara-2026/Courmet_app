package com.example.courmet.usuarios;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Teste de unidade "puro" (roda no computador): SenhaUtil só usa Java comum.
 */
public class SenhaUtilTest {

    @Test
    public void mesmaSenhaEMesmoSalt_geramOMesmoHash() {
        String salt = SenhaUtil.gerarSalt();
        assertEquals(SenhaUtil.hash("123456", salt), SenhaUtil.hash("123456", salt));
    }

    @Test
    public void saltsDiferentes_geramHashesDiferentes() {
        String salt1 = SenhaUtil.gerarSalt();
        String salt2 = SenhaUtil.gerarSalt();
        assertNotEquals(salt1, salt2); // cada chamada sorteia um salt novo
        assertNotEquals(SenhaUtil.hash("123456", salt1), SenhaUtil.hash("123456", salt2));
    }

    @Test
    public void hash_ehSha256EmHexadecimal() {
        // SHA-256 tem 32 bytes = 64 caracteres hexadecimais
        String hash = SenhaUtil.hash("senha", "abc");
        assertEquals(64, hash.length());
        assertTrue(hash.matches("[0-9a-f]{64}"));
        // O hash é de (salt + senha) juntos: "abc"+"senha" e "ab"+"csenha" dão "abcsenha"
        assertEquals(SenhaUtil.hash("csenha", "ab"), hash);
    }

    @Test
    public void confere_verdadeiroSoComASenhaCerta() {
        String salt = SenhaUtil.gerarSalt();
        String hashSalvo = SenhaUtil.hash("minhaSenha", salt);

        assertTrue(SenhaUtil.confere("minhaSenha", salt, hashSalvo));
        assertFalse(SenhaUtil.confere("outraSenha", salt, hashSalvo));
        assertFalse(SenhaUtil.confere("MINHASENHA", salt, hashSalvo)); // diferencia maiúsculas
        assertFalse(SenhaUtil.confere("minhaSenha", SenhaUtil.gerarSalt(), hashSalvo)); // salt errado
    }

    @Test
    public void confere_comDadosNulos_devolveFalseSemQuebrar() {
        String salt = SenhaUtil.gerarSalt();
        String hashSalvo = SenhaUtil.hash("x", salt);
        assertFalse(SenhaUtil.confere(null, salt, hashSalvo));
        assertFalse(SenhaUtil.confere("x", null, hashSalvo));
        assertFalse(SenhaUtil.confere("x", salt, null));
    }

    @Test
    public void gerarSalt_tem32CaracteresHexadecimais() {
        // 16 bytes aleatórios -> 32 caracteres hexadecimais
        String salt = SenhaUtil.gerarSalt();
        assertEquals(32, salt.length());
        assertTrue(salt.matches("[0-9a-f]{32}"));
    }
}

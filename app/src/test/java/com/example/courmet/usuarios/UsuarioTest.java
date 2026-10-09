package com.example.courmet.usuarios;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Teste de unidade "puro" do modelo Usuario (sem Android).
 */
public class UsuarioTest {

    private static String iniciais(String nome) {
        return new Usuario(nome, "a@b.com", null).getIniciais();
    }

    @Test
    public void usuarioNovo_comecaComIdZero() {
        assertEquals(0, new Usuario("Ana", "ana@x.com", null).getId());
    }

    @Test
    public void getIniciais_nomeCompleto_usaPrimeiroEUltimoNome() {
        assertEquals("AC", iniciais("Aluno Courmet"));
        assertEquals("MS", iniciais("Maria da Silva"));
    }

    @Test
    public void getIniciais_umNomeSo_usaUmaLetra() {
        assertEquals("M", iniciais("maria"));
    }

    @Test
    public void getIniciais_viraMaiuscula() {
        assertEquals("JS", iniciais("joão souza"));
        assertEquals("ÉA", iniciais("érica alves"));
    }

    @Test
    public void getIniciais_ignoraEspacosExtras() {
        assertEquals("AB", iniciais("   Ana    Beatriz  "));
        assertEquals("AB", iniciais("Ana\tBeatriz"));
    }

    @Test
    public void getIniciais_nomeVazioOuNulo_devolveInterrogacao() {
        assertEquals("?", iniciais(null));
        assertEquals("?", iniciais(""));
        assertEquals("?", iniciais("    "));
    }

    @Test
    public void getIniciais_naoCortaEmojiAoMeio() {
        // O emoji ocupa 2 "chars" em Java; a inicial precisa trazer o emoji inteiro
        String emoji = "😀"; // 😀
        assertEquals(emoji, iniciais(emoji + "feliz"));
    }

    @Test
    public void getIniciais_usaMaiusculaNeutraIndependenteDoIdioma() {
        java.util.Locale original = java.util.Locale.getDefault();
        try {
            java.util.Locale.setDefault(java.util.Locale.forLanguageTag("tr-TR"));
            assertEquals("IS", iniciais("isabel souza")); // em turco seria "İS"
        } finally {
            java.util.Locale.setDefault(original);
        }
    }
}

package com.example.courmet.usuarios;

import java.util.Locale;

/**
 * Modelo (entidade) de um usuário do app. Corresponde a uma linha da tabela "usuarios".
 *
 * Repare que NÃO existe um campo "senha": o banco guarda só o hash da senha
 * (veja SenhaUtil). A senha em texto puro só passa pelo app na hora de cadastrar/logar.
 */
public class Usuario {

    private long id;
    private String nome;
    private String email;
    private String telefone;

    /** Construtor para um usuário novo (id 0; o banco gera o id ao inserir). */
    public Usuario(String nome, String email, String telefone) {
        this(0, nome, email, telefone);
    }

    /** Construtor para um usuário que já existe no banco. */
    public Usuario(long id, String nome, String email, String telefone) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    /**
     * Iniciais para o avatar (ex.: "Aluno Courmet" -> "AC", "maria" -> "M", vazio -> "?").
     * Usa a primeira letra do primeiro e do último nome.
     */
    public String getIniciais() {
        if (nome == null || nome.trim().isEmpty()) return "?";
        String[] partes = nome.trim().split("\\s+");
        String iniciais = primeiraLetra(partes[0]);
        if (partes.length > 1) iniciais += primeiraLetra(partes[partes.length - 1]);
        // Locale.ROOT: maiúsculas "neutras" (em turco, por ex., "i" viraria "İ")
        return iniciais.toUpperCase(Locale.ROOT);
    }

    /**
     * Primeiro "caractere de verdade" da palavra. Alguns símbolos (como emojis) ocupam
     * 2 chars em Java; substring(0, 1) cortaria o emoji ao meio. Por isso usamos codePointAt.
     */
    private static String primeiraLetra(String palavra) {
        return new String(Character.toChars(palavra.codePointAt(0)));
    }
}

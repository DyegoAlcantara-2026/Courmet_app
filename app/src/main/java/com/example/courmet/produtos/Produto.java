package com.example.courmet.produtos;

/**
 * Modelo (entidade) de um item do cardápio: uma comida ou uma bebida.
 * Cada objeto corresponde a uma linha da tabela "produtos" no SQLite.
 */
public class Produto {

    public static final String CATEGORIA_COMIDA = "COMIDA";
    public static final String CATEGORIA_BEBIDA = "BEBIDA";

    private long id;
    private String nome;
    private String descricao;
    private String categoria;
    private double preco;

    /** Construtor para um produto novo (ainda sem id; o banco gera o id ao inserir). */
    public Produto(String nome, String descricao, String categoria, double preco) {
        this(0, nome, descricao, categoria, preco);
    }

    /** Construtor para um produto que já existe no banco. */
    public Produto(long id, String nome, String descricao, String categoria, double preco) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.categoria = categoria;
        this.preco = preco;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public double getPreco() { return preco; }
    public void setPreco(double preco) { this.preco = preco; }

    public boolean isBebida() {
        return CATEGORIA_BEBIDA.equals(categoria);
    }
}

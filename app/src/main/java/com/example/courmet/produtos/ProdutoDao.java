package com.example.courmet.produtos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object): concentra todo o SQL do CRUD de produtos.
 * As telas só chamam estes métodos e nunca escrevem SQL diretamente.
 *
 * Os métodos daqui são síncronos (esperam o banco responder). Por isso as telas NÃO
 * chamam o DAO diretamente: elas usam o ProdutoRepository, que executa estes métodos
 * em uma thread de segundo plano.
 */
public class ProdutoDao {

    private final DatabaseHelper helper;

    public ProdutoDao(Context context) {
        // getApplicationContext(): evita guardar referência a uma Activity (vazamento de memória)
        helper = DatabaseHelper.getInstance(context); // conexão única, compartilhada
    }

    /** Construtor para testes: nomeBanco == null cria um banco só na memória. */
    public ProdutoDao(Context context, String nomeBanco) {
        helper = new DatabaseHelper(context.getApplicationContext(), nomeBanco);
    }

    /** CREATE: insere e devolve o id gerado (ou -1 se falhar). */
    public long inserir(Produto produto) {
        SQLiteDatabase db = helper.getWritableDatabase();
        long id = db.insert(DatabaseHelper.TABELA_PRODUTOS, null, toValues(produto));
        // Só guarda o id se a inserção deu certo (senão o produto ficaria com id -1)
        if (id != -1) {
            produto.setId(id);
        }
        return id;
    }

    /** READ: todos os produtos, em ordem alfabética. */
    public List<Produto> listarTodos() {
        return consultar(null, null);
    }

    /** READ: só os produtos de uma categoria (Produto.CATEGORIA_COMIDA ou CATEGORIA_BEBIDA). */
    public List<Produto> listarPorCategoria(String categoria) {
        return consultar(DatabaseHelper.COL_CATEGORIA + " = ?", new String[]{categoria});
    }

    /** READ: um produto pelo id, ou null se não existir. */
    public Produto buscarPorId(long id) {
        List<Produto> resultado = consultar(DatabaseHelper.COL_ID + " = ?", new String[]{String.valueOf(id)});
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    /** UPDATE: devolve quantas linhas foram alteradas (1 se deu certo). */
    public int atualizar(Produto produto) {
        SQLiteDatabase db = helper.getWritableDatabase();
        return db.update(DatabaseHelper.TABELA_PRODUTOS, toValues(produto),
                DatabaseHelper.COL_ID + " = ?", new String[]{String.valueOf(produto.getId())});
    }

    /** DELETE: devolve quantas linhas foram apagadas (1 se deu certo). */
    public int excluir(long id) {
        SQLiteDatabase db = helper.getWritableDatabase();
        return db.delete(DatabaseHelper.TABELA_PRODUTOS,
                DatabaseHelper.COL_ID + " = ?", new String[]{String.valueOf(id)});
    }

    private List<Produto> consultar(String where, String[] args) {
        List<Produto> lista = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        // Ordenação: "COLLATE LOCALIZED" é uma regra do SQLite do Android que segue o idioma
        // do aparelho (ignora maiúsculas e acentos). Com "COLLATE NOCASE", "Água" iria para o
        // fim da lista, porque NOCASE só entende letras sem acento.
        // try-with-resources fecha o Cursor automaticamente
        try (Cursor c = db.query(DatabaseHelper.TABELA_PRODUTOS, null, where, args,
                null, null, DatabaseHelper.COL_NOME + " COLLATE LOCALIZED ASC")) {
            while (c.moveToNext()) {
                lista.add(new Produto(
                        c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COL_ID)),
                        c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_NOME)),
                        c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_DESCRICAO)),
                        c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_CATEGORIA)),
                        c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.COL_PRECO))));
            }
        }
        return lista;
    }

    private static ContentValues toValues(Produto produto) {
        ContentValues valores = new ContentValues();
        valores.put(DatabaseHelper.COL_NOME, produto.getNome());
        valores.put(DatabaseHelper.COL_DESCRICAO, produto.getDescricao());
        valores.put(DatabaseHelper.COL_CATEGORIA, produto.getCategoria());
        valores.put(DatabaseHelper.COL_PRECO, produto.getPreco());
        return valores;
    }
}

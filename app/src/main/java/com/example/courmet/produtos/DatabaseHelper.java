package com.example.courmet.produtos;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Cria e atualiza o banco SQLite do app (tabelas produtos e usuarios).
 * O Android chama onCreate() só na primeira vez que o banco é aberto, e
 * onUpgrade() quando VERSAO_BANCO aumenta em relação ao banco já instalado.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String NOME_BANCO = "courmet.db";
    // Aumente a versão sempre que mudar a estrutura das tabelas -> chama onUpgrade()
    // Histórico: 1 = tabela produtos; 2 = + tabela usuarios
    private static final int VERSAO_BANCO = 2;

    public static final String TABELA_PRODUTOS = "produtos";
    public static final String COL_ID = "id";
    public static final String COL_NOME = "nome";
    public static final String COL_DESCRICAO = "descricao";
    public static final String COL_CATEGORIA = "categoria";
    public static final String COL_PRECO = "preco";

    public static final String TABELA_USUARIOS = "usuarios";
    // COL_ID e COL_NOME acima também são usados na tabela de usuários
    public static final String COL_EMAIL = "email";
    public static final String COL_TELEFONE = "telefone";
    public static final String COL_SENHA_HASH = "senha_hash";
    public static final String COL_SENHA_SALT = "senha_salt";

    /**
     * Instância única (padrão Singleton) usada pelo app inteiro.
     * Produtos e usuários ficam no mesmo arquivo; se cada DAO abrisse sua própria
     * conexão, duas gravações ao mesmo tempo poderiam dar "database is locked".
     */
    private static DatabaseHelper instancia;

    /** Devolve sempre o mesmo DatabaseHelper do arquivo "courmet.db". */
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instancia == null) {
            // getApplicationContext(): o Singleton vive enquanto o app vive, então não
            // pode guardar uma Activity (isso causaria vazamento de memória)
            instancia = new DatabaseHelper(context.getApplicationContext(), NOME_BANCO);
        }
        return instancia;
    }

    /**
     * Construtor que permite escolher o nome do banco.
     * Passando null, o SQLite cria um banco só na memória (some ao fechar):
     * ótimo para testes automatizados, que não devem mexer nos dados reais do app.
     */
    public DatabaseHelper(Context context, String nomeBanco) {
        super(context, nomeBanco, null, VERSAO_BANCO);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABELA_PRODUTOS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_NOME + " TEXT NOT NULL, "
                + COL_DESCRICAO + " TEXT, "
                + COL_CATEGORIA + " TEXT NOT NULL, "
                + COL_PRECO + " REAL NOT NULL)");

        // Dados de exemplo para a lista não começar vazia
        inserirExemplo(db, "Omakase Premium (12 pçs)", "Seleção do Chef de sushis exóticos.", Produto.CATEGORIA_COMIDA, 180.00);
        inserirExemplo(db, "Guioza de Wagyu", "Massa fina recheada com Wagyu selado (4 unidades).", Produto.CATEGORIA_COMIDA, 48.00);
        inserirExemplo(db, "Risoto de Funghi Porcini", "Arbóreo cremoso com lâminas de trufa.", Produto.CATEGORIA_COMIDA, 89.00);
        inserirExemplo(db, "Saquê Junmai Daiginjo", "Dose de 120 ml, servido gelado.", Produto.CATEGORIA_BEBIDA, 42.00);
        inserirExemplo(db, "Água com gás", "Garrafa 300 ml.", Produto.CATEGORIA_BEBIDA, 9.50);

        criarTabelaUsuarios(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Migração incremental: aplica só o que falta, SEM apagar os dados existentes.
        // Quem instalou o app na versão 1 já tem a tabela produtos; só ganha a de usuários.
        if (oldVersion < 2) {
            criarTabelaUsuarios(db);
        }
    }

    /** Tabela de usuários (versão 2 do banco) + um usuário de exemplo para conseguir logar. */
    private static void criarTabelaUsuarios(SQLiteDatabase db) {
        // IF NOT EXISTS: se a tabela já existir por algum motivo, a migração não quebra o app.
        // (Nesse caso o insert do exemplo abaixo só devolve -1 por causa do e-mail UNIQUE.)
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABELA_USUARIOS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_NOME + " TEXT NOT NULL, "
                + COL_EMAIL + " TEXT NOT NULL UNIQUE COLLATE NOCASE, " // não permite e-mail repetido
                + COL_TELEFONE + " TEXT, "
                + COL_SENHA_HASH + " TEXT NOT NULL, "
                + COL_SENHA_SALT + " TEXT NOT NULL)");

        // Login de exemplo: aluno@courmet.com / 123456
        String salt = com.example.courmet.usuarios.SenhaUtil.gerarSalt();
        ContentValues valores = new ContentValues();
        valores.put(COL_NOME, "Aluno Courmet");
        valores.put(COL_EMAIL, "aluno@courmet.com");
        valores.put(COL_TELEFONE, "(11) 99999-0000");
        valores.put(COL_SENHA_HASH, com.example.courmet.usuarios.SenhaUtil.hash("123456", salt));
        valores.put(COL_SENHA_SALT, salt);
        db.insert(TABELA_USUARIOS, null, valores);
    }

    private static void inserirExemplo(SQLiteDatabase db, String nome, String descricao, String categoria, double preco) {
        ContentValues valores = new ContentValues();
        valores.put(COL_NOME, nome);
        valores.put(COL_DESCRICAO, descricao);
        valores.put(COL_CATEGORIA, categoria);
        valores.put(COL_PRECO, preco);
        db.insert(TABELA_PRODUTOS, null, valores);
    }
}

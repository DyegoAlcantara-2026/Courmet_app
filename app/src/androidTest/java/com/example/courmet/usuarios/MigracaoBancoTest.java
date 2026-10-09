package com.example.courmet.usuarios;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.courmet.produtos.DatabaseHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Testa a MIGRAÇÃO do banco da versão 1 (só produtos) para a versão 2 (+ usuarios).
 *
 * Simulamos um celular que tinha o app antigo: criamos "na mão" um arquivo de banco
 * igual ao da versão 1, com um produto cadastrado pelo usuário. Depois abrimos esse
 * arquivo com o DatabaseHelper atual, que deve chamar onUpgrade(1 -> 2).
 * Esperado: o produto continua lá E a tabela de usuários foi criada com o usuário de exemplo.
 *
 * Usamos um nome de arquivo único (não o courmet.db real) e apagamos tudo no final.
 */
@RunWith(AndroidJUnit4.class)
public class MigracaoBancoTest {

    private Context context;
    private String nomeBanco;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        nomeBanco = "teste_migracao_" + System.nanoTime() + ".db";
    }

    @After
    public void tearDown() {
        context.deleteDatabase(nomeBanco); // apaga o arquivo temporário (e os arquivos auxiliares)
    }

    @Test
    public void migracaoV1paraV2_mantemProdutosECriaUsuarios() {
        // 1) Banco "antigo", versão 1: só a tabela produtos, com um produto do usuário
        try (SQLiteDatabase v1 = context.openOrCreateDatabase(nomeBanco, Context.MODE_PRIVATE, null)) {
            v1.execSQL("CREATE TABLE produtos ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "nome TEXT NOT NULL, "
                    + "descricao TEXT, "
                    + "categoria TEXT NOT NULL, "
                    + "preco REAL NOT NULL)");
            v1.execSQL("INSERT INTO produtos (nome, descricao, categoria, preco) "
                    + "VALUES ('Produto Antigo', 'Cadastrado na versão 1', 'comida', 12.5)");
            v1.setVersion(1); // o mesmo que PRAGMA user_version = 1
        }

        // 2) Abre com o DatabaseHelper atual (versão 2) -> roda onUpgrade(1, 2)
        DatabaseHelper helper = new DatabaseHelper(context, nomeBanco);
        try {
            SQLiteDatabase db = helper.getWritableDatabase();
            assertEquals(2, db.getVersion());

            // O produto antigo continua lá (a migração NÃO apagou os dados)
            try (Cursor c = db.rawQuery("SELECT nome, preco FROM produtos", null)) {
                assertEquals(1, c.getCount());
                assertTrue(c.moveToFirst());
                assertEquals("Produto Antigo", c.getString(0));
                assertEquals(12.5, c.getDouble(1), 0.0001);
            }

            // A tabela de usuários existe e tem o usuário de exemplo
            try (Cursor c = db.rawQuery("SELECT email FROM usuarios", null)) {
                assertEquals(1, c.getCount());
                assertTrue(c.moveToFirst());
                assertEquals("aluno@courmet.com", c.getString(0));
            }
        } finally {
            helper.close();
        }

        // 3) E o login de exemplo funciona no banco migrado
        UsuarioDao dao = new UsuarioDao(context, nomeBanco);
        Usuario logado = dao.autenticar("aluno@courmet.com", "123456");
        assertNotNull(logado);
        assertEquals("Aluno Courmet", logado.getNome());
    }
}

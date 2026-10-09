package com.example.courmet.usuarios;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;

import com.example.courmet.produtos.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * DAO de usuários: todo o SQL do CRUD da tabela "usuarios".
 * Métodos síncronos -> as telas usam o UsuarioRepository, que chama estes métodos em segundo plano.
 *
 * O banco (DatabaseHelper) é o mesmo dos produtos: um único arquivo courmet.db com as duas tabelas.
 */
public class UsuarioDao {

    private final DatabaseHelper helper;

    public UsuarioDao(Context context) {
        helper = DatabaseHelper.getInstance(context); // conexão única, compartilhada
    }

    /** Construtor para testes: nomeBanco == null cria um banco só na memória. */
    public UsuarioDao(Context context, String nomeBanco) {
        helper = new DatabaseHelper(context.getApplicationContext(), nomeBanco);
    }

    /**
     * Padroniza o e-mail antes de salvar ou procurar: tira espaços das pontas e
     * passa para minúsculas. Assim " Aluno@Courmet.com " e "aluno@courmet.com"
     * são tratados como o MESMO e-mail em todo lugar (cadastro, edição e login).
     * Locale.ROOT evita surpresas com idiomas que têm regras próprias de maiúsculas.
     */
    static String normalizarEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * CREATE: insere com a senha (gera salt + hash). Devolve o id, ou -1 se falhar
     * (por exemplo, e-mail já cadastrado: a coluna é UNIQUE; ou e-mail/senha vazios).
     */
    public long inserir(Usuario usuario, String senha) {
        String email = normalizarEmail(usuario.getEmail());
        // Sem e-mail ou sem senha não dá para cadastrar (a pessoa nunca conseguiria logar)
        if (email == null || email.isEmpty() || senha == null || senha.isEmpty()) {
            return -1;
        }
        ContentValues valores = dadosBasicos(usuario);
        String salt = SenhaUtil.gerarSalt();
        valores.put(DatabaseHelper.COL_SENHA_SALT, salt);
        valores.put(DatabaseHelper.COL_SENHA_HASH, SenhaUtil.hash(senha, salt));

        // insert() não lança exceção: se violar uma regra (UNIQUE, NOT NULL), devolve -1
        long id = helper.getWritableDatabase().insert(DatabaseHelper.TABELA_USUARIOS, null, valores);
        if (id != -1) {
            usuario.setId(id);
            usuario.setEmail(email); // o objeto fica igual ao que foi gravado
        }
        return id;
    }

    /** READ: todos os usuários, em ordem alfabética. */
    public List<Usuario> listarTodos() {
        return consultar(null, null);
    }

    /** READ: um usuário pelo id, ou null. */
    public Usuario buscarPorId(long id) {
        List<Usuario> r = consultar(DatabaseHelper.COL_ID + " = ?", new String[]{String.valueOf(id)});
        return r.isEmpty() ? null : r.get(0);
    }

    /**
     * true se já existe OUTRO usuário (id diferente) com este e-mail.
     * A comparação ignora maiúsculas/minúsculas e espaços nas pontas.
     */
    public boolean emailEmUso(String email, long idIgnorado) {
        String normalizado = normalizarEmail(email);
        if (normalizado == null) {
            return false; // não dá para passar null no "?" da consulta
        }
        // COLLATE NOCASE: compara sem diferenciar "A" de "a" (vale também para
        // e-mails antigos que tenham sido gravados com maiúsculas)
        try (Cursor c = helper.getReadableDatabase().query(DatabaseHelper.TABELA_USUARIOS,
                new String[]{DatabaseHelper.COL_ID},
                DatabaseHelper.COL_EMAIL + " = ? COLLATE NOCASE AND " + DatabaseHelper.COL_ID + " != ?",
                new String[]{normalizado, String.valueOf(idIgnorado)}, null, null, null)) {
            return c.moveToFirst();
        }
    }

    /**
     * UPDATE: atualiza nome, e-mail e telefone. Se novaSenha não for null/vazia,
     * troca a senha também (novo salt + hash). Devolve quantas linhas mudaram
     * (0 se o id não existe ou se o novo e-mail já pertence a outro usuário).
     */
    public int atualizar(Usuario usuario, String novaSenha) {
        String email = normalizarEmail(usuario.getEmail());
        if (email == null || email.isEmpty()) {
            return 0;
        }
        ContentValues valores = dadosBasicos(usuario);
        if (novaSenha != null && !novaSenha.isEmpty()) {
            String salt = SenhaUtil.gerarSalt();
            valores.put(DatabaseHelper.COL_SENHA_SALT, salt);
            valores.put(DatabaseHelper.COL_SENHA_HASH, SenhaUtil.hash(novaSenha, salt));
        }
        try {
            int linhas = helper.getWritableDatabase().update(DatabaseHelper.TABELA_USUARIOS, valores,
                    DatabaseHelper.COL_ID + " = ?", new String[]{String.valueOf(usuario.getId())});
            if (linhas > 0) {
                usuario.setEmail(email);
            }
            return linhas;
        } catch (SQLiteConstraintException e) {
            // Diferente do insert(), o update() LANÇA exceção quando viola uma regra
            // (ex.: e-mail repetido). Devolvemos 0 para manter o mesmo estilo do inserir().
            return 0;
        }
    }

    /** DELETE: devolve quantas linhas foram apagadas. */
    public int excluir(long id) {
        return helper.getWritableDatabase().delete(DatabaseHelper.TABELA_USUARIOS,
                DatabaseHelper.COL_ID + " = ?", new String[]{String.valueOf(id)});
    }

    /**
     * Login: devolve o usuário se e-mail e senha conferem, senão null.
     * O e-mail ignora maiúsculas/minúsculas e espaços nas pontas; a senha NÃO
     * (senha "Abc" é diferente de "abc").
     */
    public Usuario autenticar(String email, String senha) {
        String normalizado = normalizarEmail(email);
        if (normalizado == null || normalizado.isEmpty() || senha == null) {
            return null;
        }
        try (Cursor c = helper.getReadableDatabase().query(DatabaseHelper.TABELA_USUARIOS, null,
                DatabaseHelper.COL_EMAIL + " = ? COLLATE NOCASE", new String[]{normalizado},
                null, null, null)) {
            if (!c.moveToFirst()) {
                return null; // e-mail não cadastrado
            }
            String salt = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_SENHA_SALT));
            String hash = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_SENHA_HASH));
            return SenhaUtil.confere(senha, salt, hash) ? lerUsuario(c) : null;
        }
    }

    private List<Usuario> consultar(String where, String[] args) {
        List<Usuario> lista = new ArrayList<>();
        // Não pedimos as colunas de senha: a tela nunca precisa delas
        String[] colunas = {DatabaseHelper.COL_ID, DatabaseHelper.COL_NOME,
                DatabaseHelper.COL_EMAIL, DatabaseHelper.COL_TELEFONE};
        try (Cursor c = helper.getReadableDatabase().query(DatabaseHelper.TABELA_USUARIOS, colunas,
                where, args, null, null, DatabaseHelper.COL_NOME + " COLLATE LOCALIZED ASC")) {
            while (c.moveToNext()) {
                lista.add(lerUsuario(c));
            }
        }
        return lista;
    }

    private static Usuario lerUsuario(Cursor c) {
        return new Usuario(
                c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COL_ID)),
                c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_NOME)),
                c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_EMAIL)),
                c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_TELEFONE)));
    }

    private static ContentValues dadosBasicos(Usuario usuario) {
        ContentValues valores = new ContentValues();
        valores.put(DatabaseHelper.COL_NOME, usuario.getNome());
        valores.put(DatabaseHelper.COL_EMAIL, normalizarEmail(usuario.getEmail()));
        valores.put(DatabaseHelper.COL_TELEFONE, usuario.getTelefone());
        return valores;
    }
}

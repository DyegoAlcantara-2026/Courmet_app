package com.example.courmet.usuarios;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Nunca guarde senhas em texto puro no banco! Guardamos um "hash":
 * uma transformação de mão única (não dá para voltar do hash para a senha).
 *
 * O "salt" é um valor aleatório diferente para cada usuário, misturado à senha antes
 * do hash. Assim, dois usuários com a mesma senha têm hashes diferentes.
 *
 * Login: calculamos o hash da senha digitada com o salt salvo e comparamos com o hash salvo.
 *
 * Observação: para um projeto acadêmico, SHA-256 + salt é suficiente. Em produção,
 * use um algoritmo lento feito para senhas (PBKDF2, bcrypt ou Argon2) e autenticação no servidor.
 */
public final class SenhaUtil {

    private SenhaUtil() {
    }

    /** Gera um salt aleatório de 16 bytes, em hexadecimal. */
    public static String gerarSalt() {
        byte[] bytes = new byte[16];
        new SecureRandom().nextBytes(bytes);
        return paraHex(bytes);
    }

    /** SHA-256(salt + senha), em hexadecimal. */
    public static String hash(String senha, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] resultado = digest.digest((salt + senha).getBytes(StandardCharsets.UTF_8));
            return paraHex(resultado);
        } catch (NoSuchAlgorithmException e) {
            // Todo Android tem SHA-256; se faltar, é um erro grave de ambiente
            throw new IllegalStateException(e);
        }
    }

    /** Confere se a senha digitada bate com o hash salvo. */
    public static boolean confere(String senha, String salt, String hashSalvo) {
        // Algum dado faltando? Então não confere (e evitamos um NullPointerException)
        if (senha == null || salt == null || hashSalvo == null) {
            return false;
        }
        // MessageDigest.isEqual compara em "tempo constante": não dá pistas pelo tempo de resposta
        return MessageDigest.isEqual(
                hash(senha, salt).getBytes(StandardCharsets.UTF_8),
                hashSalvo.getBytes(StandardCharsets.UTF_8));
    }

    private static String paraHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}

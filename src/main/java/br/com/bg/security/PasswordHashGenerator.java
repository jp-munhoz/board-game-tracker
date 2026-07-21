package br.com.bg.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Gera o hash BCrypt de uma senha em texto puro, para inserir usuarios manualmente
 * no banco (nao ha endpoint de cadastro). Uso:
 * mvn exec:java -Dexec.args="minhaSenha"
 */
public final class PasswordHashGenerator {

    private PasswordHashGenerator() {
    }

    public static void main(String[] args) {
        if (args.length != 1 || args[0].isBlank()) {
            System.err.println("Uso: mvn exec:java -Dexec.args=\"minhaSenha\"");
            System.exit(1);
        }
        System.out.println(new BCryptPasswordEncoder().encode(args[0]));
    }
}

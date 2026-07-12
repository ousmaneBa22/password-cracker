package com.passwordcracker.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Classe utilitaire regroupant les opérations liées au hachage MD5.
 * <p>
 * Cette classe est volontairement <b>statique et sans état</b> : elle ne
 * fait qu'encapsuler l'appel à {@link MessageDigest}, ce qui évite de
 * dupliquer la logique de hachage dans chaque stratégie de cassage
 * ({@code DictionaryHashCracker}, {@code BruteForceHashCracker}, ...).
 * </p>
 */
public final class MD5Utils {

    // Constructeur privé : classe utilitaire non instanciable.
    private MD5Utils() {
        throw new AssertionError("MD5Utils ne doit pas être instanciée.");
    }

    /**
     * Calcule l'empreinte MD5 d'une chaîne de caractères et la retourne
     * sous forme de chaîne hexadécimale (32 caractères, minuscules).
     *
     * @param input la chaîne à hacher (ex : un mot de passe en clair)
     * @return le hash MD5 correspondant, en hexadécimal
     */
    public static String hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] rawBytes = digest.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return toHexString(rawBytes);
        } catch (NoSuchAlgorithmException e) {
            // MD5 est garanti disponible sur toute JVM standard : cette
            // exception ne devrait jamais se produire en pratique.
            throw new IllegalStateException("Algorithme MD5 non disponible sur cette JVM.", e);
        }
    }

    /**
     * Convertit un tableau d'octets en une représentation hexadécimale.
     *
     * @param bytes le tableau d'octets à convertir
     * @return la chaîne hexadécimale correspondante
     */
    private static String toHexString(byte[] bytes) {
        StringBuilder hexBuilder = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexBuilder.append('0');
            }
            hexBuilder.append(hex);
        }
        return hexBuilder.toString();
    }

    /**
     * Vérifie qu'une chaîne correspond au format attendu d'un hash MD5
     * (32 caractères hexadécimaux).
     *
     * @param candidate la chaîne à valider
     * @return {@code true} si le format est valide, {@code false} sinon
     */
    public static boolean isValidMd5Format(String candidate) {
        return candidate != null && candidate.matches("^[a-fA-F0-9]{32}$");
    }
}

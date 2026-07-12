package com.passwordcracker;

import com.passwordcracker.utils.ConsoleColors;
import com.passwordcracker.utils.MD5Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Stratégie de cassage par dictionnaire ("DICO").
 * <p>
 * Le principe est simple : on parcourt une liste de mots candidats, on
 * calcule le hash MD5 de chacun, et on le compare au hash recherché.
 * Cette approche est très rapide mais limitée aux mots présents dans le
 * dictionnaire.
 * </p>
 * <p>
 * Deux sources de dictionnaire sont supportées :
 * <ul>
 *     <li>un dictionnaire embarqué par défaut (ressource {@code dictionnaire.txt}) ;</li>
 *     <li>un fichier externe fourni par l'utilisateur (option {@code -d}).</li>
 * </ul>
 */
public class DictionaryHashCracker implements HashCracker {

    private static final String DEFAULT_DICTIONARY_RESOURCE = "/dictionnaire.txt";

    private final String customDictionaryPath;

    /** Nombre de mots effectivement testés lors du dernier appel à {@link #crack}. */
    private long attempts = 0;

    /** Construit une stratégie utilisant uniquement le dictionnaire embarqué par défaut. */
    public DictionaryHashCracker() {
        this(null);
    }

    /**
     * Construit une stratégie utilisant un dictionnaire externe.
     *
     * @param customDictionaryPath chemin vers un fichier dictionnaire (un mot par ligne),
     *                              ou {@code null} pour utiliser le dictionnaire par défaut
     */
    public DictionaryHashCracker(String customDictionaryPath) {
        this.customDictionaryPath = customDictionaryPath;
    }

    @Override
    public String crack(String hash) {
        attempts = 0;
        List<String> words = loadDictionary();

        ConsoleColors.printInfo(
                "Dictionnaire chargé : " + words.size() + " mot(s) à tester.");

        for (String word : words) {
            attempts++;
            if (MD5Utils.hash(word).equalsIgnoreCase(hash)) {
                return word;
            }
        }
        return null;
    }

    /** @return le nombre de mots testés lors de la dernière exécution de {@link #crack}. */
    public long getAttempts() {
        return attempts;
    }

    /**
     * Charge le dictionnaire à utiliser : le fichier externe s'il a été
     * fourni et existe, sinon le dictionnaire embarqué par défaut.
     */
    private List<String> loadDictionary() {
        if (customDictionaryPath != null) {
            Path path = Path.of(customDictionaryPath);
            if (Files.isRegularFile(path)) {
                try {
                    return readLines(Files.newInputStream(path));
                } catch (IOException e) {
                    ConsoleColors.printWarning(
                            "Impossible de lire le dictionnaire personnalisé ('" + customDictionaryPath
                                    + "'), utilisation du dictionnaire par défaut.");
                }
            } else {
                ConsoleColors.printWarning(
                        "Fichier dictionnaire introuvable ('" + customDictionaryPath
                                + "'), utilisation du dictionnaire par défaut.");
            }
        }
        return loadDefaultDictionary();
    }

    /** Charge le dictionnaire embarqué dans les ressources du projet. */
    private List<String> loadDefaultDictionary() {
        try (InputStream is = DictionaryHashCracker.class.getResourceAsStream(DEFAULT_DICTIONARY_RESOURCE)) {
            if (is == null) {
                throw new IllegalStateException(
                        "Dictionnaire par défaut introuvable dans les ressources (" + DEFAULT_DICTIONARY_RESOURCE + ").");
            }
            return readLines(is);
        } catch (IOException e) {
            throw new IllegalStateException("Erreur de lecture du dictionnaire par défaut.", e);
        }
    }

    /** Lit un flux ligne par ligne et retourne les mots non vides, débarrassés des espaces superflus. */
    private List<String> readLines(InputStream inputStream) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) {
                    lines.add(trimmed);
                }
            }
        }
        return lines;
    }
}

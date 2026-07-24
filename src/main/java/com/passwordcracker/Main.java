package com.passwordcracker;

import com.passwordcracker.utils.ConsoleColors;
import com.passwordcracker.utils.MD5Utils;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Point d'entrée en ligne de commande de l'application {@code passwordCracker}.
 * <p>
 * Responsabilités de cette classe (et uniquement celles-ci) :
 * </p>
 * <ul>
 *     <li>parser et valider les arguments de la ligne de commande ;</li>
 *     <li>déléguer la création de la stratégie de cassage à {@link HashCrackerFactory} ;</li>
 *     <li>orchestrer l'exécution (mesure du temps, affichage du résultat).</li>
 * </ul>
 * <p>
 * Conformément aux contraintes du projet, <b>aucune classe concrète de
 * stratégie n'est instanciée ici</b> : seule la fabrique est utilisée.
 * </p>
 *
 * <p><b>Usage :</b></p>
 * <pre>
 *   passwordCracker -m BRUTE -h e7247759c1633c0f9f1485f3690294a9
 *   passwordCracker -m DICO  -h e7247759c1633c0f9f1485f3690294a9
 *   passwordCracker -m DICO  -h e7247759c1633c0f9f1485f3690294a9 -d mon_dictionnaire.txt
 * </pre>
 */
public final class Main {

    private Main() {
        // Classe d'entrée non instanciable.
    }

    public static void main(String[] args) {
        // Force l'encodage UTF-8 en sortie, quel que soit l'encodage par
        // défaut de la plateforme d'exécution (important pour les accents
        // et les symboles ✔ / ✘ affichés dans les résultats).
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));

        printBanner();

        Map<String, String> options;
        try {
            options = parseArguments(args);
            validateArguments(options);
        } catch (IllegalArgumentException e) {
            ConsoleColors.printError("Erreur d'arguments : " + e.getMessage());
            printUsage();
            System.exit(1);
            return;
        }

        String method = options.get("m").toUpperCase();
        String hash = options.get("h").toLowerCase();
        String dictionaryPath = options.get("d"); // peut être null

        run(method, hash, dictionaryPath);
    }

    /**
     * Exécute le cassage : création de la stratégie via la fabrique,
     * mesure du temps d'exécution, puis affichage du résultat final.
     */
    private static void run(String method, String hash, String dictionaryPath) {
        ConsoleColors.printInfo("Méthode sélectionnée : " + method);
        ConsoleColors.printInfo("Hash recherché       : " + hash);
        System.out.println();

        // --- Seule ligne du programme qui "connaît" la fabrique. ---
        // Aucune instanciation directe de DictionaryHashCracker / BruteForceHashCracker ici.
        HashCracker cracker = HashCrackerFactory.create(method, dictionaryPath);

        long startTime = System.nanoTime();
        String password = cracker.crack(hash);
        long elapsedMillis = (System.nanoTime() - startTime) / 1_000_000;

        System.out.println();
        printResult(password, cracker, elapsedMillis);
    }

    /** Affiche le résultat final de manière claire et professionnelle. */
    private static void printResult(String password, HashCracker cracker, long elapsedMillis) {
        System.out.println(ConsoleColors.BOLD + "──────────────────────────────────────────" + ConsoleColors.RESET);

        if (password != null) {
            ConsoleColors.printSuccess("✔ Password found: " + password);
        } else {
            ConsoleColors.printError("✘ Password not found");
        }

        long attempts = extractAttempts(cracker);
        if (attempts >= 0) {
            ConsoleColors.printMuted("  Tentatives effectuées : " + String.format("%,d", attempts));
        }
        ConsoleColors.printMuted("  Temps d'exécution     : " + formatDuration(elapsedMillis));
        System.out.println(ConsoleColors.BOLD + "──────────────────────────────────────────" + ConsoleColors.RESET);
    }

    /**
     * Récupère le nombre de tentatives effectuées par la stratégie utilisée.
     * Cette information est optionnelle et n'appartient pas au contrat de
     * {@link HashCracker} : elle est simplement exposée par les
     * implémentations concrètes à des fins d'affichage.
     */
    private static long extractAttempts(HashCracker cracker) {
        if (cracker instanceof DictionaryHashCracker dic) {
            return dic.getAttempts();
        }
        if (cracker instanceof BruteForceHashCracker brute) {
            return brute.getAttempts();
        }
        return -1;
    }

    /** Met en forme une durée en millisecondes de façon lisible. */
    private static String formatDuration(long millis) {
        if (millis < 1000) {
            return millis + " ms";
        }
        return String.format("%.2f s", millis / 1000.0);
    }

    /**
     * Parse les arguments de la ligne de commande sous la forme
     * {@code -clé valeur}, ex : {@code -m BRUTE -h abcd... -d dico.txt}.
     */
    private static Map<String, String> parseArguments(String[] args) {
        Map<String, String> options = new HashMap<>();

        int i = 0;
        while (i < args.length) {
            String token = args[i];
            if (!token.startsWith("-")) {
                throw new IllegalArgumentException("Argument inattendu : '" + token + "'.");
            }
            String key = token.substring(1);

            if (i + 1 >= args.length) {
                throw new IllegalArgumentException("Aucune valeur fournie pour l'option '-" + key + "'.");
            }
            String value = args[i + 1];
            options.put(key, value);
            i += 2;
        }
        return options;
    }

    /** Valide la cohérence et la présence des arguments obligatoires. */
    private static void validateArguments(Map<String, String> options) {
        if (!options.containsKey("m")) {
            throw new IllegalArgumentException("L'option -m (méthode) est obligatoire.");
        }
        if (!options.containsKey("h")) {
            throw new IllegalArgumentException("L'option -h (hash) est obligatoire.");
        }

        String method = options.get("m").toUpperCase();
        if (!method.equals(HashCrackerFactory.METHOD_DICTIONARY) && !method.equals(HashCrackerFactory.METHOD_BRUTE_FORCE)) {
            throw new IllegalArgumentException(
                    "Méthode invalide '" + options.get("m") + "'. Valeurs attendues : "
                            + HashCrackerFactory.METHOD_DICTIONARY + " ou " + HashCrackerFactory.METHOD_BRUTE_FORCE + ".");
        }

        String hash = options.get("h");
        if (!MD5Utils.isValidMd5Format(hash)) {
            throw new IllegalArgumentException(
                    "Le hash fourni ('" + hash + "') n'est pas un MD5 valide (32 caractères hexadécimaux attendus).");
        }
    }

    private static void printBanner() {
        System.out.println(ConsoleColors.BLUE + ConsoleColors.BOLD +
                "\n╔═══════════════════════════════════════╗\n" +
                "║         PasswordCracker v1.0           ║\n" +
                "║   Simple Factory Pattern - MD5 Cracker ║\n" +
                "╚═══════════════════════════════════════╝\n" + ConsoleColors.RESET);
    }

    private static void printUsage() {
        System.out.println();
        ConsoleColors.printInfo("Usage :");
        System.out.println("  passwordCracker -m <BRUTE|DICO> -h <hashMD5> [-d <dictionnaire.txt>]");
        System.out.println();
        ConsoleColors.printInfo("Exemples :");
        System.out.println("  passwordCracker -m BRUTE -h e7247759c1633c0f9f1485f3690294a9");
        System.out.println("  passwordCracker -m DICO  -h 5f4dcc3b5aa765d61d8327deb882cf99");
        System.out.println("  passwordCracker -m DICO  -h 5f4dcc3b5aa765d61d8327deb882cf99 -d mon_dico.txt");
    }
}
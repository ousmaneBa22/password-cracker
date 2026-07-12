package com.passwordcracker;

import com.passwordcracker.utils.ConsoleColors;
import com.passwordcracker.utils.BruteForceGenerator;
import com.passwordcracker.utils.MD5Utils;

/**
 * Stratégie de cassage par force brute ("BRUTE").
 * <p>
 * Cette stratégie génère systématiquement toutes les combinaisons
 * possibles de caractères minuscules (a-z), de longueur 1 à
 * {@value #MAX_LENGTH}, et compare le hash MD5 de chacune au hash
 * recherché, jusqu'à trouver une correspondance ou épuiser l'espace de
 * recherche.
 * </p>
 * <p>
 * La génération des combinaisons est déléguée à {@link BruteForceGenerator},
 * qui les produit à la volée (sans les stocker toutes en mémoire), ce qui
 * permet à cette classe de rester simple et de se concentrer sur sa seule
 * responsabilité : comparer les hashs.
 * </p>
 */
public class BruteForceHashCracker implements HashCracker {

    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";
    private static final int MAX_LENGTH = 4;

    /** Fréquence de rafraîchissement de la barre de progression (en nombre de tentatives). */
    private static final long PROGRESS_REFRESH_STEP = 2_000;

    /** Nombre de combinaisons effectivement testées lors du dernier appel à {@link #crack}. */
    private long attempts = 0;

    @Override
    public String crack(String hash) {
        attempts = 0;
        BruteForceGenerator generator = new BruteForceGenerator(ALPHABET, MAX_LENGTH);
        long total = generator.totalCombinations();

        ConsoleColors.printInfo(
                "Espace de recherche : " + total + " combinaisons possibles (alphabet a-z, longueur 1 à "
                        + MAX_LENGTH + ").");

        for (String candidate : generator) {
            attempts++;

            if (attempts % PROGRESS_REFRESH_STEP == 0) {
                ConsoleColors.printProgressBar(attempts, total, 30);
            }

            if (MD5Utils.hash(candidate).equalsIgnoreCase(hash)) {
                ConsoleColors.printProgressBar(attempts, total, 30);
                System.out.println(); // saut de ligne après la barre de progression
                return candidate;
            }
        }

        ConsoleColors.printProgressBar(total, total, 30);
        System.out.println();
        return null;
    }

    /** @return le nombre de combinaisons testées lors de la dernière exécution de {@link #crack}. */
    public long getAttempts() {
        return attempts;
    }
}

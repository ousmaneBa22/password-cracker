package com.passwordcracker.utils;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Générateur itératif de combinaisons de caractères, utilisé par la
 * stratégie {@code BruteForceHashCracker}.
 * <p>
 * Plutôt que de générer récursivement toutes les combinaisons en mémoire
 * (ce qui deviendrait rapidement coûteux en RAM), cette classe implémente
 * {@link Iterable} et produit les combinaisons <b>à la volée</b>, une par
 * une, à la manière d'un compteur en base {@code alphabet.length()}
 * (un "odomètre"). Cela permet de tester des espaces de recherche très
 * grands avec une empreinte mémoire constante.
 * </p>
 * <p>
 * Les combinaisons sont générées par longueur croissante (1, puis 2, ...
 * jusqu'à {@code maxLength}), ce qui correspond à l'ordre "naturel" d'une
 * attaque par force brute : on essaie d'abord les mots de passe les plus
 * courts, statistiquement les plus rapides à trouver.
 * </p>
 */
public final class BruteForceGenerator implements Iterable<String> {

    private final char[] alphabet;
    private final int maxLength;

    /**
     * @param alphabet  l'ensemble des caractères utilisables (ex : "abcdefghijklmnopqrstuvwxyz")
     * @param maxLength la longueur maximale des combinaisons générées
     */
    public BruteForceGenerator(String alphabet, int maxLength) {
        if (alphabet == null || alphabet.isEmpty()) {
            throw new IllegalArgumentException("L'alphabet ne peut pas être vide.");
        }
        if (maxLength < 1) {
            throw new IllegalArgumentException("La longueur maximale doit être >= 1.");
        }
        this.alphabet = alphabet.toCharArray();
        this.maxLength = maxLength;
    }

    /**
     * Calcule le nombre total de combinaisons que ce générateur produira,
     * toutes longueurs confondues (utile pour la barre de progression).
     * Formule : somme géométrique de |alphabet|^1 à |alphabet|^maxLength.
     *
     * @return le nombre total de combinaisons
     */
    public long totalCombinations() {
        long total = 0;
        long power = 1;
        for (int length = 1; length <= maxLength; length++) {
            power *= alphabet.length;
            total += power;
        }
        return total;
    }

    @Override
    public Iterator<String> iterator() {
        return new OdometerIterator();
    }

    /**
     * Itérateur interne implémentant la logique d'incrémentation façon
     * "odomètre" en base {@code alphabet.length()}.
     */
    private final class OdometerIterator implements Iterator<String> {

        // Index courant de chaque position dans l'alphabet (ex: [0,0] = "aa").
        private int[] indices;
        private boolean finished = false;

        OdometerIterator() {
            indices = new int[1];
            // indices initialisés à 0 -> première combinaison = alphabet[0]
        }

        @Override
        public boolean hasNext() {
            return !finished;
        }

        @Override
        public String next() {
            if (finished) {
                throw new NoSuchElementException("Toutes les combinaisons ont été générées.");
            }

            // Construction de la chaîne correspondant aux indices courants.
            char[] combination = new char[indices.length];
            for (int i = 0; i < indices.length; i++) {
                combination[i] = alphabet[indices[i]];
            }
            String result = new String(combination);

            advance();
            return result;
        }

        /**
         * Fait progresser l'odomètre d'un cran : incrémente la dernière
         * position, gère les retenues (carry) comme un compteur classique,
         * et augmente la longueur si toutes les combinaisons de la
         * longueur courante ont été épuisées.
         */
        private void advance() {
            int position = indices.length - 1;

            while (position >= 0) {
                indices[position]++;
                if (indices[position] < alphabet.length) {
                    return; // pas de retenue, incrémentation terminée
                }
                indices[position] = 0;
                position--;
            }

            // Retenue propagée jusqu'au bout : on passe à la longueur suivante.
            if (indices.length >= maxLength) {
                finished = true;
            } else {
                indices = new int[indices.length + 1];
            }
        }
    }
}

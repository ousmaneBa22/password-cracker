package com.passwordcracker.utils;

/**
 * Classe utilitaire fournissant des codes ANSI pour colorer et mettre en
 * forme la sortie console, ainsi que quelques méthodes d'affichage
 * réutilisables (succès, échec, information, barre de progression).
 * <p>
 * Isoler ces constantes ici évite de disperser des chaînes d'échappement
 * ANSI "magiques" dans tout le code métier.
 * </p>
 */
public final class ConsoleColors {

    public static final String RESET = "\u001B[0m";
    public static final String BOLD = "\u001B[1m";

    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String CYAN = "\u001B[36m";
    public static final String GRAY = "\u001B[90m";

    private ConsoleColors() {
        throw new AssertionError("ConsoleColors ne doit pas être instanciée.");
    }

    /** Affiche un message de succès en vert et en gras. */
    public static void printSuccess(String message) {
        System.out.println(GREEN + BOLD + message + RESET);
    }

    /** Affiche un message d'échec en rouge et en gras. */
    public static void printError(String message) {
        System.out.println(RED + BOLD + message + RESET);
    }

    /** Affiche un message d'information en cyan. */
    public static void printInfo(String message) {
        System.out.println(CYAN + message + RESET);
    }

    /** Affiche un message secondaire (statistiques, détails) en gris. */
    public static void printMuted(String message) {
        System.out.println(GRAY + message + RESET);
    }

    /** Affiche un avertissement en jaune. */
    public static void printWarning(String message) {
        System.out.println(YELLOW + message + RESET);
    }

    /**
     * Affiche/rafraîchit une barre de progression sur une seule ligne
     * grâce au retour chariot ('\r'). Utilisée notamment par la stratégie
     * de force brute afin de renseigner l'utilisateur sur l'avancement du
     * calcul, potentiellement long.
     *
     * @param current  nombre d'éléments déjà traités
     * @param total    nombre total d'éléments à traiter
     * @param barWidth largeur (en caractères) de la barre de progression
     */
    public static void printProgressBar(long current, long total, int barWidth) {
        double ratio = total == 0 ? 0 : Math.min(1.0, (double) current / total);
        int filled = (int) (ratio * barWidth);

        StringBuilder bar = new StringBuilder();
        bar.append('\r').append(CYAN).append('[');
        bar.append("=".repeat(Math.max(0, filled)));
        if (filled < barWidth) {
            bar.append('>');
            bar.append(" ".repeat(Math.max(0, barWidth - filled - 1)));
        }
        bar.append(']').append(RESET);
        bar.append(String.format(" %6.2f%%  (%,d / %,d tentatives)", ratio * 100, current, total));

        System.out.print(bar);
        System.out.flush();
    }
}

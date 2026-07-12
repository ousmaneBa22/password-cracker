package com.passwordcracker;

/**
 * Fabrique simple (patron <b>Simple Factory</b>) responsable de la
 * création des différentes stratégies {@link HashCracker}.
 * <p>
 * Il s'agit du <b>point de création unique</b> de l'application : aucune
 * classe concrète ({@code DictionaryHashCracker}, {@code BruteForceHashCracker})
 * n'est instanciée ailleurs dans le code (voir {@code Main}). Cela permet
 * de centraliser la logique de sélection de stratégie et de découpler le
 * code appelant des implémentations concrètes.
 * </p>
 * <p>
 * <b>Limite connue :</b> l'ajout d'une nouvelle stratégie nécessite de
 * modifier cette classe (ajout d'un nouveau {@code case}), ce qui viole
 * partiellement le principe Open/Closed. Ce point est développé dans le
 * README (section "Usage du patron Simple Factory" et "Questions de
 * réflexion") et sera corrigé dans une prochaine version du projet à
 * l'aide d'un patron plus avancé (Factory Method / Registry).
 * </p>
 */
public class HashCrackerFactory {

    // Constantes représentant les méthodes de cassage supportées.
    public static final String METHOD_DICTIONARY = "DICO";
    public static final String METHOD_BRUTE_FORCE = "BRUTE";

    // Constructeur privé : fabrique purement statique, non instanciable.
    private HashCrackerFactory() {
        throw new AssertionError("HashCrackerFactory ne doit pas être instanciée.");
    }

    /**
     * Crée l'implémentation de {@link HashCracker} correspondant à la
     * méthode demandée, en utilisant le dictionnaire embarqué par défaut
     * pour la stratégie {@code DICO}.
     *
     * @param method la méthode de cassage : {@code "BRUTE"} ou {@code "DICO"}
     * @return l'instance de {@link HashCracker} correspondante
     * @throws IllegalArgumentException si la méthode n'est pas reconnue
     */
    public static HashCracker create(String method) {
        return create(method, null);
    }

    /**
     * Surcharge permettant de préciser un dictionnaire externe pour la
     * stratégie {@code DICO} (option {@code -d} du programme). Ce
     * paramètre est ignoré pour les autres stratégies.
     *
     * @param method              la méthode de cassage : {@code "BRUTE"} ou {@code "DICO"}
     * @param customDictionaryPath chemin vers un dictionnaire externe (optionnel, peut être {@code null})
     * @return l'instance de {@link HashCracker} correspondante
     * @throws IllegalArgumentException si la méthode n'est pas reconnue
     */
    public static HashCracker create(String method, String customDictionaryPath) {
        if (method == null) {
            throw new IllegalArgumentException("La méthode de cassage ne peut pas être nulle.");
        }

        return switch (method.toUpperCase()) {
            case METHOD_DICTIONARY -> new DictionaryHashCracker(customDictionaryPath);
            case METHOD_BRUTE_FORCE -> new BruteForceHashCracker();
            default -> throw new IllegalArgumentException(
                    "Méthode de cassage inconnue : '" + method + "'. Valeurs attendues : "
                            + METHOD_DICTIONARY + " ou " + METHOD_BRUTE_FORCE + ".");
        };
    }
}

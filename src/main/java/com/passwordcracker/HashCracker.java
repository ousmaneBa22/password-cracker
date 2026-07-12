package com.passwordcracker;

/**
 * Interface commune à toutes les stratégies de cassage de mot de passe.
 * <p>
 * Il s'agit du contrat central de l'architecture : chaque algorithme de
 * cassage (dictionnaire, force brute, ...) doit se conformer à cette
 * interface pour pouvoir être utilisé de manière interchangeable
 * (polymorphisme) par le reste de l'application.
 * </p>
 */
public interface HashCracker {

    /**
     * Tente de retrouver le mot de passe en clair correspondant à un hash MD5.
     *
     * @param hash le hash MD5 (32 caractères hexadécimaux) à casser
     * @return le mot de passe trouvé, ou {@code null} si aucun résultat
     *         n'a pu être obtenu
     */
    String crack(String hash);
}

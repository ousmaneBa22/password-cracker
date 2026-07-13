# Partie 3 — Validation : plan de tests manuels

Ce document recense les scénarios de test exécutés pour valider le bon fonctionnement de **PasswordCracker v1**, conformément à la section "Partie 3 : Validation" du mini-projet. Chaque test a été exécuté réellement sur le JAR compilé ; les sorties ci-dessous sont les sorties effectives obtenues.

## Méthodologie

- Compilation : `javac` (Java 17), aucune dépendance externe.
- Hashs de référence calculés indépendamment en Python (`hashlib.md5`) pour garantir l'exactitude des cas de test.
- Chaque test vérifie : (1) le résultat métier (mot trouvé / non trouvé / erreur), (2) le code de sortie du processus, (3) la cohérence de l'affichage.

## Tableau de synthèse

| # | Scénario | Commande | Résultat attendu | Résultat obtenu | Statut |
|---|---|---|---|---|---|
| 1 | DICO — mot présent dans le dictionnaire | `-m DICO -h 5ebe2294ecd0e0f08eab7690d2a6ee69` | `Password found: secret` | `Password found: secret` (2 tentatives, 125 ms) | ✅ |
| 2 | DICO — hash absent du dictionnaire | `-m DICO -h bfb535f3b75a88418c0382248ef48e46` | `Password not found` | `Password not found` (50 tentatives, 44 ms) | ✅ |
| 3 | DICO — dictionnaire externe via `-d` | `-m DICO -h 9eb9b7f08a8211ce9b4ba33750a15211 -d mon_dico.txt` | `Password found: test_perso` | `Password found: test_perso` (1 tentative, 44 ms) | ✅ |
| 4 | DICO — fichier `-d` introuvable (fallback) | `-m DICO -h 5ebe2294ecd0e0f08eab7690d2a6ee69 -d /chemin/inexistant.txt` | Avertissement + repli sur le dictionnaire par défaut | Avertissement affiché, `Password found: secret` avec le dictionnaire par défaut (50 mots) | ✅ |
| 5 | BRUTE — mot d'1 caractère | `-m BRUTE -h 0cc175b9c0f1b6a831c399e269772661` | `Password found: a` | `Password found: a` (1 tentative, 61 ms) | ✅ |
| 6 | BRUTE — mot de 4 caractères | `-m BRUTE -h 020d69ec2ee5b3f192483936e2c7f561` | `Password found: xkcd` | `Password found: xkcd` (429 342 tentatives, 763 ms) | ✅ |
| 7 | Hash fourni en MAJUSCULES | `-m DICO -h 5EBE2294ECD0E0F08EAB7690D2A6EE69` | Comparaison insensible à la casse, mot trouvé | `Password found: secret` | ✅ |
| 8 | Méthode invalide | `-m XYZ -h 5ebe2294ecd0e0f08eab7690d2a6ee69` | Erreur claire, pas de crash, code de sortie 1 | `Erreur d'arguments : Méthode invalide 'XYZ'...` + usage affiché, exit 1 | ✅ |
| 9 | Hash mal formé (pas 32 caractères hexa) | `-m DICO -h abc123` | Erreur de validation, code de sortie 1 | `Erreur d'arguments : Le hash fourni ('abc123') n'est pas un MD5 valide...`, exit 1 | ✅ |
| 10 | Argument `-h` manquant | `-m DICO` | Erreur "option obligatoire", code de sortie 1 | `Erreur d'arguments : L'option -h (hash) est obligatoire.`, exit 1 | ✅ |
| 11 | Aucun argument fourni | *(rien)* | Erreur "option obligatoire", code de sortie 1 | `Erreur d'arguments : L'option -m (méthode) est obligatoire.`, exit 1 | ✅ |

**Résultat global : 11/11 tests passés.**

## Détail des cas les plus significatifs

### Test 4 — Robustesse du chargement de dictionnaire

Ce test vérifie qu'une erreur d'utilisateur (chemin de fichier invalide passé à `-d`) ne fait pas planter le programme, mais déclenche un repli gracieux (`fallback`) sur le dictionnaire embarqué, avec un avertissement explicite :

```
Fichier dictionnaire introuvable ('/tmp/n_existe_pas.txt'), utilisation du dictionnaire par défaut.
Dictionnaire chargé : 50 mot(s) à tester.
──────────────────────────────────────────
✔ Password found: secret
```

### Test 6 — Force brute sur un mot de longueur maximale

Valide que l'algorithme parcourt bien l'espace de recherche complet (475 254 combinaisons théoriques pour un alphabet a-z et une longueur ≤ 4) et retrouve un mot situé loin dans l'ordre de génération (429 342ᵉ tentative) :

```
Espace de recherche : 475254 combinaisons possibles (alphabet a-z, longueur 1 à 4).
[===========================>  ]  90.34%  (429,342 / 475,254 tentatives)
──────────────────────────────────────────
✔ Password found: xkcd
  Tentatives effectuées : 429,342
  Temps d'exécution     : 763 ms
──────────────────────────────────────────
```

### Tests 8 à 11 — Validation des entrées

Ces quatre tests confirment qu'aucune entrée utilisateur invalide ne provoque de `Exception` non gérée (`StackTrace` visible) : chaque cas est intercepté par `Main.validateArguments()` et se termine par un message clair sur la sortie standard, accompagné d'un rappel de la syntaxe (`printUsage()`), avec un code de sortie `1` signalant l'échec au shell appelant.

## Comment reproduire ces tests

```cmd
:: Compilation
mkdir out
dir /s /b src\main\java\*.java > sources.txt
javac -d out -encoding UTF-8 @sources.txt
copy src\main\resources\dictionnaire.txt out\

:: Test 1
java -cp out com.passwordcracker.Main -m DICO -h 5ebe2294ecd0e0f08eab7690d2a6ee69

:: Test 6
java -cp out com.passwordcracker.Main -m BRUTE -h 020d69ec2ee5b3f192483936e2c7f561

:: Test 8 (vérifier le code de sortie)
java -cp out com.passwordcracker.Main -m XYZ -h 5ebe2294ecd0e0f08eab7690d2a6ee69
echo %errorlevel%
```

## Limites de cette campagne de tests

- Les tests sont **manuels et documentés**, pas automatisés (pas de suite JUnit à ce stade). Une évolution possible consisterait à convertir ces scénarios en tests unitaires JUnit, exécutables directement via `javac`/`java` avec le JAR de JUnit ajouté au classpath.
- La force brute n'a été testée que jusqu'à la longueur maximale imposée par l'énoncé (4 caractères) ; le comportement au-delà n'est pas couvert (hors périmètre du mini-projet).
- Les tests de performance sont indicatifs (dépendants de la machine) et ne constituent pas un benchmark formel.

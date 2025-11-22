# Projet Banque

Application Java pour la gestion et l'analyse des mouvements bancaires.

## Fonctionnalités

- Importation de fichiers CSV contenant des mouvements bancaires
- Calcul des cumuls
- Calcul des débits et crédits mensuels
- Analyse de la fréquence des mouvements
- Calcul des attachés tiers
- Visualisation graphique des historiques

## Structure du projet

- `src/banque/` : Code source principal
  - `Banque.java` : Interface principale
  - `ImportCSV.java` : Importation des données CSV
  - `DBConnection.java` : Gestion de la connexion à la base de données
  - `CalculCumuls.java` : Calcul des cumuls
  - `CalculDebitCreditMensuel.java` : Calcul des débits/crédits mensuels
  - `CalculFrequenceMvts.java` : Analyse de fréquence
  - `CalculAttacheTiers.java` : Calcul des attachés tiers
  - Fichiers graphiques : `BanqueGraph.java`, `GraphHistorique.java`, etc.

## Prérequis

- Java JDK 8 ou supérieur
- Bibliothèque OpenCSV
- Driver JDBC pour la base de données utilisée

## Installation

1. Cloner le dépôt
2. Configurer la base de données dans `DBConnection.java`
3. Compiler le projet
4. Exécuter `Banque.java`

## Licence

[À définir]

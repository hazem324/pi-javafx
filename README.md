# CultureSpaceFX

**CultureSpaceFX** est une application de bureau moderne développée avec **Java 17** et **JavaFX**, utilisant **FXML** pour la conception de l’interface utilisateur. Elle propose une plateforme de **marché** et de **communauté** avec des fonctionnalités telles que la gestion de produits, la tarification dynamique, le vote, et la gestion d’événements. L’interface adopte un thème **vert**, est **responsive**, et inclut des **animations** ainsi qu’une **page d’erreur personnalisée**.

---

## Table des matières

* [Fonctionnalités](#fonctionnalités)
* [Pré-requis](#pré-requis)
* [Installation](#installation)
* [Configuration](#configuration)
* [Utilisation](#utilisation)
* [Structure du projet](#structure-du-projet)
* [Contribuer](#contribuer)
* [Licence](#licence)
* [Remerciements](#remerciements)

---

## Fonctionnalités

* **Gestion des produits** : Créer, modifier, supprimer et lister des produits avec tarification dynamique et système de vote.
* **Authentification utilisateur** : Inscription et connexion sécurisées pour les utilisateurs et administrateurs avec validation.
* **Marketplace** : Parcourir les produits avec des filtres (nom, prix, catégorie, disponibilité, réduction).
* **Communauté** : Listing et interactions de groupes (**en cours de développement**).
* **Événements** : Gestion des événements (**en cours de développement**).
* **Export PDF** : Génération de listes de produits en PDF via des bibliothèques comme **iText** ou **PDFBox**.
* **Interface moderne** : Design à thème vert, animations JavaFX (fondu, rebond) et mise en page responsive.
* **Page d’erreur personnalisée** : Écran d’erreur animé à thème vert pour les routes ou actions invalides.

---

## Pré-requis

* **Java** : version 17 ou supérieure (JDK)
* **Maven** : dernière version pour la gestion des dépendances
* **JavaFX** : SDK 17+ (intégré via Maven)
* **SceneBuilder** : Pour modifier les fichiers FXML (optionnel)
* **MySQL** : version 5.7 ou supérieure (ou autre base de données compatible JDBC)
* **iText / PDFBox** : Pour l’export PDF (optionnel)
* **IDE** : IntelliJ IDEA, Eclipse ou VS Code avec support JavaFX

---

## Installation

### Cloner le dépôt :

```bash
git clone <lien-du-dépôt>
cd CultureSpaceFX
```

### Installer les dépendances :

```bash
mvn clean install
```

---

## Configuration

Configurer la connexion à la base de données dans le fichier :

`src/main/resources/application.properties`

```properties
db.url=jdbc:mysql://localhost:3306/culturespacefx
db.user=ton_utilisateur
db.password=ton_mot_de_passe
```

---

## Utilisation

* Exécuter l’application via votre IDE ou en ligne de commande avec Maven.
* Naviguez dans l’interface pour gérer les produits, accéder au marketplace, et tester les fonctionnalités disponibles.
* D'autres modules (communauté, événements) seront ajoutés prochainement.

---

## Structure du projet

* `src/main/java` : Code source principal (contrôleurs, services, modèles)
* `src/main/resources` : Fichiers FXML, ressources graphiques, et fichiers de configuration
* `pom.xml` : Fichier de configuration Maven

---

## Contribuer

Les contributions sont les bienvenues !
Merci de créer une *issue* ou une *pull request* avec une description claire des modifications.

---

## Licence

Projet sous licence **MIT** (voir le fichier `LICENSE` dans le dépôt).

---

## Remerciements

* Développé avec **Java 17**, **JavaFX** et **FXML**
* Merci à la communauté JavaFX et aux bibliothèques telles que **iText** et **PDFBox**
* Inspiré par des plateformes modernes de type marketplace
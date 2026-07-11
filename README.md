# Nbody Simulation

<div align=center>

![Nbody Logo](/assets/logo.png)

![Java](https://img.shields.io/badge/Java-25-red)
![Spring Boot](https://img.shields.io/badge/SpringBoot-4.1.0-green)
![Version](https://img.shields.io/badge/Version-0.0.1-blue)
[![License: MIT](https://img.shields.io/badge/Licence-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

Un moteur physique en temps réel simulant le problème à N corps, propulsé par Spring Boot et diffusé via MQTT.

![Application image](/assets/application-system-solar.png)
![Application image](/assets/application-saturne.png)

</div>

---

## Table des matières

- [Nbody Simulation](#nbody-simulation)
  - [Table des matières](#table-des-matières)
  - [À propos](#à-propos)
  - [Fonctionnalités](#fonctionnalités)
  - [Stack technologique](#stack-technologique)
  - [Architecture](#architecture)
  - [Démarrage rapide](#démarrage-rapide)
    - [Prérequis](#prérequis)
    - [Démarrage](#démarrage)
  - [Configuration](#configuration)
  - [Prochaines étapes ?](#prochaines-étapes-)
    - [Feuille de route](#feuille-de-route)
  - [Licence](#licence)
  - [Auteur](#auteur)

---

## À propos

Ce projet est une simulation gravitationnelle interactive. Le backend calcule la physique orbitale (positions, vitesses, accélérations) à haute fréquence en appliquant la loi de la gravitation universelle de Newton. L'état de l'univers est ensuite publié via le protocole MQTT. Le frontend "dumb client" s'abonne à ce flux via WebSockets pour un rendu graphique fluide et en direct.

---

## Fonctionnalités

✨ Rendu Temps Réel - Affichage fluide via WebSockets et l'API Canvas HTML5.

🚀 Moteur Physique - Résolution orbitale optimisée avec un facteur d'adoucissement (softening) pour prévenir les explosions numériques.

🎮 Interactivité - Panneau de contrôle complet et ajout d'astres au clic directement depuis le navigateur.

🌌 Scénarios Intégrés - Génération à la volée du système solaire, de systèmes binaires, d'amas galactiques ou de supernovas.

📡 Architecture Orientée Messages - Couplage faible grâce à Spring Integration garantissant une séparation stricte des flux.

---

## Stack technologique

| Composant              | Technologie                                      |
| :--------------------- | :----------------------------------------------- |
| **Backend**            | Java, Spring Boot, Spring Integration            |
| **Frontend**           | HTML5, API Canvas, JavaScript (Vanilla), MQTT.js |
| **Broker de messages** | Eclipse Mosquitto (MQTT + WebSockets)            |

---

## Architecture

Décrivez l'architecture de votre système à haut niveau. Expliquez comment les différents composants travaillent ensemble.

```
┌─────────────────┐       (WebSockets / Port 9001)       ┌──────────────────┐
│                 │◄────────────────────────────────────►│                  │
│   Frontend JS   │                                      │ Mosquitto Broker │
│ (Client Visuel) │◄────────────────────────────────────►│                  │
└─────────────────┘        (MQTT TCP / Port 1883)        └──────────────────┘
                                                                  ▲
                                                                  │
┌─────────────────┐                                               │
│                 │◄──────────────────────────────────────────────┘
│ Backend Spring  │
│(Moteur Physique)│
└─────────────────┘
```

---

## Démarrage rapide

### Prérequis

Avant de commencer, assurez-vous d'avoir installé les éléments suivants :

- **Java 25**
- **Gradle 9.6.1**
- **Mosquitto local**

### Démarrage

1. Configuration Mosquitto : Assurez-vous d'avoir activé les WebSockets dans votre mosquitto.conf :
   listener 1883
   protocol mqtt
   listener 9001
   protocol websockets

2. Backend : Lancez l'application Spring Boot via votre IDE ou avec mvn spring-boot:run.

3. Frontend : Ouvrez simplement le fichier index.html dans n'importe quel navigateur web moderne.

---

## Configuration

Les flux MQTT sont configurables dans votre application.properties ou application.yml :

mqtt.broker-url=tcp://localhost:1883
mqtt.topic.outgoing.simulation=simulation/out
mqtt.topic.incoming.simulation-event-add=simulation/event/add
mqtt.topic.incoming.simulation-event-clear=simulation/event/clear
mqtt.topic.incoming.simulation-preset=simulation/preset

---

## Prochaines étapes ?

Voici quelques fonctionnalités et améliorations prévues pour les prochaines versions :

### Feuille de route

- Intégration de l'algorithme de Verlet pour une meilleure précision mathématique sur le long terme.
- Ajout d'un système de collisions (fusion des masses lors d'un impact).
- Implémentation de l'algorithme Barnes-Hut pour supporter plus de 10 000 corps simultanés sans perte de performance.

---

## Licence

Ce projet est autorisé sous la **licence MIT** - consultez le fichier [LICENSE](./LICENSE) pour plus de détails.

---

## Auteur

**TANGUY Hugo**

- 📧 Email : [hugo.tanguy.pro@gmail.com](hugo.tanguy.pro@gmail.com)

---

**Dernière mise à jour :** Juillet 2026

Fait avec ❤️

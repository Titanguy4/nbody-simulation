package com.titanguy.nbody.configs;

import java.util.List;

import com.titanguy.nbody.models.Body;
import com.titanguy.nbody.models.BodyType;
import com.titanguy.nbody.models.Vector2D;
import com.titanguy.nbody.services.NBodyService;

public class Presets {
    private static final double ASTRONOMICAL_UNIT = 1.496e11;

    private final NBodyService nBodyService;
    private final List<Body> bodies;

    public Presets(NBodyService nBodyService) {
        this.nBodyService = nBodyService;
        this.bodies = nBodyService.getBodies();
    }

    public void setSystemSolar() {
        nBodyService.clearBodies();

        bodies.add(new Body(0, new com.titanguy.nbody.models.BodyType("Sun"), 1.9885e30,
                new com.titanguy.nbody.models.Vector2D(0, 0),
                new com.titanguy.nbody.models.Vector2D(0, 0),
                new com.titanguy.nbody.models.Vector2D(0, 0)));

        bodies.add(new Body(1, new com.titanguy.nbody.models.BodyType("Mercury"), 3.3011e23,
                new com.titanguy.nbody.models.Vector2D(0.387 * ASTRONOMICAL_UNIT, 0),
                new com.titanguy.nbody.models.Vector2D(0, 47_400),
                new com.titanguy.nbody.models.Vector2D(0, 0)));

        bodies.add(new Body(2, new com.titanguy.nbody.models.BodyType("Venus"), 4.8675e24,
                new com.titanguy.nbody.models.Vector2D(0.723 * ASTRONOMICAL_UNIT, 0),
                new com.titanguy.nbody.models.Vector2D(0, 35_000),
                new com.titanguy.nbody.models.Vector2D(0, 0)));

        bodies.add(new Body(3, new com.titanguy.nbody.models.BodyType("Earth"), 5.97237e24,
                new com.titanguy.nbody.models.Vector2D(ASTRONOMICAL_UNIT, 0),
                new com.titanguy.nbody.models.Vector2D(0, 29_780),
                new com.titanguy.nbody.models.Vector2D(0, 0)));

        bodies.add(new Body(4, new com.titanguy.nbody.models.BodyType("Mars"), 6.4171e23,
                new com.titanguy.nbody.models.Vector2D(1.524 * ASTRONOMICAL_UNIT, 0),
                new com.titanguy.nbody.models.Vector2D(0, 24_077),
                new com.titanguy.nbody.models.Vector2D(0, 0)));

        bodies.add(new Body(5, new com.titanguy.nbody.models.BodyType("Jupiter"), 1.8982e27,
                new com.titanguy.nbody.models.Vector2D(5.204 * ASTRONOMICAL_UNIT, 0),
                new com.titanguy.nbody.models.Vector2D(0, 13_070),
                new com.titanguy.nbody.models.Vector2D(0, 0)));

        bodies.add(new Body(6, new com.titanguy.nbody.models.BodyType("Saturn"), 5.6834e26,
                new com.titanguy.nbody.models.Vector2D(9.582 * ASTRONOMICAL_UNIT, 0),
                new com.titanguy.nbody.models.Vector2D(0, 9_680),
                new com.titanguy.nbody.models.Vector2D(0, 0)));

        bodies.add(new Body(7, new com.titanguy.nbody.models.BodyType("Uranus"), 8.6810e25,
                new com.titanguy.nbody.models.Vector2D(19.201 * ASTRONOMICAL_UNIT, 0),
                new com.titanguy.nbody.models.Vector2D(0, 6_800),
                new com.titanguy.nbody.models.Vector2D(0, 0)));

        bodies.add(new Body(8, new com.titanguy.nbody.models.BodyType("Neptune"), 1.02413e26,
                new com.titanguy.nbody.models.Vector2D(30.047 * ASTRONOMICAL_UNIT, 0),
                new com.titanguy.nbody.models.Vector2D(0, 5_430),
                new com.titanguy.nbody.models.Vector2D(0, 0)));
    }

    public void setSuperNova() {
        nBodyService.clearBodies();

        // 1. Le résidu central (Une naine blanche super massive)
        this.bodies.add(new Body(
                0, new BodyType("STAR"), 2.0e30,
                new Vector2D(0, 0), new Vector2D(0, 0), new Vector2D(0, 0)));

        // 2. L'explosion : 60 débris propulsés à 80 000 m/s
        int debrisCount = 60;
        double explosionSpeed = 80000.0;
        double radius = 1.0e10; // Placés très près de l'étoile au départ

        for (int i = 1; i <= debrisCount; i++) {
            // Répartition en cercle parfait grâce à la trigonométrie
            double angle = i * (2 * Math.PI / debrisCount);

            double x = Math.cos(angle) * radius;
            double y = Math.sin(angle) * radius;

            // La vitesse pointe directement vers l'extérieur
            double vx = Math.cos(angle) * explosionSpeed;
            double vy = Math.sin(angle) * explosionSpeed;

            this.bodies.add(new Body(
                    i, new BodyType("ASTEROID"), 1.0e23,
                    new Vector2D(x, y), new Vector2D(vx, vy), new Vector2D(0, 0)));
        }
    }

    public void setGalaxyCluster() {
        nBodyService.clearBodies();
        double g = 6.67430e-11;

        // 1. Le "Trou Noir" central pour retenir la galaxie
        double centerMass = 1.0e32; // Beaucoup plus lourd que notre soleil
        this.bodies.add(new Body(
                0, new BodyType("STAR"), centerMass,
                new Vector2D(0, 0), new Vector2D(0, 0), new Vector2D(0, 0)));

        // 2. Le nuage de 150 planètes/astéroïdes
        int bodyCount = 150;

        for (int i = 1; i <= bodyCount; i++) {
            // Position aléatoire en cercle
            double angle = Math.random() * 2 * Math.PI;
            // Distance aléatoire entre 50 millions et 1,5 milliard de km
            double distance = 5.0e10 + (Math.random() * 1.5e12);

            double x = Math.cos(angle) * distance;
            double y = Math.sin(angle) * distance;

            // Calcul de la vitesse orbitale parfaite
            double orbitalVelocity = Math.sqrt((g * centerMass) / distance);

            // Vecteur perpendiculaire au centre (pour tourner en rond)
            // On ajoute un tout petit peu d'aléatoire pour que les orbites soient
            // elliptiques et chaotiques
            double vx = -Math.sin(angle) * orbitalVelocity * (0.8 + Math.random() * 0.4);
            double vy = Math.cos(angle) * orbitalVelocity * (0.8 + Math.random() * 0.4);

            // Une chance sur deux d'être une planète ou un astéroïde pour varier les
            // couleurs
            String type = Math.random() > 0.5 ? "PLANET" : "ASTEROID";

            this.bodies.add(new Body(
                    i, new BodyType(type), 1.0e23,
                    new Vector2D(x, y), new Vector2D(vx, vy), new Vector2D(0, 0)));
        }
    }

    public void setBinarySystem() {
        nBodyService.clearBodies();
        double g = 6.67430e-11;

        // Les deux étoiles jumelles
        double starMass = 2.0e30;
        double distance = 1.5e11; // Distance du centre (1 UA)

        // Vitesse orbitale pour un système binaire parfait
        double starVelocity = Math.sqrt((g * starMass) / (4 * distance));

        // Étoile 1 (à gauche, descend)
        this.bodies.add(new Body(
                1, new BodyType("STAR"), starMass,
                new Vector2D(-distance, 0), new Vector2D(0, -starVelocity), new Vector2D(0, 0)));

        // Étoile 2 (à droite, monte)
        this.bodies.add(new Body(
                2, new BodyType("STAR"), starMass,
                new Vector2D(distance, 0), new Vector2D(0, starVelocity), new Vector2D(0, 0)));

        // Ajout de 5 planètes en orbite "circumbinaire" (très loin autour des deux
        // étoiles)
        for (int i = 3; i <= 7; i++) {
            double planetDist = distance * (i * 2); // De plus en plus loin
            // La masse perçue par la planète est la somme des deux étoiles
            double planetVel = Math.sqrt((g * (starMass * 2)) / planetDist);

            this.bodies.add(new Body(
                    i, new BodyType("PLANET"), 5.0e24,
                    new Vector2D(0, planetDist), new Vector2D(planetVel, 0), new Vector2D(0, 0)));
        }
    }

    public void setPlanetaryRing() {
        nBodyService.clearBodies();
        double g = 6.67430e-11;

        // La Géante Gazeuse centrale (10 fois Jupiter)
        double centerMass = 1.898e28;
        this.bodies.add(new Body(
                0, new BodyType("PLANET"), centerMass,
                new Vector2D(0, 0), new Vector2D(0, 0), new Vector2D(0, 0)));

        // L'anneau composé de 200 astéroïdes
        int ringParticles = 200;
        double minRadius = 1.0e11; // Bord interne de l'anneau
        double maxRadius = 1.8e11; // Bord externe de l'anneau

        for (int i = 1; i <= ringParticles; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double radius = minRadius + (Math.random() * (maxRadius - minRadius));

            double x = Math.cos(angle) * radius;
            double y = Math.sin(angle) * radius;

            // Orbite parfaitement circulaire pour chaque débris
            double velocity = Math.sqrt((g * centerMass) / radius);
            double vx = -Math.sin(angle) * velocity;
            double vy = Math.cos(angle) * velocity;

            this.bodies.add(new Body(
                    i, new BodyType("ASTEROID"), 1.0e15, // Poids plume
                    new Vector2D(x, y), new Vector2D(vx, vy), new Vector2D(0, 0)));
        }
    }

    public void setGalaxyCollision() {
        nBodyService.clearBodies();

        // Création de la Galaxie A (à gauche, fonce vers la droite)
        createMiniGalaxy(100, -2.5e12, 0, 15000, 1);

        // Création de la Galaxie B (à droite, fonce vers la gauche)
        createMiniGalaxy(100, 2.5e12, 5.0e11, -15000, 200);
    }

    // Une méthode utilitaire secrète pour générer une galaxie n'importe où
    private void createMiniGalaxy(int starCount, double offsetX, double offsetY, double driftVelocityX, int startId) {
        double g = 6.67430e-11;
        double blackHoleMass = 5.0e31;

        // Le Trou Noir
        this.bodies.add(new Body(
                startId, new BodyType("STAR"), blackHoleMass,
                new Vector2D(offsetX, offsetY), new Vector2D(driftVelocityX, 0), new Vector2D(0, 0)));

        // Les étoiles
        for (int i = 1; i <= starCount; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double distance = 1.0e11 + (Math.random() * 8.0e11);

            double x = offsetX + Math.cos(angle) * distance;
            double y = offsetY + Math.sin(angle) * distance;

            double orbitalVelocity = Math.sqrt((g * blackHoleMass) / distance);

            // La vitesse est = Vitesse orbitale + Vitesse de dérive de la galaxie (Drift)
            double vx = driftVelocityX + (-Math.sin(angle) * orbitalVelocity);
            double vy = Math.cos(angle) * orbitalVelocity;

            this.bodies.add(new Body(
                    startId + i, new BodyType("STAR"), 2.0e28,
                    new Vector2D(x, y), new Vector2D(vx, vy), new Vector2D(0, 0)));
        }
    }
}

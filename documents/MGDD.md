# MGDD - Heart of the Void
## Mechanical Game Design Document

---

## 1. Vue d'ensemble

**Genre:** Tower Defense / Lane Defense  
**Concept:** Défense de base en 2D. Déployer des unités pour détruire la base ennemie.

**Objectif:** Détruire la base ennemie avant que la vôtre soit détruite.

---

## 2. Système d'Énergie

**Énergie Initiale:** 80  
**Maximum:** 150  
**Régénération:** +1 toutes les 0.5s  
**Gains:** 8-25 par ennemi tué

---

## 3. Unités Alliées

| Unité | Coût | HP | Dégâts | Att/s | Portée | Rôle |
|-------|------|----|----|-------|--------|------|
| Knight | 40 | 80 | 25 | 1.5 | 100 | Mêlée polyvalent |
| Void Vessel | 60 | 60 | 30 | 2.0 | 120 | DPS distance |
| Hornet | 85 | 50 | 35 | 2.5 | 150 | Rapide longue portée |
| God Void | 150 | 120 | 50 | 3.0 | 140 | Boss puissant |

---

## 4. Ennemis

| Type | HP | Dégâts | Att/s | Portée | Récompense |
|------|----|----|-------|--------|------------|
| Husk | 40 | 10 | 1.2 | 70 | 8 |
| Vessel | 60 | 15 | 1.0 | 80 | 12 |
| Vengefly | 30 | 18 | 2.5 | 90 | 15 |
| Radiance | 100 | 25 | 0.8 | 100 | 25 |

*Statistiques × multiplicateur de difficulté*

**Spawn:** Automatique avec intervalle décroissant (2.0s → 0.8s)

---

## 5. Niveaux

1. **City of Tears** - Difficulté ×1.0 - Base joueur: 500 HP
2. **Radiance Arena** - Difficulté ×1.2 - Base joueur: 400 HP
3. **Nightmare Realm** - Difficulté ×1.5 - Base joueur: 1 HP (défi)

---

## 6. Mécaniques de Combat

- Déplacement automatique vers la base ennemie
- Détection d'ennemis dans la portée
- Attaque automatique avec cooldown
- Projectiles pour unités à distance
- Barres de vie colorées (vert/jaune/rouge)

---

## 7. Contrôles

**Clavier:** 1-4 (sélection), Espace (pause), R (restart), Échap (menu)  
**Souris:** Clic pour déployer unité

---

## 8. Formules Clés

**Récompense:** `Base × (1 + Vague × 0.1)`  
**Intervalle Spawn:** `max(0.6, BaseInterval - Difficulté × 0.3)`  
**Stats Ennemis:** `Stat × Multiplicateur Niveau`

---

## 9. Interface

- **En haut à gauche:** Icône + Énergie actuelle/max
- **En haut au centre:** 4 cartes d'unités avec coût
- **Au-dessus des entités:** Barres de vie
- **En bas à droite:** Aide contrôles

---

## 10. Boucle de Jeu

1. Initialisation (bases, énergie, musique)
2. Gameplay (spawn ennemis, déploiement alliés, combat)
3. Fin (victoire/défaite, score, options)

---

**Version:** 1.0 - 23 Novembre 2025

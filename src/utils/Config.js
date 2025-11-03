export const GAME_CONFIG = {
    CANVAS_WIDTH: 1200,
    CANVAS_HEIGHT: 600,
    
    INITIAL_ENERGY: 100,
    MAX_ENERGY: 200,
    ENERGY_REGEN_RATE: 1,
    
    PLAYER_BASE_HP: 1000,
    ENEMY_BASE_HP: 1000,
    BASE_WIDTH: 40,
    BASE_HEIGHT: 100,
    
    ENEMY_SPAWN_INTERVAL: 3,
    ENEMY_SPAWN_X: 1150,
    
    DEFAULT_GAME_SPEED: 1.0,
    MIN_GAME_SPEED: 0.5,
    MAX_GAME_SPEED: 3.0,
    
    COLORS: {
        PLAYER_BASE: '#2196F3',
        ENEMY_BASE: '#F44336',
        ALLY: '#4CAF50',
        ENEMY: '#F44336',
        BACKGROUND: '#111',
        UI_BACKGROUND: 'rgba(0, 0, 0, 0.7)',
        TEXT: '#FFFFFF',
        ENERGY_BAR: {
            BACKGROUND: '#333',
            GRADIENT_START: '#00ff00',
            GRADIENT_MID: '#ffff00',
            GRADIENT_END: '#ff0000'
        }
    }
};

export const UNIT_TEMPLATES = {
    ALLIES: {
        basic: {
            name: 'Guerrier',
            hp: 50,
            speed: 30,
            damage: 15,
            range: 80,
            attackCooldown: 1.5,
            deploymentCost: 75,
            description: 'Unité équilibrée, bon rapport qualité/prix'
        },
        tank: {
            name: 'Tank',
            hp: 150,
            speed: 15,
            damage: 10,
            range: 60,
            attackCooldown: 2.0,
            deploymentCost: 125,
            description: 'Très résistant, parfait pour bloquer les ennemis'
        },
        archer: {
            name: 'Archer',
            hp: 30,
            speed: 25,
            damage: 25,
            range: 150,
            attackCooldown: 1.0,
            deploymentCost: 100,
            description: 'Attaque à longue portée, fragile mais puissant'
        },
        healer: {
            name: 'Soigneur',
            hp: 40,
            speed: 20,
            damage: 5,
            range: 100,
            attackCooldown: 3.0,
            deploymentCost: 150,
            description: 'Soigne les alliés proches',
            special: 'heal'
        },
        assassin: {
            name: 'Assassin',
            hp: 25,
            speed: 50,
            damage: 30,
            range: 50,
            attackCooldown: 0.8,
            deploymentCost: 90,
            description: 'Très rapide, attaque fréquemment'
        }
    },
    
    ENEMIES: {
        grunt: {
            name: 'Fantassin',
            hp: 40,
            speed: 25,
            damage: 12,
            range: 70,
            attackCooldown: 1.8,
            energyReward: 5
        },
        heavy: {
            name: 'Lourd',
            hp: 120,
            speed: 12,
            damage: 20,
            range: 65,
            attackCooldown: 2.5,
            energyReward: 12
        },
        fast: {
            name: 'Rapide',
            hp: 25,
            speed: 40,
            damage: 8,
            range: 60,
            attackCooldown: 1.2,
            energyReward: 3
        },
        boss: {
            name: 'Boss',
            hp: 300,
            speed: 8,
            damage: 35,
            range: 100,
            attackCooldown: 3.0,
            energyReward: 50
        }
    }
};

export const UI_CONFIG = {
    ENERGY_BAR: {
        WIDTH: 200,
        HEIGHT: 20,
        X: 20,
        Y: 20
    },
    
    UNIT_BUTTONS: {
        WIDTH: 120,
        HEIGHT: 30,
        X: 20,
        FIRST_Y: 60,
        SPACING: 40
    },
    
    INSTRUCTIONS_PANEL: {
        WIDTH: 250,
        MARGIN: 20
    },
    
    FONTS: {
        DEFAULT: '12px Arial',
        TITLE: '14px Arial',
        LARGE: '16px Arial',
        DEBUG: '10px Arial'
    }
};

export const GAME_STATES = {
    MENU: 'menu',
    PLAYING: 'playing',
    PAUSED: 'paused',
    GAME_OVER: 'game_over',
    VICTORY: 'victory'
};

export const ENTITY_TYPES = {
    ALLY: 'ally',
    ENEMY: 'enemy',
    PROJECTILE: 'projectile',
    EFFECT: 'effect'
};

export const DIFFICULTY_LEVELS = {
    EASY: {
        name: 'Facile',
        enemySpawnMultiplier: 0.7,
        enemyHealthMultiplier: 0.8,
        energyRegenMultiplier: 1.2
    },
    NORMAL: {
        name: 'Normal',
        enemySpawnMultiplier: 1.0,
        enemyHealthMultiplier: 1.0,
        energyRegenMultiplier: 1.0
    },
    HARD: {
        name: 'Difficile',
        enemySpawnMultiplier: 1.5,
        enemyHealthMultiplier: 1.3,
        energyRegenMultiplier: 0.8
    }
};

export const MESSAGES = {
    VICTORY: 'Victoire ! Vous avez détruit la base ennemie !',
    DEFEAT: 'Défaite ! Votre base a été détruite !',
    INSUFFICIENT_ENERGY: 'Énergie insuffisante !',
    UNIT_DEPLOYED: 'Unité déployée !',
    GAME_PAUSED: 'Jeu en pause',
    LOADING: 'Chargement...'
};

export const KEYBINDINGS = {
    PAUSE: ' ',
    UNIT_1: '1',
    UNIT_2: '2',
    UNIT_3: '3',
    HELP: 'h',
    SPEED_UP: '+',
    SPEED_DOWN: '-',
    RESET: 'r'
};
import Ally from '../entities/Ally.js';
import Enemy from '../entities/Enemy.js';
import GameUI from '../ui/GameUI.js';

class Game {
    constructor(canvasId) {
        this.id = this.generateId();
        this.canvas = document.getElementById(canvasId);
        this.ctx = this.canvas.getContext('2d');
        
        this.energy = 150;
        this.maxEnergy = 300;
        this.energyRegenRate = 3;
        
        this.playerBaseId = this.generateId();
        this.enemyBaseId = this.generateId();
        this.playerBaseHp = 1000;
        this.enemyBaseHp = 1000;
        this.maxBaseHp = 1000;
        
        this.allies = new Map();
        this.enemies = new Map();
        this.allEntities = new Map();
        
        this.gameUI = new GameUI(this.generateId(), this.energy, this.maxEnergy);
        this.gameUI.selectedUnit = 'basic';
        
        this.isRunning = false;
        this.isPaused = false;
        this.gameSpeed = 1.0;
        this.lastFrameTime = 0;
        
        this.energyNotifications = [];
        
        this.allyTemplates = this.initializeAllyTemplates();
        this.enemyTemplates = this.initializeEnemyTemplates();
        
        this.battleLineY = this.canvas.height / 2;
        
        this.enemySpawnTimer = 0;
        this.enemySpawnInterval = 2;
        
        this.setupEventListeners();
        this.setupCanvas();
    }

    initializeAllyTemplates() {
        return {
            basic: {
                hp: 60,
                speed: 40,
                damage: 20,
                range: 100,
                attackCooldown: 1.0,
                deploymentCost: 50
            },
            tank: {
                hp: 120,
                speed: 20,
                damage: 15,
                range: 80,
                attackCooldown: 1.5,
                deploymentCost: 80
            },
            archer: {
                hp: 40,
                speed: 35,
                damage: 30,
                range: 180,
                attackCooldown: 0.8,
                deploymentCost: 70
            }
        };
    }

    initializeEnemyTemplates() {
        return {
            grunt: {
                hp: 40,
                speed: 25,
                damage: 12,
                range: 70,
                attackCooldown: 1.8
            },
            heavy: {
                hp: 120,
                speed: 12,
                damage: 20,
                range: 65,
                attackCooldown: 2.5
            }
        };
    }

    setupCanvas() {
        this.canvas.width = 1200;
        this.canvas.height = 600;
        
        this.renderBases();
    }

    setupEventListeners() {
        this.canvas.addEventListener('click', (e) => this.handleCanvasClick(e));
        
        document.addEventListener('keydown', (e) => this.handleKeyDown(e));
        
        this.selectedUnitType = 'basic';
    }

    handleCanvasClick(event) {
        if (!this.isRunning) return;

        const rect = this.canvas.getBoundingClientRect();
        const x = event.clientX - rect.left;

        if (x < this.canvas.width / 2) {
            this.deployAlly(this.selectedUnitType || 'basic', 80, this.battleLineY);
        }
    }

    handleKeyDown(event) {
        switch(event.key) {
            case ' ':
                event.preventDefault();
                this.togglePause();
                break;
            case '1':
                this.selectedUnitType = 'basic';
                break;
            case '2':
                this.selectedUnitType = 'tank';
                break;
            case '3':
                this.selectedUnitType = 'archer';
                break;
        }
    }

    deployAlly(type, x, y) {
        const template = this.allyTemplates[type];
        if (!template || this.energy < template.deploymentCost) {
            return false;
        }

        const ally = new Ally(
            this.generateId(),
            x,
            y,
            template.hp,
            template.speed,
            template.damage,
            template.range,
            template.attackCooldown,
            template.deploymentCost
        );

        this.allies.set(ally.id, ally);
        this.allEntities.set(ally.id, ally);
        this.energy -= template.deploymentCost;

        return true;
    }

    spawnEnemy(type) {
        const template = this.enemyTemplates[type];
        if (!template) return;

        const enemy = new Enemy(
            this.generateId(),
            this.canvas.width - 80,
            this.battleLineY,
            template.hp,
            template.speed,
            template.damage,
            template.range,
            template.attackCooldown
        );

        this.enemies.set(enemy.id, enemy);
        this.allEntities.set(enemy.id, enemy);
    }

    update(deltaTime) {
        if (!this.isRunning || this.isPaused) return;

        deltaTime *= this.gameSpeed;

        this.energy += this.energyRegenRate * deltaTime;
        if (this.energy > this.maxEnergy) {
            this.energy = this.maxEnergy;
        }

        this.enemySpawnTimer += deltaTime;
        if (this.enemySpawnTimer >= this.enemySpawnInterval) {
            this.spawnEnemy(Math.random() > 0.5 ? 'grunt' : 'heavy');
            this.enemySpawnTimer = 0;
        }

        this.updateEntities(deltaTime);
        this.updateTargeting();
        this.cleanupDeadEntities();
        this.updateEnergyNotifications(deltaTime);
        this.gameUI.update(this.energy, this.maxEnergy);
    }

    updateEntities(deltaTime) {
        for (const entity of this.allEntities.values()) {
            entity.findTargetById = (id) => this.allEntities.get(id);
            entity.update(deltaTime);
        }
    }

    updateTargeting() {
        for (const ally of this.allies.values()) {
            if (!ally.isAlive) continue;

            let closestEnemy = null;
            let closestDistance = Infinity;

            for (const enemy of this.enemies.values()) {
                if (!enemy.isAlive) continue;

                const distance = ally.distanceTo(enemy);
                if (distance < closestDistance && distance <= ally.range) {
                    closestDistance = distance;
                    closestEnemy = enemy;
                }
            }

            ally.setTarget(closestEnemy);
        }

        for (const enemy of this.enemies.values()) {
            if (!enemy.isAlive) continue;

            let closestAlly = null;
            let closestDistance = Infinity;

            for (const ally of this.allies.values()) {
                if (!ally.isAlive) continue;

                const distance = enemy.distanceTo(ally);
                if (distance < closestDistance && distance <= enemy.range) {
                    closestDistance = distance;
                    closestAlly = ally;
                }
            }

            enemy.setTarget(closestAlly);
        }
    }

    cleanupDeadEntities() {
        for (const [id, entity] of this.allEntities.entries()) {
            if (!entity.isAlive) {
                if (this.enemies.has(id)) {
                    const energyReward = entity.getEnergyReward ? entity.getEnergyReward() : 15;
                    this.energy += energyReward;
                    if (this.energy > this.maxEnergy) {
                        this.energy = this.maxEnergy;
                    }
                    
                    this.addEnergyNotification(entity.x, entity.y, energyReward);
                }
                
                this.allEntities.delete(id);
                this.allies.delete(id);
                this.enemies.delete(id);
            }
        }
    }

    render() {
        this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);

        this.renderBackground();
        this.renderBases();

        for (const entity of this.allEntities.values()) {
            entity.render(this.ctx);
        }

        this.renderEnergyNotifications();
        this.gameUI.render(this.ctx, this.canvas.width, this.canvas.height);
        this.renderDebugInfo();
    }

    renderBackground() {
        this.ctx.strokeStyle = '#333';
        this.ctx.lineWidth = 2;
        this.ctx.setLineDash([10, 10]);
        this.ctx.beginPath();
        this.ctx.moveTo(this.canvas.width / 2, 0);
        this.ctx.lineTo(this.canvas.width / 2, this.canvas.height);
        this.ctx.stroke();
        this.ctx.setLineDash([]);
        
        this.ctx.strokeStyle = '#9370DB';
        this.ctx.lineWidth = 3;
        this.ctx.setLineDash([]);
        this.ctx.beginPath();
        this.ctx.moveTo(0, this.battleLineY);
        this.ctx.lineTo(this.canvas.width, this.battleLineY);
        this.ctx.stroke();
        
        this.ctx.fillStyle = '#9370DB';
        this.ctx.font = '14px Arial';
        this.ctx.fillText('FRONTIÈRE DU VIDE', this.canvas.width / 2 - 70, this.battleLineY - 10);
    }

    renderBases() {
        this.ctx.fillStyle = '#4A0E4E';
        this.ctx.fillRect(10, this.canvas.height / 2 - 50, 40, 100);
        
        this.ctx.fillStyle = '#8B0000';
        this.ctx.fillRect(this.canvas.width - 50, this.canvas.height / 2 - 50, 40, 100);
    }

    renderDebugInfo() {
        this.ctx.fillStyle = 'white';
        this.ctx.font = '14px Arial';
        this.ctx.fillText(`Gardiens: ${this.allies.size}`, 10, 30);
        this.ctx.fillText(`Corrompus: ${this.enemies.size}`, 10, 50);
        this.ctx.fillText(`Essence: ${Math.floor(this.energy)}/${this.maxEnergy}`, 10, 70);
        
        if (this.isPaused) {
            this.ctx.fillStyle = 'yellow';
            this.ctx.font = '24px Arial';
            this.ctx.fillText('PAUSE', this.canvas.width / 2 - 50, this.canvas.height / 2);
        }
    }

    start() {
        this.isRunning = true;
        this.lastFrameTime = performance.now();
        this.gameLoop();
    }

    stop() {
        this.isRunning = false;
    }

    togglePause() {
        this.isPaused = !this.isPaused;
    }

    gameLoop() {
        if (!this.isRunning) return;

        const currentTime = performance.now();
        const deltaTime = (currentTime - this.lastFrameTime) / 1000;
        this.lastFrameTime = currentTime;

        this.update(deltaTime);
        this.render();

        requestAnimationFrame(() => this.gameLoop());
    }

    addEnergyNotification(x, y, amount) {
        this.energyNotifications.push({
            x: x,
            y: y,
            amount: amount,
            life: 1.0, // secondes
            maxLife: 1.0
        });
    }

    updateEnergyNotifications(deltaTime) {
        for (let i = this.energyNotifications.length - 1; i >= 0; i--) {
            const notification = this.energyNotifications[i];
            notification.life -= deltaTime;
            notification.y -= 30 * deltaTime;
            
            if (notification.life <= 0) {
                this.energyNotifications.splice(i, 1);
            }
        }
    }

    renderEnergyNotifications() {
        for (const notification of this.energyNotifications) {
            const alpha = notification.life / notification.maxLife;
            this.ctx.save();
            this.ctx.globalAlpha = alpha;
            this.ctx.fillStyle = '#00FF00';
            this.ctx.font = 'bold 16px Arial';
            this.ctx.fillText(`+${notification.amount}`, notification.x, notification.y);
            this.ctx.restore();
        }
    }

    generateId() {
        return Date.now().toString(36) + Math.random().toString(36).substr(2);
    }
}

export default Game;
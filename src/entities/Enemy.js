import Entity from './Entity.js';

class Enemy extends Entity {
    constructor(id, x, y, hp, speed, damage, range, attackCooldown) {
        super(id, x, y, hp, speed, damage, range, attackCooldown);
        this.direction = -1;
        this.isMoving = true;
        this.spawnTime = Date.now();
    }

    move(deltaTime) {
        if (!this.isAlive || !this.isMoving) return;

        if (!this.target) {
            this.x += this.speed * this.direction * deltaTime;
        }
    }

    update(deltaTime) {
        super.update(deltaTime);

        if (this.target && this.isInRange(this.target)) {
            this.isMoving = false;
        } else {
            this.isMoving = true;
        }
    }

    getColor() {
        return '#8B0000';
    }

    render(ctx) {
        if (!this.isAlive) return;

        ctx.fillStyle = this.getColor();
        ctx.beginPath();
        ctx.moveTo(this.x, this.y - 15);
        ctx.lineTo(this.x - 13, this.y + 10);
        ctx.lineTo(this.x + 13, this.y + 10);
        ctx.closePath();
        ctx.fill();

        ctx.fillStyle = '#660000';
        ctx.beginPath();
        ctx.moveTo(this.x - 10, this.y);
        ctx.lineTo(this.x - 5, this.y - 5);
        ctx.lineTo(this.x - 5, this.y + 5);
        ctx.closePath();
        ctx.fill();

        this.renderHealthBar(ctx);

        if (this.target) {
            ctx.strokeStyle = 'rgba(255, 0, 0, 0.3)';
            ctx.beginPath();
            ctx.arc(this.x, this.y, this.range, 0, 2 * Math.PI);
            ctx.stroke();
        }
    }

    getEnergyReward() {
        return Math.floor(this.maxHp / 4) + 10;
    }

    isOffScreen(screenWidth) {
        return this.x < -50;
    }
}

export default Enemy;
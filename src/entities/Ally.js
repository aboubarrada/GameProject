import Entity from './Entity.js';

class Ally extends Entity {
    constructor(id, x, y, hp, speed, damage, range, attackCooldown, deploymentCost) {
        super(id, x, y, hp, speed, damage, range, attackCooldown);
        this.deploymentCost = deploymentCost;
        this.direction = 1;
        this.isMoving = true;
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
        return '#6A0DAD';
    }

    render(ctx) {
        if (!this.isAlive) return;

        ctx.fillStyle = this.getColor();
        ctx.beginPath();
        ctx.arc(this.x, this.y, 15, 0, 2 * Math.PI);
        ctx.fill();

        ctx.fillStyle = '#4B0082';
        ctx.beginPath();
        ctx.moveTo(this.x + 10, this.y);
        ctx.lineTo(this.x + 5, this.y - 5);
        ctx.lineTo(this.x + 5, this.y + 5);
        ctx.closePath();
        ctx.fill();

        this.renderHealthBar(ctx);

        if (this.target) {
            ctx.strokeStyle = 'rgba(138, 43, 226, 0.3)';
            ctx.beginPath();
            ctx.arc(this.x, this.y, this.range, 0, 2 * Math.PI);
            ctx.stroke();
        }
    }

    clone(newId, x, y) {
        return new Ally(
            newId,
            x,
            y,
            this.maxHp,
            this.speed,
            this.damage,
            this.range,
            this.attackCooldown,
            this.deploymentCost
        );
    }
}

export default Ally;
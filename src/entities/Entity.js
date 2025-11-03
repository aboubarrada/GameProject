class Entity {
    constructor(id, x, y, hp, speed, damage, range, attackCooldown) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.hp = hp;
        this.maxHp = hp;
        this.speed = speed;
        this.damage = damage;
        this.range = range;
        this.attackCooldown = attackCooldown; // temps en secondes
        this.lastAttackTime = 0;
        this.isAlive = true;
        this.target = null;
    }

    update(deltaTime) {
        if (!this.isAlive) return;
        if (this.lastAttackTime > 0) {
            this.lastAttackTime -= deltaTime;
            if (this.lastAttackTime < 0) {
                this.lastAttackTime = 0;
            }
        }

        this.move(deltaTime);
        if (this.target && this.canAttack()) {
            this.attack(this.target.id);
        }
    }

    move(deltaTime) {
    }

    render(ctx) {
        if (!this.isAlive) return;

        ctx.fillStyle = this.getColor();
        ctx.fillRect(this.x - 15, this.y - 15, 30, 30);
        this.renderHealthBar(ctx);
    }

    renderHealthBar(ctx) {
        const barWidth = 30;
        const barHeight = 4;
        const healthPercentage = this.hp / this.maxHp;

        ctx.fillStyle = 'red';
        ctx.fillRect(this.x - barWidth/2, this.y - 25, barWidth, barHeight);
        ctx.fillStyle = 'green';
        ctx.fillRect(this.x - barWidth/2, this.y - 25, barWidth * healthPercentage, barHeight);
    }

    attack(targetId) {
        if (!this.canAttack()) return;

        const target = this.findTargetById(targetId);
        if (target && this.isInRange(target)) {
            target.takeDamage(this.damage);
            this.lastAttackTime = this.attackCooldown;
        }
    }

    canAttack() {
        return this.lastAttackTime <= 0 && this.isAlive;
    }

    isInRange(target) {
        const distance = Math.sqrt(
            Math.pow(this.x - target.x, 2) + 
            Math.pow(this.y - target.y, 2)
        );
        return distance <= this.range;
    }

    takeDamage(damage) {
        this.hp -= damage;
        if (this.hp <= 0) {
            this.hp = 0;
            this.isAlive = false;
        }
    }

    findTargetById(targetId) {
        return null;
    }

    getColor() {
        return 'gray';
    }

    setTarget(target) {
        this.target = target;
    }

    distanceTo(other) {
        return Math.sqrt(
            Math.pow(this.x - other.x, 2) + 
            Math.pow(this.y - other.y, 2)
        );
    }
}

export default Entity;
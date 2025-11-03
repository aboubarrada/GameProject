class GameUI {
    constructor(id, initialEnergy, maxEnergy) {
        this.id = id;
        this.energyBarId = `energyBar_${id}`;
        this.energy = initialEnergy;
        this.maxEnergy = maxEnergy;
        
        this.energyBarWidth = 200;
        this.energyBarHeight = 20;
        this.energyBarX = 20;
        this.energyBarY = 20;
        this.unitButtons = [
            {
                id: 'basic',
                name: 'Gardien (50)',
                key: '1',
                cost: 50,
                x: 20,
                y: 60,
                width: 120,
                height: 30,
                color: '#6A0DAD'
            },
            {
                id: 'tank',
                name: 'Colosse (80)',
                key: '2',
                cost: 80,
                x: 20,
                y: 100,
                width: 120,
                height: 30,
                color: '#4B0082'
            },
            {
                id: 'archer',
                name: 'Sentinelle (70)',
                key: '3',
                cost: 70,
                x: 20,
                y: 140,
                width: 120,
                height: 30,
                color: '#8A2BE2'
            }
        ];

        this.selectedUnit = 'basic';
        this.showInstructions = true;
    }

    update(energy, maxEnergy) {
        this.energy = energy;
        this.maxEnergy = maxEnergy;
    }

    render(ctx, canvasWidth, canvasHeight) {
        ctx.save();
        ctx.fillStyle = 'rgba(0, 0, 0, 0.7)';
        ctx.fillRect(0, 0, 180, 200);

        this.renderEnergyBar(ctx);

        this.renderUnitButtons(ctx);

        if (this.showInstructions) {
            this.renderInstructions(ctx, canvasWidth, canvasHeight);
        }
        ctx.restore();
    }

    renderEnergyBar(ctx) {
        const energyPercentage = this.energy / this.maxEnergy;

        ctx.fillStyle = 'white';
        ctx.font = '14px Arial';
        ctx.fillText('Essence du Vide', this.energyBarX, this.energyBarY - 5);

        ctx.fillStyle = '#333';
        ctx.fillRect(this.energyBarX, this.energyBarY, this.energyBarWidth, this.energyBarHeight);

        const gradient = ctx.createLinearGradient(
            this.energyBarX, 0, 
            this.energyBarX + this.energyBarWidth, 0
        );
        gradient.addColorStop(0, '#00ff00');
        gradient.addColorStop(0.5, '#ffff00');
        gradient.addColorStop(1, '#ff0000');

        ctx.fillStyle = gradient;
        ctx.fillRect(
            this.energyBarX, 
            this.energyBarY, 
            this.energyBarWidth * energyPercentage, 
            this.energyBarHeight
        );

        ctx.strokeStyle = 'white';
        ctx.lineWidth = 2;
        ctx.strokeRect(this.energyBarX, this.energyBarY, this.energyBarWidth, this.energyBarHeight);
        ctx.fillStyle = 'white';
        ctx.font = '12px Arial';
        ctx.fillText(
            `${Math.floor(this.energy)}/${this.maxEnergy}`, 
            this.energyBarX + this.energyBarWidth + 10, 
            this.energyBarY + this.energyBarHeight - 5
        );
    }

    renderUnitButtons(ctx) {
        for (const button of this.unitButtons) {
            const canAfford = this.energy >= button.cost;
            const isSelected = this.selectedUnit === button.id;

            if (isSelected) {
                ctx.fillStyle = button.color;
            } else if (canAfford) {
                ctx.fillStyle = this.lightenColor(button.color, 0.3);
            } else {
                ctx.fillStyle = '#666';
            }

            ctx.fillRect(button.x, button.y, button.width, button.height);

            ctx.strokeStyle = isSelected ? 'white' : (canAfford ? '#ccc' : '#444');
            ctx.lineWidth = isSelected ? 3 : 1;
            ctx.strokeRect(button.x, button.y, button.width, button.height);

            ctx.fillStyle = canAfford ? 'white' : '#999';
            ctx.font = '12px Arial';
            ctx.fillText(button.name, button.x + 5, button.y + 20);
            ctx.fillStyle = 'yellow';
            ctx.font = '10px Arial';
            ctx.fillText(`[${button.key}]`, button.x + button.width - 25, button.y + 12);
        }
    }

    renderInstructions(ctx, canvasWidth, canvasHeight) {
        const instructions = [
            'Instructions:',
            '• Cliquez côté gauche pour invoquer',
            '• Unités apparaissent sur la frontière',
            '• Utilisez 1, 2, 3 pour changer d\'unité',
            '• Espace pour pause',
            '• ÉLIMINEZ les corrompus = +ÉNERGIE !',
            '• Défendez votre cœur du vide !',
            '',
            'Gardiens du Vide:',
            '• Gardien (50): Équilibré et loyal',
            '• Colosse (80): Bouclier vivant',
            '• Sentinelle (70): Tireur d\'élite'
        ];

        const instructionWidth = 250;
        const instructionHeight = instructions.length * 18 + 20;
        const instructionX = canvasWidth - instructionWidth - 20;
        const instructionY = 20;

        ctx.fillStyle = 'rgba(0, 0, 0, 0.8)';
        ctx.fillRect(instructionX, instructionY, instructionWidth, instructionHeight);

        ctx.strokeStyle = 'white';
        ctx.strokeRect(instructionX, instructionY, instructionWidth, instructionHeight);
        ctx.fillStyle = 'white';
        ctx.font = '12px Arial';

        for (let i = 0; i < instructions.length; i++) {
            const text = instructions[i];
            if (text.startsWith('Instructions:') || text.startsWith('Unités:')) {
                ctx.font = 'bold 14px Arial';
                ctx.fillStyle = 'yellow';
            } else {
                ctx.font = '12px Arial';
                ctx.fillStyle = 'white';
            }

            ctx.fillText(text, instructionX + 10, instructionY + 20 + i * 18);
        }
    }

    handleInput(event) {
        if (event.type === 'keydown') {
            switch(event.key) {
                case '1':
                    this.selectedUnit = 'basic';
                    break;
                case '2':
                    this.selectedUnit = 'tank';
                    break;
                case '3':
                    this.selectedUnit = 'archer';
                    break;
                case 'h':
                case 'H':
                    this.showInstructions = !this.showInstructions;
                    break;
            }
        }
    }

    canAffordUnit(unitType) {
        const button = this.unitButtons.find(b => b.id === unitType);
        return button && this.energy >= button.cost;
    }

    getSelectedUnitType() {
        return this.selectedUnit;
    }

    lightenColor(color, factor) {
        if (color.startsWith('#')) {
            let num = parseInt(color.slice(1), 16);
            let r = (num >> 16) + Math.floor((255 - (num >> 16)) * factor);
            let g = ((num >> 8) & 0x00FF) + Math.floor((255 - ((num >> 8) & 0x00FF)) * factor);
            let b = (num & 0x0000FF) + Math.floor((255 - (num & 0x0000FF)) * factor);
            
            r = Math.min(255, r);
            g = Math.min(255, g);
            b = Math.min(255, b);
            
            return `rgb(${r}, ${g}, ${b})`;
        }
        return color;
    }
}

export default GameUI;
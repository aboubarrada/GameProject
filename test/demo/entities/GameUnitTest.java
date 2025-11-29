package demo.entities;

import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

// Tests pour la classe GameUnit
public class GameUnitTest {
    
    private GameUnit allyUnit;
    private GameUnit enemyUnit;
    
    @BeforeEach
    public void setUp() {
        allyUnit = new GameUnit(100, 500, true, 80, 25, 1.5, 100, Color.SILVER, "Knight", 0);
        enemyUnit = new GameUnit(900, 500, false, 40, 10, 1.2, 70, Color.DARKRED, "Husk", 0);
    }
    
    @Test
    public void testUnitCreation() {
        assertNotNull(allyUnit);
        assertEquals(100, allyUnit.x);
        assertEquals(500, allyUnit.y);
        assertTrue(allyUnit.isAlly);
        assertEquals(80, allyUnit.health);
        assertEquals(80, allyUnit.maxHealth);
    }
    
    @Test
    public void testIsAlive() {
        assertTrue(allyUnit.isAlive());
        allyUnit.takeDamage(80);
        assertFalse(allyUnit.isAlive());
    }
    
    @Test
    public void testTakeDamage() {
        allyUnit.takeDamage(30);
        assertEquals(50, allyUnit.health);
        allyUnit.takeDamage(100);
        assertEquals(0, allyUnit.health);
    }
    
    @Test
    public void testCanAttack() {
        assertTrue(allyUnit.canAttack());
        allyUnit.resetAttackCooldown();
        assertFalse(allyUnit.canAttack());
    }
    
    @Test
    public void testDistanceTo() {
        double distance = allyUnit.distanceTo(900, 500);
        assertEquals(800, distance, 0.1);
    }
    
    @Test
    public void testUpdateReducesCooldown() {
        allyUnit.resetAttackCooldown();
        assertFalse(allyUnit.canAttack());
        allyUnit.update(1.0);
        assertTrue(allyUnit.canAttack());
    }
    
    @Test
    public void testMoveTowards() {
        double initialX = allyUnit.x;
        allyUnit.moveTowards(200, 500, 1.0);
        assertTrue(allyUnit.x > initialX);
    }
}

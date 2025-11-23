package demo.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

// Tests pour la classe GameBase
public class GameBaseTest {
    
    private GameBase playerBase;
    private GameBase enemyBase;
    
    @BeforeEach
    public void setUp() {
        playerBase = new GameBase(100, 500, true);
        enemyBase = new GameBase(900, 500, false);
    }
    
    @Test
    public void testBaseCreation() {
        assertNotNull(playerBase);
        assertEquals(100, playerBase.x);
        assertEquals(500, playerBase.y);
        assertTrue(playerBase.isPlayerBase);
        assertEquals(500, playerBase.health);
        assertEquals(500, playerBase.maxHealth);
    }
    
    @Test
    public void testEnemyBaseCreation() {
        assertNotNull(enemyBase);
        assertFalse(enemyBase.isPlayerBase);
        assertEquals(300, enemyBase.health);
        assertEquals(300, enemyBase.maxHealth);
    }
    
    @Test
    public void testIsAlive() {
        assertTrue(playerBase.isAlive());
        playerBase.takeDamage(500);
        assertFalse(playerBase.isAlive());
    }
    
    @Test
    public void testTakeDamage() {
        playerBase.takeDamage(200);
        assertEquals(300, playerBase.health);
        playerBase.takeDamage(400);
        assertEquals(0, playerBase.health);
    }
    
    @Test
    public void testUpdate() {
        playerBase.update(1.0);
        assertTrue(playerBase.isAlive());
    }
}

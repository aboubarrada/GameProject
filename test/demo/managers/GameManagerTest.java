package demo.managers;

import demo.entities.GameUnit;
import demo.entities.GameProjectile;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

// Tests pour la classe GameManager
public class GameManagerTest {
    
    private GameManager gameManager;
    
    @BeforeEach
    public void setUp() {
        gameManager = new GameManager();
    }
    
    @Test
    public void testGetAllyCost() {
        assertEquals(40, gameManager.getAllyCost(1));
        assertEquals(60, gameManager.getAllyCost(2));
        assertEquals(85, gameManager.getAllyCost(3));
        assertEquals(150, gameManager.getAllyCost(4));
    }
    
    @Test
    public void testCreateAlly() {
        GameUnit knight = gameManager.createAlly(1, 100, 500);
        assertNotNull(knight);
        assertTrue(knight.isAlly);
        assertEquals(80, knight.health);
        assertEquals("Knight", knight.name);
    }
    
    @Test
    public void testCreateAllyDifferentTypes() {
        GameUnit vessel = gameManager.createAlly(2, 100, 500);
        assertNotNull(vessel);
        assertEquals(60, vessel.health);
        
        GameUnit hornet = gameManager.createAlly(3, 100, 500);
        assertNotNull(hornet);
        assertEquals(50, hornet.health);
        
        GameUnit godVoid = gameManager.createAlly(4, 100, 500);
        assertNotNull(godVoid);
        assertEquals(120, godVoid.health);
    }
    
    @Test
    public void testCreateEnemy() {
        GameUnit enemy = gameManager.createEnemy(1, 1.0);
        assertNotNull(enemy);
        assertFalse(enemy.isAlly);
        assertTrue(enemy.health > 0);
    }
    
    @Test
    public void testCreateEnemyWithDifficulty() {
        GameUnit easyEnemy = gameManager.createEnemy(1, 1.0);
        GameUnit hardEnemy = gameManager.createEnemy(1, 2.0);
        
        assertTrue(hardEnemy.health > easyEnemy.health);
        assertTrue(hardEnemy.damage > easyEnemy.damage);
    }
    
    @Test
    public void testGetEnemyReward() {
        int reward1 = gameManager.getEnemyReward(0, 1);
        assertTrue(reward1 > 0);
        
        int reward2 = gameManager.getEnemyReward(0, 5);
        assertTrue(reward2 > reward1);
    }
    
    @Test
    public void testCalculateSpawnInterval() {
        double interval1 = gameManager.calculateSpawnInterval(1, 1.0);
        double interval2 = gameManager.calculateSpawnInterval(5, 2.0);
        
        assertTrue(interval1 > 0);
        assertTrue(interval2 < interval1);
    }
    
    @Test
    public void testFindClosestTarget() {
        GameUnit ally = gameManager.createAlly(1, 100, 500);
        
        List<GameUnit> targets = new ArrayList<>();
        GameUnit enemy1 = new GameUnit(300, 500, false, 40, 10, 1.0, 70, Color.RED, "Enemy1", 0);
        GameUnit enemy2 = new GameUnit(150, 500, false, 40, 10, 1.0, 70, Color.RED, "Enemy2", 0);
        
        targets.add(enemy1);
        targets.add(enemy2);
        
        GameUnit closest = gameManager.findClosestTarget(ally, targets);
        assertEquals(enemy2, closest);
    }
    
    @Test
    public void testCreateProjectile() {
        GameUnit ally = gameManager.createAlly(1, 100, 500);
        GameUnit enemy = new GameUnit(200, 500, false, 40, 10, 1.0, 70, Color.RED, "Enemy", 0);
        
        GameProjectile projectile = gameManager.createProjectile(ally, enemy, true);
        
        assertNotNull(projectile);
        assertEquals(ally.damage, projectile.damage);
    }
}

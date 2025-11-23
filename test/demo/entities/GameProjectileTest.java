package demo.entities;

import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

// Tests pour la classe GameProjectile
public class GameProjectileTest {
    
    private GameProjectile projectile;
    private GameUnit targetUnit;
    
    @BeforeEach
    public void setUp() {
        projectile = new GameProjectile(100, 500, 200, 500, 25, Color.CYAN, 0);
        targetUnit = new GameUnit(200, 500, false, 40, 10, 1.0, 70, Color.RED, "Enemy", 0);
    }
    
    @Test
    public void testProjectileCreation() {
        assertNotNull(projectile);
        assertEquals(100, projectile.x);
        assertEquals(500, projectile.y);
        assertEquals(200, projectile.targetX);
        assertEquals(500, projectile.targetY);
        assertEquals(25, projectile.damage);
    }
    
    @Test
    public void testUpdate() {
        double initialX = projectile.x;
        projectile.update(0.1);
        assertTrue(projectile.x > initialX);
    }
    
    @Test
    public void testIsExpired() {
        assertFalse(projectile.isExpired());
        projectile.age = 5.0;
        assertTrue(projectile.isExpired());
    }
    
    @Test
    public void testCollidesWith() {
        projectile.x = 200;
        projectile.y = 500;
        assertTrue(projectile.collidesWith(targetUnit));
        
        projectile.x = 300;
        assertFalse(projectile.collidesWith(targetUnit));
    }
}

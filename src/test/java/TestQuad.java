import edu.vanier.eastwest.models.Quad;
import javafx.geometry.Point3D;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestQuad {

    @Test
    public void GetQuadVerifyNullPoint3D(){
        Quad q1 = new Quad(1,1,1, null, false);
        assertEquals(-1, q1.getQuadrant(null));
    }
    @Test
    public void GetQuadVerifyNormalCases(){
        Quad q1 = new Quad(3,2,15, null, false);
        assertEquals(3, q1.getQuadrant(new Point3D(30, 2,55)));
        assertEquals(2, q1.getQuadrant(new Point3D(-24, 2,42)));
        assertEquals(1, q1.getQuadrant(new Point3D(32, -92,5)));
    }
}

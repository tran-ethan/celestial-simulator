import edu.vanier.eastwest.models.Vector3D;
import javafx.geometry.Point3D;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;



public class TestVector3D {
    @Test
    /**
     * --scan
     */
    public void VerifyZeroMin() {
        var vector = new Vector3D(1,1,new Point3D(1.0,1.0,1.0));
        assertEquals("zeroMinMax", vector.getColor(100, 0));
    }

    @Test
    public void VerifyZeroMax() {
        var vector = new Vector3D(1,1,new Point3D(1.0,1.0,1.0));
        assertEquals("zeroMinMax", vector.getColor(0, 100));
    }

    @Test
    public void VerifyZeroMinMax() {
        var vector = new Vector3D(1,1,new Point3D(1.0,1.0,1.0));
        assertEquals("zeroMinMax", vector.getColor(0, 0));
    }

    @Test
    public void VerifyZeroMagnitude(){
        var vector = new Vector3D(1,1,new Point3D(1.0,1.0,1.0));
        assertEquals("zeroMagnitude", vector.getColor(100, 100));
    }

    @Test
    public void VerifyMagnitudeOutOfBounds(){
        var vector = new Vector3D(1,1,new Point3D(1.0,1.0,1.0));
        vector.setMagnitude(1001);
        assertEquals("magnitudeOutOfBounds", vector.getColor(100, 100));
        vector.setMagnitude(10);
        assertEquals("magnitudeOutOfBounds", vector.getColor(10000, 1000));
    }

    @Test
    public void VerifyNegativeMagnitude(){
        var vector = new Vector3D(1,1,new Point3D(1.0,1.0,1.0));
        assertEquals("negativeMagnitude", vector.getColor(-1, 100));
        assertEquals("negativeMagnitude", vector.getColor(1, -1));
        vector.setMagnitude(-1);
        assertEquals("negativeMagnitude", vector.getColor(100, 1));
    }

    @Test
    public void VerifyBrokenBounds(){
        var vector = new Vector3D(1,1,new Point3D(1.0,1.0,1.0));
        vector.setMagnitude(1);
        assertEquals("brokenBounds", vector.getColor(1, 100));
    }
    @Test
    public void VerifyDifferentColor(){
        var vector = new Vector3D(1,1,new Point3D(1.0,1.0,1.0));
        vector.setMagnitude(100);
        assertEquals("#7A0403", vector.getColor(100, 1));
        vector.setMagnitude(1);
        assertEquals("#30123B", vector.getColor(100, 1));
        vector.setMagnitude(21);
        assertEquals("#3E9BFE", vector.getColor(101, 1));
    }
}

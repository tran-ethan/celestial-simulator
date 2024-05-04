import edu.vanier.eastwest.models.Vector3D;
import javafx.geometry.Point3D;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;



public class TestVector3D {
    @Test
    public void TestGetColorVerifyZeroMin() {
        var vector = new Vector3D(1,1,new Point3D(1.0,1.0,1.0));
        assertEquals("zeroMinMax", vector.getColor(100, 0));
    }

    @Test
    public void TestGetColorVerifyZeroMax() {
        var vector = new Vector3D(1,1,new Point3D(1.0,1.0,1.0));
        assertEquals("zeroMinMax", vector.getColor(0, 100));
    }

    @Test
    public void TestGetColorVerifyZeroMinMax() {
        var vector = new Vector3D(1,1,new Point3D(1.0,1.0,1.0));
        assertEquals("zeroMinMax", vector.getColor(0, 0));
    }

    @Test
    public void TestGetColorVerifyZeroMagnitude(){
        var vector = new Vector3D(1,1,new Point3D(1.0,1.0,1.0));
        assertEquals("zeroMagnitude", vector.getColor(100, 100));
    }

    @Test
    public void TestGetColorVerifyMagnitudeOutOfBounds(){
        var vector = new Vector3D(1,1,new Point3D(1.0,1.0,1.0));
        vector.setMagnitude(1001);
        assertEquals("magnitudeOutOfBounds", vector.getColor(100, 100));
        vector.setMagnitude(10);
        assertEquals("magnitudeOutOfBounds", vector.getColor(10000, 1000));
    }

    @Test
    public void TestGetColorVerifyNegativeMagnitude(){
        var vector = new Vector3D(1,1,new Point3D(1.0,1.0,1.0));
        assertEquals("negativeMagnitude", vector.getColor(-1, 100));
        assertEquals("negativeMagnitude", vector.getColor(1, -1));
        vector.setMagnitude(-1);
        assertEquals("negativeMagnitude", vector.getColor(100, 1));
    }

    @Test
    public void TestGetColorVerifyBrokenBounds(){
        var vector = new Vector3D(1,1,new Point3D(1.0,1.0,1.0));
        vector.setMagnitude(1);
        assertEquals("brokenBounds", vector.getColor(1, 100));
    }
    @Test
    public void TestGetColorVerifyDifferentColor(){
        var vector = new Vector3D(1,1,new Point3D(1.0,1.0,1.0));
        vector.setMagnitude(100);
        assertEquals("rgb(122,4,3)", vector.getColor(100, 1));
        vector.setMagnitude(1);
        assertEquals("rgb(48,18,59)", vector.getColor(100, 1));
        vector.setMagnitude(21);
        assertEquals("rgb(62,155,254)", vector.getColor(101, 1));
    }
}

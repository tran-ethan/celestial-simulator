import edu.vanier.eastwest.models.Vector3D;
import javafx.geometry.Point3D;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;



public class TestVector3D {
    @Test
    /**
     * --scan
     */
    public void VerifyZeroMin(){
        var vector = new Vector3D(1,1,new Point3D(1.0,1.0,1.0));
        assertEquals("zeroMinMax", vector.getColor(0, 100));
    }
}

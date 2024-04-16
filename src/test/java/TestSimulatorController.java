
import edu.vanier.eastwest.controllers.SimulatorController;
import javafx.geometry.Point3D;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestSimulatorController {
    SimulatorController controller = new SimulatorController();

    @Test
    public void TestGetGravityNull(){
        Point3D newPoint = new Point3D(100, 100, 100);
        assertEquals(controller.getGravity(null, null, 100, 100, 100), new Point3D(0,0, 0));
        assertEquals(controller.getGravity(newPoint, null, 100, 100, 100), new Point3D(0,0, 0));
        assertEquals(controller.getGravity(null, newPoint, 100, 100, 100), new Point3D(0,0, 0));
    }

    @Test
    //TODO @Yihweh
    public void TestGetGravityZero(){
        Point3D newPoint = new Point3D(100, 100, 100);
        assertEquals(controller.getGravity(new Point3D(0,0,0), new Point3D(0,0,0), 100, 100, 100), new Point3D(0,0, 0));
        assertEquals(controller.getGravity(newPoint, new Point3D(0,0,0), 100, 100, 100), new Point3D(0,0, 0));
        assertEquals(controller.getGravity(null, newPoint, 100, 100, 100), new Point3D(0,0, 0));
    }
}

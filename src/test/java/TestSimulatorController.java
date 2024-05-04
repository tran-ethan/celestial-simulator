
import edu.vanier.eastwest.controllers.SimulatorController;
import javafx.geometry.Point3D;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestSimulatorController {
    SimulatorController controller = new SimulatorController();

    @Test
    public void TestGetGravityVerifyNullPoint3D(){
        Point3D p1 = null;
        Point3D p2 = new Point3D(0,0,0);
        assertEquals(new Point3D(0,0,0), controller.getGravity(p1,p2,1,1,1));
        assertEquals(new Point3D(0,0,0), controller.getGravity(p1,p1,1,1,1));
    }

    @Test
    public void TestGetGravityVerifyZeroPoint3D(){
        Point3D p1 = new Point3D(1,0,1);
        Point3D p2 = new Point3D(0,0,0);
        assertEquals(new Point3D(-0.125,0,-0.125), controller.getGravity(p1,p2,1,1,1));
        assertEquals(new Point3D(0,0,0), controller.getGravity(p2,p2,1,1,1));
    }

    @Test
    public void TestGetGravityVerifyZeroVariables(){
        Point3D p1 = new Point3D(1, 0,1);
        Point3D p2 = new Point3D(0,0,0);
        assertEquals(new Point3D(0,0,0), controller.getGravity(p1,p2,0,1,1));
        assertEquals(new Point3D(0,0,0), controller.getGravity(p1,p2,1,0,1));
        assertEquals(new Point3D(0,0,0), controller.getGravity(p1,p2,1,1,0));
        assertEquals(new Point3D(0,0,0), controller.getGravity(p1,p2,0,0,0));
    }

    @Test
    public void TestGetGravityVerifyNegativeVariables(){
        Point3D p1 = new Point3D(1, 0,1);
        Point3D p2 = new Point3D(0,0,0);
        assertEquals(new Point3D(0,0,0), controller.getGravity(p1,p2,-2131,1,1));
        assertEquals(new Point3D(0,0,0), controller.getGravity(p1,p2,1,-1341,1));
        assertEquals(new Point3D(0,0,0), controller.getGravity(p1,p2,1,1,-14143));
        assertEquals(new Point3D(0,0,0), controller.getGravity(p1,p2,-12414,-1414,12413));
    }


    @Test
    public void TestGetGravityVerifyNormalExamples(){
        Point3D p1 = new Point3D(1, 0,1);
        Point3D p2 = new Point3D(-242,0,-300);
        Point3D p3 = new Point3D(422, 32, 23);
        assertEquals(new Point3D(-0.0020987523535689447,0,-0.002599689129317911), controller.getGravity(p1,p2,500,1,1));
        assertEquals(new Point3D(-0.9137116400873898,-0.04403429590782602,-0.44447117431961886), controller.getGravity(p3,p2,555555,434,5));
        assertEquals(new Point3D(0.000005042249875685097,0.0000003832588979143066,0.00000026349049231608577), controller.getGravity(p1,p3,34134,35,14143));
        assertEquals(new Point3D(-0.000014857223122706321,0,-0.000018403391604669146), controller.getGravity(p1,p2,222,1414,123));
    }

}

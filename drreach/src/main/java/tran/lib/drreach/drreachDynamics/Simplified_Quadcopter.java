package tran.lib.drreach.drreachDynamics;

// Simplified Dynamics of Quadcopter
// Dung Tran: 5/22/2018
// References: Nathan's master thesis, page 33.

import net.sourceforge.interval.ia_math.IAMath;
import net.sourceforge.interval.ia_math.RealInterval;

import tran.lib.drreach.drreachComputation.HyperRectangle;

public class Simplified_Quadcopter {

    /**
     * Nonlinear simplified dynamics of quadcopter
     * State vector; [x, v_x, y, v_y]^T, x: x-axis position, v_x: velocity along x-axis, y: y-axis position, v_y: velocity along y-axis
     * Dynamics:
     *              x' = v_x
     *              v_x' = 9.81 * tan(pitch)
     *              y' = v_y
     *              v_y' = 9.81 * tan(roll)/ cos(pitch)
     *
     * The control input u = [pitch, roll]^T
     * A PID controller is designed to control the quadcopter
     *
     */

    public int numDerivativeBoundCalls = 0;

    public double get_derivative_bounds(HyperRectangle rect, int faceIndex, RealInterval current_pitch, RealInterval current_roll){

        ++numDerivativeBoundCalls;

        RealInterval rv = new RealInterval(0, 0);
        int dim = faceIndex / 2;
        boolean isMin = (faceIndex % 2) == 0;

        RealInterval v_x = new RealInterval(rect.intervals[1].min, rect.intervals[1].max);   // use RealInterval in ia_math package for interval arithmetic
        RealInterval v_y = new RealInterval(rect.intervals[3].min, rect.intervals[3].max);


        if (dim < 0 || dim > 3){
            throw new java.lang.Error("invalid dimension");
        }
        else{

            if (dim == 0){  // x' = v_x
                rv = v_x;
            }
            else if (dim == 1){ // v_x' = 9.81 * tan(pitch)

                System.out.printf("current pitch = (%f, %f) \n", current_pitch.lo(), current_pitch.hi());
                RealInterval test_tan = IAMath.tan(current_pitch);
                System.out.printf("tan(pitch) = (%f, %f)\n", test_tan.lo(), test_tan.hi());
                //RealInterval test1 = new RealInterval(-1, 1);
                //rv = IAMath.mul(new RealInterval(9.81), test1);
                //rv = IAMath.mul(new RealInterval(9.81), test_tan);
                //rv = IAMath.mul(new RealInterval(9.81), test_tan);
                //rv = IAMath.mul(new RealInterval(9.81), IAMath.tan(current_pitch));
                rv = new RealInterval(0.1);
                System.out.print("dim = 1, rv.lo = " +rv.lo() + " and rv.hi = " +rv.hi() + "\n");

            }
            else if (dim == 2){ // y' = v_y
                rv = v_y;
            }
            else if (dim == 3){ // v_y' = 9.81 * tan(roll) / cos(pitch)

                rv = new RealInterval(0.1);
                //rv = IAMath.mul(new RealInterval(9.81), IAMath.div(IAMath.tan(current_roll), IAMath.cos(current_pitch)));
            }

        }

        return isMin? rv.lo() : rv.hi();

    }

}

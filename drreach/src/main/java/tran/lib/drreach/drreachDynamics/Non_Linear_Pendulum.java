package tran.lib.drreach.drreachDynamics;

// Dynamics of a nonlinear inverted pendulum
// Dung Tran: 5/3/2018, Update:
// References: 1) Real-time Reachability for Verified Simplex Design

import tran.lib.drreach.drreachComputation.HyperRectangle;
import tran.lib.drreach.drreachComputation.Interval;

public class Non_Linear_Pendulum {

    /**
     *      Nonlinear Dynamics of inverted pendulum
     *      State vector: x = [p, v, theta, omega]^T: p -cart position, v- cart velocity, theta- pendulum arm angle, omega-pendulum arm angular velocity
     *      x' = f(x,u)
     *      at dim 0: p' = v
     *      at dim 1: v' =  -(a) / (b)
     *                 a = 0.020833 * omega^2 * sin(theta) - 0.059211 * v + 0.25 * cos(theta) * (0.0001 * omega + 2.45 * sin(theta))
     *                 b = 0.0625 cos(theta)^2 - 0.604167
     *      at dim 2: theta' = omega
     *      at dim 3: omega' = (a) / (b)
     *                     a = 0.000725 * omega + 17.7625 * sin(theta) - 0.25 * cos(theta) * (-0.25 * sin (theta) * omega^2 + 0.710657 * v)
     *                     b = 0.0625 * cos(theta)^2 - 0.604167
     */

    public int numDerivativeBoundCalls = 0; // Count the number of calling the method: get_derivative_bound

    public double get_derivative_bounds(HyperRectangle rect, int faceIndex){

        Interval rv = new Interval(0.0, 0.0);
        int dim = faceIndex / 2;
        boolean isMin = (faceIndex % 2) == 0;

        Interval v = rect.intervals[1];
        Interval theta = rect.intervals[2];
        Interval omega = rect.intervals[3];

        if (dim < 0 || dim > 3){
            throw new java.lang.Error("invalid dimension");
        }
        else{

            if (dim == 0){  // p' = v
                rv = v;
            }
            else if (dim == 1){ // v' = -(a) / (b)
                ;
            }
            else if (dim == 2){ // theta' = omega
                rv = omega;
            }
            else if (dim == 3){ // omega' = (a) / (b)
                ;
            }

        }



        return isMin? rv.min : rv.max;

    }


}

package tran.lib.drreach.drreachDynamics;

// Dynamics of a nonlinear inverted pendulum
// Dung Tran: 5/3/2018, Update:
// References: 1) Real-time Reachability for Verified Simplex Design

import tran.lib.drreach.drreachComputation.HyperRectangle;
import tran.lib.drreach.drreachComputation.Interval;
import net.sourceforge.interval.ia_math.IAMath;
import net.sourceforge.interval.ia_math.RealInterval;

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

        ++numDerivativeBoundCalls;

        RealInterval rv = new RealInterval(0, 0);
        int dim = faceIndex / 2;
        boolean isMin = (faceIndex % 2) == 0;

        RealInterval v = new RealInterval(rect.intervals[1].min, rect.intervals[1].max);   // use RealInterval in ia_math package for interval arithmetic
        RealInterval theta = new RealInterval(rect.intervals[2].min, rect.intervals[2].max);
        RealInterval omega = new RealInterval(rect.intervals[3].min, rect.intervals[3].max);

        if (dim < 0 || dim > 3){
            throw new java.lang.Error("invalid dimension");
        }
        else{

            if (dim == 0){  // p' = v
                rv = v;
            }
            else if (dim == 1){ // v' = -(a) / (b)

                /**
                 *  at dim 1: v' = (omega^2 * sin(theta) + (-5401/1900)*v + cos(theta)*(3/2500*omega + 147/5*sin(theta)))
                 *                      /   (-3*cos(theta)^2 + 29)
                 */

                RealInterval A = IAMath.mul(IAMath.integerPower(omega, new RealInterval(2)), IAMath.sin(theta));
                RealInterval B = IAMath.mul(v, new RealInterval(-5401.0 / 1900.0));
                RealInterval C = IAMath.mul(IAMath.cos(theta), IAMath.add(IAMath.mul(new RealInterval(3.0 / 2500.0), omega), IAMath.mul(new RealInterval(147/5), IAMath.sin(theta))));
                RealInterval D = IAMath.add(IAMath.mul(new RealInterval(-3.0), IAMath.integerPower(IAMath.cos(theta), new RealInterval(2))), new RealInterval(29.0));

                rv = IAMath.div(IAMath.add(IAMath.add(A, B), C), D);

            }
            else if (dim == 2){ // theta' = omega
                rv = omega;
            }
            else if (dim == 3){ // omega' = (a) / (b)

                //omega' == ((87*omega)/2500 + (4263*sin(theta))/5 - cos(theta)*(- 3*sin(theta)*omega^2
                //          + (16203*v)/1900))/(3*cos(theta)^2 - 29)

                RealInterval A = IAMath.mul(new RealInterval(87.0 / 2500.0), omega);
                RealInterval B = IAMath.mul(new RealInterval(4263.0 / 5.0), IAMath.sin(theta));
                RealInterval C = IAMath.cos(theta);
                RealInterval D = IAMath.mul(new RealInterval(-3.0), IAMath.mul(IAMath.sin(theta), IAMath.integerPower(omega, new RealInterval(2))));
                RealInterval E = IAMath.mul(new RealInterval(16203.0 / 1900.0), v);
                RealInterval F = IAMath.add(IAMath.mul(new RealInterval(3.0), IAMath.integerPower(IAMath.cos(theta), new RealInterval(2))), new RealInterval(-29.0));

                rv = IAMath.div(IAMath.sub(IAMath.add(A, B), IAMath.mul(C, IAMath.add(D, E))), F);
            }

        }

        return isMin? rv.lo() : rv.hi();

    }


}

package tran.lib.drreach.drreachDynamics;

// Dynamics of a linear inverted pendulum
// Dung Tran: 5/2/2018, Update:
// References: 1) Real-time Reachability for Verified Simplex Design


import tran.lib.drreach.drreachComputation.HyperRectangle;

public class Linear_Pendulumn {


    // this class implements a saturated controlled inverted pendulum
    //////////////////////////////////////////////////////////////////
    // x' = Ax + Bu, u = Kx, u between -4.95 and 4.95
    // where x = [pos_err velocity angle angular_vel]^T

    static final double[][] A = {{0, 1, 0, 0}, {0, -10.95, -2.75, 0.0043}, {0, 0, 0, 1}, {0, 24.92, 28.58, -0.044}}; // matrix A: 4 x 4
    static final double[] B = {0, 1.94, 0, -4.44}; // matrix B transpose: 1 x 4
    static final double[] K = {0.4072, 7.2373, 18.6269, 3.6725}; // matrix K: 1 x 4

    int numDerivativeBoundCalls = 0; // Count the number of calling the method: get_derivative_bound

    public double eval_at_dim(int at_dim, double[] state){
        // evaluate the derivative Ax + Bu at specific state x

        double rv = 0;
        if (state.length == 4){

            // compute Ax part
            for (int i = 0; i < 4; i++){
                rv += A[at_dim][i] * state[i];
            }
            // compute the Bu part
            double u = 0;
            // u = Kx
            for (int i = 0; i < 4; i++){
                u += K[i] * state[i];
            }
            // input saturation
            if (u < -4.95){
                u = -4.95;
            }
            else if (u > 4.95) {
                u = 4.95;
            }

            rv += B[at_dim]*u;

        }
        else{

            throw new java.lang.Error("invalid state");
        }

        return rv;

    }

    public double get_derivative_bounds(HyperRectangle rect, int faceIndex){
        // get the derivative bound
        double rv = 0;
        ++numDerivativeBoundCalls;

        int dim = faceIndex / 2;
        boolean isMin = (faceIndex % 2) == 0;

        // sample all the corners

        double[][] points = {

            {rect.intervals[0].min, rect.intervals[1].min, rect.intervals[2].min, rect.intervals[3].min},
            {rect.intervals[0].min, rect.intervals[1].min, rect.intervals[2].min, rect.intervals[3].max},
            {rect.intervals[0].min, rect.intervals[1].min, rect.intervals[2].max, rect.intervals[3].min},
            {rect.intervals[0].min, rect.intervals[1].min, rect.intervals[2].max, rect.intervals[3].max},

            {rect.intervals[0].min, rect.intervals[1].max, rect.intervals[2].min, rect.intervals[3].min},
            {rect.intervals[0].min, rect.intervals[1].max, rect.intervals[2].min, rect.intervals[3].max},
            {rect.intervals[0].min, rect.intervals[1].max, rect.intervals[2].max, rect.intervals[3].min},
            {rect.intervals[0].min, rect.intervals[1].max, rect.intervals[2].max, rect.intervals[3].max},

            {rect.intervals[0].max, rect.intervals[1].min, rect.intervals[2].min, rect.intervals[3].min},
            {rect.intervals[0].max, rect.intervals[1].min, rect.intervals[2].min, rect.intervals[3].max},
            {rect.intervals[0].max, rect.intervals[1].min, rect.intervals[2].max, rect.intervals[3].min},
            {rect.intervals[0].max, rect.intervals[1].min, rect.intervals[2].max, rect.intervals[3].max},

            {rect.intervals[0].max, rect.intervals[1].max, rect.intervals[2].min, rect.intervals[3].min},
            {rect.intervals[0].max, rect.intervals[1].max, rect.intervals[2].min, rect.intervals[3].max},
            {rect.intervals[0].max, rect.intervals[1].max, rect.intervals[2].max, rect.intervals[3].min},
            {rect.intervals[0].max, rect.intervals[1].max, rect.intervals[2].max, rect.intervals[3].max},
        };


        if (dim >= 4){
            throw new java.lang.Error("dimension index out of bounds in get_derivative_bounds");
        }
        else{

            rv = eval_at_dim(dim, points[0]);
            for (int i = 1; i < 16; i++){
                double val = eval_at_dim(dim, points[i]);
                if (isMin && val < rv){
                    rv = val;
                }
                else if (!isMin && val > rv){
                    rv = val;
                }
            }

        }

        return rv;
    }

}

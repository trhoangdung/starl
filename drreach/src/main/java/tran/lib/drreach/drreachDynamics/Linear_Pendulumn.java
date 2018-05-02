package tran.lib.drreach.drreachDynamics;

// Dynamics of a linear inverted pendulum
// Dung Tran: 5/2/2018, Update:
// References: 1) Real-time Reachability for Verified Simplex Design


public class Linear_Pendulumn {


    // this class implements a saturated controlled inverted pendulum
    //////////////////////////////////////////////////////////////////
    // x' = Ax + Bu, u = Kx, u between -4.95 and 4.95
    // where x = [pos_err velocity angle angular_vel]^T

    static final double[][] A = {{0, 1, 0, 0}, {0, -10.95, -2.75, 0.0043}, {0, 0, 0, 1}, {0, 24.92, 28.58, -0.044}}; // matrix A: 4 x 4
    static final double[][] B = {{0}, {1.94}, {0}, {-4.44}}; // matrix B: 4 x 1
    static final double[] K = {0.4072, 7.2373, 18.6269, 3.6725}; // matrix K: 1 x 4



}

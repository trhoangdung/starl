package tran.lib.drreach.drreachDynamics;

import tran.lib.drreach.drreachComputation.HyperRectangle;

public class Dynamics_Bounds {

    public double get_dynamics_bounds(int dynamics_index, HyperRectangle rect, int faceIndex){

        /**
         * dynamics_index: 0 -> Linear Pendulum
         * dynamics_index: 1 -> Non-linear Pendulum
         * dynamics_index: 2 -> iRobot
         * dynamics_index: 3 -> miniDrone
         * ...
         */

        double bound = 0;

        if (dynamics_index < 0){
            throw new java.lang.Error("Invalid dynamics index");
        }
        else if (dynamics_index == 0){

            Linear_Pendulum lp = new Linear_Pendulum();
            bound = lp.get_derivative_bounds(rect, faceIndex);

        }
        else if (dynamics_index == 1){

            Non_Linear_Pendulum nlp = new Non_Linear_Pendulum();
            bound =  nlp.get_derivative_bounds(rect, faceIndex);

        }
        else if (dynamics_index >= 2){
            throw new java.lang.Error("Not support the dynamics index >= 2 yet");

        }

        return bound;
    }
}

package slather.util;

import slather.sim.Point;

public class Vector extends Point {

    // double x;
    // double y;

    final double EPSILON = 1e-7;

    public Vector( double x, double y){
        super(x, y);
    }

    public Vector multiply(double d) {
        return new Vector(x * d, y * d);
    }

    public Vector add(Point b) {
        return new Vector(x+b.x, y+b.y);
    }

    public Vector unitVector() {
        final double t = norm();
        if (t < EPSILON){
            return this;
        } else {
            return multiply(1.0/t);
        }
    }

}
package ch.epfl.javelo.projection;

import ch.epfl.javelo.Math2;

public record PointCh(double e, double n) {
    public PointCh{
        if(e > SwissBounds.MAX_E || e < SwissBounds.MIN_E || n > SwissBounds.MAX_N || n < SwissBounds.MIN_N) {
            throw new IllegalArgumentException();
        }
    }

    public double distanceTo(PointCh that){
        double xDistance = this.e - that.e();
        double yDistance = this.n - that.n();

        return Math2.norm(xDistance, yDistance);
    }
    public double squaredDistanceTo(PointCh that){
        double norm = distanceTo(that);
        return norm*norm;
    }



    public double lon(){
        return Ch1903.lon(e,n);
    }
    public double lat(){
        return Ch1903.lat(e,n);
    }
}

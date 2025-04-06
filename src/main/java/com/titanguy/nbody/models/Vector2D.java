package com.titanguy.nbody.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Vector2D {
    private double x;
    private double y;

    public Vector2D add(Vector2D v){
        return new Vector2D(this.x + v.x, this.y + v.y);
    }

    public Vector2D subtract(Vector2D v){
        return new Vector2D(this.x - v.x, this.y - v.y);
    }

    public Vector2D multipleByScalar(double scalar){
        this.x *= scalar;
        this.y *= scalar;
        return this;
    }

    public double distance(){
        return Math.sqrt(this.x * this.x + this.y * this.y);
    }

    public Vector2D normalize(){
        double distance = this.distance();
        return distance() == 0 ? new Vector2D(0, 0) : new Vector2D(this.x / distance, this.y / distance);
    }
}

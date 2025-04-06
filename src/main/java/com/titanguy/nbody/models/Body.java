package com.titanguy.nbody.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Body {
    private int id;
    private BodyType type;
    private double mass;
    private Vector2D position;
    private Vector2D velocity;
    private Vector2D acceleration;
}

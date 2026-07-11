package com.titanguy.nbody.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Body {
    @EqualsAndHashCode.Include
    private int id;
    private BodyType type;
    private double mass;
    private Vector2D position;
    private Vector2D velocity;
    private Vector2D acceleration;
}

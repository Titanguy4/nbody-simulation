package com.titanguy.nbody.controllers.dto;

import com.titanguy.nbody.models.BodyType;
import com.titanguy.nbody.models.Vector2D;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record BodyDto(
                @NotNull(message = "ID is mandatory") int id,

                @NotNull(message = "Body type is mandatory") BodyType type,

                @NotNull(message = "Mass is mandatory") @Positive(message = "Mass must be strictly greater than 0") double mass,

                @NotNull(message = "Position is mandatory") Vector2D position,

                @NotNull(message = "Velocity is mandatory") Vector2D velocity,

                @NotNull(message = "Acceleration is mandatory") Vector2D acceleration) {
}

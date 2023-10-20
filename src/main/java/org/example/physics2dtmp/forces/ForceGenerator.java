package org.example.physics2dtmp.forces;

import org.example.physics2dtmp.rigidbody.Rigidbody2D;

public interface ForceGenerator {
    void updateForce(Rigidbody2D body, float dt);
}

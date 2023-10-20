package org.example.physics2dtmp.forces;



import org.example.physics2dtmp.rigidbody.Rigidbody2D;
import org.joml.Vector2f;
public class Gravity2D implements ForceGenerator {

    private Vector2f gravity;

    public Gravity2D(Vector2f force) {
        this.gravity = new Vector2f(force);
    }

    @Override
    public void updateForce(Rigidbody2D body, float dt) {
        body.addForce(new Vector2f(gravity).mul(body.getMass()));
    }
}

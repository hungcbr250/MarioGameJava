package org.example.components;

import org.example.jade.GameObject;
import org.example.jade.Window;
import org.example.physics2d.Physics2D;
import org.example.physics2d.components.Rigidbody2D;
import org.jbox2d.dynamics.contacts.Contact;
import org.joml.Vector2f;

public class Fireball extends Component {
    public transient boolean goingRight = false;
    private transient Rigidbody2D rb;
    private transient float fireballSpeed = 1.7f;
    private transient Vector2f velocity = new Vector2f();
    private transient Vector2f acceleration = new Vector2f();
    private transient Vector2f terminalVelocity = new Vector2f(2.1f, 3.1f);
    private transient boolean onGround = false;
    private transient float lifetime = 4.0f;

    private static int fireballCount = 0;

    public static boolean canSpawn() {
        return fireballCount < 4;
    }

    @Override
    public void start() {
        this.rb = this.gameObject.getComponent(Rigidbody2D.class);
        this.acceleration.y = Window.getPhysics().getGravity().y * 0.7f;
        fireballCount++;
    }

    @Override
    public void update(float dt) {
        System.out.println("update fireball");
        lifetime -= dt;
        if (lifetime <= 0) {
//            fireball sẽ biến mất khi lifetime <=0
            disappear();
            return;
        }
//        nếu goingRight =true
        if (goingRight) {
//            đặt vx = fireballSpeed để chuyển động sang phải
            velocity.x = fireballSpeed;
        } else {
//            di chuyển sang trái
            velocity.x = -fireballSpeed;
        }
//  kiểm tra fireball có ở trên mặt đất không
        checkOnGround();
//        nếu đang ở trên mặt đất
        if (onGround) {
//            đặt v a như dưới để tạo hiệu ứng nảy
            this.acceleration.y = 1.5f;
            this.velocity.y = 2.5f;
        } else {
            this.acceleration.y = Window.getPhysics().getGravity().y * 0.7f;
        }

        this.velocity.y += this.acceleration.y * dt;
        this.velocity.y = Math.max(Math.min(this.velocity.y, this.terminalVelocity.y), -terminalVelocity.y);
        this.rb.setVelocity(velocity);
    }

    public void checkOnGround() {
        float innerPlayerWidth = 0.25f * 0.7f;
        float yVal = -0.09f;
        onGround = Physics2D.checkOnGround(this.gameObject, innerPlayerWidth, yVal);
    }

    @Override
    public void beginCollision(GameObject obj, Contact contact, Vector2f contactNormal) {
        if (Math.abs(contactNormal.x) > 0.8f) {
            this.goingRight = contactNormal.x < 0;
        }
    }

    @Override
    public void preSolve(GameObject obj, Contact contact, Vector2f contactNormal) {
        if (obj.getComponent(PlayerController.class) != null ||
                obj.getComponent(Fireball.class) != null) {
            contact.setEnabled(false);
        }
    }

    public void disappear() {
        fireballCount--;
        this.gameObject.destroy();
    }
}

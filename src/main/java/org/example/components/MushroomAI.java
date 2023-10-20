package org.example.components;

import org.example.jade.GameObject;
import org.example.physics2d.components.Rigidbody2D;
import org.example.util.AssetPool;
import org.jbox2d.dynamics.contacts.Contact;
import org.joml.Vector2f;

public class MushroomAI extends Component {
    private transient boolean goingRight = true;
    private transient Rigidbody2D rb;
    private transient Vector2f speed = new Vector2f(1.0f, 0.0f);
    private transient float maxSpeed = 0.8f;
    private transient boolean hitPlayer = false;


    @Override
    public void start() {
        this.rb = gameObject.getComponent(Rigidbody2D.class);
        AssetPool.getSound("assets/sounds/powerup_appears.ogg").play();
    }

    @Override
    public void update(float dt) {
        System.out.println("update Mushroom");
        if (goingRight && Math.abs(rb.getVelocity().x) < maxSpeed) {
            rb.addVelocity(speed);
        } else if (!goingRight && Math.abs(rb.getVelocity().x) < maxSpeed) {
            rb.addVelocity(new Vector2f(-speed.x, speed.y));
        }
    }

    @Override
    public void preSolve(GameObject obj, Contact contact, Vector2f contactNormal) {
        System.out.println("preSolve Mushroom");
//        lấy ra player
        PlayerController playerController = obj.getComponent(PlayerController.class);
        if (playerController != null) {
            contact.setEnabled(false);
//            nếu hitPlayer != true ,để mushroom chỉ tương tác vs nv 1 lần
            if (!hitPlayer) {
//                nếu nv đang ở trạng thái small
                if (playerController.isSmall()) {
                    playerController.powerup();
                } else {
                    AssetPool.getSound("assets/sounds/coin.ogg").play();
                }
                this.gameObject.destroy();
                hitPlayer = true;
            }
        }
//        nếu đối tượng va chạm ko phải ground cũng không phải player
        else if (obj.getComponent(Ground.class) == null) {
//            tắt va chạm
            contact.setEnabled(false);
            return;
        }
//      nếu va chạm theo chiều ngang
        if (Math.abs(contactNormal.y) < 0.1f) {
            goingRight = contactNormal.x < 0;
        }
    }



}

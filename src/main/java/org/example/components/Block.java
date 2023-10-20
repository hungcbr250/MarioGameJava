package org.example.components;

import org.example.jade.GameObject;
import org.example.util.AssetPool;
import org.jbox2d.dynamics.contacts.Contact;
import org.joml.Vector2f;

public abstract class Block extends Component {
    private transient boolean bopGoingUp = true;
    private transient boolean doBopAnimation = false;
    private transient Vector2f bopStart;
    private transient Vector2f topBopLocation;
    private transient boolean active = true;

    public float bopSpeed = 0.4f;

    @Override
    public void start() {
        this.bopStart = new Vector2f(this.gameObject.transform.position);
        this.topBopLocation = new Vector2f(bopStart).add(0.0f, 0.02f);
    }

    // làm cho block nảy lên
    @Override
    public void update(float dt) {
        if (doBopAnimation) {
            if (bopGoingUp) {
                if (this.gameObject.transform.position.y < topBopLocation.y) {
//                    di chuyển lên
                    this.gameObject.transform.position.y += bopSpeed * dt;
                } else {
                    bopGoingUp = false;
                }
            } else {
                if (this.gameObject.transform.position.y > bopStart.y) {
//                    di chuyển xuống
                    this.gameObject.transform.position.y -= bopSpeed * dt;
                } else {
//                    di chuyển về vị trí ban đầu
                    this.gameObject.transform.position.y = this.bopStart.y;
                    bopGoingUp = true;
                    doBopAnimation = false;
                }
            }
        }
    }

    @Override
    public void beginCollision(GameObject obj, Contact contact, Vector2f contactNormal) {
//        lấy ra đối tượng va chạm
        PlayerController playerController = obj.getComponent(PlayerController.class);
//        nê active true và có nv va chạm và hướng va chạm xảy ra ở dưới
        if (active && playerController != null && contactNormal.y < -0.8f) {
            doBopAnimation = true;
            AssetPool.getSound("assets/sounds/bump.ogg").play();
//            xử lí va chạm với nv khi ăn item
            playerHit(playerController);
        }
    }

    public void setInactive() {
        this.active = false;
    }

    abstract void playerHit(PlayerController playerController);
}

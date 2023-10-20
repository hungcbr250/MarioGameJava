package org.example.components;

import org.example.jade.Camera;
import org.example.jade.GameObject;
import org.example.jade.Window;
import org.example.physics2d.Physics2D;
import org.example.physics2d.components.Rigidbody2D;
import org.example.util.AssetPool;
import org.jbox2d.dynamics.contacts.Contact;
import org.joml.Vector2f;

public class TurtleAI extends Component {
    private transient boolean goingRight = false;
    private transient Rigidbody2D rb;
    private transient float walkSpeed = 0.6f;
    private transient Vector2f velocity = new Vector2f();
    private transient Vector2f acceleration = new Vector2f();
    private transient Vector2f terminalVelocity = new Vector2f(2.1f, 3.1f);
    private transient boolean onGround = false;
    private transient boolean isDead = false;
    private transient boolean isMoving = false;
    private transient StateMachine stateMachine;
    private float movingDebounce = 0.32f;

    @Override
    public void start() {
        this.stateMachine = this.gameObject.getComponent(StateMachine.class);
        this.rb = gameObject.getComponent(Rigidbody2D.class);
        this.acceleration.y = Window.getPhysics().getGravity().y * 0.7f;
    }

    @Override
    public void update(float dt) {
        System.out.println("update TurtleAI");
        movingDebounce -= dt;
        Camera camera = Window.getScene().camera();
//        nếu vị trí hiện tại vượt ra ngoài camera
        if (this.gameObject.transform.position.x >
                camera.position.x + camera.getProjectionSize().x * camera.getZoom()) {
            return;
        }
// nếu còn sống hoặc đang di chuyển
        if (!isDead || isMoving) {
//            nếu đi sang phải
            if (goingRight) {
                gameObject.transform.scale.x = -0.25f;
                velocity.x = walkSpeed;
//                gia tốc =0 => vận tốc ko đổi
                acceleration.x = 0;
            } else {
                gameObject.transform.scale.x = 0.25f;
                velocity.x = -walkSpeed;
                acceleration.x = 0;
            }
        } else {
            velocity.x = 0;
        }

        checkOnGround();
        if (onGround) {
//      chỉ di chuyển theo phương ngang , vận tốc dọc ,gia tốc dọc =0
            this.acceleration.y = 0;
            this.velocity.y = 0;
        } else {
            this.acceleration.y = Window.getPhysics().getGravity().y * 0.7f;
        }
        this.velocity.y += this.acceleration.y * dt;
        this.velocity.y = Math.max(Math.min(this.velocity.y, this.terminalVelocity.y), -terminalVelocity.y);
        this.rb.setVelocity(velocity);

        if (this.gameObject.transform.position.x <
                Window.getScene().camera().position.x - 0.5f) {// ||
            //this.gameObject.transform.position.y < 0.0f) {
            this.gameObject.destroy();
        }
    }

    public void checkOnGround() {
        float innerPlayerWidth = 0.25f * 0.7f;
        float yVal = -0.2f;
        onGround = Physics2D.checkOnGround(this.gameObject, innerPlayerWidth, yVal);
    }

    public void stomp() {
        this.isDead = true;
        this.isMoving = false;
        this.velocity.zero();
        this.rb.setVelocity(this.velocity);
        this.rb.setAngularVelocity(0.0f);
        this.rb.setGravityScale(0.0f);
        this.stateMachine.trigger("squashMe");
        AssetPool.getSound("assets/sounds/bump.ogg").play();
    }

    @Override
    public void preSolve(GameObject obj, Contact contact, Vector2f contactNormal) {
        System.out.println("PreSolve Turtle");
        GoombaAI goomba = obj.getComponent(GoombaAI.class);
//        nếu turtle chết và đang di chuyển và tồn tại goomba
        if (isDead && isMoving && goomba != null) {
            goomba.stomp();
//            va chạm sẽ bị vô hiệu hóa
            contact.setEnabled(false);
            AssetPool.getSound("assets/sounds/kick.ogg").play();
        }

        PlayerController playerController = obj.getComponent(PlayerController.class);
//        nếu va chạm xảy ra với nv
        if (playerController != null) {
            if (!isDead && !playerController.isDead() &&
                    !playerController.isHurtInvincible() &&
                    contactNormal.y > 0.58f) {
                playerController.enemyBounce();
                stomp();
                walkSpeed *= 3.0f;
            }
//            nếu movingDebounce hết và ko chết và ko ở trong trạng thái bất tử
//            và đang di chuyển hoặc ko chết và va chạm ở phía dưới
            else if (movingDebounce < 0 && !playerController.isDead() &&
                    !playerController.isHurtInvincible() &&
                    (isMoving || !isDead) && contactNormal.y < 0.58f) {
//                xử lí nv với hàm die
                playerController.die();
                if (!playerController.isDead()) {
//                    nếu nv ko chết thì va chạm bị vô hiệu hóa
                    contact.setEnabled(false);
                }
            }
//          nếu nv ko chết và ko trong trạng thái bất tử
            else if (!playerController.isDead() && !playerController.isHurtInvincible()) {
//                turtle chết và va chạm ở phía dưới
                if (isDead && contactNormal.y > 0.58f) {
                    playerController.enemyBounce();
//                    đảo ngược trạng thái của isMoving
                    isMoving = !isMoving;
                    goingRight = contactNormal.x < 0;
                } else if (isDead && !isMoving) {
                    isMoving = true;
//                    điều khiển lại hướng sau khi va chạm contactNormal.x < 0 sẽ trả về true hoặc false
                    goingRight = contactNormal.x < 0;
                    movingDebounce = 0.32f;
                }
            } else if (!playerController.isDead() && playerController.isHurtInvincible()) {
                contact.setEnabled(false);
            }
        }
//        va chạm với đối tượng ko phải ng chơi
        else if (Math.abs(contactNormal.y) < 0.1f && !obj.isDead() && obj.getComponent(MushroomAI.class) == null) {
            goingRight = contactNormal.x < 0;
            if (isMoving && isDead) {
                AssetPool.getSound("assets/sounds/bump.ogg").play();
            }
        }
// nếu va chạm với fireball
        if (obj.getComponent(Fireball.class) != null) {
//            nếu chưa chết
            if (!isDead) {
                walkSpeed *= 3.0f;
                stomp();
            } else {
//                đảo ngc trạng thái đối tượng
                isMoving = !isMoving;
                goingRight = contactNormal.x < 0;
            }
            obj.getComponent(Fireball.class).disappear();
            contact.setEnabled(false);
        }
    }
}

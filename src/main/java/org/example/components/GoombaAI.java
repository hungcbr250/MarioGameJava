package org.example.components;

import org.example.jade.Camera;
import org.example.jade.GameObject;
import org.example.jade.Window;
import org.example.physics2d.Physics2D;
import org.example.physics2d.components.Rigidbody2D;
import org.example.util.AssetPool;
import org.jbox2d.dynamics.contacts.Contact;
import org.joml.Vector2f;

public class GoombaAI extends Component {
    private transient boolean goingRight = false;
    private transient Rigidbody2D rb;
    private transient float walkSpeed = 0.6f;
    private transient Vector2f velocity = new Vector2f();
    private transient Vector2f acceleration = new Vector2f();
    private transient Vector2f terminalVelocity = new Vector2f();
    private transient boolean onGround = false;
    private transient boolean isDead = false;
    private transient float timeToKill = 0.5f;
    private transient StateMachine stateMachine;

    @Override
    public void start() {
        this.stateMachine = gameObject.getComponent(StateMachine.class);
        this.rb = gameObject.getComponent(Rigidbody2D.class);
        this.acceleration.y = Window.getPhysics().getGravity().y * 0.7f;
    }

    // di chuyển tu động
    @Override
    public void update(float dt) {
        System.out.println("update goombaai");
        Camera camera = Window.getScene().camera();
//        kiểm tra xem vị trí của đối tượng có vượt quá vị trí của camera
        if (this.gameObject.transform.position.x > camera.position.x + camera.getProjectionSize().x * camera.getZoom()) {
//            có thì ko xử lí gì
            return;
        }
//        nếu đã chết
        if (isDead) {
//            đếm ngược
            timeToKill -= dt;
            if (timeToKill <= 0) {
//                xóa đối tượng
                this.gameObject.destroy();
            }
//            thay đổi vận tốc về 0
            this.rb.setVelocity(new Vector2f());
            return;
        }
        if (goingRight) {
            velocity.x = walkSpeed;
        } else {
            velocity.x = -walkSpeed;
        }

        checkOnGround();
//        nếu đang ở trên mặt đất
        if (onGround) {
//          vận tốc dọc và gia tốc dọc = 0
            this.acceleration.y = 0;
            this.velocity.y = 0;
        } else {
//            nếu ko ở trên mặt đất sẽ bị ảnh hưởng bởi trọng lực
            this.acceleration.y = Window.getPhysics().getGravity().y * 0.7f;
        }
        this.velocity.y += this.acceleration.y * dt;
        this.velocity.y = Math.max(Math.min(this.velocity.y, this.terminalVelocity.y), -terminalVelocity.y);
        this.rb.setVelocity(velocity);
        if (this.gameObject.transform.position.x < Window.getScene().camera().position.x - 0.5f) {
            this.gameObject.destroy();
        }
    }

    public void checkOnGround() {
        System.out.println("check tren md GoomBa");
        float innerPlayerWidth = 0.25f * 0.7f;
        float yVal = -0.14f;
        onGround = Physics2D.checkOnGround(this.gameObject, innerPlayerWidth, yVal);
    }

    // xử lí va chạm
    @Override
    public void preSolve(GameObject obj, Contact contact, Vector2f contactNormal) {
        System.out.println("preSolve Goomba");
//        nếu chết rồi thì ko xử lí gì
        if (isDead) {
            return;
        }
//        lấy ra đối tượng va chạm
        PlayerController playerController = obj.getComponent(PlayerController.class);
        if (playerController != null) {
//            nếu nv ko chết và ko trong tg bất tử khi bị thương và va chạm ở dưới
            if (!playerController.isDead() && !playerController.isHurtInvincible() &&
                    contactNormal.y > 0.58f) {
//                đẩy người chơi lên
                playerController.enemyBounce();
//                thực hiện hàm đạp lên
                stomp();
            }
//            nếu người chơi ko trong trạng thái bất tử
            else if (!playerController.isDead() && !playerController.isInvincible()) {
//                thực hiện hàm die() để xử lí người chơi
                playerController.die();
//                nếu người chơi vẫn ko chết thì va chạm sẽ bị vô hiệu hóa
                if (!playerController.isDead()) {
                    contact.setEnabled(false);
                }
            } else if (!playerController.isDead() && playerController.isInvincible()) {
                contact.setEnabled(false);
            }
        }
//        va chạm giữa các đối tượng khác ko phải vs ng chơi
        else if (Math.abs(contactNormal.y) < 0.1f) {
//            đổi chiều  vật thể đó
            goingRight = contactNormal.x < 0;
        }
//       nếu đối tượng xảy ra va chạm là 1 quả bóng lửa
        if (obj.getComponent(Fireball.class) != null) {
            stomp();
//            sau đó fireball sẽ biến mất
            obj.getComponent(Fireball.class).disappear();
        }
    }

    public void stomp() {
        stomp(true);
    }

    // nv đè lên địch
    public void stomp(boolean playSound) {
        this.isDead = true;
        this.velocity.zero();
        this.rb.setVelocity(new Vector2f());
        this.rb.setAngularVelocity(0.0f);
        this.rb.setGravityScale(0.0f);
        this.stateMachine.trigger("squashMe");
        this.rb.setIsSensor();
        if (playSound) {
            AssetPool.getSound("assets/sounds/bump.ogg").play();
        }
    }
}

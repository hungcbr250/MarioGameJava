package org.example.components;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.example.jade.*;
import org.example.manager.GameManager;
import org.example.manager.PlayerData;
import org.example.physics2d.Physics2D;
import org.example.physics2d.components.PillboxCollider;
import org.example.physics2d.components.Rigidbody2D;
import org.example.physics2d.enums.BodyType;
import org.example.scenes.LevelEditorSceneInitializer;
import org.example.scenes.LevelSceneInitializer;
import org.example.util.AssetPool;
import org.jbox2d.dynamics.contacts.Contact;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.io.*;
import java.util.*;

import static org.lwjgl.glfw.GLFW.*;

public class PlayerController extends Component {
    private enum PlayerState {
        //        trạng thái của nhân vật
        Small,
        Big,
        Fire,
        Invincible
    }

    public float walkSpeed = 1.9f;
    public float jumpBoost = 1.0f;
    public float jumpImpulse = 3.0f;
    public float slowDownForce = 0.05f;
    public Vector2f terminalVelocity = new Vector2f(2.1f, 3.1f);

    private PlayerState playerState = PlayerState.Small;
    public transient boolean onGround = false;
    private transient float groundDebounce = 0.0f;
    private transient float groundDebounceTime = 0.1f;
    private transient Rigidbody2D rb;
    private transient StateMachine stateMachine;
    private transient float bigJumpBoostFactor = 1.05f;
    private transient float playerWidth = 0.25f;
    private transient int jumpTime = 0;
    private transient Vector2f acceleration = new Vector2f();
    private transient Vector2f velocity = new Vector2f();
    private transient boolean isDead = false;
    private transient int enemyBounce = 0;

    private transient float hurtInvincibilityTimeLeft = 0;
    private transient float hurtInvincibilityTime = 1.4f;
    private transient float deadMaxHeight = 0;
    private transient float deadMinHeight = 0;
    private transient boolean deadGoingUp = true;
    private transient float blinkTime = 0.0f;
    private transient SpriteRenderer spr;

    private transient boolean playWinAnimation = false;
    private transient float timeToCastle = 4.5f;
    private transient float walkTime = 2.2f;
    private ImGuiLayer imGuiLayer;
    private boolean isPowerupActive = false; //  kiểm soát biến hình
    private boolean stopClick = false; // kiểm soát bấm liên tục
    private int numberMouseClick = 1;
    private static int score = 0;
    private int recordScore;
    PlayerData playerData = GameManager.getInstance().getPlayerData();
    List<PlayerData> playerDataList;

    @Override
    public void start() {
        this.spr = gameObject.getComponent(SpriteRenderer.class);
        this.rb = gameObject.getComponent(Rigidbody2D.class);
        this.stateMachine = gameObject.getComponent(StateMachine.class);
        this.rb.setGravityScale(0.0f);
        imGuiLayer = new ImGuiLayer();
        //loadScore();
    }

//    public PlayerData getPlayerData() {
//        playerData = GameManager.getInstance().getPlayerData(1);
//        return playerData;
//    }

    @Override
    public void update(float dt) {
        System.out.println("test2"+GameManager.getInstance().getPlayerData().getName());

        System.out.println("update playcontroller");
        checkMuaDo();
//        kiểm tra xem người chơi có đang ở trong trạng thái winAnimation ko
        if (playWinAnimation) {
            checkOnGround();
            if (!onGround) {
//              co tỉ lệ nhân vật
                gameObject.transform.scale.x = -0.25f;
//                giảm độ cao theo thời gian
                gameObject.transform.position.y -= dt;
//               chuyển
                stateMachine.trigger("stopRunning");
                stateMachine.trigger("stopJumping");
            } else {
//                kiểm tra xem còn thời gian
                if (this.walkTime > 0) {
                    gameObject.transform.scale.x = 0.25f;
//                  cập nhật quãng đường
                    //   gameObject.transform.position.x += dt;
                    stateMachine.trigger("startRunning");
                }
                if (!AssetPool.getSound("assets/sounds/stage_clear.ogg").isPlaying()) {
                    AssetPool.getSound("assets/sounds/stage_clear.ogg").play();
                }
                timeToCastle -= dt;
                walkTime -= dt;

                if (timeToCastle <= 0) {
                    if (Window.RELEASE_BUILD) {
                        // NOTE: Just infinitely loop. If you wanted additional levels
                        //       you could set up some state to figure out which level
                        //       is next and then load that in the LevelSceneInitializer
                        Window.changeScene(new LevelSceneInitializer());
                    } else {
                        Window.changeScene(new LevelEditorSceneInitializer());
                    }
                }
            }
            return;
        }
        if (isDead) {
//            kiểm tra vị trí hiện tại rồi cho nv lên và xuống
//            kiểm tra vị trí hiện tại của nv xem có ở dưới vị trí chết cao nhất không
            if (this.gameObject.transform.position.y < deadMaxHeight && deadGoingUp) {
//                di chuyển nv lên vị trí ..
                this.gameObject.transform.position.y += dt * walkSpeed / 2.0f;
            } else if (this.gameObject.transform.position.y >= deadMaxHeight && deadGoingUp) {
//                để ngăn nv tiếp tục di chuyển lên trên
                deadGoingUp = false;
            }
//            kiểm tra xem vtri hiện tại có cao hơn vị trí deadMinHeight ko và deadGoingUp = false
            else if (!deadGoingUp && gameObject.transform.position.y > deadMinHeight) {
                this.rb.setBodyType(BodyType.Kinematic);
                this.acceleration.y = Window.getPhysics().getGravity().y * 0.7f;
                this.velocity.y += this.acceleration.y * dt;
                this.velocity.y = Math.max(Math.min(this.velocity.y, this.terminalVelocity.y), -this.terminalVelocity.y);
                this.rb.setVelocity(this.velocity);
//              vận tốc góc =0 để nv ko b quay
                this.rb.setAngularVelocity(0);
            } else if (!deadGoingUp && gameObject.transform.position.y <= deadMinHeight) {
                Window.changeScene(new LevelSceneInitializer());
            }
            return;
        }
//    nguời chơi đang trong trạng thái bất tử
        if (hurtInvincibilityTimeLeft > 0) {
//            giảm dần tg bất tử
            hurtInvincibilityTimeLeft -= dt;
            blinkTime -= dt;

            if (blinkTime <= 0) {
                blinkTime = 0.2f;
//                ==1 => màu trắng
                if (spr.getColor().w == 1) {
                    spr.setColor(new Vector4f(1, 1, 1, 0));
                } else {
//                    chuyển thành màu trong suốt
                    spr.setColor(new Vector4f(1, 1, 1, 1));
                }
            } else {
//                ==0 => ko trong suốt
                if (spr.getColor().w == 0) {
                    spr.setColor(new Vector4f(1, 1, 1, 1));
                }
            }
        }
        if (KeyListener.isKeyPressed(GLFW_KEY_RIGHT) || KeyListener.isKeyPressed(GLFW_KEY_D)) {
            System.out.println("D");
//            thiết lập tỉ lệ nv
            this.gameObject.transform.scale.x = playerWidth;
            this.acceleration.x = walkSpeed * 3;
//          v âm tức là đang đi ngược hướng muốn di chuyển
            if (this.velocity.x < 0) {
                this.stateMachine.trigger("switchDirection");
                this.velocity.x += slowDownForce; // làm chậm lại tốc độ di chuyển
            } else {
//                bắt đầu đi
                this.stateMachine.trigger("startRunning");
            }
        } else if (KeyListener.isKeyPressed(GLFW_KEY_LEFT) || KeyListener.isKeyPressed(GLFW_KEY_A)) {
            System.out.println("A");
            this.gameObject.transform.scale.x = -playerWidth;
            this.acceleration.x = -walkSpeed * 3;

            if (this.velocity.x > 0) {
                this.stateMachine.trigger("switchDirection");
                this.velocity.x -= slowDownForce;
            } else {
                this.stateMachine.trigger("startRunning");
            }
        } else {
            this.acceleration.x = 0;
            if (this.velocity.x > 0) {
                //giảm tốc độ di chuyển,
                this.velocity.x = Math.max(0, this.velocity.x - slowDownForce);
            } else if (this.velocity.x < 0) {
                this.velocity.x = Math.min(0, this.velocity.x + slowDownForce);
            }

            if (this.velocity.x == 0) {
                this.stateMachine.trigger("stopRunning");
            }
        }

        if (KeyListener.keyBeginPress(GLFW_KEY_E) && playerState == PlayerState.Fire &&
                Fireball.canSpawn()) {
//             tạo một vị trí mới dựa trên vị trí và hướng của người chơi, sau đó tạo một GameObject
//             mới để đại diện cho quả bóng lửa và đặt nó ở vị trí được tính toán.
            Vector2f position = new Vector2f(this.gameObject.transform.position)
                    .add(this.gameObject.transform.scale.x > 0
                            ? new Vector2f(0.26f, 0)
                            : new Vector2f(-0.26f, 0));
            GameObject fireball = Prefabs.generateFireball(position);
//            fireball di chuyển theo hướng người chơi
            fireball.getComponent(Fireball.class).goingRight =
                    this.gameObject.transform.scale.x > 0;
            Window.getScene().addGameObjectToScene(fireball);
        }
        checkOnGround();
//        nhảy
        if (KeyListener.isKeyPressed(GLFW_KEY_SPACE) && (jumpTime > 0 || onGround || groundDebounce > 0)) {
            if ((onGround || groundDebounce > 0) && jumpTime == 0) {
                AssetPool.getSound("assets/sounds/jump-small.ogg").play();
                jumpTime = 28;
                this.velocity.y = jumpImpulse; //độ giất khi nhảy
            } else if (jumpTime > 0) {
//            kiểm soát độ cao và thời gian của nhảy
                jumpTime--;
//                nhảy cao hơn khi giữa nút space lâu hơn
                this.velocity.y = ((jumpTime / 2.2f) * jumpBoost);
            } else {
                this.velocity.y = 0;
            }
//            để  nhân vật thực hiện nhảy tiếp
            groundDebounce = 0;
        } else if (enemyBounce > 0) {
            enemyBounce--;
//            tạo hiệu ứng nảy
            this.velocity.y = ((enemyBounce / 2.2f) * jumpBoost);
        } else if (!onGround) {
            if (this.jumpTime > 0) {
//                giảm 35% v.y tạo hiệu ứng giảm dần của vận tốc
                this.velocity.y *= 0.35f;
//                nv ko còn time đẻ nhảy nữa
                this.jumpTime = 0;
            }
            groundDebounce -= dt;
            this.acceleration.y = Window.getPhysics().getGravity().y * 0.7f;
        } else {
            this.velocity.y = 0;
            this.acceleration.y = 0;
            groundDebounce = groundDebounceTime;
        }
        // cập nật vận tốc
        this.velocity.x += this.acceleration.x * dt;
        this.velocity.y += this.acceleration.y * dt;
        this.velocity.x = Math.max(Math.min(this.velocity.x, this.terminalVelocity.x), -this.terminalVelocity.x);
        this.velocity.y = Math.max(Math.min(this.velocity.y, this.terminalVelocity.y), -this.terminalVelocity.y);
        this.rb.setVelocity(this.velocity);
        this.rb.setAngularVelocity(0);

        if (!onGround) {
            stateMachine.trigger("jump");
        } else {
            stateMachine.trigger("stopJumping");
        }
    }

    //    kiểm tra xem có đang đứng trên mặt đất ko
    public void checkOnGround() {
        System.out.println("check trên mặt đất playerController");
//        xác định chiều rộng hiệu quả
        float innerPlayerWidth = this.playerWidth * 0.6f;
        float yVal = playerState == PlayerState.Small ? -0.14f : -0.24f;
//        kiểm tra xem có ở trên mặt đắt ko onGround =true = dang ở trên mặt đất
        onGround = Physics2D.checkOnGround(this.gameObject, innerPlayerWidth, yVal);
    }

    //    cập nhật vị trí
    public void setPosition(Vector2f newPos) {
        System.out.println("cập nhật vị trí");
        this.gameObject.transform.position.set(newPos);
        this.rb.setPosition(newPos);
    }

    //chuyển đổi trạng thái nhân vật
    public void powerup() {
        System.out.println("powerup PlayerContr");
        if (playerState == PlayerState.Small) {
            playerState = PlayerState.Big;
            AssetPool.getSound("assets/sounds/powerup.ogg").play();
//            thay đổi chiều cao nhân vật
            gameObject.transform.scale.y = 0.42f;
            PillboxCollider pb = gameObject.getComponent(PillboxCollider.class);
//            kiểm tra xem có ăn được thuốc hay ko
            if (pb != null) {
//                tăng sức mạnh nhân vật
                jumpBoost *= bigJumpBoostFactor;
                walkSpeed *= bigJumpBoostFactor;
                pb.setHeight(0.42f);
            }
        } else if (playerState == PlayerState.Big) {
            playerState = PlayerState.Fire;
            AssetPool.getSound("assets/sounds/powerup.ogg").play();
        }
//  gửi tín hiệu tới stateMachine để kích hoạt hành động của nhân vật
        stateMachine.trigger("powerup");
    }


    //cờ
    public void playWinAnimation(GameObject flagpole) {
        System.out.println("playWinAni");
//        kiểm tra xem playWinAnimation đã được kích hoạt hay chưa,nếu rồi ko thực hiện nữa
        if (!playWinAnimation) {
            playWinAnimation = true;
//        cho nhân vật đứng yên
            velocity.set(0.0f, 0.0f);
            acceleration.set(0.0f, 0.0f);
            rb.setVelocity(velocity);
//            ko va chamj
            rb.setIsSensor();
            rb.setBodyType(BodyType.Static);
//            đặt vị trí nhân vật = ví trí cột co
            gameObject.transform.position.x = flagpole.transform.position.x;
            AssetPool.getSound("assets/sounds/main-theme-overworld.ogg").stop();
            AssetPool.getSound("assets/sounds/flagpole.ogg").play();
        }
    }

    //    xử lí va chạm giữa nv và mặt đắt
    @Override
    public void beginCollision(GameObject collidingObject, Contact contact, Vector2f contactNormal) {
        System.out.println("BeginCollision Playcontroller");
//        nếu nv đã chết thì ko thực hiện gì
        if (isDead) return;
//        kiểm tra xem đối tượng va chạm có phải mặt đất ko
        if (collidingObject.getComponent(Ground.class) != null) {
            if (Math.abs(contactNormal.x) > 0.8f) {
//                nếu va chạm với b mặt ngang của mặt đất thì vx=0 ,vật cản ở trc mặt
                this.velocity.x = 0;
            } else if (contactNormal.y > 0.8f) {
//                nếu va chạm từ phía trên thi dặt... vật cản ở dưới chân
                this.velocity.y = 0;
                this.acceleration.y = 0;
                this.jumpTime = 0;
            }
        }
    }

    public void enemyBounce() {
        this.enemyBounce = 8;
    }

    public boolean isDead() {
        return this.isDead;
    }

    //xác định người chơi xem có đang ở trong trạng thái bất tử sau khi bị thương ko
    public boolean isHurtInvincible() {
        return this.hurtInvincibilityTimeLeft > 0 || playWinAnimation;
    }

    // xác định xem người chơi có đang trong trạng thái bất tử ko
    public boolean isInvincible() {
        return this.playerState == PlayerState.Invincible ||
                this.hurtInvincibilityTimeLeft > 0 || playWinAnimation;
    }

    public void die() {
        this.stateMachine.trigger("die");
//        Nếu đang ở trạng thái small
        if (this.playerState == PlayerState.Small) {
//            ngừng chuyển động
            this.velocity.set(0, 0);
            this.acceleration.set(0, 0);
            this.rb.setVelocity(new Vector2f());
//            đặt thành trạng thái chết
            this.isDead = true;
//            không tương tác với các vật thể khác
            this.rb.setIsSensor();
            AssetPool.getSound("assets/sounds/main-theme-overworld.ogg").stop();
            AssetPool.getSound("assets/sounds/mario_die.ogg").play();
//            Xác định vị trí tối đa và tối thiểu của người chơi khi chết, để sau khi chết,
//            người chơi sẽ rơi từ vị trí tối đa đến tối thiểu và sau đó trở lại màn chơi mới.
            deadMaxHeight = this.gameObject.transform.position.y + 0.3f;
            this.rb.setBodyType(BodyType.Static);
            if (gameObject.transform.position.y > 0) {
                deadMinHeight = -0.25f;
            }
            playerData=GameManager.getInstance().getPlayerData();
            GameManager.getInstance().updatePlayerDie(playerData.getId(),0);
            score = 0;
           // GameManager.getInstance().UpdateScoreJson(1);


//            nếu đang ở trạng thái big
        } else if (this.playerState == PlayerState.Big) {
//            chuyển về trạng thái small
            this.playerState = PlayerState.Small;
//            cập nhật lại chiều cao
            gameObject.transform.scale.y = 0.25f;
            PillboxCollider pb = gameObject.getComponent(PillboxCollider.class);
//            cập nhật lại chỉ số
            if (pb != null) {
                jumpBoost /= bigJumpBoostFactor;
                walkSpeed /= bigJumpBoostFactor;
                pb.setHeight(0.25f);
            }
//            đặt lại thời gian bất tử khi bị thương
            hurtInvincibilityTimeLeft = hurtInvincibilityTime;
            AssetPool.getSound("assets/sounds/pipe.ogg").play();
        } else if (playerState == PlayerState.Fire) {
//            chuyển về trạng thái big nếu đang ở trang thái fire
            this.playerState = PlayerState.Big;
//            đặt lại thời gian bất tử
            hurtInvincibilityTimeLeft = hurtInvincibilityTime;
            AssetPool.getSound("assets/sounds/pipe.ogg").play();
        }
    }

    public boolean hasWon() {
        return false;
    }
    public boolean isSmall() {
        return this.playerState == PlayerState.Small;
    }

    public int getScore() {
        return GameManager.getInstance().getPlayerData().getScore();
    }

    public void powerupBuyEqip() {
        if (!isPowerupActive) {
            isPowerupActive = true;
            playerState = PlayerState.Fire;
            gameObject.transform.scale.y = 0.42f;
            PillboxCollider pb = gameObject.getComponent(PillboxCollider.class);
            jumpBoost *= bigJumpBoostFactor;
            walkSpeed *= bigJumpBoostFactor;
            pb.setHeight(0.42f);
            stateMachine.trigger("powerup");

            // Tạo một TimerTask để quay về trạng thái ban đầu sau .. giây
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    isPowerupActive = false;
                    playerState = PlayerState.Small;
                    gameObject.transform.scale.y = 0.25f;
                    PillboxCollider pb = gameObject.getComponent(PillboxCollider.class);
                    if (pb != null) {
                        jumpBoost /= bigJumpBoostFactor;
                        walkSpeed /= bigJumpBoostFactor;
                        pb.setHeight(0.25f);
                    }
                    stopClick = false;
                    timer.cancel();
                }
            }, 5000);
        }
    }

    public void checkMuaDo() {
        if (imGuiLayer.buyPressed()) {
            if (!stopClick) {
                if (getScore() < 50) {
                    imGuiLayer.buyNotPressed();
                } else {
                    int idPlayer=GameManager.getInstance().getPlayerData().getId();
                    GameManager.getInstance().subScore(idPlayer, 50);
                    powerupBuyEqip();
                    imGuiLayer.buyNotPressed();
                    this.stopClick = true;
                }
            } else {
                imGuiLayer.buyNotPressed();
            }
        }
    }

    // lưu score vào file
//    public void saveScore() {
//        BufferedWriter writer = null;
//        try {
//            writer = new BufferedWriter(new FileWriter("score.txt", true));
//            writer.write(Integer.toString(score));
//            writer.newLine();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        } finally {
//            try {
//                if (writer != null) {
//                    writer.close();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//    public void loadScore() {
//        BufferedReader reader = null;
//        try {
//            reader = new BufferedReader(new FileReader("score.txt"));
//            String line;
//            List<String> lines = new ArrayList<>();
//            while ((line = reader.readLine()) != null) {
//                lines.add(line);
//            }
//            if (!lines.isEmpty()) {
//                String lastLine = lines.get(lines.size() - 1);
//                score = Integer.parseInt(lastLine);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

//    public int loadRecordScore() {
//        BufferedReader reader = null;
//        try {
//            reader = new BufferedReader(new FileReader("score.txt"));
//            String line;
//            List<String> lines = new ArrayList<>();
//            while ((line = reader.readLine()) != null) {
//                lines.add(line);
//            }
//            if (!lines.isEmpty()) {
//                this.recordScore = lines.stream().mapToInt(s -> Integer.parseInt(s)).max().getAsInt();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return recordScore;
//    }

    public static void main(String[] args) {
        GameManager gameManager = new GameManager();
        for (Map.Entry<Integer, PlayerData> entry : gameManager.getMapPlayer().entrySet()) {
            int id = entry.getKey();
            PlayerData playerData = entry.getValue();
            System.out.println("ID: " + id + ", Player Data: " + playerData.toString());
        }

    }

}

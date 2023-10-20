package org.example.components;

import org.example.jade.Direction;
import org.example.jade.GameObject;
import org.example.jade.KeyListener;
import org.example.jade.Window;
import org.example.util.AssetPool;
import org.jbox2d.dynamics.contacts.Contact;
import org.joml.Vector2f;

import static org.lwjgl.glfw.GLFW.*;

public class Pipe extends Component {
    private Direction direction;
    private String connectingPipeName = "";
    private boolean isEntrance = false;
    private transient GameObject connectingPipe = null;
    private transient float entranceVectorTolerance = 0.6f;
    private transient PlayerController collidingPlayer = null;

    public Pipe(Direction direction) {
        this.direction = direction;
    }

    @Override
    public void start() {
        connectingPipe = Window.getScene().getGameObject(connectingPipeName);
    }

    @Override
    public void update(float dt) {
        System.out.println("update Pipe");
//        kiểm tra xem có cổng nào được kết nối ko
        if (connectingPipe == null) {
            return;
        }
//       có xảy ra va chạm
        if (collidingPlayer != null) {
            boolean playerEntering = false;
            switch (direction) {
                case Up:
                    if ((KeyListener.isKeyPressed(GLFW_KEY_DOWN)
                            || KeyListener.isKeyPressed(GLFW_KEY_S))
                            && isEntrance
                            && playerAtEntrance()) {
                        playerEntering = true;
                    }
                    break;
                case Left:
                    if ((KeyListener.isKeyPressed(GLFW_KEY_RIGHT)
                            || KeyListener.isKeyPressed(GLFW_KEY_D))
                            && isEntrance
                            && playerAtEntrance()) {
                        playerEntering = true;
                    }
                    break;
                case Right:
                    if ((KeyListener.isKeyPressed(GLFW_KEY_LEFT)
                            || KeyListener.isKeyPressed(GLFW_KEY_A))
                            && isEntrance
                            && playerAtEntrance()) {
                        playerEntering = true;
                    }
                    break;
                case Down:
                    if ((KeyListener.isKeyPressed(GLFW_KEY_UP)
                            || KeyListener.isKeyPressed(GLFW_KEY_W))
                            && isEntrance
                            && playerAtEntrance()) {
                        playerEntering = true;
                    }

                    break;
            }
// chuyển đến vị trí pipe connect
            if (playerEntering) {
                collidingPlayer.setPosition(
                        getPlayerPosition(connectingPipe)
                );
                AssetPool.getSound("assets/sounds/pipe.ogg").play();
            }
        }
    }

    public boolean playerAtEntrance() {
//        nếu ko có va chạm
        if (collidingPlayer == null) {
            return false;
        }
// xác định vị trí và giới hạn của cổng và người chơi trong không gian và thực hiện kiểm tra
// xem người chơi có đứng gần cổng hay không để có thể thực hiện hành động.
        Vector2f min = new Vector2f(gameObject.transform.position).
                sub(new Vector2f(gameObject.transform.scale).mul(0.5f));//góc dưới bên trái hcn Pipe
        Vector2f max = new Vector2f(gameObject.transform.position).
                add(new Vector2f(gameObject.transform.scale).mul(0.5f));//góc trên bên phải hcn Pipe
        Vector2f playerMax = new Vector2f(collidingPlayer.gameObject.transform.position).
                add(new Vector2f(collidingPlayer.gameObject.transform.scale).mul(0.5f));//góc trên bên phải hcn player
        Vector2f playerMin = new Vector2f(collidingPlayer.gameObject.transform.position).
                sub(new Vector2f(collidingPlayer.gameObject.transform.scale).mul(0.5f));//góc dươi bên trái hcn player

        switch (direction) {
            case Up:
                return playerMin.y >= max.y &&
                        playerMax.x > min.x &&
                        playerMin.x < max.x;
            case Down:
                return playerMax.y <= min.y &&
                        playerMax.x > min.x &&
                        playerMin.x < max.x;
            case Right:
                return playerMin.x >= max.x &&
                        playerMax.y > min.y &&
                        playerMin.y < max.y;
            case Left:
                return playerMin.x <= min.x &&
                        playerMax.y > min.y &&
                        playerMin.y < max.y;
        }
        return false;
    }

    @Override
    public void beginCollision(GameObject collidingObject, Contact contact, Vector2f contactNormal) {
        PlayerController playerController = collidingObject.getComponent(PlayerController.class);
        if (playerController != null) {
            collidingPlayer = playerController;
        }
    }

    @Override
    public void endCollision(GameObject collidingObject, Contact contact, Vector2f contactNormal) {
        PlayerController playerController = collidingObject.getComponent(PlayerController.class);
        if (playerController != null) {
            collidingPlayer = null;
        }
    }

    private Vector2f getPlayerPosition(GameObject pipe) {
        Pipe pipeComponent = pipe.getComponent(Pipe.class);
        switch (pipeComponent.direction) {
            case Up:
                return new Vector2f(pipe.transform.position).add(0.0f, 0.5f);
            case Left:
                return new Vector2f(pipe.transform.position).add(-0.5f, 0.0f);
            case Right:
                return new Vector2f(pipe.transform.position).add(0.5f, 0.0f);
            case Down:
                return new Vector2f(pipe.transform.position).add(0.0f, -0.5f);
        }
        return new Vector2f();
    }

}

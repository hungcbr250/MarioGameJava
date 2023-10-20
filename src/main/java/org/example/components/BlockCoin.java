package org.example.components;

import org.example.util.AssetPool;
import org.joml.Vector2f;

public class BlockCoin extends Component {
    private Vector2f topY;
    private float coinSpeed = 1.4f;

    @Override
    public void start() {
//        vị trí mà coin sẽ bay lên
        topY = new Vector2f(this.gameObject.transform.position.y).add(0, 0.5f);
        AssetPool.getSound("assets/sounds/coin.ogg").play();
    }

    @Override
    public void update(float dt) {
//    nếu vị trí hiện tại nhỏ hơn vị trí sẽ bay lên
        if (this.gameObject.transform.position.y < topY.y) {
//            đồng tiền di chuyển lên vị trí
            this.gameObject.transform.position.y += dt * coinSpeed;
//            làm nhỏ dần đồng tiền scale.x sẽ không nhỏ hơn -1.0f
            this.gameObject.transform.scale.x -= (0.5f * dt) % -1.0f;
        } else {
//            khi v trí của nó lơn hơn topY thì nó sẽ bị hủy bỏ ko còn tồn tại nữa
            gameObject.destroy();
        }
    }
}

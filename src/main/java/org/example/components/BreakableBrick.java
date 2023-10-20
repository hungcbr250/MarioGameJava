package org.example.components;

import org.example.util.AssetPool;

public class BreakableBrick extends Block{
    @Override
    public void playerHit(PlayerController playerController) {
        if (!playerController.isSmall()) {
            AssetPool.getSound("assets/sounds/break_block.ogg").play();
            gameObject.destroy();
        }
    }

}

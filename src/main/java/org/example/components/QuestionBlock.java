package org.example.components;

import org.example.jade.GameObject;
import org.example.jade.Prefabs;
import org.example.jade.Window;
import org.example.manager.GameManager;
import org.example.manager.PlayerData;

public class QuestionBlock extends Block {
    private enum BlockType {
        Coin,
        Powerup,
        Invincibility
    }

    public BlockType blockType = BlockType.Coin;
    private static int score = 0;
    private PlayerController p = new PlayerController();

    @Override
    void playerHit(PlayerController playerController) {
        switch (blockType) {
            case Coin:
                doPowerup(playerController);
                break;
            case Powerup:
                doPowerup(playerController);
                break;
            case Invincibility:
                doInvincibility(playerController);
                break;
        }
        StateMachine stateMachine = gameObject.getComponent(StateMachine.class);
        if (stateMachine != null) {
            stateMachine.trigger("setInactive");
            this.setInactive();
        }
    }

    private void doInvincibility(PlayerController playerController) {
    }

    private void doPowerup(PlayerController playerController) {
        if (playerController.isSmall()) {
           // p.setSoreMushFLow(20);
            int idPlayer=GameManager.getInstance().getPlayerData().getId();
            GameManager.getInstance().updatePlayer(idPlayer,20);
            spawnMushroom();
        } else {
          //  p.setSoreMushFLow(30);
            int idPlayer=GameManager.getInstance().getPlayerData().getId();
            GameManager.getInstance().updatePlayer(idPlayer,30);
            spawnFlower();
        }
    }

    private void doCoin(PlayerController playerController) {
        GameObject coin = Prefabs.generateBlockCoin();
        coin.transform.position.set(this.gameObject.transform.position);
        coin.transform.position.y += 0.25f;
        Window.getScene().addGameObjectToScene(coin);
    }

    private void spawnMushroom() {
        GameObject mushroom = Prefabs.generateMushroom();
//        đặt vị tri của nấm
        mushroom.transform.position.set(gameObject.transform.position);
        mushroom.transform.position.y += 0.25f;
//        hiển thị ra màn hình
        Window.getScene().addGameObjectToScene(mushroom);
    }

    private void spawnFlower() {
        GameObject flower = Prefabs.generateFlower();
        flower.transform.position.set(gameObject.transform.position);
        flower.transform.position.y += 0.25f;
        Window.getScene().addGameObjectToScene(flower);
    }
}

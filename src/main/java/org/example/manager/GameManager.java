package org.example.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameManager {
    private static GameManager instance;
    PlayerData playerData;
    List<PlayerData> playerDataList;

    public Map<Integer, PlayerData> mapPlayer = new HashMap<>();

    public static GameManager getInstance() {
        if (instance != null)
            return instance;
        instance = new GameManager();
        return instance;
    }

    public GameManager() {
        init();
    }

    public void init() {
        playerDataList = loadPlayerData();//load data từ json
        for (PlayerData data : playerDataList) {
            mapPlayer.put(data.getId(), data);
        }
    }

    public void updatePlayer(int playerId, int score) {
        PlayerData player = mapPlayer.get(playerId);
        if (player != null) {
            player.setScore(player.getScore() + score);
            if(player.getScore() >= player.getMaxScore()){
                player.setMaxScore(player.getScore());
            }
            mapPlayer.put(playerId, player); // Cập nhật lại đối tượng trong map
            UpdateScoreJson();
        }
    }
    public void updatePlayerDie(int playerId, int score) {
        PlayerData player = mapPlayer.get(playerId);
        if (player != null) {
            player.setScore(score);
            mapPlayer.put(playerId, player); // Cập nhật lại đối tượng trong map
            UpdateScoreJson();
        }
    }

    public void subScore(int playerId, int score) {
        PlayerData player = mapPlayer.get(playerId);
        if (player != null) {
            player.setScore(player.getScore() - score);
            mapPlayer.put(playerId, player); // Cập nhật lại đối tượng trong map
            UpdateScoreJson();
        }
    }


    public static void setInstance(GameManager instance) {
        GameManager.instance = instance;
    }

    public PlayerData getPlayerData() {
        return playerData;
    }

    public void setPlayerData(PlayerData playerData) {
        this.playerData = playerData;
    }

    public List<PlayerData> getPlayerDataList() {
        return playerDataList;
    }

    public void setPlayerDataList(List<PlayerData> playerDataList) {
        this.playerDataList = playerDataList;
    }

    public Map<Integer, PlayerData> getMapPlayer() {
        return mapPlayer;
    }

    public void setMapPlayer(Map<Integer, PlayerData> mapPlayer) {
        this.mapPlayer = mapPlayer;
    }


    public PlayerData getPlayerData(int id) {
        return mapPlayer.get(1);
    }

    public void UpdateScoreJson() {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();

        try (Reader reader = new FileReader("scorejson.txt")) {
            Type playerListType = new TypeToken<List<PlayerData>>() {}.getType();
            playerDataList = gson.fromJson(reader, playerListType);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (PlayerData playerData : playerDataList) {
            if (playerData.getId() == getPlayerData().getId()) {
                playerData.setScore(getPlayerData().getScore());
                if (getPlayerData().getScore() >= getPlayerData().getMaxScore()) {
                    playerData.setMaxScore(getPlayerData().getScore());
                }
                break;
            }
        }
        try (Writer writer = new FileWriter("scorejson.txt")) {
            gson.toJson(playerDataList, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public List<PlayerData> loadPlayerData() {
        List<PlayerData> playerDataList = new ArrayList<>();
        Gson gson = new Gson();
        try {
            FileReader reader = new FileReader("scorejson.txt");
//            chỉ định kiểu dữ liệu mà bạn muốn Gson chuyển đổi
            Type playerListType = new TypeToken<List<PlayerData>>() {
            }.getType();
//            đọc dữ liệu từ reader rồi chuyển thành kiểu playerListType
            playerDataList = gson.fromJson(reader, playerListType);
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return playerDataList;
    }

    public PlayerData getPlayerByName(String tenNhap) {
        for (Map.Entry<Integer, PlayerData> entry : getMapPlayer().entrySet()) {
            if (tenNhap.equalsIgnoreCase(entry.getValue().getName())) {
                return playerData = entry.getValue();
            }
        }
        playerData = new PlayerData(getNextId(), tenNhap, 0, 0);
        mapPlayer.put(playerData.id, playerData);
        return playerData;
    }

    public void savePlayerDataNew() {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        List<PlayerData> playerDataList = loadPlayerData();
        boolean exist = false;
        for (PlayerData data : playerDataList) {
            if (playerData.getName().equalsIgnoreCase(data.getName())) {
                exist = true;
                break;
            }
        }
        if (!exist) {
            playerDataList.add(playerData);
        }
        try {
            FileWriter writer = new FileWriter("scorejson.txt");
            writer.write(gson.toJson(playerDataList));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public List<PlayerData> initHard() {
        playerDataList = new ArrayList<>();
        playerDataList.add(new PlayerData(1, "Hung", 0, 0));
        playerDataList.add(new PlayerData(2, "Hungkk", 0, 0));
        playerDataList.add(new PlayerData(3, "Hungoo", 0, 0));
        return playerDataList;
    }

    public void savePlayerData() {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        try {
            FileWriter writer = new FileWriter("scorejson.txt");
            playerDataList = initHard();
            writer.write(gson.toJson(playerData));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getNextId() {
        int maxId = 0;
        for (Map.Entry<Integer, PlayerData> entry : GameManager.getInstance().getMapPlayer().entrySet()) {
            int id = entry.getKey();
            if (id > maxId) {
                maxId = id;
            }
        }
        return ++maxId;
    }

    public static void main(String[] args) {
//        int i = GameManager.getInstance().getPlayerData().getId();
//        for (Map.Entry<Integer, PlayerData> entry : GameManager.getInstance().getMapPlayer().entrySet()) {
//            int id = entry.getKey();
//            PlayerData playerData = entry.getValue();
//            System.out.println("ID: " + id + ", Player Data: " + playerData.toString());
//        }
//        PlayerData playerData1 = GameManager.getInstance().getPlayerByName("Hungoo");
//        System.out.println("test" + GameManager.getInstance().getPlayerData().getName());
//        PlayerData playerData1 = new PlayerData("hung", 0, 0);
//        PlayerData playerData2 = new PlayerData("hung3", 0, 0);
//        PlayerData playerData3 = new PlayerData("hung3", 0, 0);
//        System.out.println(playerData1.getId());
//        System.out.println(playerData2.getId());
//        System.out.println(playerData3.getId());

//        PlayerData playerData1 = new PlayerData();
//        playerData1.savePlayerData();
        System.out.println(GameManager.getInstance().getNextId());
    }

}

package org.example.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.example.components.Component;
import org.example.components.ComponentDeserializer;
import org.example.jade.GameObject;
import org.example.jade.GameObjectDeserializer;

import javax.annotation.processing.Generated;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class PlayerData {
    int id;
    String name;
    int score;
    int maxScore;
    public List<PlayerData> playerData;

    public PlayerData(int id,String name, int score, int maxScore) {
        this.id = id;
        this.name = name;
        this.score = score;
        this.maxScore = maxScore;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(int maxScore) {
        this.maxScore = maxScore;
    }

    public List<PlayerData> getPlayerData() {
        return playerData;
    }

    public void setPlayerData(List<PlayerData> playerData) {
        this.playerData = playerData;
    }

    public PlayerData() {
    }
    public int getId() {
        return id;
    }

    public int getScore() {
        return score;
    }

    public void subScore(int scoreSub) {
        score -= scoreSub;
        if (score < 0)
            score = 0;
    }
    public static void main(String[] args) {
        PlayerData playerData1 = new PlayerData();
//        List<PlayerData> playerDataList = new ArrayList<>();
//        playerDataList = playerData1.loadPlayerData();
//        System.out.println(playerDataList.get(1).getName());

//        PlayerData data=new PlayerData("hungbb",2,2);
//        PlayerData data2=new PlayerData("hungbb",2,2);
//        System.out.println(data.getId());
//        System.out.println(data2.getId());

    }
}

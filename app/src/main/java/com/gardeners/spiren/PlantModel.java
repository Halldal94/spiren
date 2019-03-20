package com.gardeners.spiren;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class PlantModel {
    private static final DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance();
    private static final List<Level> LEVELS = Arrays.asList(
            new Level(100, 3),
            new Level(150, 5),
            new Level(200, 7),
            new Level(250, 10)
    );

    private int level, length, water, fertilizer, bugs, members;
    private Date previousAction;

    public int getLength(){
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getWater(){
        return water;
    }

    public void setWater(int water) {
        this.water = water;
    }

    public int getFertilizer(){
        return fertilizer;
    }

    public void setFertilizer(int fertilizer) {
        this.fertilizer = fertilizer;
    }

    public int getBugs(){
        return bugs;
    }

    public void setBugs(int bugs) {
        this.bugs = bugs;
    }

    public int getMembers(){
        return members;
    }

    public void setMembers(int members) {
        this.members = members;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getMaxHealth() {
        return LEVELS.get(level - 1).getMaxWater();
    }

    public int getMaxWater() {
        return LEVELS.get(level - 1).getMaxWater();
    }

    public int getMaxBugs() {
        return LEVELS.get(level - 1).getMaxBugs();
    }

    public Date getPreviousAction() {
        return previousAction;
    }

    public void setPreviousAction(Date previousAction) {
        this.previousAction = previousAction;
    }

    public int getHealth(){
        return water - (10 * bugs);
    }

    public JSONObject toJson() {
        JSONObject data = new JSONObject();
        try {
            data.put("length", getLength());
            data.put("water", getWater());
            data.put("fertilizer", getFertilizer());
            data.put("bugs", getBugs());
            data.put("members", getMembers());
            data.put("level", getLevel());
            data.put("previousAction", DATE_FORMAT.format(getPreviousAction()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return data;
    }

    public void fromJson(JSONObject data){
        try {
            length = (Integer) data.get("length");
            water = (Integer) data.get("water");
            fertilizer = (Integer) data.get("fertilizer");
            bugs = (Integer) data.get("bugs");
            members = (Integer) data.get("members");
            level = (Integer) data.get("level");
            previousAction = DATE_FORMAT.parse((String) data.get("previous"));
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}

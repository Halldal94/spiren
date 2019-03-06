package com.gardeners.spiren;

import android.util.Log;

import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class Plant {

    private MainActivity game;

    private int length, health , water, fertelizer, bugs, members;
    private int level, maxHealth, maxWater, maxBugs;
    private static List<Integer> playerSum;
    private Date previusAction;

    public Plant(MainActivity game){
        this.game = game;
        fetchData();
        stupidNoDataInit();
    }

    private void fetchData(){
        //TODO: implement
    }

    private void stupidNoDataInit(){
        this.length = 140;
        this.health = 1;
        this.water = 0;
        this.fertelizer = 0;
        this.members = 1;
        this.level = 1;

        switch (level) {
            case 1:
                maxHealth = maxWater = 100;
                maxBugs = 3;
                break;
            case 2:
                maxHealth = maxWater = 150;
                maxBugs = 5;
                break;
            case 3:
                maxHealth = maxWater = 200;
                maxBugs = 7;
                break;
            case 4:
                maxHealth = maxWater = 250;
                maxBugs = 10;
                break;
        }

        this.playerSum = Arrays.asList(40, 23, 14, 13, 10, 9, 8, 7, 6, 6, 5, 5);
    }

    public int getLength(){
        return length;
    }

    public int getHealth(){
        return health;
    }

    public int getWater(){
        return water;
    }

    public int getFertelizer(){
        return fertelizer;
    }

    public int getBugs(){
        return bugs;
    }

    public int getMembers(){
        return members;
    }

    public int getLevel(){
        return level;
    }

    public Date getPrevius() {
        return previusAction;
    }

    public void waterPlant(){
        if(!isSameDay(previusAction, new Date())) {
            water += playerSum.get(members - 1);
            calculateHealth();
            previusAction = new Date();
        }
    }

    public void fertelizerPlant(){
        if(!isSameDay(previusAction, new Date())) {
            fertelizer += playerSum.get(members - 1);
            previusAction = new Date();
        }
    }

    public void calculateHealth(){
        health = water - (10 * bugs);
    }

    public void killBugs(){
        if(!isSameDay(previusAction, new Date())){
            bugs -= 1;
            previusAction = new Date();
        }
    }

    public void growTimer(){
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 12);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);

        Timer timer = new Timer();
        TimerTask grow = new TimerTask() {
            @Override
            public void run() {
                grow();
            }
        };
        timer.schedule(grow, today.getTime(), TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS)); // period: 1 day
        game.updateInfo();
    }

    private void bugSpawner(){
        Timer timer = new Timer ();
        TimerTask spawn = new TimerTask () {
            @Override
            public void run () {
                int randInt = ThreadLocalRandom.current().nextInt(0, 101);
                if (randInt <= 10) {
                    bugs += 1;
                }
            }
        };
        timer.schedule (spawn, 0l, 1000*60*60);
    }

    private boolean isSameDay(Date day1, Date day2){
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(day1);
        cal2.setTime(day2);
        return cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) && cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
    }

    public void grow(){
        float percent = (maxHealth/health) * 100;
        if (percent < 25){
            length -= 8 - ((fertelizer / 100) * 4);
        }else if (percent < 45){
            length -= 4 - ((fertelizer / 100) * 2);
        } else if (percent < 55){
            length += 0;
        }else if (percent < 75) {
            length += 4 + ((fertelizer / 100) * 2);
        } else {
            length += 8 + ((fertelizer / 100) * 4);
        }
        if(length < 140){
            length = 140;
        }
        game.updateInfo();
    }

    public void testData(){
        for(int i = 0; i < 20; i++){
            waterPlant();
            members += 1;
        }
    }

    public void loadData(JSONObject data){
        try {
            length = (Integer) data.get("length");
            health = (Integer) data.get("health");
            water = (Integer) data.get("water");
            fertelizer = (Integer) data.get("fertelizer");
            bugs = (Integer) data.get("bugs");
            members = (Integer) data.get("members");
            level = (Integer) data.get("level");
            previusAction = (Date) new Date((String) data.get("previus"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

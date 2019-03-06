package com.gardeners.spiren;

import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
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
    private List<Integer> playerSum;
    private Date now, previus;

    public Plant(MainActivity game){
        this.game = game;
        fetchData();
        stupidNoDataInit();
        testData();
        growTimer();
    }

    private void fetchData(){
        //TODO: implement
    }

    private void stupidNoDataInit(){
        this.length = 20;
        this.health = 50;
        this.water = 30;
        this.fertelizer = 70;
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

        this.playerSum = new ArrayList<Integer>();
        playerSum.add(40);
        playerSum.add(23);
        playerSum.add(14);
        playerSum.add(13);
        playerSum.add(10);
        playerSum.add(9);
        playerSum.add(8);
        playerSum.add(7);
        playerSum.add(6);
        playerSum.add(6);
        playerSum.add(5);
        playerSum.add(5);
    }

    public String getLength(){
        return length + " cm";
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

    public void waterPlant(){
        water += playerSum.get(members - 1);
        calculateHealth();
    }

    public void fertelizerPlant(){
        fertelizer += playerSum.get(members - 1);
    }

    public void calculateHealth(){
        health = water - (10 * bugs);
    }

    public void killBugs(){
        bugs -= 1;
    }

    private void growTimer(){
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
        game.updateInfo();
    }

    public void testData(){
        for(int i = 0; i < 20; i++){
            waterPlant();
            members += 1;
        }
    }
}

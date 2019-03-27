package com.gardeners.spiren;

import android.util.Log;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class PlantController {
    private static final Random bugsRandom = new Random();
    private static final int MIN_LENGTH = 15;
    private static final int MAX_LENGTH = 120;
    private static final List<Integer> playerSum = Arrays.asList(40, 23, 14, 13, 10, 9, 8, 7, 6, 6, 5, 5);

    private final PlantModel model;
    private final MainActivity game;

    public PlantController(PlantModel model, MainActivity game) {
        this.model = model;
        this.game = game;
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
                if (bugsRandom.nextInt(10) == 0) {
                    model.setBugs(model.getBugs() + 1);
                }
            }
        };
        timer.schedule (spawn, 0l, 1000*60*60);
    }

    public void bugSpawnerDev(){
        if (model.getBugs() < model.getMaxBugs()){
            Log.d("devTest", "spawn bugs");
            model.setBugs(model.getBugs() + 1);
        } else {
            Log.d("devTest", "too many bugs");
        }
    }

    public void initialize(){
        model.setLevel(1);
        model.setHeight(MIN_LENGTH);
        model.setWater(0);
        model.setFertilizer(0);
        model.setMembers(1);
    }

    public void grow() {
        Log.d("devTest", "growing");
        int healthPercent = (100 * model.getHealth()) / model.getMaxHealth();
        int baseGrowth;
        int fertilizerMultiplier;
        if (healthPercent < 25){
            baseGrowth = -6;
            fertilizerMultiplier = 3;
        } else if (healthPercent < 45){
            baseGrowth = -3;
            fertilizerMultiplier = 2;
        } else if (healthPercent < 55){
            baseGrowth = 0;
            fertilizerMultiplier = 0;
        } else if (healthPercent < 75) {
            baseGrowth = 3;
            fertilizerMultiplier = 2;
        } else {
            baseGrowth = 6;
            fertilizerMultiplier = 3;
        }
        int length = model.getHeight() + baseGrowth + (fertilizerMultiplier * model.getFertilizer()) / 100;
        if(length < MIN_LENGTH) {
            length = MIN_LENGTH;
        }
        if (length > MAX_LENGTH) {
            length = MAX_LENGTH;
        }
        model.setHeight(length);
        game.updateInfo();
        game.onGrow();
    }

    public void water() {
        Date now = new Date();
        if(!isSameDay(model.getPreviousAction(), now)) {
            model.setWater(model.getWater() + playerSum.get(model.getMembers() - 1));
            model.setPreviousAction(now);
        }
    }

    public void fertilize() {
        Date now = new Date();
        if(!isSameDay(model.getPreviousAction(), now)) {
            model.setFertilizer(model.getFertilizer() + playerSum.get(model.getMembers() - 1));
            model.setPreviousAction(now);
        }
    }

    public void killBugs(){
        Date now = new Date();
        if(!isSameDay(model.getPreviousAction(), now)){
            if (model.getBugs() > 0) {
                model.setBugs(model.getBugs() - 1);
                model.setPreviousAction(now);
            }
        }
    }

    public void resetPreviousAction(){
        Log.d("devTest", "reset action");
        model.setPreviousAction(new Date(1994, 1, 4));
    }

    private static boolean isSameDay(Date day1, Date day2){
        if(day1 != null && day2 != null){
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            cal1.setTime(day1);
            cal2.setTime(day2);
            return cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) && cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
        }
        return false;
    }
}

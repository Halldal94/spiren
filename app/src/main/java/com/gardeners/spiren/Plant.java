package com.gardeners.spiren;

public class Plant {

    private int length;
    private int health;
    private int water;
    private int fertelizer;
    private int members;

    public Plant(){
        fetchData();
        stupidNoDataInit();
    }

    private void fetchData(){
        //TODO: implement
    }
    private void stupidNoDataInit(){
        this.length = 20;
        this.health = 50;
        this.water = 30;
        this.fertelizer = 70;
        this.members = 5;
    }

    public String getLength() {
        return length + " cm";
    }

    public int getHealth() {
        return health;
    }

    public int getWater() {
        return water;
    }

    public int getFertelizer() {
        return fertelizer;
    }

    public void waterPlant(){

    }

    public void fertelizPlant(){
        //TODO: Implement
    }
}

package org.example;

import java.time.LocalTime;

public class Hit {
    private Integer damage;
    private LocalTime timeOfHit;
    private boolean critical;
    private boolean miss;

    public Hit(int damage, LocalTime timeOfHit, boolean critical) {
        this.damage = damage;
        this.timeOfHit = timeOfHit;
        this.critical = critical;
    }

    public int getDamage() {
        return damage;
    }

    public Hit setDamage(int damage) {
        this.damage = damage;
        return this;
    }

    public LocalTime getTimeOfHit() {
        return timeOfHit;
    }

    public Hit setTimeOfHit(LocalTime timeOfHit) {
        this.timeOfHit = timeOfHit;
        return this;
    }

    public boolean isCritical() {
        return critical;
    }

    public Hit setCritical(boolean critical) {
        this.critical = critical;
        return this;
    }

    public boolean isMiss() {
        return miss;
    }

    public Hit setMiss(boolean miss) {
        this.miss = miss;
        return this;
    }
}

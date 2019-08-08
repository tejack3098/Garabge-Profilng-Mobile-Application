package com.gpp.gpp.home;

public class Cratings {
    private Float dry_wet;
    private Float plastic;

    public Cratings() {
    }

    public Cratings(Float dry_wet, Float plastic) {
        this.dry_wet = dry_wet;
        this.plastic = plastic;
    }

    public Float getDry_wet() {
        return dry_wet;
    }

    public void setDry_wet(Float dry_wet) {
        this.dry_wet = dry_wet;
    }

    public Float getPlastic() {
        return plastic;
    }

    public void setPlastic(Float plastic) {
        this.plastic = plastic;
    }
}

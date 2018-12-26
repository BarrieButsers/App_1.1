package project.test.mk.app;

public class Profil {

    private String name;
    private double temperatur, gMax;
    private int straßenbedingung;

    public Profil(String name, int straßenbedingung, double temperatur, double gMax) {
        this.name = name;
        this.straßenbedingung = straßenbedingung;
        this.temperatur = temperatur;
        this.gMax = gMax;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStraßenbedingung() {
        return straßenbedingung;
    }

    public void setStraßenbedingung(int straßenbedingung) {
        this.straßenbedingung = straßenbedingung;
    }

    public double getTemperatur() {
        return temperatur;
    }

    public void setTemperatur(double temperatur) {
        this.temperatur = temperatur;
    }

    public double getgMax() {
        return gMax;
    }

    public void setgMax(double gMax) {
        this.gMax = gMax;
    }
}

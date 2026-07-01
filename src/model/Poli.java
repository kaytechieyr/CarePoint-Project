package model;

public class Poli {

    private int poliId;
    private String namaPoli;

    public Poli() {
    }

    public Poli(int poliId, String namaPoli) {
        this.poliId = poliId;
        this.namaPoli = namaPoli;
    }

    public int getPoliId() {
        return poliId;
    }

    public void setPoliId(int poliId) {
        this.poliId = poliId;
    }

    public String getNamaPoli() {
        return namaPoli;
    }

    public void setNamaPoli(String namaPoli) {
        this.namaPoli = namaPoli;
    }

    @Override
    public String toString() {
        return namaPoli;
    }
}

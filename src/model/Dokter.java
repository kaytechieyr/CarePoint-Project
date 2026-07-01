package model;

public class Dokter {

    private int dokterId;
    private int poliId;
    private String namaDokter;
    private String namaPoli;

    public Dokter() {
    }

    public Dokter(int dokterId, int poliId, String namaDokter) {
        this.dokterId = dokterId;
        this.poliId = poliId;
        this.namaDokter = namaDokter;
    }

    public int getDokterId() {
        return dokterId;
    }

    public void setDokterId(int dokterId) {
        this.dokterId = dokterId;
    }

    public int getPoliId() {
        return poliId;
    }

    public void setPoliId(int poliId) {
        this.poliId = poliId;
    }

    public String getNamaDokter() {
        return namaDokter;
    }

    public void setNamaDokter(String namaDokter) {
        this.namaDokter = namaDokter;
    }

    public String getNamaPoli() {
        return namaPoli;
    }

    public void setNamaPoli(String namaPoli) {
        this.namaPoli = namaPoli;
    }

    @Override
    public String toString() {
        if (namaPoli == null || namaPoli.isBlank()) {
            return namaDokter;
        }
        return namaDokter + " - " + namaPoli;
    }
}

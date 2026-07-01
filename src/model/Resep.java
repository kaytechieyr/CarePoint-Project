package model;

import java.sql.Date;

public class Resep {

    private int resepId;
    private int rekamMedisId;
    private Date tglResep;

    public Resep() {
    }

    public Resep(int resepId, int rekamMedisId, Date tglResep) {
        this.resepId = resepId;
        this.rekamMedisId = rekamMedisId;
        this.tglResep = tglResep;
    }

    public int getResepId() {
        return resepId;
    }

    public void setResepId(int resepId) {
        this.resepId = resepId;
    }

    public int getRekamMedisId() {
        return rekamMedisId;
    }

    public void setRekamMedisId(int rekamMedisId) {
        this.rekamMedisId = rekamMedisId;
    }

    public Date getTglResep() {
        return tglResep;
    }

    public void setTglResep(Date tglResep) {
        this.tglResep = tglResep;
    }

    @Override
    public String toString() {
        return "Resep{"
                + "resepId=" + resepId
                + ", rekamMedisId=" + rekamMedisId
                + ", tglResep=" + tglResep
                + '}';
    }
}

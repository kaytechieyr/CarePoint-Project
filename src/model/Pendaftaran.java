package model;

import java.sql.Date;

public class Pendaftaran {

    private int pendaftaranId;
    private int pasienId;
    private int dokterId;
    private int noAntrean;
    private Date tglKunjungan;
    private String keluhan;
    private String status;

    public Pendaftaran() {
    }

    public Pendaftaran(int pendaftaranId, int pasienId, int dokterId, int noAntrean, Date tglKunjungan, String keluhan, String status) {
        this.pendaftaranId = pendaftaranId;
        this.pasienId = pasienId;
        this.dokterId = dokterId;
        this.noAntrean = noAntrean;
        this.tglKunjungan = tglKunjungan;
        this.keluhan = keluhan;
        this.status = status;
    }

    public int getPendaftaranId() {
        return pendaftaranId;
    }

    public void setPendaftaranId(int pendaftaranId) {
        this.pendaftaranId = pendaftaranId;
    }

    public int getPasienId() {
        return pasienId;
    }

    public void setPasienId(int pasienId) {
        this.pasienId = pasienId;
    }

    public int getDokterId() {
        return dokterId;
    }

    public void setDokterId(int dokterId) {
        this.dokterId = dokterId;
    }

    public int getNoAntrean() {
        return noAntrean;
    }

    public void setNoAntrean(int noAntrean) {
        this.noAntrean = noAntrean;
    }

    public Date getTglKunjungan() {
        return tglKunjungan;
    }

    public void setTglKunjungan(Date tglKunjungan) {
        this.tglKunjungan = tglKunjungan;
    }

    public String getKeluhan() {
        return keluhan;
    }

    public void setKeluhan(String keluhan) {
        this.keluhan = keluhan;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Pendaftaran{"
                + "pendaftaranId=" + pendaftaranId
                + ", pasienId=" + pasienId
                + ", dokterId=" + dokterId
                + ", noAntrean=" + noAntrean
                + ", tglKunjungan=" + tglKunjungan
                + ", keluhan='" + keluhan + '\''
                + ", status='" + status + '\''
                + '}';
    }
}

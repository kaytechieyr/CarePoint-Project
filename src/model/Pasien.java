package model;

import java.sql.Date;

public class Pasien {

    private int pasienId;
    private int userId;
    private String nik;
    private String namaLengkap;
    private String jenisKelamin;
    private Date tglLahir;
    private String alamat;
    private String noTelepon;

    public Pasien() {
    }

    public Pasien(int pasienId, int userId, String nik, String namaLengkap, String jenisKelamin, Date tglLahir, String alamat, String noTelepon) {
        this.pasienId = pasienId;
        this.userId = userId;
        this.nik = nik;
        this.namaLengkap = namaLengkap;
        this.jenisKelamin = jenisKelamin;
        this.tglLahir = tglLahir;
        this.alamat = alamat;
        this.noTelepon = noTelepon;
    }

    public int getPasienId() {
        return pasienId;
    }

    public void setPasienId(int pasienId) {
        this.pasienId = pasienId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getNik() {
        return nik;
    }

    public void setNik(String nik) {
        this.nik = nik;
    }

    public String getNamaLengkap() {
        return namaLengkap;
    }

    public void setNamaLengkap(String namaLengkap) {
        this.namaLengkap = namaLengkap;
    }

    public String getJenisKelamin() {
        return jenisKelamin;
    }

    public void setJenisKelamin(String jenisKelamin) {
        this.jenisKelamin = jenisKelamin;
    }

    public Date getTglLahir() {
        return tglLahir;
    }

    public void setTglLahir(Date tglLahir) {
        this.tglLahir = tglLahir;
    }

    public String getAlamat() {
        return alamat;
    }

    public void setAlamat(String alamat) {
        this.alamat = alamat;
    }

    public String getNoTelepon() {
        return noTelepon;
    }

    public void setNoTelepon(String noTelepon) {
        this.noTelepon = noTelepon;
    }

    @Override
    public String toString() {
        return "Pasien{"
                + "pasienId=" + pasienId
                + ", userId=" + userId
                + ", nik='" + nik + '\''
                + ", namaLengkap='" + namaLengkap + '\''
                + ", jenisKelamin='" + jenisKelamin + '\''
                + ", tglLahir=" + tglLahir
                + ", alamat='" + alamat + '\''
                + ", noTelepon='" + noTelepon + '\''
                + '}';
    }
}

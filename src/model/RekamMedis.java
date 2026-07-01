package model;

public class RekamMedis {

    private int rekamMedisId;
    private int pendaftaranId;
    private String diagnosa;

    public RekamMedis() {
    }

    public RekamMedis(int rekamMedisId, int pendaftaranId, String diagnosa) {
        this.rekamMedisId = rekamMedisId;
        this.pendaftaranId = pendaftaranId;
        this.diagnosa = diagnosa;
    }

    public int getRekamMedisId() {
        return rekamMedisId;
    }

    public void setRekamMedisId(int rekamMedisId) {
        this.rekamMedisId = rekamMedisId;
    }

    public int getPendaftaranId() {
        return pendaftaranId;
    }

    public void setPendaftaranId(int pendaftaranId) {
        this.pendaftaranId = pendaftaranId;
    }

    public String getDiagnosa() {
        return diagnosa;
    }

    public void setDiagnosa(String diagnosa) {
        this.diagnosa = diagnosa;
    }

    @Override
    public String toString() {
        return "RekamMedis{"
                + "rekamMedisId=" + rekamMedisId
                + ", pendaftaranId=" + pendaftaranId
                + ", diagnosa='" + diagnosa + '\''
                + '}';
    }
}

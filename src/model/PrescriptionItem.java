package model;

public class PrescriptionItem {

    private int detailResepId;
    private String medicineName;
    private String dosage;
    private String usageInstruction;

    public int getDetailResepId() {
        return detailResepId;
    }

    public void setDetailResepId(int detailResepId) {
        this.detailResepId = detailResepId;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getUsageInstruction() {
        return usageInstruction;
    }

    public void setUsageInstruction(String usageInstruction) {
        this.usageInstruction = usageInstruction;
    }
}

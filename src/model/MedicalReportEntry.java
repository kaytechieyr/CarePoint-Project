package model;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MedicalReportEntry {

    private int rekamMedisId;
    private int pendaftaranId;
    private Date visitDate;
    private String patientName;
    private String doctorName;
    private String poliName;
    private String diagnosis;
    private final List<PrescriptionItem> prescriptions = new ArrayList<>();

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

    public Date getVisitDate() {
        return visitDate;
    }

    public void setVisitDate(Date visitDate) {
        this.visitDate = visitDate;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getPoliName() {
        return poliName;
    }

    public void setPoliName(String poliName) {
        this.poliName = poliName;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public List<PrescriptionItem> getPrescriptions() {
        return Collections.unmodifiableList(prescriptions);
    }

    public void addPrescription(PrescriptionItem item) {
        if (item != null) {
            prescriptions.add(item);
        }
    }

    public String summarizeMedicines(int limit) {
        return summarizeByField(limit, 0);
    }

    public String summarizeDosages(int limit) {
        return summarizeByField(limit, 1);
    }

    public String summarizeUsages(int limit) {
        return summarizeByField(limit, 2);
    }

    private String summarizeByField(int limit, int fieldIndex) {
        if (prescriptions.isEmpty()) {
            return "-";
        }

        StringBuilder builder = new StringBuilder();
        int count = Math.min(limit, prescriptions.size());
        for (int i = 0; i < count; i++) {
            PrescriptionItem item = prescriptions.get(i);
            String value;
            if (fieldIndex == 0) {
                value = item.getMedicineName();
            } else if (fieldIndex == 1) {
                value = item.getDosage();
            } else {
                value = item.getUsageInstruction();
            }

            if (value == null || value.isBlank()) {
                value = "-";
            }
            if (builder.length() > 0) {
                builder.append(fieldIndex == 0 ? ", " : "; ");
            }
            builder.append(value.trim());
        }

        if (prescriptions.size() > limit) {
            if (builder.length() > 0) {
                builder.append(fieldIndex == 0 ? ", " : "; ");
            }
            builder.append("+ ").append(prescriptions.size() - limit).append(" more");
        }

        return builder.toString();
    }
}

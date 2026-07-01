package model;

import java.util.ArrayList;
import java.util.List;

public class DoctorDirectoryItem {

    private int dokterId;
    private String namaDokter;
    private String namaPoli;
    private final List<String> schedules = new ArrayList<>();

    public int getDokterId() {
        return dokterId;
    }

    public void setDokterId(int dokterId) {
        this.dokterId = dokterId;
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

    public List<String> getSchedules() {
        return schedules;
    }

    public void addSchedule(String scheduleText) {
        schedules.add(scheduleText);
    }
}

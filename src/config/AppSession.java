package config;

import model.Pasien;
import model.User;

public final class AppSession {

    private static User currentUser;
    private static Pasien currentPasien;

    private AppSession() {
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static Pasien getCurrentPasien() {
        return currentPasien;
    }

    public static void setCurrentPasien(Pasien pasien) {
        currentPasien = pasien;
    }

    public static void clear() {
        currentUser = null;
        currentPasien = null;
    }
}

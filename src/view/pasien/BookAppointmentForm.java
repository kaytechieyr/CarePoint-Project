package view.pasien;

import config.AppSession;
import config.UITheme;
import dao.DokterDAO;
import dao.JadwalDokterDAO;
import dao.PasienDAO;
import dao.PendaftaranDAO;
import dao.PoliDAO;
import model.Dokter;
import model.JadwalDokter;
import model.Pasien;
import model.Pendaftaran;
import model.Poli;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DateFormatter;
import javax.swing.text.DefaultFormatterFactory;

public class BookAppointmentForm extends JPanel {

    private final PoliDAO poliDAO = new PoliDAO();
    private final DokterDAO dokterDAO = new DokterDAO();
    private final JadwalDokterDAO jadwalDokterDAO = new JadwalDokterDAO();
    private final PasienDAO pasienDAO = new PasienDAO();
    private final PendaftaranDAO pendaftaranDAO = new PendaftaranDAO();

    private Pasien currentPasien;

    private JTextFieldOrLabel txtPatientName;
    private JComboBox<Poli> cbPoli;
    private JComboBox<Dokter> cbDoctor;
    private JComboBox<JadwalDokter> cbSchedule;
    private JSpinner spVisitDate;
    private JTextAreaOrLabel txtComplaint;
    private JLabel lblSummaryPoli;
    private JLabel lblSummaryDoctor;
    private JLabel lblSummarySchedule;
    private JLabel lblSummaryDate;
    private JLabel lblSummaryQueue;
    private boolean loadingCombos;

    public BookAppointmentForm() {
        setLayout(new BorderLayout());

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG);
        root.setBorder(new EmptyBorder(20, 20, 20, 20));
        root.add(createHeader(), BorderLayout.NORTH);
        root.add(createContent(), BorderLayout.CENTER);

        add(UITheme.createScrollPane(root), BorderLayout.CENTER);

        loadPatientContext();
        loadPoliData();
        updateSummary();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Book Appointment");
        title.setFont(new Font("SansSerif", Font.BOLD, 30));
        title.setForeground(new Color(18, 52, 86));

        JLabel subtitle = new JLabel("Easily secure your appointment queue with our specialists.");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(new Color(87, 104, 127));

        header.add(title);
        header.add(Box.createVerticalStrut(6));
        header.add(subtitle);
        return header;
    }

    private JPanel createContent() {
        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);

        JPanel bookingCard = createBookingCard();
        JPanel summaryCard = createSummaryCard();

        JSplitPaneWrapper split = new JSplitPaneWrapper(bookingCard, summaryCard);
        content.add(UITheme.createScrollPane(split), BorderLayout.CENTER);
        return content;
    }

    private JPanel createBookingCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 236, 246)),
                new EmptyBorder(20, 20, 20, 20)));

        JLabel title = new JLabel("Book Appointment");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(new Color(18, 52, 86));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 12, 0);

        txtPatientName = new JTextFieldOrLabel();
        txtPatientName.setEditable(false);
        cbPoli = new JComboBox<>();
        UITheme.styleComboBox(cbPoli);
        cbDoctor = new JComboBox<>();
        UITheme.styleComboBox(cbDoctor);
        cbSchedule = new JComboBox<>();
        UITheme.styleComboBox(cbSchedule);

        spVisitDate = new JSpinner(new SpinnerDateModel(new java.util.Date(), null, null, java.util.Calendar.DAY_OF_MONTH));
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spVisitDate, "yyyy-MM-dd");
        spVisitDate.setEditor(editor);
        ((JFormattedTextField) editor.getTextField()).setEditable(false);
        spVisitDate.setFont(UITheme.BODY_FONT);

        txtComplaint = new JTextAreaOrLabel();

        addField(form, gbc, "Patient Name", txtPatientName);
        addField(form, gbc, "Poli", cbPoli);
        addField(form, gbc, "Doctor", cbDoctor);
        addField(form, gbc, "Schedule", cbSchedule);
        addField(form, gbc, "Visit Date", spVisitDate);
        addField(form, gbc, "Complaint", txtComplaint);

        JPanel buttons = new JPanel();
        buttons.setOpaque(false);
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));

        JButton btnBook = UITheme.createPrimaryButton("Book Appointment");
        JButton btnReset = UITheme.createSecondaryButton("Reset");
        btnBook.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                bookAppointment();
            }
        });
        btnReset.addActionListener(e -> resetForm());

        buttons.add(btnBook);
        buttons.add(Box.createHorizontalStrut(10));
        buttons.add(btnReset);

        card.add(title, BorderLayout.NORTH);
        card.add(form, BorderLayout.CENTER);
        card.add(buttons, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createSummaryCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 236, 246)),
                new EmptyBorder(20, 20, 20, 20)));

        JLabel title = new JLabel("Booking Summary");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(new Color(18, 52, 86));

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        lblSummaryPoli = createSummaryLabel("Poli: -");
        lblSummaryDoctor = createSummaryLabel("Doctor: -");
        lblSummarySchedule = createSummaryLabel("Schedule: -");
        lblSummaryDate = createSummaryLabel("Visit Date: -");
        lblSummaryQueue = createSummaryLabel("Next Queue: -");

        body.add(lblSummaryPoli);
        body.add(Box.createVerticalStrut(8));
        body.add(lblSummaryDoctor);
        body.add(Box.createVerticalStrut(8));
        body.add(lblSummarySchedule);
        body.add(Box.createVerticalStrut(8));
        body.add(lblSummaryDate);
        body.add(Box.createVerticalStrut(8));
        body.add(lblSummaryQueue);

        JTextAreaOrLabel note = new JTextAreaOrLabel();
        note.setText("Appointment will be saved with status Pending.");
        note.setEditable(false);
        note.setOpaque(false);
        note.setFont(new Font("SansSerif", Font.PLAIN, 13));
        note.setForeground(new Color(87, 104, 127));
        note.setBorder(new EmptyBorder(16, 0, 0, 0));

        card.add(title, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        card.add(note, BorderLayout.SOUTH);
        return card;
    }

    private JLabel createSummaryLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.PLAIN, 14));
        label.setForeground(new Color(52, 71, 103));
        return label;
    }

    private void addField(JPanel form, GridBagConstraints gbc, String labelText, java.awt.Component field) {
        JLabel label = new JLabel(labelText);
        UITheme.applyLabel(label);
        form.add(label, gbc);
        gbc.gridy++;
        form.add(field, gbc);
        gbc.gridy++;
    }

    private void loadPatientContext() {
        currentPasien = AppSession.getCurrentPasien();
        if (currentPasien == null && AppSession.getCurrentUser() != null) {
            currentPasien = pasienDAO.findByUserId(AppSession.getCurrentUser().getUserId());
            AppSession.setCurrentPasien(currentPasien);
        }

        if (currentPasien == null) {
            JOptionPane.showMessageDialog(this,
                    "Patient session was not found. Please login again.",
                    "Session Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        txtPatientName.setText(currentPasien.getNamaLengkap());
    }

    private void loadPoliData() {
        loadingCombos = true;
        cbPoli.removeAllItems();
        for (Poli poli : poliDAO.findAll()) {
            cbPoli.addItem(poli);
        }
        loadingCombos = false;

        cbPoli.addActionListener(e -> {
            if (!loadingCombos) {
                loadDoctorsForSelectedPoli();
            }
        });
        cbDoctor.addActionListener(e -> {
            if (!loadingCombos) {
                loadSchedulesForSelectedDoctor();
            }
        });
        cbSchedule.addActionListener(e -> updateSummary());

        if (cbPoli.getItemCount() > 0) {
            cbPoli.setSelectedIndex(0);
            loadDoctorsForSelectedPoli();
        }

        updateSummary();
    }

    private void loadDoctorsForSelectedPoli() {
        Poli selectedPoli = (Poli) cbPoli.getSelectedItem();
        loadingCombos = true;
        cbDoctor.removeAllItems();
        cbSchedule.removeAllItems();

        if (selectedPoli != null) {
            for (Dokter dokter : dokterDAO.findByPoliId(selectedPoli.getPoliId())) {
                cbDoctor.addItem(dokter);
            }
        }

        if (cbDoctor.getItemCount() > 0) {
            cbDoctor.setSelectedIndex(0);
        }
        loadingCombos = false;
        loadSchedulesForSelectedDoctor();
        updateSummary();
    }

    private void loadSchedulesForSelectedDoctor() {
        Dokter selectedDoctor = (Dokter) cbDoctor.getSelectedItem();
        loadingCombos = true;
        cbSchedule.removeAllItems();

        if (selectedDoctor != null) {
            List<JadwalDokter> schedules = jadwalDokterDAO.findByDokterId(selectedDoctor.getDokterId());
            for (JadwalDokter jadwal : schedules) {
                cbSchedule.addItem(jadwal);
            }
        }

        if (cbSchedule.getItemCount() > 0) {
            cbSchedule.setSelectedIndex(0);
        }
        loadingCombos = false;
        updateSummary();
    }

    private void updateSummary() {
        Poli poli = (Poli) cbPoli.getSelectedItem();
        Dokter doctor = (Dokter) cbDoctor.getSelectedItem();
        JadwalDokter schedule = (JadwalDokter) cbSchedule.getSelectedItem();
        java.util.Date visitDate = (java.util.Date) spVisitDate.getValue();

        lblSummaryPoli.setText("Poli: " + (poli != null ? poli.getNamaPoli() : "-"));
        lblSummaryDoctor.setText("Doctor: " + (doctor != null ? doctor.getNamaDokter() : "-"));
        lblSummarySchedule.setText("Schedule: " + (schedule != null ? schedule.toString() : "-"));
        lblSummaryDate.setText("Visit Date: " + new SimpleDateFormat("yyyy-MM-dd").format(visitDate));

        if (doctor != null && visitDate != null) {
            Date sqlDate = new Date(visitDate.getTime());
            int nextQueue = pendaftaranDAO.getNextQueueNumber(doctor.getDokterId(), sqlDate);
            lblSummaryQueue.setText("Next Queue: " + nextQueue);
        } else {
            lblSummaryQueue.setText("Next Queue: -");
        }
    }

    private void bookAppointment() {
        if (currentPasien == null) {
            JOptionPane.showMessageDialog(this,
                    "Patient session is missing.",
                    "Validation",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Poli poli = (Poli) cbPoli.getSelectedItem();
        Dokter doctor = (Dokter) cbDoctor.getSelectedItem();
        JadwalDokter schedule = (JadwalDokter) cbSchedule.getSelectedItem();
        String complaint = txtComplaint.getText().trim();
        if (poli == null || doctor == null || schedule == null || complaint.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please complete poli, doctor, schedule, and complaint.",
                    "Validation",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        java.util.Date visitDateUtil = (java.util.Date) spVisitDate.getValue();
        LocalDate visitDate = visitDateUtil.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        String javaDay = visitDate.getDayOfWeek().name();
        String normalizedVisitDay = normalizeDayName(javaDay);
        String normalizedScheduleDay = normalizeDayName(schedule.getHari());
        System.out.println("[BookAppointment] visitDate=" + visitDate
                + ", javaDay=" + javaDay
                + ", scheduleDay=" + schedule.getHari()
                + ", normalizedVisitDay=" + normalizedVisitDay
                + ", normalizedScheduleDay=" + normalizedScheduleDay);

        if (!normalizedVisitDay.equals(normalizedScheduleDay)) {
            JOptionPane.showMessageDialog(this,
                    "Selected visit date must match the schedule day: " + schedule.getHari(),
                    "Validation",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Date sqlDate = Date.valueOf(visitDate);
            int queueNumber = pendaftaranDAO.getNextQueueNumber(doctor.getDokterId(), sqlDate);
            Pendaftaran pendaftaran = new Pendaftaran();
            pendaftaran.setPasienId(currentPasien.getPasienId());
            pendaftaran.setDokterId(doctor.getDokterId());
            pendaftaran.setNoAntrean(queueNumber);
            pendaftaran.setTglKunjungan(sqlDate);
            pendaftaran.setKeluhan(complaint);
            pendaftaran.setStatus("Menunggu");

            pendaftaranDAO.insert(pendaftaran);
            JOptionPane.showMessageDialog(this,
                    "Appointment booked successfully.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            resetForm();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetForm() {
        txtComplaint.setText("");
        spVisitDate.setValue(new java.util.Date());
        if (cbPoli.getItemCount() > 0) {
            cbPoli.setSelectedIndex(0);
        }
        loadDoctorsForSelectedPoli();
        updateSummary();
    }

    private String normalizeDayName(String day) {
        if (day == null) {
            return "";
        }

        switch (day.trim().toLowerCase(Locale.ROOT)) {
            case "monday":
            case "senin":
                return "SENIN";
            case "tuesday":
            case "selasa":
                return "SELASA";
            case "wednesday":
            case "rabu":
                return "RABU";
            case "thursday":
            case "kamis":
                return "KAMIS";
            case "friday":
            case "jumat":
            case "jum'at":
                return "JUMAT";
            case "saturday":
            case "sabtu":
                return "SABTU";
            case "sunday":
            case "minggu":
                return "MINGGU";
            default:
                return day.trim().toUpperCase(Locale.ROOT);
        }
    }

    private static class JTextFieldOrLabel extends javax.swing.JTextField {
        JTextFieldOrLabel() {
            super();
            setFont(UITheme.BODY_FONT);
            setBackground(new Color(251, 253, 255));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 215, 232)),
                    new EmptyBorder(10, 12, 10, 12)));
        }
    }

    private static class JTextAreaOrLabel extends javax.swing.JTextArea {
        JTextAreaOrLabel() {
            super(4, 20);
            setLineWrap(true);
            setWrapStyleWord(true);
            setFont(UITheme.BODY_FONT);
            setBackground(new Color(251, 253, 255));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 215, 232)),
                    new EmptyBorder(10, 12, 10, 12)));
        }
    }

    private static class JSplitPaneWrapper extends JPanel {
        JSplitPaneWrapper(JPanel left, JPanel right) {
            super(new BorderLayout());
            javax.swing.JSplitPane split = new javax.swing.JSplitPane(javax.swing.JSplitPane.HORIZONTAL_SPLIT, left, right);
            split.setDividerLocation(560);
            split.setResizeWeight(0.55);
            split.setContinuousLayout(true);
            split.setBorder(null);
            add(split, BorderLayout.CENTER);
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("CarePoint - Book Appointment");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setContentPane(new BookAppointmentForm());
            frame.setMinimumSize(new Dimension(1280, 820));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}

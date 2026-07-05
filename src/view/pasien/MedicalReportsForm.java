package view.pasien;

import config.AppSession;
import config.TableScrollSupport;
import config.UITheme;
import dao.MedicalReportDAO;
import dao.PasienDAO;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import model.MedicalReportEntry;
import model.Pasien;
import model.PrescriptionItem;

public class MedicalReportsForm extends JPanel {

    private static final int SUMMARY_METRIC_WRAP_WIDTH = 280;

    private final PasienDAO pasienDAO = new PasienDAO();
    private final MedicalReportDAO medicalReportDAO = new MedicalReportDAO();

    private Pasien currentPasien;
    private final List<MedicalReportEntry> reports = new ArrayList<>();
    private DefaultTableModel tableModel;
    private JTable tableReports;
    private JLabel totalReportsValue;
    private JLabel lastVisitValue;
    private JTextArea latestDoctorValue;
    private JTextArea latestDiagnosisValue;
    private JPanel recentBodyContainer;
    private JLabel recentHeaderLabel;
    private JLabel recentDoctorValue;
    private JLabel recentPoliValue;
    private JLabel recentVisitValue;
    private JLabel recentDiagnosisValue;

    public MedicalReportsForm() {
        setLayout(new BorderLayout());
        setOpaque(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG);
        root.setBorder(new EmptyBorder(20, 20, 20, 20));
        root.add(createHeader(), BorderLayout.NORTH);
        root.add(createContent(), BorderLayout.CENTER);

        add(UITheme.createScrollPane(root), BorderLayout.CENTER);

        loadPatientContext();
        refreshData();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Medical Reports");
        title.setFont(UITheme.DISPLAY_FONT);
        title.setForeground(UITheme.PRIMARY_DARK);

        JLabel subtitle = new JLabel(
                "Securely access your clinical records, treatments, and doctor's notes.");
        subtitle.setFont(UITheme.BODY_FONT);
        subtitle.setForeground(UITheme.MUTED);

        header.add(title);
        header.add(Box.createVerticalStrut(6));
        header.add(subtitle);
        return header;
    }

    private JPanel createContent() {
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        content.add(createSummarySection());
        content.add(Box.createVerticalStrut(16));
        content.add(createRecentReportSection());
        content.add(Box.createVerticalStrut(16));
        content.add(createHistorySection());
        return content;
    }

    private JPanel createSummarySection() {
        JPanel card = createSectionCard("Summary", "A quick snapshot of your latest medical visits.");
        JPanel grid = new JPanel();
        grid.setOpaque(false);
        grid.setLayout(new BoxLayout(grid, BoxLayout.Y_AXIS));

        totalReportsValue = createMetricValue("0");
        lastVisitValue = createMetricValue("-");

        latestDoctorValue = createWrappedMetricValue("-");
        latestDiagnosisValue = createWrappedMetricValue("-");

        JPanel firstRow = createMetricRow(
                createMetricCard("Total Reports", totalReportsValue),
                createMetricCard("Last Visit", lastVisitValue));
        JPanel secondRow = createMetricRow(
                createMetricCard("Latest Doctor", latestDoctorValue),
                createMetricCard("Latest Diagnosis", latestDiagnosisValue));

        grid.add(firstRow);
        grid.add(Box.createVerticalStrut(12));
        grid.add(secondRow);
        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    private JPanel createRecentReportSection() {
        JPanel card = createSectionCard("Recent Report", "The newest report is highlighted here for quick access.");
        recentBodyContainer = new JPanel(new BorderLayout());
        recentBodyContainer.setOpaque(false);
        card.add(recentBodyContainer, BorderLayout.CENTER);
        return card;
    }

    private JPanel createHistorySection() {
        JPanel card = createSectionCard("Report History", "Complete medical history with horizontal drag-to-scroll support.");

        tableModel = new DefaultTableModel(
                new Object[] { "Report ID", "Visit Date", "Doctor", "Poli", "Diagnosis", "Medicine", "Dosage",
                        "Usage Instruction" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableReports = new JTable(tableModel);
        tableReports.setRowHeight(30);
        tableReports.setFont(UITheme.BODY_FONT);
        tableReports.getTableHeader().setFont(UITheme.LABEL_FONT);
        tableReports.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JScrollPane scrollPane = TableScrollSupport.createTableScrollPane(tableReports,
                110, 130, 170, 150, 360, 340, 280, 380);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(0, 270));
        scrollPane.setMinimumSize(new Dimension(0, 220));

        JButton btnRefresh = UITheme.createPrimaryButton("Refresh");
        btnRefresh.addActionListener(e -> refreshData());

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);

        JLabel label = new JLabel("Full history from the database");
        label.setFont(new Font("SansSerif", Font.PLAIN, 13));
        label.setForeground(UITheme.MUTED);

        headerRow.add(label, BorderLayout.WEST);
        headerRow.add(btnRefresh, BorderLayout.EAST);

        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);
        body.add(headerRow, BorderLayout.NORTH);
        body.add(scrollPane, BorderLayout.CENTER);

        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private void loadPatientContext() {
        currentPasien = AppSession.getCurrentPasien();
        if (currentPasien == null && AppSession.getCurrentUser() != null) {
            currentPasien = pasienDAO.findByUserId(AppSession.getCurrentUser().getUserId());
            AppSession.setCurrentPasien(currentPasien);
        }
    }

    private void refreshData() {
        reports.clear();
        tableModel.setRowCount(0);

        if (currentPasien == null) {
            renderEmptyState();
            return;
        }

        reports.addAll(medicalReportDAO.findByPasienId(currentPasien.getPasienId()));
        if (reports.isEmpty()) {
            renderEmptyState();
            return;
        }

        for (MedicalReportEntry entry : reports) {
            tableModel.addRow(new Object[] {
                    entry.getRekamMedisId(),
                    formatDate(entry.getVisitDate()),
                    safeText(entry.getDoctorName()),
                    safeText(entry.getPoliName()),
                    safeText(entry.getDiagnosis()),
                    entry.summarizeMedicines(Integer.MAX_VALUE),
                    entry.summarizeDosages(Integer.MAX_VALUE),
                    entry.summarizeUsages(Integer.MAX_VALUE)
            });
        }

        renderSummary(reports.get(0));
        renderRecentReport(reports.get(0));
    }

    private void renderSummary(MedicalReportEntry latest) {
        totalReportsValue.setText(String.valueOf(reports.size()));
        lastVisitValue.setText(formatDate(latest.getVisitDate()));
        latestDoctorValue.setText(safeText(latest.getDoctorName()));
        latestDiagnosisValue.setText(safeText(latest.getDiagnosis()));
        revalidate();
        repaint();
    }

    private void renderRecentReport(MedicalReportEntry latest) {
        recentBodyContainer.removeAll();

        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 10, 0);

        recentDoctorValue = createDetailValue(safeText(latest.getDoctorName()));
        recentPoliValue = createDetailValue(safeText(latest.getPoliName()));
        recentVisitValue = createDetailValue(formatDate(latest.getVisitDate()));
        recentDiagnosisValue = createDetailValue(safeText(latest.getDiagnosis()));

        body.add(createDetailRow("Doctor Name", recentDoctorValue), gbc);
        gbc.gridy++;
        body.add(createDetailRow("Poli", recentPoliValue), gbc);
        gbc.gridy++;
        body.add(createDetailRow("Visit Date", recentVisitValue), gbc);
        gbc.gridy++;
        body.add(createDetailRow("Diagnosis Preview", recentDiagnosisValue), gbc);
        gbc.gridy++;
        body.add(createPrescriptionSummaryPanel(latest.getPrescriptions()), gbc);
        gbc.gridy++;

        JButton btnViewDetail = UITheme.createPrimaryButton("View Detail");
        btnViewDetail.addActionListener(e -> showReportDetail(latest));

        JPanel buttonRow = new JPanel(new BorderLayout());
        buttonRow.setOpaque(false);
        buttonRow.add(btnViewDetail, BorderLayout.EAST);
        body.add(buttonRow, gbc);

        recentBodyContainer.add(body, BorderLayout.CENTER);
        recentBodyContainer.revalidate();
        recentBodyContainer.repaint();
    }

    private JPanel createPrescriptionSummaryPanel(List<PrescriptionItem> prescriptions) {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);

        JLabel title = new JLabel("Prescription Summary");
        title.setFont(new Font("SansSerif", Font.BOLD, 13));
        title.setForeground(UITheme.MUTED);
        panel.add(title, BorderLayout.NORTH);

        if (prescriptions == null || prescriptions.isEmpty()) {
            JLabel empty = new JLabel("No prescription recorded.");
            empty.setFont(UITheme.BODY_FONT);
            empty.setForeground(UITheme.MUTED);
            panel.add(empty, BorderLayout.CENTER);
            return panel;
        }

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));

        for (int i = 0; i < prescriptions.size(); i++) {
            PrescriptionItem item = prescriptions.get(i);
            list.add(createPrescriptionChip(item));
            if (i < prescriptions.size() - 1) {
                list.add(Box.createVerticalStrut(8));
            }
        }

        panel.add(list, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createPrescriptionChip(PrescriptionItem item) {
        JPanel chip = new JPanel(new BorderLayout(0, 4));
        chip.setBackground(new Color(248, 251, 255));
        chip.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 231, 242)),
                new EmptyBorder(10, 12, 10, 12)));

        JLabel medicine = new JLabel("Medicine: " + safeText(item.getMedicineName()));
        medicine.setFont(new Font("SansSerif", Font.BOLD, 13));
        medicine.setForeground(UITheme.PRIMARY_DARK);

        JLabel dosage = new JLabel("Dosage: " + safeText(item.getDosage()));
        dosage.setFont(UITheme.BODY_FONT);
        dosage.setForeground(UITheme.TEXT);

        JLabel usage = new JLabel("Usage: " + safeText(item.getUsageInstruction()));
        usage.setFont(UITheme.BODY_FONT);
        usage.setForeground(UITheme.TEXT);

        chip.add(medicine, BorderLayout.NORTH);

        JPanel middle = new JPanel();
        middle.setOpaque(false);
        middle.setLayout(new BoxLayout(middle, BoxLayout.Y_AXIS));
        middle.add(dosage);
        middle.add(usage);
        chip.add(middle, BorderLayout.CENTER);
        return chip;
    }

    private void renderEmptyState() {
        totalReportsValue.setText("0");
        lastVisitValue.setText("-");
        latestDoctorValue.setText("-");
        latestDiagnosisValue.setText("-");

        recentBodyContainer.removeAll();
        recentBodyContainer.add(createEmptyStatePanel("No medical reports available yet."), BorderLayout.CENTER);
        recentBodyContainer.revalidate();
        recentBodyContainer.repaint();
        revalidate();
        repaint();
    }

    private JPanel createEmptyStatePanel(String message) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 216, 231)),
                new EmptyBorder(20, 20, 20, 20)));

        JLabel label = new JLabel(message, JLabel.CENTER);
        label.setFont(UITheme.BODY_FONT);
        label.setForeground(UITheme.MUTED);
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createSectionCard(String titleText, String subtitleText) {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER),
                new EmptyBorder(18, 18, 18, 18)));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel title = new JLabel(titleText);
        title.setFont(UITheme.TITLE_FONT);
        title.setForeground(UITheme.PRIMARY_DARK);

        JLabel subtitle = new JLabel(subtitleText);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(UITheme.MUTED);

        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(subtitle);

        card.add(header, BorderLayout.NORTH);
        return card;
    }

    private JPanel createMetricCard(String labelText, JComponent valueComponent) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(new Color(248, 251, 255));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 231, 242)),
                new EmptyBorder(16, 16, 16, 16)));

        JLabel label = new JLabel(labelText);
        label.setFont(UITheme.LABEL_FONT);
        label.setForeground(UITheme.MUTED);

        card.add(label, BorderLayout.NORTH);
        card.add(valueComponent, BorderLayout.CENTER);
        return card;
    }

    private JPanel createMetricRow(JPanel leftCard, JPanel rightCard) {
        JPanel row = new JPanel(new GridLayout(1, 2, 12, 0));
        row.setOpaque(false);
        row.add(leftCard);
        row.add(rightCard);
        return row;
    }

    private JLabel createMetricValue(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 22));
        label.setForeground(UITheme.PRIMARY_DARK);
        return label;
    }

    private JTextArea createWrappedMetricValue(String text) {
        JTextArea area = new SummaryMetricTextArea(SUMMARY_METRIC_WRAP_WIDTH);
        area.setText(text);
        return area;
    }

    private static final class SummaryMetricTextArea extends JTextArea {

        private final int wrapWidth;

        private SummaryMetricTextArea(int wrapWidth) {
            this.wrapWidth = wrapWidth;
            setFont(new Font("SansSerif", Font.BOLD, 22));
            setForeground(UITheme.PRIMARY_DARK);
            setLineWrap(true);
            setWrapStyleWord(true);
            setEditable(false);
            setFocusable(false);
            setOpaque(false);
            setBorder(null);
            setMargin(new Insets(0, 0, 0, 0));
        }

        @Override
        public Dimension getPreferredSize() {
            int width = wrapWidth;
            if (getWidth() > 0) {
                width = getWidth();
            } else if (getParent() != null && getParent().getWidth() > 0) {
                width = getParent().getWidth();
            }

            setSize(width, Short.MAX_VALUE);
            Dimension size = super.getPreferredSize();
            size.width = width;
            return size;
        }
    }

    private JPanel createDetailRow(String labelText, JLabel valueLabel) {
        JPanel row = new JPanel(new BorderLayout(0, 4));
        row.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(UITheme.LABEL_FONT);
        label.setForeground(UITheme.MUTED);

        row.add(label, BorderLayout.NORTH);
        row.add(valueLabel, BorderLayout.CENTER);
        return row;
    }

    private JLabel createDetailValue(String text) {
        JLabel label = new JLabel("<html><div style='width:100%;'>" + escapeHtml(text) + "</div></html>");
        label.setFont(UITheme.BODY_FONT);
        label.setForeground(UITheme.TEXT);
        return label;
    }

    private void showReportDetail(MedicalReportEntry item) {
        StringBuilder builder = new StringBuilder();
        builder.append("Report ID: ").append(item.getRekamMedisId()).append('\n');
        builder.append("Visit Date: ").append(formatDate(item.getVisitDate())).append('\n');
        builder.append("Doctor: ").append(safeText(item.getDoctorName())).append('\n');
        builder.append("Poli: ").append(safeText(item.getPoliName())).append('\n');
        builder.append("Diagnosis: ").append(safeText(item.getDiagnosis())).append('\n');
        builder.append('\n');
        builder.append("Medicine List:\n");

        List<PrescriptionItem> prescriptions = item.getPrescriptions();
        if (prescriptions.isEmpty()) {
            builder.append("- No prescription recorded.\n");
        } else {
            for (int i = 0; i < prescriptions.size(); i++) {
                PrescriptionItem prescription = prescriptions.get(i);
                builder.append(i + 1).append(". ")
                        .append(safeText(prescription.getMedicineName())).append('\n')
                        .append("   Dosage: ").append(safeText(prescription.getDosage())).append('\n')
                        .append("   Usage: ").append(safeText(prescription.getUsageInstruction())).append('\n');
            }
        }

        JTextArea area = new JTextArea(builder.toString());
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(UITheme.BODY_FONT);
        area.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(area);
        scrollPane.setPreferredSize(new Dimension(520, 380));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        JOptionPane.showMessageDialog(this, scrollPane, "Medical Report Detail", JOptionPane.INFORMATION_MESSAGE);
    }

    private String formatDate(Date date) {
        if (date == null) {
            return "-";
        }
        return new SimpleDateFormat("dd MMM yyyy").format(date);
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, Math.max(0, maxLength - 3)).trim() + "...";
    }

    private String safeText(String value) {
        return value == null || value.isBlank() ? "-" : value.trim();
    }

    private String escapeHtml(String value) {
        return safeText(value)
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("CarePoint - Medical Reports");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setContentPane(new MedicalReportsForm());
            frame.setMinimumSize(new Dimension(1280, 820));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}

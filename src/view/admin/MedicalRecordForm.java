package view.admin;

import config.TableScrollSupport;
import config.UITheme;
import dao.MedicalRecordDAO;
import model.MedicalReportEntry;
import model.PrescriptionItem;
import view.common.WrapLayout;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

public class MedicalRecordForm extends JPanel {

    private final MedicalRecordDAO medicalRecordDAO = new MedicalRecordDAO();

    private JComboBox<AppointmentOption> cbAppointment;
    private JComboBox<String> cbStatusFilter;
    private JTextField txtPatientName;
    private JTextArea txtDiagnosis;
    private JTextField txtMedicineName;
    private JTextField txtDosage;
    private JTextField txtUsageInstruction;
    private JTable tablePrescriptionItems;
    private DefaultTableModel prescriptionTableModel;
    private JTable tableMedicalRecords;
    private DefaultTableModel reportTableModel;
    private JTable tablePrescriptionDetails;
    private DefaultTableModel prescriptionDetailTableModel;

    private final List<PrescriptionItem> currentPrescriptionItems = new ArrayList<>();
    private int selectedPrescriptionIndex = -1;
    private int currentRekamMedisId = -1;
    private boolean suppressAppointmentChange = false;

    public MedicalRecordForm() {
        setLayout(new BorderLayout());

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG);
        root.setBorder(new EmptyBorder(20, 20, 20, 20));
        root.add(createHeader(), BorderLayout.NORTH);
        root.add(createContent(), BorderLayout.CENTER);

        JScrollPane outerScrollPane = UITheme.createScrollPane(root);
        outerScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        outerScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(outerScrollPane, BorderLayout.CENTER);

        loadAppointments();
        refreshReports();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Medical Record");
        title.setFont(UITheme.DISPLAY_FONT);
        title.setForeground(UITheme.PRIMARY_DARK);

        JLabel subtitle = new JLabel("Manage secure patient health histories and clinical documentation.");
        subtitle.setFont(UITheme.BODY_FONT);
        subtitle.setForeground(UITheme.MUTED);

        header.add(title);
        header.add(Box.createVerticalStrut(6));
        header.add(subtitle);
        return header;
    }

    private JPanel createContent() {
        JPanel workspace = new JPanel(new BorderLayout(0, 18));
        workspace.setOpaque(false);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                createFormWorkspacePanel(),
                createTablePanel());
        mainSplit.setBorder(null);
        mainSplit.setContinuousLayout(true);
        mainSplit.setOneTouchExpandable(true);
        mainSplit.setResizeWeight(0.52);
        mainSplit.setDividerSize(10);
        mainSplit.setDividerLocation(760);

        workspace.add(mainSplit, BorderLayout.CENTER);
        return workspace;
    }

    private JPanel createFormWorkspacePanel() {
    JPanel workspace = new JPanel(new BorderLayout(0, 12));
    workspace.setOpaque(false);
    workspace.setMinimumSize(new Dimension(520, 520));

    JPanel editorCard = createMedicalRecordEntryPanel();
    JPanel prescriptionCard = createPrescriptionEditorPanel();

    JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, editorCard, prescriptionCard);
    split.setBorder(null);
    split.setContinuousLayout(true);
    split.setOneTouchExpandable(true);
    
    // Gunakan ini untuk membagi ruang secara proporsional otomatis (45% atas, 55% bawah)
    split.setResizeWeight(0.45); 
    split.setDividerSize(10);
    
    // HAPUS ATAU KOMENTARI BARIS DI BAWAH INI AGAR FIELD DIAGNOSIS TIDAK KEPOTONG:
    // split.setDividerLocation(340); 

    workspace.add(split, BorderLayout.CENTER);
    return workspace;
}

    private JPanel createMedicalRecordEntryPanel() {
        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER),
                new EmptyBorder(18, 18, 18, 18)));

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        JPanel reportForm = new JPanel(new GridBagLayout());
        reportForm.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 10, 0);

        cbAppointment = new JComboBox<>();
        UITheme.styleComboBox(cbAppointment);
        cbAppointment.addActionListener(e -> onAppointmentChanged());

        txtPatientName = createField();
        txtPatientName.setEditable(false);
        txtDiagnosis = createTextArea(8);
        JScrollPane diagnosisScroll = UITheme.createScrollPane(txtDiagnosis);
        diagnosisScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        diagnosisScroll.setPreferredSize(new Dimension(360, 160));

        addField(reportForm, gbc, "Appointment", cbAppointment);
        addField(reportForm, gbc, "Patient Name", txtPatientName);
        addField(reportForm, gbc, "Diagnosis", diagnosisScroll);

        JPanel reportButtons = new JPanel(new WrapLayout(java.awt.FlowLayout.LEFT, 8, 8));
        reportButtons.setOpaque(false);
        reportButtons.setAlignmentX(LEFT_ALIGNMENT);

        JButton btnAdd = UITheme.createPrimaryButton("Add");
        JButton btnUpdate = UITheme.createSecondaryButton("Update");
        JButton btnDelete = UITheme.createDangerButton("Delete");
        JButton btnSave = UITheme.createPrimaryButton("Save");

        btnAdd.addActionListener(e -> newReport());
        btnUpdate.addActionListener(e -> updateReport());
        btnDelete.addActionListener(e -> deleteReport());
        btnSave.addActionListener(e -> saveReport());

        body.add(reportForm);
        body.add(Box.createVerticalStrut(12));
        body.add(reportButtons);

        reportButtons.add(btnAdd);
        reportButtons.add(Box.createHorizontalStrut(8));
        reportButtons.add(btnUpdate);
        reportButtons.add(Box.createHorizontalStrut(8));
        reportButtons.add(btnDelete);
        reportButtons.add(Box.createHorizontalStrut(8));
        reportButtons.add(btnSave);

        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private JPanel createPrescriptionEditorPanel() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER),
                new EmptyBorder(18, 18, 18, 18)));

        JLabel title = new JLabel("Prescription Items");
        title.setFont(UITheme.TITLE_FONT);
        title.setForeground(UITheme.PRIMARY_DARK);

        JLabel subtitle = new JLabel("Add multiple medicines, dosage, and usage instructions for the selected report.");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(UITheme.MUTED);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        JPanel itemForm = new JPanel(new GridBagLayout());
        itemForm.setOpaque(false);
        GridBagConstraints itemGbc = new GridBagConstraints();
        itemGbc.gridx = 0;
        itemGbc.gridy = 0;
        itemGbc.weightx = 1.0;
        itemGbc.fill = GridBagConstraints.HORIZONTAL;
        itemGbc.insets = new Insets(0, 0, 10, 0);

        txtMedicineName = createField();
        txtDosage = createField();
        txtUsageInstruction = createField();

        addField(itemForm, itemGbc, "Medicine Name", txtMedicineName);
        addField(itemForm, itemGbc, "Dosage", txtDosage);
        addField(itemForm, itemGbc, "Usage Instruction", txtUsageInstruction);

        JPanel itemButtons = new JPanel(new WrapLayout(java.awt.FlowLayout.LEFT, 8, 8));
        itemButtons.setOpaque(false);
        itemButtons.setAlignmentX(LEFT_ALIGNMENT);

        JButton btnAddItem = UITheme.createPrimaryButton("Add Item");
        JButton btnUpdateItem = UITheme.createSecondaryButton("Update Item");
        JButton btnDeleteItem = UITheme.createDangerButton("Delete Item");
        JButton btnClearItem = UITheme.createSecondaryButton("Clear Item");

        btnAddItem.addActionListener(e -> addPrescriptionItem());
        btnUpdateItem.addActionListener(e -> updatePrescriptionItem());
        btnDeleteItem.addActionListener(e -> deletePrescriptionItem());
        btnClearItem.addActionListener(e -> clearPrescriptionItemForm());

        itemButtons.add(btnAddItem);
        itemButtons.add(Box.createHorizontalStrut(8));
        itemButtons.add(btnUpdateItem);
        itemButtons.add(Box.createHorizontalStrut(8));
        itemButtons.add(btnDeleteItem);
        itemButtons.add(Box.createHorizontalStrut(8));
        itemButtons.add(btnClearItem);

        prescriptionTableModel = new DefaultTableModel(new Object[] { "Medicine", "Dosage", "Usage Instruction" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablePrescriptionItems = new JTable(prescriptionTableModel);
        tablePrescriptionItems.setRowHeight(28);
        tablePrescriptionItems.setFont(UITheme.BODY_FONT);
        tablePrescriptionItems.getTableHeader().setFont(UITheme.LABEL_FONT);
        tablePrescriptionItems.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablePrescriptionItems.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    fillPrescriptionItemSelection();
                }
            }
        });
        JScrollPane prescriptionScroll = TableScrollSupport.createTableScrollPane(tablePrescriptionItems, 130, 110, 170);
        prescriptionScroll.setPreferredSize(new Dimension(540, 220));

        JPanel sectionHeader = new JPanel(new BorderLayout());
        sectionHeader.setOpaque(false);
        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.add(title);
        titleBlock.add(subtitle);
        sectionHeader.add(titleBlock, BorderLayout.WEST);

        body.add(itemForm);
        body.add(Box.createVerticalStrut(10));
        body.add(itemButtons);
        body.add(Box.createVerticalStrut(12));
        body.add(prescriptionScroll);

        card.add(sectionHeader, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private JPanel createTablePanel() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER),
                new EmptyBorder(18, 18, 18, 18)));
        card.setMinimumSize(new Dimension(520, 520));

        JLabel title = new JLabel("Medical Records");
        title.setFont(UITheme.TITLE_FONT);
        title.setForeground(UITheme.PRIMARY_DARK);

        JLabel subtitle = new JLabel("One medical report per row. Select a report to inspect its prescription details below.");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(UITheme.MUTED);

        reportTableModel = new DefaultTableModel(
                new Object[] { "Report ID", "Appointment", "Patient", "Diagnosis" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableMedicalRecords = new JTable(reportTableModel);
        tableMedicalRecords.setRowHeight(30);
        tableMedicalRecords.setFont(UITheme.BODY_FONT);
        tableMedicalRecords.getTableHeader().setFont(UITheme.LABEL_FONT);
        tableMedicalRecords.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tableMedicalRecords.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableMedicalRecords.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    fillFormFromReportSelection();
                }
            }
        });

        JScrollPane scrollPane = TableScrollSupport.createTableScrollPane(tableMedicalRecords,
                90, 180, 180, 240);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        prescriptionDetailTableModel = new DefaultTableModel(
                new Object[] { "Medicine", "Dosage", "Usage Instruction" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablePrescriptionDetails = new JTable(prescriptionDetailTableModel);
        tablePrescriptionDetails.setRowHeight(28);
        tablePrescriptionDetails.setFont(UITheme.BODY_FONT);
        tablePrescriptionDetails.getTableHeader().setFont(UITheme.LABEL_FONT);
        tablePrescriptionDetails.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tablePrescriptionDetails.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane detailScrollPane = TableScrollSupport.createTableScrollPane(tablePrescriptionDetails,
                180, 120, 240);
        detailScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        detailScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        JPanel topSection = new JPanel(new BorderLayout(0, 8));
        topSection.setOpaque(false);
        topSection.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomSection = new JPanel(new BorderLayout(0, 8));
        bottomSection.setOpaque(false);
        JLabel detailTitle = new JLabel("Prescription Details");
        detailTitle.setFont(UITheme.TITLE_FONT);
        detailTitle.setForeground(UITheme.PRIMARY_DARK);
        JLabel detailSubtitle = new JLabel("Automatically synchronized with the selected medical report.");
        detailSubtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        detailSubtitle.setForeground(UITheme.MUTED);

        JPanel detailHeader = new JPanel(new BorderLayout());
        detailHeader.setOpaque(false);
        JPanel detailTitleBlock = new JPanel();
        detailTitleBlock.setOpaque(false);
        detailTitleBlock.setLayout(new BoxLayout(detailTitleBlock, BoxLayout.Y_AXIS));
        detailTitleBlock.add(detailTitle);
        detailTitleBlock.add(detailSubtitle);
        detailHeader.add(detailTitleBlock, BorderLayout.WEST);
        bottomSection.add(detailHeader, BorderLayout.NORTH);
        bottomSection.add(detailScrollPane, BorderLayout.CENTER);

        JButton btnRefresh = UITheme.createPrimaryButton("Refresh");
        btnRefresh.addActionListener(e -> refreshAllData());

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(subtitle, BorderLayout.WEST);
        JPanel topRight = new JPanel();
        topRight.setOpaque(false);
        topRight.setLayout(new BoxLayout(topRight, BoxLayout.X_AXIS));

        JLabel filterLabel = new JLabel("Status");
        filterLabel.setFont(UITheme.LABEL_FONT);
        filterLabel.setForeground(UITheme.MUTED);

        cbStatusFilter = new JComboBox<>(new String[] { "All", "Disetujui", "Selesai" });
        UITheme.styleComboBox(cbStatusFilter);
        cbStatusFilter.addActionListener(e -> refreshAllData());
        cbStatusFilter.setMaximumSize(new Dimension(160, 34));

        topRight.add(filterLabel);
        topRight.add(Box.createHorizontalStrut(8));
        topRight.add(cbStatusFilter);
        topRight.add(Box.createHorizontalStrut(10));
        topRight.add(btnRefresh);

        top.add(topRight, BorderLayout.EAST);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(title, BorderLayout.NORTH);
        header.add(top, BorderLayout.CENTER);

        javax.swing.JSplitPane split = new javax.swing.JSplitPane(javax.swing.JSplitPane.VERTICAL_SPLIT, topSection, bottomSection);
        split.setResizeWeight(0.42);
        split.setDividerLocation(0.42);
        split.setOneTouchExpandable(true);
        split.setContinuousLayout(true);
        split.setBorder(null);

        card.add(header, BorderLayout.NORTH);
        card.add(split, BorderLayout.CENTER);
        card.setMinimumSize(new Dimension(520, 420));
        return card;
    }

    private void loadAppointments() {
        loadAppointments(currentRekamMedisId > 0 ? findPendaftaranIdByMedicalRecord(currentRekamMedisId) : null);
    }

    private void loadAppointments(Integer preferredPendaftaranId) {
        cbAppointment.removeAllItems();
        List<Object[]> rows = medicalRecordDAO.findAppointmentOptions(getSelectedStatusFilter());
        for (Object[] row : rows) {
            Integer rekamMedisId = row[5] != null ? Integer.valueOf(row[5].toString()) : null;
            cbAppointment.addItem(new AppointmentOption(
                    Integer.parseInt(row[0].toString()),
                    safeText(row[1]),
                    safeText(row[2]),
                    safeText(row[3]),
                    row[4] instanceof java.sql.Date ? (java.sql.Date) row[4] : null,
                    rekamMedisId));
        }

        if (preferredPendaftaranId != null && preferredPendaftaranId > 0) {
            selectAppointmentByPendaftaranId(preferredPendaftaranId);
        } else if (cbAppointment.getItemCount() > 0) {
            cbAppointment.setSelectedIndex(0);
        } else {
            currentRekamMedisId = -1;
            updatePatientNameFromAppointment();
            txtDiagnosis.setText("");
            currentPrescriptionItems.clear();
            refreshPrescriptionTable();
            clearPrescriptionDetails();
        }

        updatePatientNameFromAppointment();
    }

    private void refreshReports() {
        refreshReports(getSelectedStatusFilter());
    }

    private void refreshReports(String statusFilter) {
        int selectedReportId = currentRekamMedisId;
        List<MedicalReportEntry> rows = medicalRecordDAO.findAllReports(statusFilter);
        tableMedicalRecords.clearSelection();
        reportTableModel.setRowCount(0);
        for (MedicalReportEntry entry : rows) {
            reportTableModel.addRow(new Object[] {
                    entry.getRekamMedisId(),
                    formatAppointment(entry),
                    safeText(entry.getPatientName()),
                    safeText(entry.getDiagnosis())
            });
        }
        if (selectedReportId > 0) {
            selectReportRowById(selectedReportId);
        } else {
            clearPrescriptionDetails();
        }
    }

    private void refreshAllData() {
        Integer preferredPendaftaranId = getSelectedAppointment() != null ? getSelectedAppointment().pendaftaranId : null;
        refreshReports();
        loadAppointments(preferredPendaftaranId);
    }

    private void onAppointmentChanged() {
        if (suppressAppointmentChange) {
            return;
        }
        updatePatientNameFromAppointment();
        AppointmentOption option = getSelectedAppointment();
        if (option == null) {
            currentRekamMedisId = -1;
            txtDiagnosis.setText("");
            currentPrescriptionItems.clear();
            refreshPrescriptionTable();
            clearPrescriptionDetails();
            return;
        }

        if (option.rekamMedisId != null && option.rekamMedisId > 0) {
            MedicalReportEntry report = medicalRecordDAO.findReportByRekamMedisId(option.rekamMedisId);
            if (report != null) {
                loadReportIntoForm(report);
            }
        } else {
            currentRekamMedisId = -1;
            txtDiagnosis.setText("");
            currentPrescriptionItems.clear();
            refreshPrescriptionTable();
            clearPrescriptionDetails();
        }
    }

    private void updatePatientNameFromAppointment() {
        AppointmentOption option = getSelectedAppointment();
        txtPatientName.setText(option != null ? option.patientName : "");
    }

    private void loadReportIntoForm(MedicalReportEntry report) {
        currentRekamMedisId = report.getRekamMedisId();
        suppressAppointmentChange = true;
        try {
            selectAppointmentByPendaftaranId(report.getPendaftaranId());
        } finally {
            suppressAppointmentChange = false;
        }
        txtDiagnosis.setText(safeText(report.getDiagnosis()));

        currentPrescriptionItems.clear();
        currentPrescriptionItems.addAll(report.getPrescriptions());
        refreshPrescriptionTable();
        refreshPrescriptionDetails(report.getPrescriptions());
        clearPrescriptionItemForm();
    }

    private void fillFormFromReportSelection() {
        int selectedRow = tableMedicalRecords.getSelectedRow();
        if (selectedRow < 0) {
            return;
        }

        int reportId = Integer.parseInt(reportTableModel.getValueAt(selectedRow, 0).toString());
        MedicalReportEntry report = medicalRecordDAO.findReportByRekamMedisId(reportId);
        if (report != null) {
            loadReportIntoForm(report);
        }
    }

    private void saveReport() {
        persistReport(false);
    }

    private void updateReport() {
        persistReport(true);
    }

    private void persistReport(boolean requireExisting) {
        AppointmentOption appointment = getSelectedAppointment();
        if (appointment == null) {
            JOptionPane.showMessageDialog(this, "Please select an appointment.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (currentPrescriptionItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please add at least one prescription item.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (txtDiagnosis.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Diagnosis is required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (requireExisting && currentRekamMedisId <= 0) {
            JOptionPane.showMessageDialog(this, "Select an existing medical record first.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            currentRekamMedisId = medicalRecordDAO.saveMedicalRecord(
                    currentRekamMedisId,
                    appointment.pendaftaranId,
                    txtDiagnosis.getText().trim(),
                    new ArrayList<>(currentPrescriptionItems));
            JOptionPane.showMessageDialog(this, "Medical record saved successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            refreshReports();
            loadAppointments(appointment.pendaftaranId);
            selectAppointmentByPendaftaranId(appointment.pendaftaranId);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteReport() {
        if (currentRekamMedisId <= 0) {
            JOptionPane.showMessageDialog(this, "Select a medical record first.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete this medical record?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            boolean deleted = medicalRecordDAO.deleteMedicalRecord(currentRekamMedisId);
            if (deleted) {
                JOptionPane.showMessageDialog(this, "Medical record deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshReports();
                loadAppointments(null);
                newReport();
            } else {
                JOptionPane.showMessageDialog(this, "No medical record was deleted.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void newReport() {
        currentRekamMedisId = -1;
        tableMedicalRecords.clearSelection();
        suppressAppointmentChange = true;
        try {
            cbAppointment.setSelectedIndex(-1);
        } finally {
            suppressAppointmentChange = false;
        }
        txtPatientName.setText("");
        txtDiagnosis.setText("");
        currentPrescriptionItems.clear();
        refreshPrescriptionTable();
        clearPrescriptionDetails();
        clearPrescriptionItemForm();
    }

    private void addPrescriptionItem() {
        PrescriptionItem item = new PrescriptionItem();
        item.setMedicineName(txtMedicineName.getText().trim());
        item.setDosage(txtDosage.getText().trim());
        item.setUsageInstruction(txtUsageInstruction.getText().trim());

        if (item.getMedicineName().isEmpty() || item.getDosage().isEmpty() || item.getUsageInstruction().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Medicine, dosage, and usage instruction are required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        currentPrescriptionItems.add(item);
        refreshPrescriptionTable();
        clearPrescriptionItemForm();
    }

    private void updatePrescriptionItem() {
        if (selectedPrescriptionIndex < 0 || selectedPrescriptionIndex >= currentPrescriptionItems.size()) {
            JOptionPane.showMessageDialog(this, "Select a prescription item first.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        PrescriptionItem item = currentPrescriptionItems.get(selectedPrescriptionIndex);
        item.setMedicineName(txtMedicineName.getText().trim());
        item.setDosage(txtDosage.getText().trim());
        item.setUsageInstruction(txtUsageInstruction.getText().trim());

        if (item.getMedicineName().isEmpty() || item.getDosage().isEmpty() || item.getUsageInstruction().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Medicine, dosage, and usage instruction are required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        refreshPrescriptionTable();
        clearPrescriptionItemForm();
    }

    private void deletePrescriptionItem() {
        if (selectedPrescriptionIndex < 0 || selectedPrescriptionIndex >= currentPrescriptionItems.size()) {
            JOptionPane.showMessageDialog(this, "Select a prescription item first.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        currentPrescriptionItems.remove(selectedPrescriptionIndex);
        refreshPrescriptionTable();
        clearPrescriptionItemForm();
    }

    private void refreshPrescriptionTable() {
        prescriptionTableModel.setRowCount(0);
        for (PrescriptionItem item : currentPrescriptionItems) {
            prescriptionTableModel.addRow(new Object[] {
                    safeText(item.getMedicineName()),
                    safeText(item.getDosage()),
                    safeText(item.getUsageInstruction())
            });
        }
        selectedPrescriptionIndex = -1;
        tablePrescriptionItems.clearSelection();
    }

    private void refreshPrescriptionDetails(List<PrescriptionItem> items) {
        prescriptionDetailTableModel.setRowCount(0);
        if (items == null) {
            return;
        }

        for (PrescriptionItem item : items) {
            prescriptionDetailTableModel.addRow(new Object[] {
                    safeText(item.getMedicineName()),
                    safeText(item.getDosage()),
                    safeText(item.getUsageInstruction())
            });
        }
    }

    private void clearPrescriptionDetails() {
        if (prescriptionDetailTableModel != null) {
            prescriptionDetailTableModel.setRowCount(0);
        }
    }

    private void fillPrescriptionItemSelection() {
        int selectedRow = tablePrescriptionItems.getSelectedRow();
        if (selectedRow < 0) {
            return;
        }

        selectedPrescriptionIndex = selectedRow;
        PrescriptionItem item = currentPrescriptionItems.get(selectedRow);
        txtMedicineName.setText(safeText(item.getMedicineName()));
        txtDosage.setText(safeText(item.getDosage()));
        txtUsageInstruction.setText(safeText(item.getUsageInstruction()));
    }

    private void clearPrescriptionItemForm() {
        txtMedicineName.setText("");
        txtDosage.setText("");
        txtUsageInstruction.setText("");
        selectedPrescriptionIndex = -1;
        tablePrescriptionItems.clearSelection();
    }

    private AppointmentOption getSelectedAppointment() {
        return (AppointmentOption) cbAppointment.getSelectedItem();
    }

    private void selectAppointmentByPendaftaranId(int pendaftaranId) {
        for (int i = 0; i < cbAppointment.getItemCount(); i++) {
            AppointmentOption option = cbAppointment.getItemAt(i);
            if (option != null && option.pendaftaranId == pendaftaranId) {
                suppressAppointmentChange = true;
                try {
                    cbAppointment.setSelectedIndex(i);
                } finally {
                    suppressAppointmentChange = false;
                }
                updatePatientNameFromAppointment();
                return;
            }
        }
    }

    private void selectReportRowById(int reportId) {
        for (int row = 0; row < reportTableModel.getRowCount(); row++) {
            Object value = reportTableModel.getValueAt(row, 0);
            if (value != null && Integer.parseInt(value.toString()) == reportId) {
                tableMedicalRecords.getSelectionModel().setSelectionInterval(row, row);
                tableMedicalRecords.scrollRectToVisible(tableMedicalRecords.getCellRect(row, 0, true));
                return;
            }
        }
    }

    private Integer findPendaftaranIdByMedicalRecord(int rekamMedisId) {
        MedicalReportEntry report = medicalRecordDAO.findReportByRekamMedisId(rekamMedisId);
        return report != null ? report.getPendaftaranId() : null;
    }

    private String getSelectedStatusFilter() {
        Object selected = cbStatusFilter != null ? cbStatusFilter.getSelectedItem() : null;
        return selected != null ? selected.toString() : "All";
    }

    private void addField(JPanel form, GridBagConstraints gbc, String labelText, java.awt.Component field) {
        JLabel label = new JLabel(labelText);
        UITheme.applyLabel(label);
        form.add(label, gbc);
        gbc.gridy++;
        form.add(field, gbc);
        gbc.gridy++;
    }

    private JTextField createField() {
        return UITheme.createTextField();
    }

    private JTextArea createTextArea(int rows) {
        JTextArea area = new JTextArea(rows, 20);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(UITheme.BODY_FONT);
        area.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER),
                new EmptyBorder(10, 12, 10, 12)));
        area.setBackground(new Color(251, 253, 255));
        return area;
    }

    private String formatAppointment(MedicalReportEntry entry) {
        return "#" + entry.getPendaftaranId() + " | " + formatDate(entry.getVisitDate());
    }

    private String formatDate(java.sql.Date date) {
        return date == null ? "-" : new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    private String safeText(String value) {
        return value == null || value.isBlank() ? "-" : value.trim();
    }

    private String safeText(Object value) {
        return value == null ? "-" : safeText(String.valueOf(value));
    }

    private static class AppointmentOption {
        private final int pendaftaranId;
        private final String patientName;
        private final String doctorName;
        private final String poliName;
        private final java.sql.Date visitDate;
        private final Integer rekamMedisId;

        private AppointmentOption(int pendaftaranId, String patientName, String doctorName, String poliName,
                java.sql.Date visitDate, Integer rekamMedisId) {
            this.pendaftaranId = pendaftaranId;
            this.patientName = patientName;
            this.doctorName = doctorName;
            this.poliName = poliName;
            this.visitDate = visitDate;
            this.rekamMedisId = rekamMedisId;
        }

        @Override
        public String toString() {
            return "#" + pendaftaranId + " | " + patientName + " | " + doctorName + " | " + poliName + " | "
                    + (visitDate != null ? new SimpleDateFormat("yyyy-MM-dd").format(visitDate) : "-");
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("CarePoint - Medical Record");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setContentPane(new MedicalRecordForm());
            frame.setMinimumSize(new Dimension(1280, 820));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}

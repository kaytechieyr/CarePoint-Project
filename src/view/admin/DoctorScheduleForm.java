package view.admin;

import config.UITheme;
import config.TableScrollSupport;
import dao.DokterDAO;
import dao.JadwalDokterDAO;
import model.Dokter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JFormattedTextField;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

public class DoctorScheduleForm extends JPanel {

    private final DokterDAO dokterDAO = new DokterDAO();
    private final JadwalDokterDAO jadwalDokterDAO = new JadwalDokterDAO();

    private JTextField txtScheduleId;
    private JComboBox<Dokter> cbDoctor;
    private JComboBox<String> cbDay;
    private JFormattedTextField txtStartTime;
    private JFormattedTextField txtEndTime;
    private JTextField txtSearch;
    private JTable tableSchedule;
    private DefaultTableModel tableModel;
    private int selectedScheduleId = -1;

    public DoctorScheduleForm() {
        setLayout(new BorderLayout());

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG);
        root.setBorder(new EmptyBorder(20, 20, 20, 20));
        root.add(createHeader(), BorderLayout.NORTH);
        root.add(createContent(), BorderLayout.CENTER);

        add(UITheme.createScrollPane(root), BorderLayout.CENTER);

        loadDoctors();
        refreshTable();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Doctor Schedule");
        title.setFont(new Font("SansSerif", Font.BOLD, 30));
        title.setForeground(new Color(18, 52, 86));

        JLabel subtitle = new JLabel("Manage and track doctor availability schedules across all clinics.");
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

        JPanel formPanel = createFormCard();
        JPanel tablePanel = createTableCard();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, formPanel, tablePanel);
        splitPane.setBorder(null);
        splitPane.setDividerLocation(380);
        splitPane.setResizeWeight(0.34);
        splitPane.setContinuousLayout(true);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerSize(8);

        content.add(splitPane, BorderLayout.CENTER);
        return content;
    }

    private JPanel createFormCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 236, 246)),
                new EmptyBorder(20, 20, 20, 20)));

        JLabel title = new JLabel("Schedule Form");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(new Color(18, 52, 86));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 10, 0);

        txtScheduleId = UITheme.createTextField();
        txtScheduleId.setEditable(false);
        cbDoctor = new JComboBox<>();
        UITheme.styleComboBox(cbDoctor);
        cbDay = new JComboBox<>(new String[] {
                "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday" });
        UITheme.styleComboBox(cbDay);
        txtStartTime = createTimeField();
        txtEndTime = createTimeField();

        addField(form, gbc, "Schedule ID", txtScheduleId);
        addField(form, gbc, "Doctor", cbDoctor);
        addField(form, gbc, "Day", cbDay);
        addField(form, gbc, "Start Time (HH:mm)", txtStartTime);
        addField(form, gbc, "End Time (HH:mm)", txtEndTime);

        JPanel buttons = new JPanel();
        buttons.setOpaque(false);
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));

        JButton btnAdd = UITheme.createPrimaryButton("Add");
        JButton btnUpdate = UITheme.createSecondaryButton("Update");
        JButton btnDelete = UITheme.createDangerButton("Delete");
        JButton btnClear = UITheme.createSecondaryButton("Clear");
        JButton btnRefresh = UITheme.createPrimaryButton("Refresh");

        btnAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addSchedule();
            }
        });
        btnUpdate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateSchedule();
            }
        });
        btnDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteSchedule();
            }
        });
        btnClear.addActionListener(e -> clearForm());
        btnRefresh.addActionListener(e -> refreshTable());

        buttons.add(btnAdd);
        buttons.add(Box.createVerticalStrut(8));
        buttons.add(btnUpdate);
        buttons.add(Box.createVerticalStrut(8));
        buttons.add(btnDelete);
        buttons.add(Box.createVerticalStrut(8));
        buttons.add(btnClear);
        buttons.add(Box.createVerticalStrut(8));
        buttons.add(btnRefresh);

        card.add(title, BorderLayout.NORTH);
        card.add(form, BorderLayout.CENTER);
        card.add(buttons, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createTableCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 236, 246)),
                new EmptyBorder(20, 20, 20, 20)));

        JLabel title = new JLabel("Schedule Data");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(new Color(18, 52, 86));

        JPanel searchRow = new JPanel(new BorderLayout());
        searchRow.setOpaque(false);
        txtSearch = UITheme.createTextField();
        txtSearch.setToolTipText("Search doctor name or day");
        JButton btnSearch = UITheme.createPrimaryButton("Search");
        JButton btnRefresh = UITheme.createSecondaryButton("Refresh");
        btnSearch.addActionListener(e -> refreshTable(txtSearch.getText().trim()));
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            refreshTable();
        });

        JPanel searchButtons = new JPanel();
        searchButtons.setOpaque(false);
        searchButtons.add(btnSearch);
        searchButtons.add(Box.createHorizontalStrut(8));
        searchButtons.add(btnRefresh);

        searchRow.add(txtSearch, BorderLayout.CENTER);
        searchRow.add(searchButtons, BorderLayout.EAST);

        tableModel = new DefaultTableModel(
                new Object[] { "ID", "Doctor ID", "Doctor", "Poli", "Day", "Start Time", "End Time" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableSchedule = new JTable(tableModel);
        tableSchedule.setRowHeight(28);
        tableSchedule.setFont(new Font("SansSerif", Font.PLAIN, 13));
        tableSchedule.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        tableSchedule.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableSchedule.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    fillFormFromSelection();
                }
            }
        });

        JScrollPane scrollPane = TableScrollSupport.createTableScrollPane(tableSchedule, 70, 90, 140, 120, 110, 110, 110);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(title, BorderLayout.NORTH);
        top.add(searchRow, BorderLayout.CENTER);

        card.add(top, BorderLayout.NORTH);
        card.add(scrollPane, BorderLayout.CENTER);
        return card;
    }

    private void configureTableColumns() {
        TableScrollSupport.configureWideTable(tableSchedule, 70, 90, 140, 120, 110, 110, 110);
    }

    private void addField(JPanel form, GridBagConstraints gbc, String labelText, java.awt.Component field) {
        JLabel label = new JLabel(labelText);
        UITheme.applyLabel(label);
        form.add(label, gbc);
        gbc.gridy++;
        form.add(field, gbc);
        gbc.gridy++;
    }

    private JFormattedTextField createTimeField() {
        try {
            javax.swing.text.MaskFormatter mask = new javax.swing.text.MaskFormatter("##:##");
            mask.setPlaceholderCharacter('_');
            JFormattedTextField field = new JFormattedTextField(mask);
            field.setFont(UITheme.BODY_FONT);
            field.setBackground(new Color(251, 253, 255));
            field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 215, 232)),
                    new EmptyBorder(10, 12, 10, 12)));
            return field;
        } catch (java.text.ParseException e) {
            throw new RuntimeException("Failed to create time field: " + e.getMessage(), e);
        }
    }

    private void loadDoctors() {
        List<Dokter> doctors = dokterDAO.findAll();
        cbDoctor.removeAllItems();
        for (Dokter dokter : doctors) {
            cbDoctor.addItem(dokter);
        }
    }

    private void refreshTable() {
        refreshTable(txtSearch != null ? txtSearch.getText().trim() : null);
    }

    private void refreshTable(String keyword) {
        List<Object[]> rows = jadwalDokterDAO.findAll(keyword);
        tableModel.setRowCount(0);
        for (Object[] row : rows) {
            tableModel.addRow(row);
        }
        configureTableColumns();
        clearSelectionOnly();
    }

    private void fillFormFromSelection() {
        int selectedRow = tableSchedule.getSelectedRow();
        if (selectedRow < 0) {
            return;
        }

        selectedScheduleId = Integer.parseInt(tableModel.getValueAt(selectedRow, 0).toString());
        int doctorId = Integer.parseInt(tableModel.getValueAt(selectedRow, 1).toString());
        txtScheduleId.setText(String.valueOf(selectedScheduleId));
        selectDoctorById(doctorId);
        cbDay.setSelectedItem(tableModel.getValueAt(selectedRow, 4).toString());
        txtStartTime.setText(tableModel.getValueAt(selectedRow, 5).toString());
        txtEndTime.setText(tableModel.getValueAt(selectedRow, 6).toString());
    }

    private void selectDoctorById(int doctorId) {
        if (cbDoctor.getItemCount() == 0) {
            return;
        }
        for (int i = 0; i < cbDoctor.getItemCount(); i++) {
            Dokter dokter = cbDoctor.getItemAt(i);
            if (dokter != null && dokter.getDokterId() == doctorId) {
                cbDoctor.setSelectedIndex(i);
                return;
            }
        }
    }

    private Dokter getSelectedDoctor() {
        return (Dokter) cbDoctor.getSelectedItem();
    }

    private boolean isValidTime(String value) {
        return value != null && value.matches("\\d{2}:\\d{2}");
    }

    private void addSchedule() {
        Dokter dokter = getSelectedDoctor();
        if (dokter == null) {
            JOptionPane.showMessageDialog(this, "Please choose a doctor.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String hari = String.valueOf(cbDay.getSelectedItem());
        String jamMulai = txtStartTime.getText().trim();
        String jamSelesai = txtEndTime.getText().trim();

        if (!isValidTime(jamMulai) || !isValidTime(jamSelesai)) {
            JOptionPane.showMessageDialog(this, "Please enter valid time in HH:mm format.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (jamMulai.compareTo(jamSelesai) >= 0) {
            JOptionPane.showMessageDialog(this, "Start time must be earlier than end time.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            jadwalDokterDAO.insertSchedule(dokter.getDokterId(), hari, jamMulai, jamSelesai);
            JOptionPane.showMessageDialog(this, "Schedule saved successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            refreshTable();
            clearForm();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateSchedule() {
        if (selectedScheduleId <= 0) {
            JOptionPane.showMessageDialog(this, "Please select a schedule row first.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Dokter dokter = getSelectedDoctor();
        if (dokter == null) {
            JOptionPane.showMessageDialog(this, "Please choose a doctor.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String hari = String.valueOf(cbDay.getSelectedItem());
        String jamMulai = txtStartTime.getText().trim();
        String jamSelesai = txtEndTime.getText().trim();

        if (!isValidTime(jamMulai) || !isValidTime(jamSelesai)) {
            JOptionPane.showMessageDialog(this, "Please enter valid time in HH:mm format.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (jamMulai.compareTo(jamSelesai) >= 0) {
            JOptionPane.showMessageDialog(this, "Start time must be earlier than end time.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            boolean updated = jadwalDokterDAO.updateSchedule(selectedScheduleId, dokter.getDokterId(), hari, jamMulai, jamSelesai);
            if (updated) {
                JOptionPane.showMessageDialog(this, "Schedule updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshTable();
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "No schedule was updated.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSchedule() {
        if (selectedScheduleId <= 0) {
            JOptionPane.showMessageDialog(this, "Please select a schedule row first.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete this schedule?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            boolean deleted = jadwalDokterDAO.deleteSchedule(selectedScheduleId);
            if (deleted) {
                JOptionPane.showMessageDialog(this, "Schedule deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshTable();
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "No schedule was deleted.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        clearSelectionOnly();
        if (cbDoctor.getItemCount() > 0) {
            cbDoctor.setSelectedIndex(0);
        }
        if (cbDay.getItemCount() > 0) {
            cbDay.setSelectedIndex(0);
        }
        txtStartTime.setText("");
        txtEndTime.setText("");
        txtScheduleId.setText("");
    }

    private void clearSelectionOnly() {
        selectedScheduleId = -1;
        if (tableSchedule.getSelectionModel() != null) {
            tableSchedule.clearSelection();
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("CarePoint - Doctor Schedule");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setContentPane(new DoctorScheduleForm());
            frame.setMinimumSize(new Dimension(1260, 780));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}

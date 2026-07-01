package view.admin;

import config.TableScrollSupport;
import config.UITheme;
import dao.PendaftaranDAO;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
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

public class RegistrationApprovalForm extends JPanel {

    private final PendaftaranDAO pendaftaranDAO = new PendaftaranDAO();

    private JTextField txtSearch;
    private JTextField txtAppointmentId;
    private JTextField txtPatientName;
    private JTextField txtDoctorName;
    private JTextField txtPoliName;
    private JTextField txtVisitDate;
    private JTextField txtQueueNumber;
    private JTextArea txtComplaint;
    private JComboBox<String> cbStatus;
    private JComboBox<String> cbSearchStatus;
    private JTable tableAppointments;
    private DefaultTableModel tableModel;
    private int selectedPendaftaranId = -1;

    public RegistrationApprovalForm() {
        setLayout(new BorderLayout());

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG);
        root.setBorder(new EmptyBorder(20, 20, 20, 20));
        root.add(createHeader(), BorderLayout.NORTH);
        root.add(createContent(), BorderLayout.CENTER);

        add(UITheme.createScrollPane(root), BorderLayout.CENTER);

        refreshTable();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Registration Approval");
        title.setFont(new Font("SansSerif", Font.BOLD, 30));
        title.setForeground(new Color(18, 52, 86));

        JLabel subtitle = new JLabel("Monitor all appointments and update their status.");
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

        JPanel detailsCard = createDetailsCard();
        JPanel tableCard = createTableCard();

        JSplitPaneWrapper split = new JSplitPaneWrapper(detailsCard, tableCard);
        content.add(UITheme.createScrollPane(split), BorderLayout.CENTER);
        return content;
    }

    private JPanel createDetailsCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 236, 246)),
                new EmptyBorder(20, 20, 20, 20)));

        JLabel title = new JLabel("Appointment Details");
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

        txtAppointmentId = createField(true);
        txtPatientName = createField(true);
        txtDoctorName = createField(true);
        txtPoliName = createField(true);
        txtVisitDate = createField(true);
        txtQueueNumber = createField(true);
        txtComplaint = new JTextArea(4, 20);
        txtComplaint.setLineWrap(true);
        txtComplaint.setWrapStyleWord(true);
        txtComplaint.setEditable(false);
        txtComplaint.setFont(UITheme.BODY_FONT);
        txtComplaint.setBackground(new Color(251, 253, 255));
        txtComplaint.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 215, 232)),
                new EmptyBorder(10, 12, 10, 12)));
        cbStatus = new JComboBox<>(new String[] { "Menunggu", "Disetujui", "Selesai", "Dibatalkan" });
        UITheme.styleComboBox(cbStatus);

        addField(form, gbc, "Appointment ID", txtAppointmentId);
        addField(form, gbc, "Patient Name", txtPatientName);
        addField(form, gbc, "Doctor", txtDoctorName);
        addField(form, gbc, "Poli", txtPoliName);
        addField(form, gbc, "Visit Date", txtVisitDate);
        addField(form, gbc, "Queue Number", txtQueueNumber);
        addField(form, gbc, "Complaint", new JScrollPane(txtComplaint));
        addField(form, gbc, "Status", cbStatus);

        JPanel buttons = new JPanel();
        buttons.setOpaque(false);
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));

        JButton btnApprove = UITheme.createPrimaryButton("Approve");
        JButton btnUpdate = UITheme.createSecondaryButton("Update Status");
        JButton btnDelete = UITheme.createDangerButton("Delete");
        JButton btnRefresh = UITheme.createPrimaryButton("Refresh");

        btnApprove.addActionListener(e -> changeStatus("Disetujui"));
        btnUpdate.addActionListener(e -> changeStatus(String.valueOf(cbStatus.getSelectedItem())));
        btnDelete.addActionListener(e -> deleteSelectedAppointment());
        btnRefresh.addActionListener(e -> refreshTable());

        buttons.add(btnApprove);
        buttons.add(Box.createHorizontalStrut(10));
        buttons.add(btnUpdate);
        buttons.add(Box.createHorizontalStrut(10));
        buttons.add(btnDelete);
        buttons.add(Box.createHorizontalStrut(10));
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

        JLabel title = new JLabel("All Appointments");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(new Color(18, 52, 86));

        JPanel searchRow = new JPanel(new BorderLayout());
        searchRow.setOpaque(false);
        txtSearch = createField(true);
        txtSearch.setToolTipText("Search by patient, doctor, poli, complaint, or status");
        cbSearchStatus = new JComboBox<>(new String[] { "All", "Menunggu", "Disetujui", "Selesai", "Dibatalkan" });
        UITheme.styleComboBox(cbSearchStatus);
        JButton btnSearch = UITheme.createSecondaryButton("Search");
        JButton btnReset = UITheme.createPrimaryButton("Refresh");

        btnSearch.addActionListener(e -> refreshTable(txtSearch.getText().trim(), String.valueOf(cbSearchStatus.getSelectedItem())));
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            cbSearchStatus.setSelectedIndex(0);
            refreshTable();
        });

        JPanel searchButtons = new JPanel();
        searchButtons.setOpaque(false);
        searchButtons.add(btnSearch);
        searchButtons.add(Box.createHorizontalStrut(8));
        searchButtons.add(btnReset);

        JPanel searchStack = new JPanel(new BorderLayout(0, 8));
        searchStack.setOpaque(false);
        searchStack.add(txtSearch, BorderLayout.CENTER);

        JPanel searchMeta = new JPanel(new BorderLayout(8, 0));
        searchMeta.setOpaque(false);
        searchMeta.add(cbSearchStatus, BorderLayout.CENTER);
        searchMeta.add(searchButtons, BorderLayout.EAST);
        searchStack.add(searchMeta, BorderLayout.SOUTH);

        searchRow.add(searchStack, BorderLayout.CENTER);

        tableModel = new DefaultTableModel(
                new Object[] { "ID", "Patient", "Doctor", "Poli", "Visit Date", "Queue", "Status", "Complaint" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableAppointments = new JTable(tableModel);
        tableAppointments.setRowHeight(28);
        tableAppointments.setFont(new Font("SansSerif", Font.PLAIN, 13));
        tableAppointments.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        tableAppointments.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableAppointments.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    fillSelection();
                }
            }
        });

        JScrollPane scrollPane = TableScrollSupport.createTableScrollPane(tableAppointments,
                70, 140, 140, 120, 110, 80, 110, 280);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(title, BorderLayout.NORTH);
        top.add(searchRow, BorderLayout.CENTER);

        card.add(top, BorderLayout.NORTH);
        card.add(scrollPane, BorderLayout.CENTER);
        return card;
    }

    private JTextField createField(boolean singleLine) {
        JTextField field = UITheme.createTextField();
        return field;
    }

    private void addField(JPanel form, GridBagConstraints gbc, String labelText, java.awt.Component field) {
        JLabel label = new JLabel(labelText);
        UITheme.applyLabel(label);
        form.add(label, gbc);
        gbc.gridy++;
        form.add(field, gbc);
        gbc.gridy++;
    }

    private void refreshTable() {
        refreshTable(txtSearch != null ? txtSearch.getText().trim() : null,
                cbSearchStatus != null ? String.valueOf(cbSearchStatus.getSelectedItem()) : null);
    }

    private void refreshTable(String keyword, String status) {
        List<Object[]> rows = pendaftaranDAO.searchAll(keyword, status);
        tableModel.setRowCount(0);
        for (Object[] row : rows) {
            tableModel.addRow(row);
        }
        TableScrollSupport.configureWideTable(tableAppointments, 70, 140, 140, 120, 110, 80, 110, 280);
        clearSelection();
    }

    private void fillSelection() {
        int selectedRow = tableAppointments.getSelectedRow();
        if (selectedRow < 0) {
            return;
        }

        selectedPendaftaranId = Integer.parseInt(tableModel.getValueAt(selectedRow, 0).toString());
        txtAppointmentId.setText(String.valueOf(selectedPendaftaranId));
        txtPatientName.setText(tableModel.getValueAt(selectedRow, 1).toString());
        txtDoctorName.setText(tableModel.getValueAt(selectedRow, 2).toString());
        txtPoliName.setText(tableModel.getValueAt(selectedRow, 3).toString());
        txtVisitDate.setText(tableModel.getValueAt(selectedRow, 4).toString());
        txtQueueNumber.setText(tableModel.getValueAt(selectedRow, 5).toString());
        cbStatus.setSelectedItem(pendaftaranDAO.statusForDisplay(tableModel.getValueAt(selectedRow, 6).toString()));
        txtComplaint.setText(tableModel.getValueAt(selectedRow, 7).toString());
    }

    private void changeStatus(String status) {
        if (selectedPendaftaranId <= 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select an appointment first.",
                    "Validation",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            boolean updated = pendaftaranDAO.updateStatus(selectedPendaftaranId, status);
            if (updated) {
                JOptionPane.showMessageDialog(this,
                        "Appointment status updated successfully.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                refreshTable();
            } else {
                JOptionPane.showMessageDialog(this,
                        "No appointment was updated.",
                        "Warning",
                        JOptionPane.WARNING_MESSAGE);
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedAppointment() {
        if (selectedPendaftaranId <= 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select an appointment first.",
                    "Validation",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete this appointment?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            boolean deleted = pendaftaranDAO.delete(selectedPendaftaranId);
            if (deleted) {
                JOptionPane.showMessageDialog(this,
                        "Appointment deleted successfully.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                refreshTable();
            } else {
                JOptionPane.showMessageDialog(this,
                        "No appointment was deleted.",
                        "Warning",
                        JOptionPane.WARNING_MESSAGE);
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearSelection() {
        selectedPendaftaranId = -1;
        if (tableAppointments != null) {
            tableAppointments.clearSelection();
        }
        if (txtAppointmentId != null) {
            txtAppointmentId.setText("");
            txtPatientName.setText("");
            txtDoctorName.setText("");
            txtPoliName.setText("");
            txtVisitDate.setText("");
            txtQueueNumber.setText("");
            txtComplaint.setText("");
            if (cbStatus != null && cbStatus.getItemCount() > 0) {
                cbStatus.setSelectedIndex(0);
            }
        }
    }

    private static class JSplitPaneWrapper extends JPanel {
        JSplitPaneWrapper(JPanel left, JPanel right) {
            super(new BorderLayout());
            javax.swing.JSplitPane split = new javax.swing.JSplitPane(javax.swing.JSplitPane.HORIZONTAL_SPLIT, left, right);
            split.setDividerLocation(420);
            split.setResizeWeight(0.34);
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
            JFrame frame = new JFrame("CarePoint - Registration Approval");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setContentPane(new RegistrationApprovalForm());
            frame.setMinimumSize(new Dimension(1280, 820));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}

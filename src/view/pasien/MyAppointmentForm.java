package view.pasien;

import config.AppSession;
import config.TableScrollSupport;
import config.UITheme;
import dao.PasienDAO;
import dao.PendaftaranDAO;
import model.Pasien;

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
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

public class MyAppointmentForm extends JPanel {

    private final PendaftaranDAO pendaftaranDAO = new PendaftaranDAO();
    private final PasienDAO pasienDAO = new PasienDAO();

    private Pasien currentPasien;
    private JTextField txtPatientName;
    private JTextField txtSearch;
    private JComboBox<String> cbStatusFilter;
    private JTextField txtSelectedInfo;
    private JTable tableAppointments;
    private DefaultTableModel tableModel;
    private int selectedPendaftaranId = -1;

    public MyAppointmentForm() {
        setLayout(new BorderLayout());

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG);
        root.setBorder(new EmptyBorder(20, 20, 20, 20));
        root.add(createHeader(), BorderLayout.NORTH);
        root.add(createContent(), BorderLayout.CENTER);

        add(UITheme.createScrollPane(root), BorderLayout.CENTER);

        loadPatientContext();
        refreshTable();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("My Appointment");
        title.setFont(new Font("SansSerif", Font.BOLD, 30));
        title.setForeground(new Color(18, 52, 86));

        JLabel subtitle = new JLabel("Track your own appointments and cancel only your bookings.");
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

        JPanel filterCard = createFilterCard();
        JPanel tableCard = createTableCard();

        JSplitPaneWrapper split = new JSplitPaneWrapper(filterCard, tableCard);
        content.add(UITheme.createScrollPane(split), BorderLayout.CENTER);
        return content;
    }

    private JPanel createFilterCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 236, 246)),
                new EmptyBorder(20, 20, 20, 20)));

        JLabel title = new JLabel("Filter & Actions");
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

        txtPatientName = createField();
        txtPatientName.setEditable(false);
        txtSearch = createField();
        cbStatusFilter = new JComboBox<>(new String[] { "All", "Menunggu", "Disetujui", "Selesai", "Dibatalkan" });
        UITheme.styleComboBox(cbStatusFilter);
        txtSelectedInfo = createField();
        txtSelectedInfo.setEditable(false);

        addField(form, gbc, "Patient Name", txtPatientName);
        addField(form, gbc, "Keyword", txtSearch);
        addField(form, gbc, "Status", cbStatusFilter);
        addField(form, gbc, "Selected", txtSelectedInfo);

        JPanel buttons = new JPanel();
        buttons.setOpaque(false);
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));

        JButton btnRefresh = UITheme.createPrimaryButton("Refresh");
        JButton btnCancel = UITheme.createDangerButton("Cancel Appointment");
        btnRefresh.addActionListener(e -> refreshTable());
        btnCancel.addActionListener(e -> cancelAppointment());

        buttons.add(btnRefresh);
        buttons.add(Box.createHorizontalStrut(10));
        buttons.add(btnCancel);

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

        JLabel title = new JLabel("Appointment History");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(new Color(18, 52, 86));

        tableModel = new DefaultTableModel(
                new Object[] { "ID", "Doctor", "Poli", "Visit Date", "Schedule Time", "Queue", "Status", "Complaint" }, 0) {
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
                    fillSelectionInfo();
                }
            }
        });

        JScrollPane scrollPane = TableScrollSupport.createTableScrollPane(tableAppointments,
                70, 150, 120, 120, 120, 80, 120, 280);

        card.add(title, BorderLayout.NORTH);
        card.add(scrollPane, BorderLayout.CENTER);
        return card;
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

    private void refreshTable() {
        if (currentPasien == null) {
            return;
        }

        String keyword = txtSearch.getText().trim();
        String status = String.valueOf(cbStatusFilter.getSelectedItem());
        List<Object[]> rows = pendaftaranDAO.searchByPasienId(currentPasien.getPasienId(), keyword, status);
        tableModel.setRowCount(0);
        for (Object[] row : rows) {
            tableModel.addRow(row);
        }
        clearSelectionOnly();
    }

    private void fillSelectionInfo() {
        int selectedRow = tableAppointments.getSelectedRow();
        if (selectedRow < 0) {
            return;
        }

        selectedPendaftaranId = Integer.parseInt(tableModel.getValueAt(selectedRow, 0).toString());
        txtSelectedInfo.setText(
                tableModel.getValueAt(selectedRow, 1).toString() + " | "
                        + tableModel.getValueAt(selectedRow, 2).toString() + " | "
                        + tableModel.getValueAt(selectedRow, 6).toString());
    }

    private void cancelAppointment() {
        if (selectedPendaftaranId <= 0 || currentPasien == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select your appointment first.",
                    "Validation",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Cancel this appointment?",
                "Confirm Cancel",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            boolean canceled = pendaftaranDAO.cancelByPasienId(selectedPendaftaranId, currentPasien.getPasienId());
            if (canceled) {
                JOptionPane.showMessageDialog(this,
                        "Appointment canceled successfully.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                refreshTable();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Appointment was not canceled.",
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

    private void clearSelectionOnly() {
        selectedPendaftaranId = -1;
        txtSelectedInfo.setText("");
        if (tableAppointments.getSelectionModel() != null) {
            tableAppointments.clearSelection();
        }
    }

    private static class JSplitPaneWrapper extends JPanel {
        JSplitPaneWrapper(JPanel left, JPanel right) {
            super(new BorderLayout());
            javax.swing.JSplitPane split = new javax.swing.JSplitPane(javax.swing.JSplitPane.HORIZONTAL_SPLIT, left, right);
            split.setDividerLocation(360);
            split.setResizeWeight(0.30);
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
            JFrame frame = new JFrame("CarePoint - My Appointment");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setContentPane(new MyAppointmentForm());
            frame.setMinimumSize(new Dimension(1280, 820));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}

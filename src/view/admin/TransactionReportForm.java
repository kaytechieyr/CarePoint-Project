package view.admin;

import config.TableScrollSupport;
import config.UITheme;
import dao.TransactionReportDAO;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
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
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class TransactionReportForm extends JPanel {

    private final TransactionReportDAO transactionReportDAO = new TransactionReportDAO();

    private JTextField txtKeyword;
    private JComboBox<String> cbPeriod;
    private JTable tableReport;
    private DefaultTableModel tableModel;

    public TransactionReportForm() {
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

        JLabel title = new JLabel("Transaction Report");
        title.setFont(UITheme.DISPLAY_FONT);
        title.setForeground(UITheme.PRIMARY_DARK);

        JLabel subtitle = new JLabel("Review appointments and medical records from the live database.");
        subtitle.setFont(UITheme.BODY_FONT);
        subtitle.setForeground(UITheme.MUTED);

        header.add(title);
        header.add(Box.createVerticalStrut(6));
        header.add(subtitle);
        return header;
    }

    private JPanel createContent() {
        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);

        JPanel filterPanel = createFilterPanel();
        JPanel tablePanel = createTablePanel();
        filterPanel.setMinimumSize(new Dimension(280, 520));
        tablePanel.setMinimumSize(new Dimension(520, 520));

        content.add(UITheme.createScrollPane(UITheme.createHorizontalSplitPane(
                filterPanel,
                tablePanel,
                320,
                0.26)), BorderLayout.CENTER);
        return content;
    }

    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(320, 0));
        panel.setMinimumSize(new Dimension(280, 520));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER),
                new EmptyBorder(18, 18, 18, 18)));

        JLabel title = new JLabel("Filter");
        title.setFont(UITheme.TITLE_FONT);
        title.setForeground(UITheme.PRIMARY_DARK);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 10, 0);

        txtKeyword = createField();
        cbPeriod = createCombo(new String[] { "Today", "This Week", "This Month", "Custom" });

        addField(form, gbc, "Keyword", txtKeyword);
        addField(form, gbc, "Period", cbPeriod);

        JPanel buttons = new JPanel();
        buttons.setOpaque(false);
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));

        JButton btnSearch = createPrimaryButton("Search");
        JButton btnRefresh = createSecondaryButton("Refresh");
        btnSearch.addActionListener(e -> refreshTable());
        btnRefresh.addActionListener(e -> {
            txtKeyword.setText("");
            cbPeriod.setSelectedIndex(0);
            refreshTable();
        });

        buttons.add(btnSearch);
        buttons.add(Box.createVerticalStrut(8));
        buttons.add(btnRefresh);

        panel.add(title, BorderLayout.NORTH);
        panel.add(form, BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setMinimumSize(new Dimension(520, 520));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER),
                new EmptyBorder(18, 18, 18, 18)));

        JLabel title = new JLabel("Report Data");
        title.setFont(UITheme.TITLE_FONT);
        title.setForeground(UITheme.PRIMARY_DARK);

        tableModel = new DefaultTableModel(
                new Object[] { "Date", "Patient", "Doctor", "Type", "Status", "Diagnosis" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableReport = new JTable(tableModel);
        tableReport.setRowHeight(28);
        tableReport.setFont(UITheme.BODY_FONT);
        tableReport.getTableHeader().setFont(UITheme.LABEL_FONT);

        JScrollPane scrollPane = TableScrollSupport.createTableScrollPane(tableReport, 120, 180, 180, 160, 120, 220);

        panel.add(title, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void refreshTable() {
        String keyword = txtKeyword != null ? txtKeyword.getText().trim() : null;
        String period = cbPeriod != null ? String.valueOf(cbPeriod.getSelectedItem()) : null;
        List<Object[]> rows = transactionReportDAO.findAll(keyword, period);
        tableModel.setRowCount(0);
        for (Object[] row : rows) {
            tableModel.addRow(row);
        }
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

    private JComboBox<String> createCombo(String[] values) {
        JComboBox<String> combo = new JComboBox<>(values);
        combo.setFont(UITheme.BODY_FONT);
        combo.setBackground(new Color(251, 253, 255));
        combo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER),
                new EmptyBorder(6, 8, 6, 8)));
        return combo;
    }

    private JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        styleButton(button, new Color(28, 94, 194), Color.WHITE);
        button.setForeground(Color.BLACK);
        return button;
    }

    private JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);
        styleButton(button, new Color(235, 243, 252), new Color(28, 94, 194));
        return button;
    }

    private void styleButton(JButton button, Color background, Color foreground) {
        button.setFont(UITheme.BUTTON_FONT);
        button.setForeground(foreground);
        button.setBackground(background);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("CarePoint - Transaction Report");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setContentPane(new TransactionReportForm());
            frame.setMinimumSize(new Dimension(1200, 760));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}

package view.pasien;

import config.UITheme;
import dao.DoctorDirectoryDAO;
import dao.PoliDAO;
import model.DoctorDirectoryItem;
import model.Poli;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

public class MyDoctorForm extends JPanel {

    private final DoctorDirectoryDAO doctorDirectoryDAO = new DoctorDirectoryDAO();
    private final PoliDAO poliDAO = new PoliDAO();

    private JTextField txtSearch;
    private JComboBox<Poli> cbPoli;
    private JPanel cardContainer;

    public MyDoctorForm() {
        setLayout(new BorderLayout());

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG);
        root.setBorder(new EmptyBorder(20, 20, 20, 20));
        root.add(createHeader(), BorderLayout.NORTH);
        root.add(createContent(), BorderLayout.CENTER);

        add(UITheme.createScrollPane(root), BorderLayout.CENTER);

        loadPoliFilter();
        refreshDirectory();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("My Doctor");
        title.setFont(new Font("SansSerif", Font.BOLD, 30));
        title.setForeground(new Color(18, 52, 86));

        JLabel subtitle = new JLabel("Browse doctors, poli, and practice schedules in real time.");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(new Color(87, 104, 127));

        header.add(title);
        header.add(Box.createVerticalStrut(6));
        header.add(subtitle);
        return header;
    }

    private JPanel createContent() {
        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setOpaque(false);

        JPanel filterCard = createFilterCard();
        JPanel directoryCard = createDirectoryCard();

        content.add(filterCard, BorderLayout.NORTH);
        content.add(directoryCard, BorderLayout.CENTER);
        return content;
    }

    private JPanel createFilterCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 236, 246)),
                new EmptyBorder(18, 18, 18, 18)));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 12, 0);

        txtSearch = UITheme.createTextField();
        txtSearch.setToolTipText("Search doctor name");
        cbPoli = new JComboBox<>();
        UITheme.styleComboBox(cbPoli);

        addField(form, gbc, "Search Doctor", txtSearch);
        addField(form, gbc, "Filter Poli", cbPoli);

        JPanel actions = new JPanel();
        actions.setOpaque(false);
        actions.setLayout(new BoxLayout(actions, BoxLayout.X_AXIS));

        JButton btnSearch = UITheme.createPrimaryButton("Search");
        JButton btnRefresh = UITheme.createSecondaryButton("Refresh");
        btnSearch.addActionListener(e -> refreshDirectory());
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            if (cbPoli.getItemCount() > 0) {
                cbPoli.setSelectedIndex(0);
            }
            refreshDirectory();
        });

        actions.add(btnSearch);
        actions.add(Box.createHorizontalStrut(10));
        actions.add(btnRefresh);

        JLabel title = new JLabel("Doctor Finder");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(new Color(18, 52, 86));

        card.add(title, BorderLayout.NORTH);
        card.add(form, BorderLayout.CENTER);
        card.add(actions, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createDirectoryCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 236, 246)),
                new EmptyBorder(18, 18, 18, 18)));

        JLabel title = new JLabel("Available Doctors");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(new Color(18, 52, 86));

        cardContainer = new JPanel();
        cardContainer.setLayout(new BoxLayout(cardContainer, BoxLayout.Y_AXIS));
        cardContainer.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(cardContainer);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 236, 246)));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(UITheme.BG);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

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

    private void loadPoliFilter() {
        cbPoli.removeAllItems();
        cbPoli.addItem(new Poli(0, "All Poli"));
        List<Poli> poliList = poliDAO.findAll();
        for (Poli poli : poliList) {
            cbPoli.addItem(poli);
        }
        if (cbPoli.getItemCount() > 0) {
            cbPoli.setSelectedIndex(0);
        }
    }

    private void refreshDirectory() {
        Integer poliId = null;
        Poli selectedPoli = (Poli) cbPoli.getSelectedItem();
        if (selectedPoli != null && selectedPoli.getPoliId() > 0) {
            poliId = selectedPoli.getPoliId();
        }

        String keyword = txtSearch.getText().trim();
        List<DoctorDirectoryItem> items = doctorDirectoryDAO.findDirectory(keyword, poliId);
        cardContainer.removeAll();
        for (DoctorDirectoryItem item : items) {
            cardContainer.add(createDoctorCard(item));
            cardContainer.add(Box.createVerticalStrut(14));
        }

        if (items.isEmpty()) {
            JLabel empty = new JLabel("No doctor data found.");
            empty.setFont(new Font("SansSerif", Font.PLAIN, 14));
            empty.setForeground(new Color(87, 104, 127));
            cardContainer.add(empty);
        }

        cardContainer.revalidate();
        cardContainer.repaint();
    }

    private JPanel createDoctorCard(DoctorDirectoryItem item) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setPreferredSize(new Dimension(100, 220));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 236, 246)),
                new EmptyBorder(16, 16, 16, 16)));

        JLabel name = new JLabel(item.getNamaDokter());
        name.setFont(new Font("SansSerif", Font.BOLD, 18));
        name.setForeground(new Color(18, 52, 86));

        JLabel poli = new JLabel(item.getNamaPoli());
        poli.setFont(new Font("SansSerif", Font.PLAIN, 14));
        poli.setForeground(new Color(87, 104, 127));

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.add(name);
        info.add(Box.createVerticalStrut(4));
        info.add(poli);

        JPanel schedulePanel = new JPanel();
        schedulePanel.setOpaque(false);
        schedulePanel.setLayout(new BoxLayout(schedulePanel, BoxLayout.Y_AXIS));

        if (item.getSchedules().isEmpty()) {
            JLabel schedule = new JLabel("No schedule available");
            schedule.setFont(new Font("SansSerif", Font.PLAIN, 13));
            schedule.setForeground(new Color(127, 141, 161));
            schedulePanel.add(schedule);
        } else {
            for (String scheduleText : item.getSchedules()) {
                JLabel schedule = new JLabel("<html><div style='width: 270px;'>" + scheduleText + "</div></html>");
                schedule.setFont(new Font("SansSerif", Font.PLAIN, 13));
                schedule.setForeground(new Color(52, 71, 103));
                schedulePanel.add(schedule);
                schedulePanel.add(Box.createVerticalStrut(4));
            }
        }

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        JButton btnRefreshCard = new JButton("Open");
        btnRefreshCard.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnRefreshCard.setForeground(new Color(18, 52, 86));
        btnRefreshCard.setBackground(new Color(235, 243, 252));
        btnRefreshCard.setFocusPainted(false);
        btnRefreshCard.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRefreshCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(214, 228, 242)),
                new EmptyBorder(8, 14, 8, 14)));
        footer.add(btnRefreshCard, BorderLayout.EAST);

        card.setAlignmentX(LEFT_ALIGNMENT);
        card.add(info, BorderLayout.NORTH);
        card.add(schedulePanel, BorderLayout.CENTER);
        card.add(footer, BorderLayout.SOUTH);
        return card;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("CarePoint - My Doctor");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setContentPane(new MyDoctorForm());
            frame.setMinimumSize(new Dimension(1280, 820));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}

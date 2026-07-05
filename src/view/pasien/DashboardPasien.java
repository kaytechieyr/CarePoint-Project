package view.pasien;

import config.AppSession;
import config.UITheme;
import dao.DashboardStatsDAO;
import dao.PasienDAO;
import model.Pasien;
import view.auth.LoginForm;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.CardLayout;
import java.awt.GridBagLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

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
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import view.common.SidebarIcons;
import view.common.SidebarPanel;

public class DashboardPasien extends JFrame {

    private final DashboardStatsDAO dashboardStatsDAO = new DashboardStatsDAO();
    private final PasienDAO pasienDAO = new PasienDAO();

    private final CardLayout cardLayout;
    private final JPanel contentPanel;
    private final JLabel lblUpcoming = createMetricLabel();
    private final JLabel lblCompleted = createMetricLabel();
    private final JLabel lblDoctors = createMetricLabel();
    private final JLabel lblReports = createMetricLabel();

    private Pasien currentPasien;

    public DashboardPasien() {
        setTitle("CarePoint - Patient Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1280, 780));
        setResizable(true);
        setLayout(new BorderLayout());

        loadPatientContext();

        SidebarPanel sidebar = createSidebar();
        contentPanel = new JPanel();
        cardLayout = new CardLayout();
        contentPanel.setLayout(cardLayout);
        contentPanel.setBackground(UITheme.BG);
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        contentPanel.setMinimumSize(new Dimension(720, 520));
        contentPanel.add(createDashboardPanel(), "DASHBOARD");
        contentPanel.add(new MyDoctorForm(), "DOCTOR");
        contentPanel.add(new BookAppointmentForm(), "BOOK");
        contentPanel.add(new MyAppointmentForm(), "APPOINTMENT");
        contentPanel.add(new MedicalReportsForm(), "REPORTS");

        JSplitPane splitPane = UITheme.createHorizontalSplitPane(
                sidebar,
                contentPanel,
                280,
                0.0);
        sidebar.attachToSplitPane(splitPane);
        add(splitPane, BorderLayout.CENTER);

        refreshDashboardStats();

        pack();
        setLocationRelativeTo(null);
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
        }
    }

    private SidebarPanel createSidebar() {
        return new SidebarPanel(
                "Patient Panel",
                currentPasien != null ? currentPasien.getNamaLengkap() : "Guest",
                Arrays.asList(
                        new SidebarPanel.MenuItem("Dashboard", SidebarIcons.dashboard(new Color(18, 52, 86)),
                                () -> showCard("DASHBOARD")),
                        new SidebarPanel.MenuItem("My Doctor", SidebarIcons.userMedical(new Color(18, 52, 86)),
                                () -> showCard("DOCTOR")),
                        new SidebarPanel.MenuItem("Book Appointment", SidebarIcons.calendarPlus(new Color(18, 52, 86)),
                                () -> showCard("BOOK")),
                        new SidebarPanel.MenuItem("My Appointment", SidebarIcons.calendarCheck(new Color(18, 52, 86)),
                                () -> showCard("APPOINTMENT")),
                        new SidebarPanel.MenuItem("Medical Reports", SidebarIcons.document(new Color(18, 52, 86)),
                                () -> showCard("REPORTS"))
                ),
                () -> {
                    int confirm = JOptionPane.showConfirmDialog(
                            DashboardPasien.this,
                            "Are you sure you want to logout?",
                            "Logout",
                            JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        new LoginForm().setVisible(true);
                        dispose();
                    }
                });
    }

    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        button.setPreferredSize(new Dimension(220, 46));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setForeground(new Color(18, 52, 86));
        button.setBackground(new Color(235, 243, 252));
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(214, 228, 242)),
                new EmptyBorder(10, 14, 10, 14)));
        return button;
    }

    private JLabel createMetricLabel() {
        JLabel label = new JLabel("0");
        label.setFont(new Font("SansSerif", Font.BOLD, 34));
        label.setForeground(new Color(37, 99, 171));
        return label;
    }

    private JPanel createDashboardPanel() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 16));
        wrapper.setOpaque(false);
        wrapper.setMinimumSize(new Dimension(720, 520));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Dashboard");
        title.setFont(new Font("SansSerif", Font.BOLD, 30));
        title.setForeground(new Color(18, 52, 86));

        JLabel subtitle = new JLabel("Welcome back! Here is a quick summary of your active medical registrations.");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(new Color(87, 104, 127));

        header.add(title);
        header.add(Box.createVerticalStrut(6));
        header.add(subtitle);

        JPanel cards = new JPanel(new GridBagLayout());
        cards.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 16, 16);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        cards.add(createStatCard("Upcoming Appointment", lblUpcoming, "Scheduled appointment"), position(gbc, 0, 0));
        cards.add(createStatCard("Completed Visits", lblCompleted, "Past clinic visits"), position(gbc, 1, 0));
        cards.add(createStatCard("Doctors", lblDoctors, "Available speciality doctors"), position(gbc, 0, 1));
        cards.add(createStatCard("Reports", lblReports, "Medical reports ready to review"), position(gbc, 1, 1));

        JPanel quickAction = new JPanel(new BorderLayout());
        quickAction.setOpaque(false);
        JButton btnBookAppointment = UITheme.createPrimaryButton("Book Appointment");
        btnBookAppointment.addActionListener(e -> showCard("BOOK"));
        quickAction.add(btnBookAppointment, BorderLayout.WEST);

        JScrollPane scrollPane = new JScrollPane(cards);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getViewport().setBackground(UITheme.BG);

        wrapper.add(header, BorderLayout.NORTH);
        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setOpaque(false);
        body.add(scrollPane, BorderLayout.CENTER);
        body.add(quickAction, BorderLayout.SOUTH);
        wrapper.add(body, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createStatCard(String label, JLabel valueLabel, String description) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(290, 180));
        card.setMaximumSize(new Dimension(290, 180));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 236, 246)),
                new EmptyBorder(18, 18, 18, 18)));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        lbl.setForeground(new Color(95, 112, 134));

        JLabel desc = new JLabel("<html><div style='width: 230px;'>" + description + "</div></html>");
        desc.setFont(new Font("SansSerif", Font.PLAIN, 13));
        desc.setForeground(new Color(87, 104, 127));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(lbl);
        top.add(Box.createVerticalStrut(10));
        top.add(valueLabel);
        top.add(Box.createVerticalStrut(6));
        top.add(desc);

        card.add(top, BorderLayout.NORTH);
        return card;
    }

    private void refreshDashboardStats() {
        if (currentPasien == null) {
            lblUpcoming.setText("0");
            lblCompleted.setText("0");
            lblDoctors.setText("0");
            lblReports.setText("0");
            return;
        }

        int pasienId = currentPasien.getPasienId();
        lblUpcoming.setText(String.valueOf(dashboardStatsDAO.countUpcomingAppointmentsForPatient(pasienId)));
        lblCompleted.setText(String.valueOf(dashboardStatsDAO.countCompletedVisitsForPatient(pasienId)));
        lblDoctors.setText(String.valueOf(dashboardStatsDAO.countAvailableDoctors()));
        lblReports.setText(String.valueOf(dashboardStatsDAO.countReportsForPatient(pasienId)));
    }

    private void showCard(String cardName) {
        if ("DASHBOARD".equals(cardName)) {
            refreshDashboardStats();
        }
        cardLayout.show(contentPanel, cardName);
    }

    private GridBagConstraints position(GridBagConstraints base, int x, int y) {
        GridBagConstraints gbc = (GridBagConstraints) base.clone();
        gbc.gridx = x;
        gbc.gridy = y;
        return gbc;
    }

    private JPanel createPlaceholderPanel(String titleText) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JLabel title = new JLabel(titleText);
        title.setFont(new Font("SansSerif", Font.BOLD, 30));
        title.setForeground(new Color(18, 52, 86));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(title);
        header.add(Box.createVerticalStrut(8));

        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(Color.WHITE);
        body.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 236, 246)),
                new EmptyBorder(24, 24, 24, 24)));

        panel.add(header, BorderLayout.NORTH);
        panel.add(body, BorderLayout.CENTER);
        return panel;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new DashboardPasien().setVisible(true);
            }
        });
    }
}

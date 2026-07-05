package view.auth;

import config.UITheme;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import config.AppSession;
import dao.PasienDAO;
import dao.UserDAO;
import model.Pasien;
import model.User;
import view.admin.DashboardAdmin;
import view.pasien.DashboardPasien;

public class LoginForm extends JFrame {

    private final UserDAO userDAO = new UserDAO();
    private final PasienDAO pasienDAO = new PasienDAO();
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnGoRegister;

    public LoginForm() {
        setTitle("CarePoint - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(1100, 680));
        setLayout(new BorderLayout());
        setResizable(true);

        JPanel leftPanel = createBrandPanel();
        JPanel rightPanel = createLoginPanel();

        JSplitPane splitPane = UITheme.createHorizontalSplitPane(
                leftPanel,
                rightPanel,
                620,
                0.58);

        add(UITheme.createScrollPane(splitPane), BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }

    private JPanel createBrandPanel() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                UITheme.paintSoftGeometricBackground((Graphics2D) g, getWidth(), getHeight());
            }
        };
        panel.setBackground(Color.WHITE);
        panel.setMinimumSize(new java.awt.Dimension(420, 520));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER),
                new EmptyBorder(32, 32, 32, 32)));

        JPanel topBlock = new JPanel();
        topBlock.setOpaque(false);
        topBlock.setLayout(new BoxLayout(topBlock, BoxLayout.Y_AXIS));

        JLabel lblBadge = new JLabel("CarePoint");
        lblBadge.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        lblBadge.setFont(new Font("SansSerif", Font.BOLD, 30));
        lblBadge.setForeground(UITheme.PRIMARY_DARK);

        JLabel lblTitle = new JLabel("<html><div style='width: 420px;'>"
                + "Your digital gateway to seamless healthcare services."
                + "</div></html>");
        lblTitle.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        lblTitle.setFont(new Font("SansSerif", Font.PLAIN, 18));
        lblTitle.setForeground(UITheme.MUTED);

        topBlock.add(lblBadge);
        topBlock.add(Box.createVerticalStrut(12));
        topBlock.add(lblTitle);
        topBlock.add(Box.createVerticalStrut(22));

        JPanel illustration = createLandingHeroPanel();

        panel.add(topBlock, BorderLayout.NORTH);
        panel.add(illustration, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.SURFACE);
        panel.setMinimumSize(new java.awt.Dimension(420, 520));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER),
                new EmptyBorder(36, 36, 36, 36)));

        JLabel title = new JLabel("Login");
        UITheme.applyTitle(title);

        JLabel subtitle = new JLabel("Enter your username and password to continue.");
        subtitle.setFont(UITheme.BODY_FONT);
        subtitle.setForeground(UITheme.MUTED);

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(title);
        header.add(Box.createVerticalStrut(12));
        header.add(subtitle);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 14, 0);

        JLabel lblUsername = createFieldLabel("Username");
        txtUsername = createField();
        JLabel lblPassword = createFieldLabel("Password");
        txtPassword = createPasswordField();

        formPanel.add(lblUsername, gbc);
        gbc.gridy++;
        formPanel.add(txtUsername, gbc);
        gbc.gridy++;
        formPanel.add(lblPassword, gbc);
        gbc.gridy++;
        formPanel.add(txtPassword, gbc);

        btnLogin = createPrimaryButton("Login");
        btnGoRegister = createSecondaryButton("Create Patient Account");

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.add(Box.createVerticalStrut(4));
        buttonPanel.add(btnLogin);
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(btnGoRegister);

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.add(header, BorderLayout.NORTH);
        content.add(formPanel, BorderLayout.CENTER);
        content.add(buttonPanel, BorderLayout.SOUTH);

        JPanel inner = new JPanel(new BorderLayout());
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(12, 6, 12, 6));
        inner.add(content, BorderLayout.NORTH);

        panel.add(inner, BorderLayout.CENTER);

        wireActions();
        return panel;
    }

    private void wireActions() {
        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = txtUsername.getText().trim();
                String password = new String(txtPassword.getPassword()).trim();

                if (username.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(LoginForm.this,
                            "Please fill username and password.",
                            "Validation",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                try {
                    User user = userDAO.login(username, password);
                    if (user == null) {
                        JOptionPane.showMessageDialog(LoginForm.this,
                                "Login failed. Username or password is incorrect.",
                                "Login Failed",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    AppSession.clear();
                    AppSession.setCurrentUser(user);

                    if ("admin".equalsIgnoreCase(user.getRole())) {
                        JOptionPane.showMessageDialog(LoginForm.this,
                                "Login successful as admin.",
                                "Login Success",
                                JOptionPane.INFORMATION_MESSAGE);
                        DashboardAdmin dashboardAdmin = new DashboardAdmin();
                        dashboardAdmin.setVisible(true);
                    } else {
                        Pasien pasien = pasienDAO.findByUserId(user.getUserId());
                        if (pasien == null) {
                            AppSession.clear();
                            JOptionPane.showMessageDialog(LoginForm.this,
                                    "Patient profile was not found for this account.",
                                    "Login Failed",
                                    JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        AppSession.setCurrentPasien(pasien);
                        JOptionPane.showMessageDialog(LoginForm.this,
                                "Login successful as patient.",
                                "Login Success",
                                JOptionPane.INFORMATION_MESSAGE);
                        DashboardPasien dashboardPasien = new DashboardPasien();
                        dashboardPasien.setVisible(true);
                    }

                    dispose();
                } catch (RuntimeException ex) {
                    JOptionPane.showMessageDialog(LoginForm.this,
                            "Terjadi kesalahan saat login: " + ex.getMessage(),
                            "Login Failed",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnGoRegister.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RegisterForm registerForm = new RegisterForm();
                registerForm.setVisible(true);
                dispose();
            }
        });
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        UITheme.applyLabel(label);
        return label;
    }

    private JTextField createField() {
        return UITheme.createTextField();
    }

    private JPasswordField createPasswordField() {
        return UITheme.createPasswordField();
    }

    private JButton createPrimaryButton(String text) {
        JButton button = UITheme.createPrimaryButton(text);
        button.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        button.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 46));
        return button;
    }

    private JButton createSecondaryButton(String text) {
        JButton button = UITheme.createSecondaryButton(text);
        button.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        button.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 46));
        return button;
    }

    private JPanel createLandingHeroPanel() {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 220));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 34, 34);

                int w = getWidth();
                int h = getHeight();
                g2.setColor(new Color(224, 236, 247));
                g2.fill(new Ellipse2D.Double(w * 0.08, h * 0.14, 44, 44));
                g2.fill(new Ellipse2D.Double(w * 0.80, h * 0.16, 60, 60));
                g2.fill(new Ellipse2D.Double(w * 0.18, h * 0.74, 72, 72));

                g2.setColor(new Color(233, 243, 252));
                g2.fillRoundRect((int) (w * 0.12), (int) (h * 0.38), (int) (w * 0.76), (int) (h * 0.28), 30, 30);

                g2.setColor(UITheme.PRIMARY_DARK);
                g2.fill(new Ellipse2D.Double(w * 0.40, h * 0.14, 84, 84));
                g2.setColor(UITheme.PRIMARY);
                g2.fillRoundRect((int) (w * 0.34), (int) (h * 0.32), 150, 162, 28, 28);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect((int) (w * 0.42), (int) (h * 0.40), 42, 16, 10, 10);
                g2.fillRoundRect((int) (w * 0.45), (int) (h * 0.36), 12, 46, 10, 10);
                g2.setColor(new Color(81, 142, 219));
                g2.fill(new RoundRectangle2D.Double(w * 0.27, h * 0.68, 210, 34, 18, 18));
                g2.dispose();
            }
        };
        card.setPreferredSize(new java.awt.Dimension(360, 280));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER),
                new EmptyBorder(18, 18, 18, 18)));

        JLabel hint = new JLabel("<html><div style='text-align:center;'>"
                + "CarePoint"
                + "</div></html>", SwingConstants.CENTER);
        hint.setFont(new Font("SansSerif", Font.BOLD, 28));
        hint.setForeground(UITheme.PRIMARY_DARK);
        card.add(hint, BorderLayout.SOUTH);
        return card;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LoginForm().setVisible(true);
            }
        });
    }
}

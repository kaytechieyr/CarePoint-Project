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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
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

import dao.PasienDAO;
import model.Pasien;
import model.User;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class RegisterForm extends JFrame {

    private final PasienDAO pasienDAO = new PasienDAO();
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JPasswordField txtConfirmPassword;
    private JTextField txtNamaLengkap;
    private JTextField txtNIK;
    private JTextField txtNoTelepon;
    private JTextField txtAlamat;
    private JTextField txtTanggalLahir;
    private JComboBox<String> cbJenisKelamin;
    private JButton btnRegister;
    private JButton btnBackToLogin;

    public RegisterForm() {
        setTitle("CarePoint - Register Patient");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(1180, 760));
        setLayout(new BorderLayout());
        setResizable(true);

        JPanel leftPanel = createBrandPanel();
        JPanel rightPanel = createFormPanel();

        JSplitPane splitPane = UITheme.createHorizontalSplitPane(
                leftPanel,
                rightPanel,
                360,
                0.34);

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
        panel.setMinimumSize(new java.awt.Dimension(320, 520));
        panel.setPreferredSize(new java.awt.Dimension(360, 0));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER),
                new EmptyBorder(36, 36, 36, 36)));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Patient Registration");
        title.setFont(new Font("SansSerif", Font.BOLD, 30));
        title.setForeground(UITheme.PRIMARY_DARK);
        title.setAlignmentX(JComponent.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("<html><div style='width: 280px;'>"
                + "Desktop-based patient registration and medical reporting information system."
                + "</div></html>");
        subtitle.setFont(UITheme.BODY_FONT);
        subtitle.setForeground(UITheme.MUTED);
        subtitle.setAlignmentX(JComponent.LEFT_ALIGNMENT);

        header.add(title);
        header.add(Box.createVerticalStrut(12));
        header.add(subtitle);
        JPanel center = createLandingHeroPanel();

        panel.add(header, BorderLayout.NORTH);
        panel.add(center, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.SURFACE);
        panel.setMinimumSize(new java.awt.Dimension(520, 520));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER),
                new EmptyBorder(36, 36, 36, 36)));

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);

        JLabel title = new JLabel("Create Account");
        UITheme.applyTitle(title);

        JLabel subtitle = new JLabel("Fill in the patient profile and account details.");
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

        txtNamaLengkap = createField();
        txtNIK = createField();
        txtNoTelepon = createField();
        txtAlamat = createField();
        txtTanggalLahir = createField();
        txtTanggalLahir.setToolTipText("yyyy-MM-dd");
        txtUsername = createField();
        txtPassword = createPasswordField();
        txtConfirmPassword = createPasswordField();
        cbJenisKelamin = new JComboBox<>(new String[] { "L", "P" });
        styleComboBox(cbJenisKelamin);

        addField(formPanel, gbc, "Full Name", txtNamaLengkap);
        addField(formPanel, gbc, "NIK", txtNIK);
        addField(formPanel, gbc, "Phone Number", txtNoTelepon);
        addField(formPanel, gbc, "Address", txtAlamat);
        addField(formPanel, gbc, "Gender", cbJenisKelamin);
        addField(formPanel, gbc, "Date of Birth (yyyy-MM-dd)", txtTanggalLahir);
        addField(formPanel, gbc, "Username", txtUsername);
        addField(formPanel, gbc, "Password", txtPassword);
        addField(formPanel, gbc, "Confirm Password", txtConfirmPassword);

        btnRegister = createPrimaryButton("Register");
        btnBackToLogin = createSecondaryButton("Back to Login");

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.add(Box.createVerticalStrut(4));
        buttonPanel.add(btnRegister);
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(btnBackToLogin);

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

    private void addField(JPanel panel, GridBagConstraints gbc, String labelText, java.awt.Component field) {
        JLabel label = new JLabel(labelText);
        UITheme.applyLabel(label);

        panel.add(label, gbc);
        gbc.gridy++;
        panel.add(field, gbc);
        gbc.gridy++;
    }

    private void wireActions() {
        btnRegister.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String namaLengkap = txtNamaLengkap.getText().trim();
                String nik = txtNIK.getText().trim();
                String noTelepon = txtNoTelepon.getText().trim();
                String alamat = txtAlamat.getText().trim();
                String tanggalLahirText = txtTanggalLahir.getText().trim();
                String username = txtUsername.getText().trim();
                String password = new String(txtPassword.getPassword()).trim();
                String confirmPassword = new String(txtConfirmPassword.getPassword()).trim();
                String jenisKelamin = String.valueOf(cbJenisKelamin.getSelectedItem());

                if (namaLengkap.isEmpty() || nik.isEmpty() || noTelepon.isEmpty()
                        || alamat.isEmpty() || tanggalLahirText.isEmpty() || username.isEmpty() || password.isEmpty()
                        || confirmPassword.isEmpty()) {
                    JOptionPane.showMessageDialog(RegisterForm.this,
                            "Please complete all fields.",
                            "Validation",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (!password.equals(confirmPassword)) {
                    JOptionPane.showMessageDialog(RegisterForm.this,
                            "Password and confirmation do not match.",
                            "Validation",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                Date tanggalLahir;
                try {
                    tanggalLahir = Date.valueOf(LocalDate.parse(tanggalLahirText));
                } catch (DateTimeParseException ex) {
                    JOptionPane.showMessageDialog(RegisterForm.this,
                            "Date of Birth must use format yyyy-MM-dd.",
                            "Validation",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                User user = new User();
                user.setUsername(username);
                user.setPassword(password);
                user.setRole("pasien");

                Pasien pasien = new Pasien();
                pasien.setNik(nik);
                pasien.setNamaLengkap(namaLengkap);
                pasien.setJenisKelamin(jenisKelamin);
                pasien.setTglLahir(tanggalLahir);
                pasien.setAlamat(alamat);
                pasien.setNoTelepon(noTelepon);

                try {
                    boolean success = pasienDAO.registerPasien(user, pasien);
                    if (success) {
                        JOptionPane.showMessageDialog(RegisterForm.this,
                                "Registration successful. Data has been saved.",
                                "Registration Success",
                                JOptionPane.INFORMATION_MESSAGE);

                        LoginForm loginForm = new LoginForm();
                        loginForm.setVisible(true);
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(RegisterForm.this,
                                "Username is already taken. Please use another username.",
                                "Registration Failed",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (RuntimeException ex) {
                    JOptionPane.showMessageDialog(RegisterForm.this,
                            "An error occurred while registering: " + ex.getMessage(),
                            "Registration Failed",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnBackToLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                LoginForm loginForm = new LoginForm();
                loginForm.setVisible(true);
                dispose();
            }
        });
    }

    private JTextField createField() {
        return UITheme.createTextField();
    }

    private JPasswordField createPasswordField() {
        return UITheme.createPasswordField();
    }

    private void styleComboBox(JComboBox<String> comboBox) {
        UITheme.styleComboBox(comboBox);
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
                g2.fill(new java.awt.geom.Ellipse2D.Double(w * 0.08, h * 0.18, 44, 44));
                g2.fill(new java.awt.geom.Ellipse2D.Double(w * 0.80, h * 0.14, 60, 60));
                g2.fill(new java.awt.geom.Ellipse2D.Double(w * 0.16, h * 0.72, 74, 74));

                g2.setColor(new Color(233, 243, 252));
                g2.fillRoundRect((int) (w * 0.12), (int) (h * 0.38), (int) (w * 0.74), (int) (h * 0.28), 30, 30);

                g2.setColor(UITheme.PRIMARY_DARK);
                g2.fill(new java.awt.geom.Ellipse2D.Double(w * 0.40, h * 0.14, 84, 84));
                g2.setColor(UITheme.PRIMARY);
                g2.fillRoundRect((int) (w * 0.34), (int) (h * 0.32), 150, 162, 28, 28);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect((int) (w * 0.42), (int) (h * 0.40), 42, 16, 10, 10);
                g2.fillRoundRect((int) (w * 0.45), (int) (h * 0.36), 12, 46, 10, 10);
                g2.setColor(new Color(81, 142, 219));
                g2.fillRoundRect((int) (w * 0.27), (int) (h * 0.68), 210, 34, 18, 18);
                g2.dispose();
            }
        };
        card.setPreferredSize(new java.awt.Dimension(360, 280));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER),
                new EmptyBorder(18, 18, 18, 18)));

        JLabel hint = new JLabel("CarePoint", SwingConstants.CENTER);
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
                new RegisterForm().setVisible(true);
            }
        });
    }
}

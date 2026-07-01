package view.common;

import config.UITheme;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

public class SidebarPanel extends JPanel {

    public static final int EXPANDED_WIDTH = 280;
    public static final int COLLAPSED_WIDTH = 84;
    private static final Color SIDEBAR_BG = new Color(18, 52, 86);
    private static final Color MENU_TEXT = new Color(18, 52, 86);
    private static final Color MENU_BG = new Color(235, 243, 252);
    private static final Color MENU_BORDER = new Color(214, 228, 242);
    private static final Color LOGOUT_BG = Color.WHITE;
    private static final Color LOGOUT_BORDER = new Color(205, 220, 235);
    private static final Color ICON_COLOR = new Color(194, 215, 242);
    private static final Color BUTTON_ICON_COLOR = UITheme.PRIMARY_DARK;

    private final JPanel headerDetails;
    private final JLabel brandLabel;
    private final JLabel roleLabel;
    private final JLabel userLabel;
    private final JButton toggleButton;
    private final JPanel menuPanel;
    private final JPanel footerPanel;
    private final List<SidebarMenuButton> menuButtons = new ArrayList<>();
    private final SidebarMenuButton logoutButton;

    private JSplitPane splitPane;
    private Timer animationTimer;
    private boolean collapsed;

    public static final class MenuItem {
        private final String label;
        private final Icon icon;
        private final Runnable action;

        public MenuItem(String label, Icon icon, Runnable action) {
            this.label = label;
            this.icon = icon;
            this.action = action;
        }
    }

    public SidebarPanel(String roleText, String userText, List<MenuItem> items, Runnable logoutAction) {
        setBackground(SIDEBAR_BG);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(24, 20, 24, 20));
        setPreferredSize(new Dimension(EXPANDED_WIDTH, 0));
        setMinimumSize(new Dimension(COLLAPSED_WIDTH, 0));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JPanel toggleRow = new JPanel(new BorderLayout());
        toggleRow.setOpaque(false);
        toggleButton = createToggleButton();
        toggleRow.add(toggleButton, BorderLayout.WEST);
        header.add(toggleRow);
        header.add(Box.createVerticalStrut(16));

        headerDetails = new JPanel();
        headerDetails.setOpaque(false);
        headerDetails.setLayout(new BoxLayout(headerDetails, BoxLayout.Y_AXIS));
        headerDetails.setAlignmentX(Component.LEFT_ALIGNMENT);

        brandLabel = new JLabel("CarePoint");
        brandLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        brandLabel.setForeground(Color.WHITE);
        brandLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        roleLabel = new JLabel(roleText);
        roleLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        roleLabel.setForeground(new Color(194, 215, 242));
        roleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        headerDetails.add(brandLabel);
        headerDetails.add(Box.createVerticalStrut(6));
        headerDetails.add(roleLabel);

        if (userText != null && !userText.isBlank()) {
            headerDetails.add(Box.createVerticalStrut(12));
            userLabel = new JLabel(userText);
            userLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
            userLabel.setForeground(Color.WHITE);
            userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            headerDetails.add(userLabel);
        } else {
            userLabel = null;
        }

        header.add(headerDetails);

        menuPanel = new JPanel();
        menuPanel.setOpaque(false);
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));

        for (int i = 0; i < items.size(); i++) {
            MenuItem item = items.get(i);
            SidebarMenuButton button = new SidebarMenuButton(item.label, item.icon, false);
            if (item.action != null) {
                button.addActionListener(e -> item.action.run());
            }
            menuButtons.add(button);
            menuPanel.add(button);
            if (i < items.size() - 1) {
                menuPanel.add(Box.createVerticalStrut(10));
            }
        }

        logoutButton = new SidebarMenuButton("Logout", SidebarIcons.signOut(BUTTON_ICON_COLOR), true);
        if (logoutAction != null) {
            logoutButton.addActionListener(e -> logoutAction.run());
        }

        footerPanel = new JPanel(new BorderLayout());
        footerPanel.setOpaque(false);
        footerPanel.add(logoutButton, BorderLayout.SOUTH);

        add(header, BorderLayout.NORTH);
        add(menuPanel, BorderLayout.CENTER);
        add(footerPanel, BorderLayout.SOUTH);

        toggleButton.addActionListener(e -> toggleCollapsed());
        applyCollapsedState(false, false);
    }

    public void attachToSplitPane(JSplitPane splitPane) {
        this.splitPane = splitPane;
        applySidebarWidth(collapsed ? COLLAPSED_WIDTH : EXPANDED_WIDTH);
        SwingUtilities.invokeLater(() -> applySidebarWidth(collapsed ? COLLAPSED_WIDTH : EXPANDED_WIDTH));
    }

    private JButton createToggleButton() {
        JButton button = new JButton();
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setOpaque(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(28, 28));
        button.setMaximumSize(new Dimension(28, 28));
        button.setMinimumSize(new Dimension(28, 28));
        button.setIcon(SidebarIcons.chevronLeft(ICON_COLOR));
        button.setToolTipText("Collapse sidebar");
        return button;
    }

    private void toggleCollapsed() {
        applyCollapsedState(!collapsed, true);
    }

    private void applyCollapsedState(boolean nextCollapsed, boolean animate) {
        boolean changed = collapsed != nextCollapsed;
        collapsed = nextCollapsed;

        setBorder(new EmptyBorder(24, collapsed ? 10 : 20, 24, collapsed ? 10 : 20));
        headerDetails.setVisible(!collapsed);
        menuPanel.setBorder(new EmptyBorder(collapsed ? 12 : 24, 0, collapsed ? 12 : 24, 0));
        footerPanel.setBorder(new EmptyBorder(collapsed ? 12 : 0, 0, 0, 0));

        toggleButton.setIcon(collapsed ? SidebarIcons.chevronRight(ICON_COLOR) : SidebarIcons.chevronLeft(ICON_COLOR));
        toggleButton.setToolTipText(collapsed ? "Expand sidebar" : "Collapse sidebar");

        for (SidebarMenuButton button : menuButtons) {
            button.setCollapsedMode(collapsed);
        }
        logoutButton.setCollapsedMode(collapsed);

        revalidate();
        repaint();

        if (animate && changed) {
            animateWidth(collapsed ? COLLAPSED_WIDTH : EXPANDED_WIDTH);
        } else {
            applySidebarWidth(collapsed ? COLLAPSED_WIDTH : EXPANDED_WIDTH);
        }
    }

    private void animateWidth(int targetWidth) {
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }

        int startWidth = splitPane != null ? splitPane.getDividerLocation() : (collapsed ? EXPANDED_WIDTH : COLLAPSED_WIDTH);
        if (startWidth <= 0) {
            startWidth = collapsed ? EXPANDED_WIDTH : COLLAPSED_WIDTH;
        }

        final int from = startWidth;
        final int to = targetWidth;
        final long startedAt = System.currentTimeMillis();
        final int durationMs = 180;

        animationTimer = new Timer(15, e -> {
            float progress = Math.min(1f, (System.currentTimeMillis() - startedAt) / (float) durationMs);
            float eased = ease(progress);
            int width = Math.round(from + (to - from) * eased);
            applySidebarWidth(width);
            if (progress >= 1f) {
                animationTimer.stop();
                applySidebarWidth(to);
            }
        });
        animationTimer.start();
    }

    private float ease(float t) {
        return t < 0.5f ? 2f * t * t : -1f + (4f - 2f * t) * t;
    }

    private void applySidebarWidth(int width) {
        setPreferredSize(new Dimension(width, 0));
        setMinimumSize(new Dimension(width, 0));
        setMaximumSize(new Dimension(width, Integer.MAX_VALUE));
        if (splitPane != null) {
            splitPane.setDividerLocation(width);
            splitPane.revalidate();
            splitPane.repaint();
        }
        revalidate();
        repaint();
    }

    private final class SidebarMenuButton extends JButton {

        private final String label;
        private final boolean logoutStyle;
        private final Icon normalIcon;

        private SidebarMenuButton(String label, Icon icon, boolean logoutStyle) {
            this.label = label;
            this.logoutStyle = logoutStyle;
            this.normalIcon = icon;
            setAlignmentX(JComponent.LEFT_ALIGNMENT);
            setHorizontalAlignment(SwingConstants.LEFT);
            setHorizontalTextPosition(SwingConstants.RIGHT);
            setIconTextGap(10);
            setFont(new Font("SansSerif", Font.BOLD, 14));
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setOpaque(true);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(logoutStyle ? LOGOUT_BORDER : MENU_BORDER),
                    new EmptyBorder(10, 14, 10, 14)));
            setBackground(logoutStyle ? LOGOUT_BG : MENU_BG);
            setForeground(MENU_TEXT);
            setIcon(normalIcon);
            setCollapsedMode(false);
        }

        private void setCollapsedMode(boolean compact) {
            if (compact) {
                setText("");
                setToolTipText(label);
                setHorizontalAlignment(SwingConstants.CENTER);
                setIconTextGap(0);
                setIcon(normalIcon);
                setPreferredSize(new Dimension(46, 46));
                setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
                setMargin(new Insets(8, 8, 8, 8));
            } else {
                setText(label);
                setToolTipText(label);
                setHorizontalAlignment(SwingConstants.LEFT);
                setIconTextGap(10);
                setIcon(normalIcon);
                setPreferredSize(new Dimension(220, 46));
                setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
                setMargin(new Insets(10, 14, 10, 14));
            }
            repaint();
        }
    }
}

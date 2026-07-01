package config;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JSplitPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

public final class UITheme {

    public static final Color BG = new Color(246, 250, 255);
    public static final Color SURFACE = Color.WHITE;
    public static final Color PRIMARY = new Color(20, 85, 168);
    public static final Color PRIMARY_DARK = new Color(18, 52, 86);
    public static final Color PRIMARY_LIGHT = new Color(233, 243, 252);
    public static final Color BORDER = new Color(221, 232, 243);
    public static final Color TEXT = new Color(28, 43, 63);
    public static final Color MUTED = new Color(90, 110, 132);
    public static final Color DANGER_TEXT = new Color(196, 63, 63);

    public static final Font DISPLAY_FONT = new Font("SansSerif", Font.BOLD, 30);
    public static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 20);
    public static final Font LABEL_FONT = new Font("SansSerif", Font.BOLD, 13);
    public static final Font BODY_FONT = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font BUTTON_FONT = new Font("SansSerif", Font.BOLD, 13);

    private UITheme() {
    }

    public static Border cardBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(18, 18, 18, 18));
    }

    public static Border panelPadding(int top, int left, int bottom, int right) {
        return new EmptyBorder(top, left, bottom, right);
    }

    public static void applyTitle(JLabel label) {
        label.setFont(DISPLAY_FONT);
        label.setForeground(PRIMARY_DARK);
    }

    public static void applySectionTitle(JLabel label) {
        label.setFont(TITLE_FONT);
        label.setForeground(PRIMARY_DARK);
    }

    public static void applyBody(JLabel label) {
        label.setFont(BODY_FONT);
        label.setForeground(MUTED);
    }

    public static void applyLabel(JLabel label) {
        label.setFont(LABEL_FONT);
        label.setForeground(new Color(52, 71, 103));
    }

    public static JTextField createTextField() {
        JTextField field = new JTextField();
        styleTextComponent(field);
        return field;
    }

    public static JPasswordField createPasswordField() {
        JPasswordField field = new JPasswordField();
        styleTextComponent(field);
        return field;
    }

    public static JTextArea createTextArea() {
        JTextArea area = new JTextArea();
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        styleTextComponent(area);
        return area;
    }

    public static JComboBox<?> styleComboBox(JComboBox<?> comboBox) {
        comboBox.setFont(BODY_FONT);
        comboBox.setBackground(SURFACE);
        comboBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(6, 8, 6, 8)));
        return comboBox;
    }

    public static JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setForeground(PRIMARY_DARK);
        button.setBackground(new Color(223, 236, 248));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(186, 210, 232)),
                new EmptyBorder(12, 16, 12, 16)));
        button.setOpaque(true);
        return button;
    }

    public static JButton createSidebarLogoutButton(String text) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setForeground(PRIMARY_DARK);
        button.setBackground(Color.WHITE);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(205, 220, 235)),
                new EmptyBorder(12, 16, 12, 16)));
        button.setOpaque(true);
        return button;
    }

    public static JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setForeground(PRIMARY);
        button.setBackground(PRIMARY_LIGHT);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(214, 228, 242)),
                new EmptyBorder(12, 16, 12, 16)));
        button.setOpaque(true);
        return button;
    }

    public static JButton createDangerButton(String text) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setForeground(DANGER_TEXT);
        button.setBackground(new Color(240, 244, 249));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 226, 233)),
                new EmptyBorder(12, 16, 12, 16)));
        button.setOpaque(true);
        return button;
    }

    private static void styleTextComponent(JComponent component) {
        component.setFont(BODY_FONT);
        if (component instanceof JTextField) {
            ((JTextField) component).setBackground(new Color(251, 253, 255));
        } else if (component instanceof JPasswordField) {
            ((JPasswordField) component).setBackground(new Color(251, 253, 255));
        } else if (component instanceof JTextArea) {
            ((JTextArea) component).setBackground(new Color(251, 253, 255));
        }
        component.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(10, 12, 10, 12)));
    }

    public static void paintSoftGeometricBackground(Graphics2D g2, int width, int height) {
        Object oldAA = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(new Color(244, 249, 255));
        g2.fillRect(0, 0, width, height);

        g2.setColor(new Color(225, 236, 248));
        g2.fill(new RoundRectangle2D.Double(width * 0.62, -30, width * 0.42, height * 0.38, 64, 64));
        g2.setColor(new Color(233, 243, 252));
        g2.fill(new Ellipse2D.Double(width * 0.72, height * 0.18, width * 0.18, width * 0.18));
        g2.fill(new Ellipse2D.Double(width * 0.08, height * 0.68, width * 0.22, width * 0.22));

        g2.setColor(new Color(214, 230, 245));
        g2.setStroke(new BasicStroke(2f));
        Shape wave = buildAbstractWave(width, height);
        g2.draw(wave);

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAA);
    }

    private static Shape buildAbstractWave(int width, int height) {
        Path2D path = new Path2D.Double();
        path.moveTo(width * 0.58, height * 0.84);
        path.curveTo(width * 0.67, height * 0.74, width * 0.79, height * 0.76, width * 0.88, height * 0.66);
        path.curveTo(width * 0.93, height * 0.61, width * 0.95, height * 0.54, width * 1.02, height * 0.50);
        return path;
    }

    public static JScrollPane createScrollPane(JComponent content) {
        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getViewport().setBackground(BG);
        installWheelScrollSupport(content, scrollPane);
        return scrollPane;
    }

    public static JSplitPane createHorizontalSplitPane(java.awt.Component left, java.awt.Component right, int dividerLocation,
            double resizeWeight) {
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        splitPane.setDividerLocation(dividerLocation);
        splitPane.setResizeWeight(resizeWeight);
        splitPane.setContinuousLayout(true);
        splitPane.setOneTouchExpandable(false);
        splitPane.setBorder(null);
        splitPane.setDividerSize(8);
        return splitPane;
    }

    public static void installWheelScrollSupport(JComponent root, JScrollPane scrollPane) {
        MouseWheelListener relay = new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (!scrollPane.isVisible()) {
                    return;
                }
                MouseWheelEvent converted = new MouseWheelEvent(
                        scrollPane,
                        e.getID(),
                        e.getWhen(),
                        e.getModifiersEx(),
                        e.getX(),
                        e.getY(),
                        e.getClickCount(),
                        e.isPopupTrigger(),
                        e.getScrollType(),
                        e.getScrollAmount(),
                        e.getWheelRotation());
                scrollPane.dispatchEvent(converted);
                e.consume();
            }
        };
        attachWheelListenerRecursive(root, relay);
    }

    private static void attachWheelListenerRecursive(Component component, MouseWheelListener listener) {
        component.addMouseWheelListener(listener);
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                attachWheelListenerRecursive(child, listener);
            }
        }
    }
}

package view.common;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.Icon;

public final class SidebarIcons {

    private static final int SIZE = 18;

    private SidebarIcons() {
    }

    public static Icon dashboard(Color color) {
        return new PaintedIcon(color, (g2, x, y, w, h) -> {
            Stroke old = g2.getStroke();
            g2.setStroke(new BasicStroke(1.9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(new RoundRectangle2D.Double(x + 2.2, y + 4, w - 4.4, h - 6, 3.5, 3.5));
            g2.draw(new RoundRectangle2D.Double(x + 6.2, y + 9, 5.6, 5.6, 1.5, 1.5));
            g2.drawLine(p(x + 4), p(y + 7), p(x + 9), p(y + 2.8));
            g2.drawLine(p(x + 9), p(y + 2.8), p(x + 14), p(y + 7));
            g2.setStroke(old);
        });
    }

    public static Icon calendar(Color color) {
        return new PaintedIcon(color, (g2, x, y, w, h) -> {
            Stroke old = g2.getStroke();
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(new RoundRectangle2D.Double(x + 2, y + 4, w - 4, h - 5, 3, 3));
            g2.drawLine(x + 2, y + 7, x + w - 2, y + 7);
            g2.fill(new RoundRectangle2D.Double(x + 5, y + 1.5, 2.4, 3.8, 1.2, 1.2));
            g2.fill(new RoundRectangle2D.Double(x + w - 7.4, y + 1.5, 2.4, 3.8, 1.2, 1.2));
            g2.setStroke(old);
        });
    }

    public static Icon calendarPlus(Color color) {
        return new PaintedIcon(color, (g2, x, y, w, h) -> {
            drawCalendarBase(g2, x, y, w, h);
            Stroke old = g2.getStroke();
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(x + 9, y + 10, x + 9, y + 14);
            g2.drawLine(x + 7, y + 12, x + 11, y + 12);
            g2.setStroke(old);
        });
    }

    public static Icon calendarCheck(Color color) {
        return new PaintedIcon(color, (g2, x, y, w, h) -> {
            drawCalendarBase(g2, x, y, w, h);
            Stroke old = g2.getStroke();
            g2.setStroke(new BasicStroke(1.9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            Path2D path = new Path2D.Double();
            path.moveTo(x + 5.3, y + 11.2);
            path.lineTo(x + 7.5, y + 13.5);
            path.lineTo(x + 12.3, y + 8.5);
            g2.draw(path);
            g2.setStroke(old);
        });
    }

    public static Icon clipboardCheck(Color color) {
        return new PaintedIcon(color, (g2, x, y, w, h) -> {
            Stroke old = g2.getStroke();
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(new RoundRectangle2D.Double(x + 4, y + 2.5, w - 8, h - 4.5, 2.5, 2.5));
            g2.draw(new RoundRectangle2D.Double(x + 6.5, y + 1.2, 5, 3.3, 1.1, 1.1));
            Path2D path = new Path2D.Double();
            path.moveTo(x + 6.5, y + 10.3);
            path.lineTo(x + 8.5, y + 12.2);
            path.lineTo(x + 12.2, y + 8.4);
            g2.draw(path);
            g2.setStroke(old);
        });
    }

    public static Icon document(Color color) {
        return new PaintedIcon(color, (g2, x, y, w, h) -> {
            Stroke old = g2.getStroke();
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            Path2D page = new Path2D.Double();
            page.moveTo(x + 4, y + 2.5);
            page.lineTo(x + 11, y + 2.5);
            page.lineTo(x + 14, y + 5.5);
            page.lineTo(x + 14, y + 15.2);
            page.lineTo(x + 4, y + 15.2);
            page.closePath();
            g2.draw(page);
            g2.drawLine(p(x + 11), p(y + 2.5), p(x + 11), p(y + 5.8));
            g2.drawLine(p(x + 6.5), p(y + 8), p(x + 11.2), p(y + 8));
            g2.drawLine(p(x + 6.5), p(y + 11), p(x + 11.2), p(y + 11));
            g2.setStroke(old);
        });
    }

    public static Icon chart(Color color) {
        return new PaintedIcon(color, (g2, x, y, w, h) -> {
            Stroke old = g2.getStroke();
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(p(x + 3.5), p(y + 15), p(x + 15), p(y + 15));
            g2.drawLine(p(x + 5.5), p(y + 13), p(x + 5.5), p(y + 9));
            g2.drawLine(p(x + 9), p(y + 13), p(x + 9), p(y + 6.5));
            g2.drawLine(p(x + 12.5), p(y + 13), p(x + 12.5), p(y + 8.5));
            g2.drawLine(p(x + 5.5), p(y + 9), p(x + 9), p(y + 6.5));
            g2.drawLine(p(x + 9), p(y + 6.5), p(x + 12.5), p(y + 8.5));
            g2.setStroke(old);
        });
    }

    public static Icon userMedical(Color color) {
        return new PaintedIcon(color, (g2, x, y, w, h) -> {
            Stroke old = g2.getStroke();
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(new java.awt.geom.Ellipse2D.Double(x + 6, y + 2.8, 5.8, 5.8));
            g2.draw(new RoundRectangle2D.Double(x + 3.8, y + 9.3, 9.4, 5.2, 4, 4));
            g2.drawLine(p(x + 15), p(y + 5.5), p(x + 15), p(y + 10.5));
            g2.drawLine(p(x + 12.5), p(y + 8), p(x + 17.5), p(y + 8));
            g2.setStroke(old);
        });
    }

    public static Icon signOut(Color color) {
        return new PaintedIcon(color, (g2, x, y, w, h) -> {
            Stroke old = g2.getStroke();
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(p(x + 3), p(y + 3.8), p(x + 9), p(y + 3.8));
            g2.drawLine(p(x + 3), p(y + 14), p(x + 9), p(y + 14));
            g2.drawLine(p(x + 9), p(y + 3.8), p(x + 9), p(y + 14));
            g2.drawLine(p(x + 9), p(y + 8.9), p(x + 16), p(y + 8.9));
            g2.drawLine(p(x + 13.8), p(y + 6.4), p(x + 16), p(y + 8.9));
            g2.drawLine(p(x + 13.8), p(y + 11.4), p(x + 16), p(y + 8.9));
            g2.setStroke(old);
        });
    }

    public static Icon chevronLeft(Color color) {
        return new PaintedIcon(color, (g2, x, y, w, h) -> {
            Stroke old = g2.getStroke();
            g2.setStroke(new BasicStroke(2.1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(p(x + 11), p(y + 3.5), p(x + 6), p(y + 9));
            g2.drawLine(p(x + 6), p(y + 9), p(x + 11), p(y + 14.5));
            g2.setStroke(old);
        });
    }

    public static Icon chevronRight(Color color) {
        return new PaintedIcon(color, (g2, x, y, w, h) -> {
            Stroke old = g2.getStroke();
            g2.setStroke(new BasicStroke(2.1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(p(x + 6), p(y + 3.5), p(x + 11), p(y + 9));
            g2.drawLine(p(x + 11), p(y + 9), p(x + 6), p(y + 14.5));
            g2.setStroke(old);
        });
    }

    private static void drawCalendarBase(Graphics2D g2, int x, int y, int w, int h) {
        Stroke old = g2.getStroke();
        g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(new RoundRectangle2D.Double(x + 2, y + 4, w - 4, h - 5, 3, 3));
        g2.drawLine(x + 2, y + 7, x + w - 2, y + 7);
        g2.fill(new RoundRectangle2D.Double(x + 5, y + 1.5, 2.4, 3.8, 1.2, 1.2));
        g2.fill(new RoundRectangle2D.Double(x + w - 7.4, y + 1.5, 2.4, 3.8, 1.2, 1.2));
        g2.setStroke(old);
    }

    private interface Painter {
        void paint(Graphics2D g2, int x, int y, int w, int h);
    }

    private static final class PaintedIcon implements Icon {

        private final Color color;
        private final Painter painter;

        private PaintedIcon(Color color, Painter painter) {
            this.color = color;
            this.painter = painter;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            painter.paint(g2, x, y, getIconWidth(), getIconHeight());
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return SIZE;
        }

        @Override
        public int getIconHeight() {
            return SIZE;
        }
    }

    private static int p(double value) {
        return (int) Math.round(value);
    }
}

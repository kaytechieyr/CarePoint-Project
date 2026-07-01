package config;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Cursor;
import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.TableColumn;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

public final class TableScrollSupport {

    private static final int DRAG_THRESHOLD = 4;
    private static final String DRAG_SCROLL_INSTALLED_KEY = TableScrollSupport.class.getName() + ".dragScrollInstalled";
    private static final String HEADER_RESIZE_INSTALLED_KEY = TableScrollSupport.class.getName() + ".headerResizeInstalled";
    private static final int RESIZE_HOTZONE_PX = 5;

    private TableScrollSupport() {
    }

    public static JScrollPane createTableScrollPane(JTable table, int... preferredWidths) {
        configureWideTable(table, preferredWidths);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(UITheme.BG);

        installDragScroll(table, scrollPane);
        return scrollPane;
    }

    public static void configureWideTable(JTable table, int... preferredWidths) {
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        if (header != null) {
            header.setReorderingAllowed(false);
            header.setResizingAllowed(false);
            installHeaderResizeSupport(table, header);
        }

        if (table.getColumnModel() == null) {
            return;
        }

        if (preferredWidths == null) {
            for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
                table.getColumnModel().getColumn(i).setResizable(true);
            }
            return;
        }

        for (int i = 0; i < table.getColumnModel().getColumnCount() && i < preferredWidths.length; i++) {
            int width = Math.max(40, preferredWidths[i]);
            TableColumn column = table.getColumnModel().getColumn(i);
            column.setPreferredWidth(width);
            column.setResizable(true);
        }

        for (int i = preferredWidths.length; i < table.getColumnModel().getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setResizable(true);
        }
    }

    private static void installHeaderResizeSupport(JTable table, JTableHeader header) {
        if (header == null) {
            return;
        }

        Object installed = header.getClientProperty(HEADER_RESIZE_INSTALLED_KEY);
        if (Boolean.TRUE.equals(installed)) {
            return;
        }
        header.putClientProperty(HEADER_RESIZE_INSTALLED_KEY, Boolean.TRUE);

        final int[] resizingColumn = new int[] { -1 };
        final int[] dragStartX = new int[] { 0 };
        final int[] startWidth = new int[] { 0 };

        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                updateHeaderCursor(header, e.getPoint());
            }

            @Override
            public void mousePressed(MouseEvent e) {
                int columnIndex = findResizableColumn(header, e.getPoint());
                if (columnIndex < 0) {
                    return;
                }

                resizingColumn[0] = columnIndex;
                dragStartX[0] = e.getX();
                startWidth[0] = table.getColumnModel().getColumn(columnIndex).getWidth();
                updateHeaderCursor(header, e.getPoint());
                e.consume();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                resizingColumn[0] = -1;
                updateHeaderCursor(header, e.getPoint());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (resizingColumn[0] < 0) {
                    header.setCursor(Cursor.getDefaultCursor());
                }
            }
        };

        MouseMotionAdapter motionAdapter = new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (resizingColumn[0] < 0) {
                    updateHeaderCursor(header, e.getPoint());
                    return;
                }

                TableColumnModel columnModel = table.getColumnModel();
                if (columnModel == null || resizingColumn[0] >= columnModel.getColumnCount()) {
                    return;
                }

                int delta = e.getX() - dragStartX[0];
                int newWidth = Math.max(40, startWidth[0] + delta);
                TableColumn column = columnModel.getColumn(resizingColumn[0]);
                column.setPreferredWidth(newWidth);
                column.setWidth(newWidth);
                table.doLayout();
                table.revalidate();
                table.repaint();
                header.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
                e.consume();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                updateHeaderCursor(header, e.getPoint());
            }
        };

        header.addMouseListener(mouseAdapter);
        header.addMouseMotionListener(motionAdapter);
    }

    private static void updateHeaderCursor(JTableHeader header, Point point) {
        if (header == null || point == null) {
            return;
        }
        if (findResizableColumn(header, point) >= 0) {
            header.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
        } else {
            header.setCursor(Cursor.getDefaultCursor());
        }
    }

    private static int findResizableColumn(JTableHeader header, Point point) {
        if (header == null || point == null) {
            return -1;
        }

        int columnIndex = header.columnAtPoint(point);
        if (columnIndex < 0) {
            return -1;
        }

        Rectangle rect = header.getHeaderRect(columnIndex);
        if (rect == null) {
            return -1;
        }

        int distanceToRightEdge = Math.abs(point.x - (rect.x + rect.width));
        if (distanceToRightEdge <= RESIZE_HOTZONE_PX) {
            return columnIndex;
        }

        return -1;
    }

    public static void enableDragToScroll(JTable table) {
        if (table == null) {
            return;
        }

        Object installed = table.getClientProperty(DRAG_SCROLL_INSTALLED_KEY);
        if (Boolean.TRUE.equals(installed)) {
            return;
        }
        table.putClientProperty(DRAG_SCROLL_INSTALLED_KEY, Boolean.TRUE);

        JScrollPane scrollPane = findScrollPane(table);
        installDragScroll(table, scrollPane);
    }

    public static void installDragToScroll(JTable table, JScrollPane scrollPane) {
        if (table == null) {
            return;
        }
        if (scrollPane != null) {
            scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        }
        enableDragToScroll(table);
    }

    private static void installDragScroll(JTable table, JScrollPane scrollPane) {
        final Point[] pressPoint = new Point[1];
        final Point[] viewOrigin = new Point[1];
        final boolean[] dragging = new boolean[1];

        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!javax.swing.SwingUtilities.isLeftMouseButton(e)) {
                    return;
                }
                JScrollPane currentScrollPane = scrollPane != null ? scrollPane : findScrollPane(table);
                if (currentScrollPane == null) {
                    return;
                }
                pressPoint[0] = e.getPoint();
                JViewport viewport = currentScrollPane.getViewport();
                viewOrigin[0] = viewport != null ? viewport.getViewPosition() : null;
                dragging[0] = false;
                setDragCursor(table, currentScrollPane, Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                pressPoint[0] = null;
                viewOrigin[0] = null;
                dragging[0] = false;
                setDragCursor(table, scrollPane != null ? scrollPane : findScrollPane(table), Cursor.getDefaultCursor());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!dragging[0]) {
                    setDragCursor(table, scrollPane != null ? scrollPane : findScrollPane(table), Cursor.getDefaultCursor());
                }
            }
        };

        MouseMotionAdapter motionAdapter = new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (pressPoint[0] == null || viewOrigin[0] == null) {
                    return;
                }

                JScrollPane currentScrollPane = scrollPane != null ? scrollPane : findScrollPane(table);
                if (currentScrollPane == null) {
                    return;
                }

                int deltaX = e.getX() - pressPoint[0].x;
                int deltaY = e.getY() - pressPoint[0].y;
                if (Math.abs(deltaX) < DRAG_THRESHOLD && Math.abs(deltaY) < DRAG_THRESHOLD) {
                    return;
                }

                JViewport viewport = currentScrollPane.getViewport();
                if (viewport == null) {
                    return;
                }

                Rectangle viewRect = viewport.getViewRect();
                Point target = new Point(viewOrigin[0]);
                target.translate(-deltaX, -deltaY);

                int maxX = Math.max(0, table.getWidth() - viewRect.width);
                int maxY = Math.max(0, table.getHeight() - viewRect.height);
                if (target.x < 0) {
                    target.x = 0;
                } else if (target.x > maxX) {
                    target.x = maxX;
                }
                if (target.y < 0) {
                    target.y = 0;
                } else if (target.y > maxY) {
                    target.y = maxY;
                }

                viewport.setViewPosition(target);
                dragging[0] = true;
                setDragCursor(table, currentScrollPane, Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                e.consume();
            }
        };

        table.addMouseListener(mouseAdapter);
        table.addMouseMotionListener(motionAdapter);

        if (scrollPane != null) {
            attachDragListeners(scrollPane.getViewport(), mouseAdapter, motionAdapter);
            attachDragListeners(scrollPane, mouseAdapter, motionAdapter);
        }
    }

    private static void setDragCursor(JTable table, JScrollPane scrollPane, Cursor cursor) {
        if (table != null) {
            table.setCursor(cursor);
        }
        if (scrollPane != null) {
            scrollPane.setCursor(cursor);
            JViewport viewport = scrollPane.getViewport();
            if (viewport != null) {
                viewport.setCursor(cursor);
            }
        }
    }

    private static void attachDragListeners(Component component, MouseAdapter mouseAdapter, MouseMotionAdapter motionAdapter) {
        if (component == null) {
            return;
        }
        component.addMouseListener(mouseAdapter);
        component.addMouseMotionListener(motionAdapter);
    }

    private static JScrollPane findScrollPane(JComponent component) {
        java.awt.Container current = component.getParent();
        while (current != null) {
            if (current instanceof JScrollPane) {
                return (JScrollPane) current;
            }
            current = current.getParent();
        }
        return null;
    }
}

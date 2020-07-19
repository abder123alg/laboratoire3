import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.*;

public class ZoomAndPan1 extends JPanel {

    BufferedImage img;
    private boolean init = true;
    private int zoomLevel = 0;
    private int minZoomLevel = -20;
    private int maxZoomLevel = 10;
    private double zoomMultiplicationFactor = 1.2;

    private Point dragStartScreen;
    private Point dragEndScreen;
    private AffineTransform coordTransform = new AffineTransform();

    public ZoomAndPan1() throws IOException {
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dragStartScreen = e.getPoint();
                dragEndScreen = null;
            }
        });
        this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                pan(e);
            }
        });
        this.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.isControlDown()) {
                    zoom(e);
                }
            }
        });
        img = ImageIO.read(new File("file.path"));
    }

    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        int x = (int) (this.size().getWidth() - (img.getWidth() * .2)) / 2;
        int y = (int) (this.size().getHeight() - (img.getHeight() * .2)) / 2;

        AffineTransform at = new AffineTransform();
        at.translate(x, y);
        at.scale(.2, .2);
        if (init) {
            g2.setTransform(at);
            init = false;
            coordTransform = g2.getTransform();
        } else {
            g2.setTransform(coordTransform);
        }

        g2.drawImage(img, 0, 0, this);

        g2.dispose();
    }

    private void pan(MouseEvent e) {
        try {
            dragEndScreen = e.getPoint();
            Point2D.Float dragStart = transformPoint(dragStartScreen);
            Point2D.Float dragEnd = transformPoint(dragEndScreen);
            double dx = dragEnd.getX() - dragStart.getX();
            double dy = dragEnd.getY() - dragStart.getY();
            coordTransform.translate(dx, dy);
            dragStartScreen = dragEndScreen;
            dragEndScreen = null;
            repaint();
        } catch (NoninvertibleTransformException ex) {
            ex.printStackTrace();
        }
    }

    private void zoom(MouseWheelEvent e) {
        try {
            int wheelRotation = e.getWheelRotation();
            Point p = e.getPoint();
            if (wheelRotation > 0) {
                if (zoomLevel < maxZoomLevel) {
                    zoomLevel++;
                    Point2D p1 = transformPoint(p);
                    coordTransform.scale(1 / zoomMultiplicationFactor, 1 / zoomMultiplicationFactor);
                    Point2D p2 = transformPoint(p);
                    coordTransform.translate(p2.getX() - p1.getX(), p2.getY() - p1.getY());
                    repaint();
                }
            } else {
                if (zoomLevel > minZoomLevel) {
                    zoomLevel--;
                    Point2D p1 = transformPoint(p);
                    coordTransform.scale(zoomMultiplicationFactor, zoomMultiplicationFactor);
                    Point2D p2 = transformPoint(p);
                    coordTransform.translate(p2.getX() - p1.getX(), p2.getY() - p1.getY());
                    repaint();
                }
            }
        } catch (NoninvertibleTransformException ex) {
            ex.printStackTrace();
        }
    }

    private Point2D.Float transformPoint(Point p1) throws NoninvertibleTransformException {
        AffineTransform inverse = coordTransform.createInverse();
        Point2D.Float p2 = new Point2D.Float();
        inverse.transform(p1, p2);
        return p2;
    }

    public Dimension getPreferredSize() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        return new Dimension(screenSize.width, screenSize.height);
    }

    public static void main(String[] args) {
        try {
            JFrame frame = new JFrame("Zoom and Pan ");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.add(new ZoomAndPan1(), BorderLayout.CENTER);
            frame.pack();
            frame.setVisible(true);
        } catch (IOException ex) {
            Logger.getLogger(ZoomAndPan1.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
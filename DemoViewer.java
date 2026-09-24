import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;

public class DemoViewer {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("3D Renderer Demo");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            Container pane = frame.getContentPane();
            pane.setLayout(new BorderLayout());

            JSlider headingSlider = new JSlider(0, 360, 180);
            pane.add(headingSlider, BorderLayout.SOUTH);

            JSlider pitchSlider = new JSlider(-90, 90, 0);
            pane.add(pitchSlider, BorderLayout.EAST);

            List<Triangle> triangles = createDemoTriangles();

            JPanel renderPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(Color.BLACK);
                    g2.fillRect(0, 0, getWidth(), getHeight());

                    double heading = Math.toRadians(headingSlider.getValue());
                    double pitch = Math.toRadians(pitchSlider.getValue());
                    int centerX = getWidth() / 2;
                    int centerY = getHeight() / 2;

                    for (Triangle triangle : triangles) {
                        Vertex v1 = rotateVertex(triangle.v1, heading, pitch);
                        Vertex v2 = rotateVertex(triangle.v2, heading, pitch);
                        Vertex v3 = rotateVertex(triangle.v3, heading, pitch);

                        Point p1 = projectVertex(v1, centerX, centerY);
                        Point p2 = projectVertex(v2, centerX, centerY);
                        Point p3 = projectVertex(v3, centerX, centerY);

                        Path2D.Double polygon = new Path2D.Double();
                        polygon.moveTo(p1.x, p1.y);
                        polygon.lineTo(p2.x, p2.y);
                        polygon.lineTo(p3.x, p3.y);
                        polygon.closePath();

                        g2.setColor(triangle.color);
                        g2.fill(polygon);
                        g2.setColor(triangle.color.darker());
                        g2.draw(polygon);
                    }

                    g2.dispose();
                }
            };

            headingSlider.addChangeListener(e -> renderPanel.repaint());
            pitchSlider.addChangeListener(e -> renderPanel.repaint());

            MouseAdapter mouseController = new MouseAdapter() {
                private int lastX;
                private int lastY;

                @Override
                public void mousePressed(MouseEvent e) {
                    lastX = e.getX();
                    lastY = e.getY();
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    int dx = e.getX() - lastX;
                    int dy = e.getY() - lastY;

                    lastX = e.getX();
                    lastY = e.getY();

                    int newHeading = headingSlider.getValue() + dx;
                    int newPitch = pitchSlider.getValue() - dy;

                    headingSlider.setValue(Math.max(0, Math.min(360, newHeading)));
                    pitchSlider.setValue(Math.max(-90, Math.min(90, newPitch)));
                }
            };

            renderPanel.addMouseListener(mouseController);
            renderPanel.addMouseMotionListener(mouseController);

            pane.add(renderPanel, BorderLayout.CENTER);

            frame.setSize(420, 420);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    private static List<Triangle> createDemoTriangles() {
        List<Triangle> triangles = new ArrayList<>();

        Vertex[] vertices = {
            new Vertex(-1, -1, -1),
            new Vertex(1, -1, -1),
            new Vertex(1, 1, -1),
            new Vertex(-1, 1, -1),
            new Vertex(-1, -1, 1),
            new Vertex(1, -1, 1),
            new Vertex(1, 1, 1),
            new Vertex(-1, 1, 1)
        };

        int[][] faces = {
            {0, 1, 2, 3},
            {4, 5, 6, 7},
            {0, 1, 5, 4},
            {1, 2, 6, 5},
            {2, 3, 7, 6},
            {3, 0, 4, 7}
        };

        Color[] colors = {
            Color.RED,
            Color.BLUE,
            Color.GREEN,
            Color.YELLOW,
            Color.MAGENTA,
            Color.CYAN
        };

        for (int i = 0; i < faces.length; i++) {
            int[] face = faces[i];
            Color color = colors[i % colors.length];
            triangles.add(new Triangle(vertices[face[0]], vertices[face[1]], vertices[face[2]], color));
            triangles.add(new Triangle(vertices[face[0]], vertices[face[2]], vertices[face[3]], color));
        }

        return triangles;
    }

    private static Vertex rotateVertex(Vertex vertex, double heading, double pitch) {
        double x = vertex.x;
        double y = vertex.y;
        double z = vertex.z;

        double x1 = x * Math.cos(heading) - z * Math.sin(heading);
        double z1 = x * Math.sin(heading) + z * Math.cos(heading);

        double y1 = y * Math.cos(pitch) - z1 * Math.sin(pitch);
        double z2 = y * Math.sin(pitch) + z1 * Math.cos(pitch);

        return new Vertex(x1, y1, z2);
    }

    private static Point projectVertex(Vertex vertex, int centerX, int centerY) {
        double cameraDistance = 5.0;
        double scale = 100.0;
        double perspective = cameraDistance / (cameraDistance + vertex.z + 3.0);
        int x = (int) Math.round(centerX + vertex.x * scale * perspective);
        int y = (int) Math.round(centerY + vertex.y * scale * perspective);
        return new Point(x, y);
    }

    private static final class Vertex {
        final double x;
        final double y;
        final double z;

        Vertex(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    private static final class Triangle {
        final Vertex v1;
        final Vertex v2;
        final Vertex v3;
        final Color color;

        Triangle(Vertex v1, Vertex v2, Vertex v3, Color color) {
            this.v1 = v1;
            this.v2 = v2;
            this.v3 = v3;
            this.color = color;
        }
    }
}
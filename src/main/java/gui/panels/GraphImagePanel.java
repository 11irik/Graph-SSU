package gui.panels;

import graph.adapters.EdgeAdapter;
import graph.adapters.GraphAdapter;
import graph.adapters.NodeAdapter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

public class GraphImagePanel extends JPanel implements MouseListener, MouseMotionListener {
    protected GraphAdapter graph;
    ArrayList<EdgeAdapter> visitedEdges = new ArrayList<>();
    ArrayList<NodeAdapter> visitedNodes = new ArrayList<>();

    //Graphics attributes
    private Font[] keyFont = new Font[30];
    private int nodeSize;

    private double aspect;
    private int maxNodeRandom = 500;
    protected float regularWidth = 0.5f;

    //Interactive attributes
    NodeAdapter selectedNode;
    boolean selected = false;

    public GraphImagePanel(GraphAdapter graph) {
        this.graph = graph;

        addMouseListener(this);
        addMouseMotionListener(this);

        setBorder(BorderFactory.createEtchedBorder());
        setBackground(new Color(200, 200, 200));

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        aspect = Math.min(g.getClipBounds().height, g.getClipBounds().width) / maxNodeRandom;
        Graphics2D g2 = (Graphics2D) g;

        graph.getEdges().forEach(edge -> drawEdge(g, edge, Color.BLACK, aspect));

        g2.setStroke(new BasicStroke(regularWidth));
        graph.getNodes().forEach(node -> drawNode(g2, (node), aspect));
    }

    int shiftX = 30;
    int shiftY = 30;

    private void drawNode(Graphics g, NodeAdapter node, double aspect) {
        nodeSize = (int) aspect * maxNodeRandom / 8;

        Graphics2D g2 = (Graphics2D) g;

        g2.setColor(node.getColor());
        g.fillOval((int) (node.getX() * aspect - nodeSize / 2) + shiftX, (int) (node.getY() * aspect - nodeSize / 2) + shiftY, nodeSize, nodeSize);
        g2.setColor(Color.BLACK);
        g.drawOval((int) (node.getX() * aspect - nodeSize / 2) + shiftX, (int) (node.getY() * aspect - nodeSize / 2) + shiftY, nodeSize, nodeSize);

        g.setFont(keyFont[(int) (8 * aspect * 2)]);
        FontMetrics fontMetrics = g.getFontMetrics();
        int width = fontMetrics.stringWidth("" + node.getKey());
        g.drawString("" + node.getKey(), (int) (node.getX() * aspect) + (nodeSize / 8 - width) / 2 + shiftX, (int) (node.getY() * aspect) + nodeSize / 4 + shiftY);
    }

    public void drawEdge(Graphics g, EdgeAdapter edge, Color color, double aspect) {

        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(color);
        Stroke stroke = new BasicStroke(2f);
        g2.setStroke(stroke);

        NodeAdapter start = edge.getStart();
        NodeAdapter end = edge.getEnd();

        g2.drawLine(
                (int) (start.getX() * aspect + shiftX),
                (int) (start.getY() * aspect + shiftY),
                (int) (end.getX() * aspect + shiftX),
                (int) (end.getY() * aspect + shiftY)
        );

        if (edge.isWeighted()) {
            g.setFont(keyFont[(int) (8 * aspect)]);

            String str = String.valueOf(edge.getWeight());
            FontMetrics fm = g.getFontMetrics();
            Rectangle2D rect = fm.getStringBounds(str, g);
            int x = (int) ((start.getX() + end.getX()) / 2);
            int y = (int) ((start.getY() + end.getY()) / 2);
            g.setColor(Color.WHITE);
            g.fillRect((int) (x * aspect), (int) (y * aspect) - fm.getAscent(), (int) rect.getWidth(), (int) rect.getHeight());
            g.setColor(Color.BLACK);
            g.drawString(str, (int) (x * aspect), (int) (y * aspect));
        }

        if (edge.isOriented()) {
            int deltaY = end.getY() - start.getY();
            int deltaX = end.getX() - start.getX();
            int edgeLength = (int) Math.sqrt(deltaX * deltaX + deltaY * deltaY) - (int) (nodeSize / 2 / aspect);

            double angle = Math.atan2(deltaY, deltaX);
            double angleDelta = 2 * Math.PI / 20;
            int arrowArmLength = 20;
            if (edgeLength < nodeSize * 2 / aspect) {
                arrowArmLength = edgeLength / 3;
            }
            int nodeContactX = start.getX() + (int) (Math.cos(angle) * (edgeLength));
            int nodeContactY = start.getY() + (int) (Math.sin(angle) * (edgeLength));

            int leftArrowArmX = nodeContactX - (int) (Math.cos(angle - angleDelta) * arrowArmLength);
            int leftArrowArmY = nodeContactY - (int) (Math.sin(angle - angleDelta) * arrowArmLength);

            int rightArrowArmX = nodeContactX - (int) (Math.cos(angle + angleDelta) * arrowArmLength);
            int rightArrowArmY = nodeContactY - (int) (Math.sin(angle + angleDelta) * arrowArmLength);

            g2.drawLine(
                    (int) (nodeContactX * aspect),
                    (int) (nodeContactY * aspect),
                    (int) (leftArrowArmX * aspect),
                    (int) (leftArrowArmY * aspect)
            );

            g2.drawLine(
                    (int) (nodeContactX * aspect),
                    (int) (nodeContactY * aspect),
                    (int) (rightArrowArmX * aspect),
                    (int) (rightArrowArmY * aspect)
            );
        }
    }

    private NodeAdapter selectNodeFromCoords(int x, int y) {
        NodeAdapter node = null;
        for (NodeAdapter n : graph.getNodes()) {
            if (Math.abs(n.getX() * aspect - x + shiftX) < nodeSize / 2 &&
                    Math.abs(n.getY() * aspect - y + shiftY) < nodeSize / 2) {
                node = n;
            }
        }
        return node;
    }
    private NodeAdapter node1 = null;
    private NodeAdapter node2 = null;

    public void addEdge(double weight) {
        if (node1 != null && node2 != null) {
            graph.addEdge(node1, node2, weight);
            repaint();
            node1 = null;
            node2 = null;
        }
    }

    public void addEdge() {
        if (node1 != null && node2 != null) {
            graph.addEdge(node1, node2);
            repaint();
            node1 = null;
            node2 = null;
        }
    }

    public void deleteEdge() {
        if (node1 != null && node2 != null) {
            graph.deleteEdge(node1, node2);
            repaint();
            node1 = null;
            node2 = null;
        }
    }

    public void deleteNode() {
        if (node2 != null) {
            graph.deleteNode(node2);
            repaint();
            node2 = null;
        }
    }

    public GraphAdapter getGraph() {
        return graph;
    }

    //Mouse Listener
    @Override
    public void mousePressed(MouseEvent e) {
        try {
            selectedNode = selectNodeFromCoords(e.getX(), e.getY());
            selectedNode.setX((int) (e.getX() / aspect));
            selectedNode.setY((int) (e.getY() / aspect));
            selected = true;
            repaint();
        } catch (Exception k) {
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        selected = false;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (selected) {
            selectedNode.setX((int) (e.getX() / aspect));
            selectedNode.setY((int) (e.getY() / aspect));

            repaint();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 1) {
            if (node2 == null) {
                node2 = selectNodeFromCoords(e.getX(), e.getY());
                if(node2 != null) {
                    node2.setColor(Color.RED);
                }
            } else {
                NodeAdapter temp = selectNodeFromCoords(e.getX(), e.getY());
                if (temp != node2 && temp != null) {
                    if (node1 != null) {
                        node1.setDefaultColor();
                    }
                    node1 = node2;
                    node1.setColor(Color.GREEN);
                    node2 = temp;
                    node2.setColor(Color.RED);
                }
            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseExited(MouseEvent mouseEvent) {

    }
}

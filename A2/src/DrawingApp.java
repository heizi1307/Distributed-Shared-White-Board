import javax.swing.*;
import java.awt.*;

public class DrawingApp {
    public static void main(String[] args) {
        // 创建 JFrame
        JFrame frame = new JFrame("Drawing Application");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 创建绘图区域
        JPanel drawingPanel = new JPanel();
        drawingPanel.setBackground(Color.WHITE);

        // 创建工具栏
        JPanel toolbarPanel = new JPanel();
        JButton pencilBtn = new JButton("Pencil");
        JButton lineBtn = new JButton("Line");
        JButton rectangleBtn = new JButton("Rectangle");
        JButton circleBtn = new JButton("Circle");
        JButton colorPickerBtn = new JButton("Color Picker");

        // 添加工具按钮到工具栏
        toolbarPanel.add(pencilBtn);
        toolbarPanel.add(lineBtn);
        toolbarPanel.add(rectangleBtn);
        toolbarPanel.add(circleBtn);
        toolbarPanel.add(colorPickerBtn);

        // 创建菜单栏
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem openItem = new JMenuItem("Open");
        JMenuItem saveItem = new JMenuItem("Save");
        JMenuItem exitItem = new JMenuItem("Exit");

        // 添加菜单项到菜单
        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        // 添加菜单到菜单栏
        menuBar.add(fileMenu);

        // 将绘图区域、工具栏和菜单栏添加到 JFrame
        frame.add(drawingPanel, BorderLayout.CENTER);
        frame.add(toolbarPanel, BorderLayout.NORTH);
        frame.setJMenuBar(menuBar);

        // 设置 JFrame 大小并显示
        frame.setSize(800, 600);
        frame.setVisible(true);
    }
}

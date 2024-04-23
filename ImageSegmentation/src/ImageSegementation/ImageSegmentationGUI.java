package ImageSegementation;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;

public class ImageSegmentationGUI extends JFrame {
    private final JLabel inputImageLabel;
    private final JLabel grayImageLabel;
    private final JLabel segmentedImageLabel;
    private final JTextField thresholdField;
    private BufferedImage currentImage;

    public ImageSegmentationGUI() {
        super("图像分割工具");
        inputImageLabel = new JLabel("点击选择图片", JLabel.CENTER);
        grayImageLabel = new JLabel("灰度图", JLabel.CENTER);
        segmentedImageLabel = new JLabel("分割后图像", JLabel.CENTER);
        inputImageLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                chooseAndDisplayImage();
            }
        });
        JSplitPane mainSplitPane = getjSplitPane();
        this.add(mainSplitPane, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        thresholdField = new JTextField("", 5);
        thresholdField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                thresholdField.setText("");
            }
        });
        Font buttonFont = new Font("", Font.BOLD, 20);
        thresholdField.setFont(buttonFont);
        JLabel thresholdLabel = new JLabel("阈值:");
        thresholdLabel.setFont(buttonFont);
        buttonPanel.add(thresholdLabel);
        buttonPanel.add(thresholdField);
        JButton segmentButton = new JButton("开始分割");
        segmentButton.setFont(buttonFont);
        segmentButton.addActionListener(e -> segmentImage());
        buttonPanel.add(segmentButton);
        this.add(buttonPanel, BorderLayout.SOUTH);
        this.setSize(1200, 600);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }

    public static void main(String[] args) {
        new ImageSegmentationGUI();
    }

    private JSplitPane getjSplitPane() {
        JScrollPane inputScrollPane = new JScrollPane(inputImageLabel);
        JScrollPane grayScrollPane = new JScrollPane(grayImageLabel);
        JScrollPane segmentedScrollPane = new JScrollPane(segmentedImageLabel);
        JSplitPane bottomSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, grayScrollPane, segmentedScrollPane);
        bottomSplitPane.setResizeWeight(0.5);
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, inputScrollPane, bottomSplitPane);
        mainSplitPane.setResizeWeight(0.5);
        return mainSplitPane;
    }
    public int calculateNumberOfRegions(int[][] regions) {
        boolean[] isRegionPresent = new boolean[findMaxRegionId(regions) + 1];
        for (int[] region : regions) {
            for (int i : region) {
                isRegionPresent[i] = true;
            }
        }
        int count = 0;
        for (boolean present : isRegionPresent) {
            if (present) {
                count++;
            }
        }
        return count;
    }

    private int findMaxRegionId(int[][] regions) {
        int maxRegionId = 0;
        for (int[] region : regions) {
            for (int i : region) {
                if (i > maxRegionId) {
                    maxRegionId = i;
                }
            }
        }
        return maxRegionId;
    }

    public double calculateAverageRegionSize(int[][] regions, int numberOfRegions) {
        int maxRegionId = findMaxRegionId(regions);
        int[] regionSizes = new int[maxRegionId + 1];

        for (int[] region : regions) {
            for (int i : region) {
                if (i < regionSizes.length) {
                    regionSizes[i]++;
                } else {
                    System.err.println("区域编号超出预期范围: " + i);
                }
            }
        }
        int totalSize = 0;
        for (int size : regionSizes) {
            totalSize += size;
        }
        return (double) totalSize / numberOfRegions;
    }
    private void segmentImage() {
        if (currentImage == null) {
            JOptionPane.showMessageDialog(this, "请先选择一张图片！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        double threshold;
        try {
            String thresholdText = thresholdField.getText();
            boolean isAutoThreshold = thresholdText.isEmpty() || thresholdText.equals("自动");
            if (isAutoThreshold) {
                ImageSegmentation segmentation = new ImageSegmentation();
                threshold = segmentation.calculateAutoThreshold(currentImage);
                thresholdField.setText(String.format("%.5f", threshold));
            } else {
                threshold = Double.parseDouble(thresholdText);
            }

            if (threshold < 0 || threshold > 1) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "阈值必须是0到1之间的数字！", "输入错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            long startTime = System.currentTimeMillis();
            ImageSegmentation segmentation = new ImageSegmentation();
            BufferedImage grayImage = ImageSegmentation.convertToGrayImage(currentImage);
            ImageIcon grayIcon = new ImageIcon(grayImage);
            grayImageLabel.setIcon(grayIcon);
            Graph graph = segmentation.imageToGraph(grayImage);
            graph.Prim(0, threshold);
            int sizeThreshold = 1;
            int[][] regions = segmentation.mergeSmallRegions(graph, grayImage, sizeThreshold);
            BufferedImage segmentedImage = segmentation.visualizeRegions(currentImage, regions);
            ImageIcon segmentedIcon = new ImageIcon(segmentedImage);
            segmentedImageLabel.setIcon(segmentedIcon);
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            int numberOfRegions = calculateNumberOfRegions(regions);
            double averageRegionSize = calculateAverageRegionSize(regions, numberOfRegions);
            String desktopPath = System.getProperty("user.home") + "/Desktop";
            File outputImageFile = new File(desktopPath + "/output_segmented.jpg");
            ImageIO.write(segmentedImage, "jpg", outputImageFile);
            JOptionPane.showMessageDialog(this, "分割完成！分割后的图像已经保存在桌面。\n耗时: " + duration + " 毫秒。\n"
                    + "图像分辨率: " + currentImage.getWidth() + "x" + currentImage.getHeight() + "\n"
                    + "区域数量: " + numberOfRegions + "\n"
                    + "平均区域大小: " + averageRegionSize);        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void chooseAndDisplayImage() {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("jpg、png类型的图片", "jpg","png");
        fileChooser.setFileFilter(filter);
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                BufferedImage image = ImageIO.read(selectedFile);
                currentImage = image;
                ImageIcon icon = new ImageIcon(image);
                inputImageLabel.setIcon(icon);
                inputImageLabel.setText("");
                this.pack();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "无法加载图片", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

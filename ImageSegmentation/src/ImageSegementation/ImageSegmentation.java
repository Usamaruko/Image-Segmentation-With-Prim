package ImageSegementation;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageSegmentation {
    private static double getGrayValue(BufferedImage image, int x, int y) {
        Color color = new Color(image.getRGB(x, y));
        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();
        return (0.2989 * red + 0.5870 * green + 0.1140 * blue);
    }

    public static BufferedImage convertToGrayImage(BufferedImage originalImage) {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        BufferedImage grayImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                double grayValue = getGrayValue(originalImage, i, j);
                int grayPixel = new Color((int) grayValue, (int) grayValue, (int) grayValue).getRGB();
                grayImage.setRGB(i, j, grayPixel);

            }
        }
        return grayImage;
    }

    public double calculateAutoThreshold(BufferedImage image) {
        double sigmaSquare = calculateVariance(image);
        double sumDiff = 0;
        int count = 0;
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                double grayCur = getGrayValue(image, x, y);
                if (x < image.getWidth() - 1) {
                    double grayRight = getGrayValue(image, x + 1, y);
                    sumDiff += Math.abs(grayCur - grayRight);
                    count++;
                }
                if (y < image.getHeight() - 1) {
                    double grayDown = getGrayValue(image, x, y + 1);
                    sumDiff += Math.abs(grayCur - grayDown);
                    count++;
                }
            }
        }
        double avgDiff = sumDiff / count;
        return Math.exp(-Math.pow(avgDiff, 2) / sigmaSquare);
    }

    private double calculateWeight(double grayValue1, double grayValue2, double sigmaSquare) {
        return Math.exp(-Math.pow((grayValue1 - grayValue2), 2) / sigmaSquare);
    }

    private double calculateVariance(BufferedImage image) {
        long sum = 0;
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                double grayValue = getGrayValue(image, i, j);
                sum += (long) grayValue;
            }
        }
        int totalPixels = image.getWidth() * image.getHeight();
        double average = (double) sum / totalPixels;
        double varianceSum = 0;
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                double grayValue = getGrayValue(image, i, j);
                varianceSum += Math.pow(grayValue - average, 2);
            }
        }
        return varianceSum / totalPixels;
    }


    public Graph imageToGraph(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        Graph graph = new Graph(width, height);
        double sigmaSquare = calculateVariance(image);
        String[] vertices = new String[width * height];
        int vertexIdx = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                vertices[vertexIdx++] = x + "," + y;
            }
        }
        Pixel[] edges = new Pixel[(width - 1) * height + (height - 1) * width];
        int edgeIdx = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int curIdx = x * height + y;
                double grayCur = getGrayValue(image, x, y);
                if (x < width - 1) {
                    int rightIdx = (x + 1) * height + y;
                    double grayRight = getGrayValue(image, x + 1, y);
                    double weight = calculateWeight(grayCur, grayRight, sigmaSquare);
                    edges[edgeIdx++] = new Pixel(curIdx, rightIdx, weight);
                }
                if (y < height - 1) {
                    int downIdx = x * height + (y + 1);
                    double grayDown = getGrayValue(image, x, y + 1);
                    double weight = calculateWeight(grayCur, grayDown, sigmaSquare);
                    edges[edgeIdx++] = new Pixel(curIdx, downIdx, weight);
                }
            }
        }
        graph.createAdjGraphClass(vertices, edges);
        return graph;
    }

    public BufferedImage visualizeRegions(BufferedImage originalImage, int[][] regions) {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        BufferedImage segmentedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Color blackColor = Color.BLACK;
        Color whiteColor = Color.WHITE;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (hasAdjacentRegionWithDifferentId(x, y, regions)) {
                    segmentedImage.setRGB(x, y, blackColor.getRGB());
                } else {
                    segmentedImage.setRGB(x, y, whiteColor.getRGB());
                }
            }
        }
        return segmentedImage;
    }

    private boolean hasAdjacentRegionWithDifferentId(int x, int y, int[][] regions) {
        int regionId = regions[x][y];
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int[] dir : directions) {
            int newX = x + dir[0];
            int newY = y + dir[1];
            if (newX >= 0 && newX < regions.length && newY >= 0 && newY < regions[0].length) {
                if (regions[newX][newY] != regionId) {
                    return true;
                }
            }
        }
        return false;
    }

    public int[][] mergeSmallRegions(Graph graph, BufferedImage image, int sizeThreshold) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[][] regions = new int[width][height];
        int[] regionSizes = new int[graph.numberOfVertices];
        for (int i = 0; i < graph.numberOfVertices; i++) {
            int x = i / height;
            int y = i % height;
            regions[x][y] = graph.findRegion(i);
            regionSizes[regions[x][y]]++;
        }
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int regionId = regions[x][y];
                if (regionSizes[regionId] < sizeThreshold) {
                    int largerRegionId = findLargerAdjacentRegionId(x, y, regions, regionSizes, width, height);
                    regions[x][y] = largerRegionId;
                    regionSizes[regionId]--;
                    regionSizes[largerRegionId]++;
                }
            }
        }
        return regions;
    }

    private int findLargerAdjacentRegionId(int x, int y, int[][] regions, int[] regionSizes, int width, int height) {
        int largestRegionId = regions[x][y];
        int largestSize = regionSizes[largestRegionId];
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int[] dir : directions) {
            int newX = x + dir[0];
            int newY = y + dir[1];
            if (newX >= 0 && newX < width && newY >= 0 && newY < height) {
                int neighborRegionId = regions[newX][newY];
                int neighborSize = regionSizes[neighborRegionId];
                if (neighborSize > largestSize) {
                    largestRegionId = neighborRegionId;
                    largestSize = neighborSize;
                }
            }
        }
        return largestRegionId;
    }
}

## Image Segmentation using Prim's Algorithm

This Java program implements image segmentation using Prim's algorithm, employing a network-like structure to assist in generating segmentation results, particularly suited for images with clear contrasts. It consists of three main components: Graph, ImageSegmentation, and ImageSegmentationGUI.

The Graph class represents the underlying graph structure used in Prim's algorithm, responsible for creating the graph, executing Prim's algorithm, and merging regions.

The ImageSegmentation class provides methods for converting an image to a graph, performing segmentation, and visualizing regions. It includes functionalities such as calculating grayscale values, converting images to grayscale, determining automatic threshold values, calculating weights, variance, converting images to graphs, visualizing regions, and merging small regions.

The ImageSegmentationGUI class is the graphical user interface component, facilitating user interaction with the image segmentation process. It allows users to load images, configure segmentation parameters, and visualize the segmented regions in real-time.

这个Java程序利用Prim算法实现图像分割，采用类似网络的结构辅助生成分割结果，特别适用于对比度明显的图像。它包括三个主要组件：Graph（图）、ImageSegmentation（图像分割）和ImageSegmentationGUI（图像分割图形用户界面）。

Graph类表示Prim算法中使用的基础图结构，负责创建图、执行Prim算法和合并区域。

ImageSegmentation类提供了将图像转换为图形、执行分割和可视化区域等方法。它包括计算灰度值、将图像转换为灰度图像、确定自动阈值、计算权重和方差、将图像转换为图形、可视化区域以及合并小区域等功能。

ImageSegmentationGUI类是图形用户界面组件，方便用户与图像分割过程进行交互。它允许用户加载图像、配置分割参数，并实时可视化分割后的区域。

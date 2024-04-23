package ImageSegementation;

public class Graph {
    HeaderNode[] adjacencyList;
    int numberOfVertices, numberOfEdges;
    int[] visited;
    int[] region;
    private boolean[] inMST;//标记是否在最小生成树里面
    private PriorityQueue minEdgeQueue;//存储边的优先队列，用于 Prim 算法

    public Graph(int width, int height) {
        numberOfVertices = width * height;
        adjacencyList = new HeaderNode[numberOfVertices];
        region = new int[numberOfVertices];
        for (int i = 0; i < numberOfVertices; i++) {
            adjacencyList[i] = new HeaderNode();
            region[i] = i;
        }
    }

    // 合并两个顶点的区域
    public void mergeRegions(int vertex1, int vertex2) {
        int region1 = findRegion(vertex1);
        int region2 = findRegion(vertex2);
        if (region1 != region2) {
            region[region1] = region2;
        }
    }

    // 查找顶点的区域
    public int findRegion(int vertex) {
        if (region[vertex] != vertex) {
            region[vertex] = findRegion(region[vertex]);
        }
        return region[vertex];
    }

    public void createAdjGraphClass(String[] vertices, Pixel[] edges) {
        Edge edge;
        numberOfVertices = vertices.length;
        numberOfEdges = 2 * edges.length;
        visited = new int[numberOfVertices];
        for (int i = 0; i < numberOfVertices; i++) {
            adjacencyList[i] = new HeaderNode(vertices[i]);
            adjacencyList[i].firstEdge = null;
        }
        for (Pixel pixel : edges) {
            int x = pixel.x;
            int y = pixel.y;
            double weight = pixel.grayValue;
            edge = new Edge();
            edge.terminalPoint = new Pixel(y, x, weight);
            edge.weight = weight;
            edge.nextEdge = adjacencyList[x].firstEdge;
            adjacencyList[x].firstEdge = edge;
            edge = new Edge();
            edge.terminalPoint = new Pixel(x, y, weight);
            edge.weight = weight;
            edge.nextEdge = adjacencyList[y].firstEdge;
            adjacencyList[y].firstEdge = edge;
        }
    }

    public void Prim(int startVertex, double threshold) {
        inMST = new boolean[numberOfVertices];
        minEdgeQueue = new PriorityQueue(numberOfEdges);
        for (int i = 0; i < numberOfVertices; i++) {
            inMST[i] = false;
        }
        inMST[startVertex] = true;
        Edge currentEdge = adjacencyList[startVertex].firstEdge;
        while (currentEdge != null) {
            if (currentEdge.weight > threshold) {
                minEdgeQueue.push(currentEdge);
            }
            currentEdge = currentEdge.nextEdge;
        }
        while (!minEdgeQueue.isEmpty()) {
            Edge minEdge = minEdgeQueue.poll();
            if (!inMST[minEdge.terminalPoint.x] || !inMST[minEdge.terminalPoint.y]) {
                //这里的 x 和 y 并不是坐标点，而是用于表示两个不同的顶点。因为我已经检查一条边的两个端点是否已经在最小生成树中了，所以是不同的顶点
                inMST[minEdge.terminalPoint.x] = true;
                inMST[minEdge.terminalPoint.y] = true;
                mergeRegions(minEdge.terminalPoint.x, minEdge.terminalPoint.y);
                addEdgesToQueue(minEdge.terminalPoint.x, threshold);
                addEdgesToQueue(minEdge.terminalPoint.y, threshold);
            }
        }
    }

    private void addEdgesToQueue(int vertex, double threshold) {
        Edge edge = adjacencyList[vertex].firstEdge;
        while (edge != null) {
            if (edge.weight > threshold && (!inMST[edge.terminalPoint.x] || !inMST[edge.terminalPoint.y])) {
                minEdgeQueue.push(edge);
            }
            edge = edge.nextEdge;
        }
    }

}

class Pixel {
    int x;
    int y;
    double grayValue;

    public Pixel(int x, int y, double grayValue) {
        this.x = x;
        this.y = y;
        this.grayValue = grayValue;
    }
}

//边结点类
class Edge implements Comparable<Edge> {
    Pixel terminalPoint;
    Edge nextEdge;
    double weight;

    public int compareTo(Edge another) {
        return Double.compare(this.weight, another.weight);
    }
}

//头结点类
class HeaderNode {
    String data;
    Edge firstEdge;

    public HeaderNode() {
    }

    public HeaderNode(String data) {
        this.data = data;
    }
}
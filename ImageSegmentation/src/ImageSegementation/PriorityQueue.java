package ImageSegementation;

public class PriorityQueue {
    private Edge[] data;
    private int size;
    private int capacity;

    public PriorityQueue(int capacity) {
        this.capacity = capacity;
        this.data = new Edge[capacity];
        this.size = 0;
    }

    public void push(Edge edge) {
        if (size == capacity) {
            expandData();
        }
        data[size] = edge;
        siftUp(size);
        size++;
    }

    public Edge poll() {
        if (size == 0) {
            return null;
        }
        Edge minEdge = data[0];
        data[0] = data[size - 1];
        size--;
        return minEdge;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    private void siftUp(int index) {
        while (index > 0) {
            int parentIndex = (index - 1) / 2;
            if (data[index].compareTo(data[parentIndex]) >= 0) {
                break;
            }
            swap(index, parentIndex);
            index = parentIndex;
        }
    }

    private void swap(int i, int j) {
        Edge temp = data[i];
        data[i] = data[j];
        data[j] = temp;
    }

    private void expandData() {
        capacity = capacity * 2;
        Edge[] newData = new Edge[capacity];
        System.arraycopy(data, 0, newData, 0, data.length);
        data = newData;
    }
}
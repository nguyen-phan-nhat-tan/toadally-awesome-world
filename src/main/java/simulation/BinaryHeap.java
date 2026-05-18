package simulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * A binary heap implementation of AdjustablePriorityQueue.
 * 
 * This implementation maintains a min-heap structure along with a HashMap for
 * efficient lookups. The time complexities are:
 * - insert: O(log n)
 * - extractMin: O(log n)
 * - peekMin: O(1)
 * - updatePriority: O(log n)
 * - getPriority: O(1)
 * - contains: O(1)
 * - size: O(1)
 * - clear: O(n)
 *
 * @param <E> the type of elements in this queue
 */
public class BinaryHeap<E> implements AdjustablePriorityQueue<E> {
    
    /**
     * Helper class to pair an element with its priority.
     */
    private static class HeapEntry<E> {
        E element;
        int priority;

        HeapEntry(E element, int priority) {
            this.element = element;
            this.priority = priority;
        }
    }

    private final List<HeapEntry<E>> heap;
    private final Map<E, Integer> elementIndex; // Maps element to its index in heap

    /**
     * Creates an empty binary heap.
     */
    public BinaryHeap() {
        this.heap = new ArrayList<>();
        this.elementIndex = new HashMap<>();
    }

    @Override
    public boolean insert(E element, int priority) {
        // Check if element already exists
        if (elementIndex.containsKey(element)) {
            return false;
        }

        // Add to heap
        int index = heap.size();
        heap.add(new HeapEntry<>(element, priority));
        elementIndex.put(element, index);

        // Bubble up
        bubbleUp(index);

        return true;
    }

    @Override
    public E extractMin() {
        if (heap.isEmpty()) {
            return null;
        }

        HeapEntry<E> minEntry = heap.get(0);
        E minElement = minEntry.element;

        // Remove element from mapping
        elementIndex.remove(minElement);

        // Move last element to root
        if (heap.size() > 1) {
            HeapEntry<E> lastEntry = heap.remove(heap.size() - 1);
            heap.set(0, lastEntry);
            elementIndex.put(lastEntry.element, 0);
            bubbleDown(0);
        } else {
            heap.remove(0);
        }

        return minElement;
    }

    @Override
    public E peekMin() {
        if (heap.isEmpty()) {
            return null;
        }
        return heap.get(0).element;
    }

    @Override
    public boolean updatePriority(E element, int newPriority) {
        if (!elementIndex.containsKey(element)) {
            return false;
        }

        int index = elementIndex.get(element);
        HeapEntry<E> entry = heap.get(index);
        int oldPriority = entry.priority;

        entry.priority = newPriority;

        if (newPriority < oldPriority) {
            // Priority decreased (higher priority), bubble up
            bubbleUp(index);
        } else if (newPriority > oldPriority) {
            // Priority increased (lower priority), bubble down
            bubbleDown(index);
        }
        // If priority is unchanged, do nothing

        return true;
    }

    @Override
    public int getPriority(E element) {
        if (!elementIndex.containsKey(element)) {
            return Integer.MAX_VALUE;
        }
        int index = elementIndex.get(element);
        return heap.get(index).priority;
    }

    @Override
    public boolean contains(Object element) {
        return elementIndex.containsKey(element);
    }

    @Override
    public int size() {
        return heap.size();
    }

    @Override
    public void clear() {
        heap.clear();
        elementIndex.clear();
    }

    /**
     * Moves an element up the heap to restore heap property.
     * Time complexity: O(log n)
     */
    private void bubbleUp(int index) {
        while (index > 0) {
            int parentIndex = (index - 1) / 2;
            HeapEntry<E> entry = heap.get(index);
            HeapEntry<E> parentEntry = heap.get(parentIndex);

            if (entry.priority >= parentEntry.priority) {
                break;
            }

            // Swap
            swap(index, parentIndex);
            index = parentIndex;
        }
    }

    /**
     * Moves an element down the heap to restore heap property.
     * Time complexity: O(log n)
     */
    private void bubbleDown(int index) {
        while (true) {
            int minIndex = index;
            int leftChild = 2 * index + 1;
            int rightChild = 2 * index + 2;

            if (leftChild < heap.size()
                    && heap.get(leftChild).priority < heap.get(minIndex).priority) {
                minIndex = leftChild;
            }

            if (rightChild < heap.size()
                    && heap.get(rightChild).priority < heap.get(minIndex).priority) {
                minIndex = rightChild;
            }

            if (minIndex == index) {
                break;
            }

            swap(index, minIndex);
            index = minIndex;
        }
    }

    /**
     * Swaps two elements in the heap and updates the index map.
     * Time complexity: O(1)
     */
    private void swap(int i, int j) {
        HeapEntry<E> entry1 = heap.get(i);
        HeapEntry<E> entry2 = heap.get(j);

        heap.set(i, entry2);
        heap.set(j, entry1);

        elementIndex.put(entry1.element, j);
        elementIndex.put(entry2.element, i);
    }

    // Collection interface methods

    @Override
    public boolean add(E e) {
        return insert(e, Integer.MAX_VALUE);
    }

    @Override
    public boolean remove(Object o) {
        if (!elementIndex.containsKey(o)) {
            return false;
        }

        @SuppressWarnings("unchecked")
        E element = (E) o;

        int index = elementIndex.get(element);
        elementIndex.remove(element);

        if (index == heap.size() - 1) {
            heap.remove(index);
            return true;
        }

        // Move last element to removed position
        HeapEntry<E> lastEntry = heap.remove(heap.size() - 1);
        heap.set(index, lastEntry);
        elementIndex.put(lastEntry.element, index);

        // Restore heap property
        int parentIndex = (index - 1) / 2;
        if (index > 0 && lastEntry.priority < heap.get(parentIndex).priority) {
            bubbleUp(index);
        } else {
            bubbleDown(index);
        }

        return true;
    }

    @Override
    public boolean isEmpty() {
        return heap.isEmpty();
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {
            private final Iterator<HeapEntry<E>> iter = new ArrayList<>(heap).iterator();

            @Override
            public boolean hasNext() {
                return iter.hasNext();
            }

            @Override
            public E next() {
                return iter.next().element;
            }
        };
    }

    @Override
    public Object[] toArray() {
        Object[] result = new Object[heap.size()];
        for (int i = 0; i < heap.size(); i++) {
            result[i] = heap.get(i).element;
        }
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        if (a.length < heap.size()) {
            a = (T[]) new Object[heap.size()];
        }
        for (int i = 0; i < heap.size(); i++) {
            a[i] = (T) heap.get(i).element;
        }
        if (a.length > heap.size()) {
            a[heap.size()] = null;
        }
        return a;
    }

    @Override
    public boolean containsAll(java.util.Collection<?> c) {
        for (Object o : c) {
            if (!contains(o)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(java.util.Collection<? extends E> c) {
        boolean changed = false;
        for (E e : c) {
            if (add(e)) {
                changed = true;
            }
        }
        return changed;
    }

    @Override
    public boolean removeAll(java.util.Collection<?> c) {
        boolean changed = false;
        for (Object o : c) {
            if (remove(o)) {
                changed = true;
            }
        }
        return changed;
    }

    @Override
    public boolean retainAll(java.util.Collection<?> c) {
        boolean changed = false;
        for (Iterator<E> iter = iterator(); iter.hasNext();) {
            E e = iter.next();
            if (!c.contains(e)) {
                remove(e);
                changed = true;
            }
        }
        return changed;
    }
}

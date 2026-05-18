package simulation;

/**
 * An adjustable priority queue that supports efficient updates to element priorities.
 * 
 * This interface extends PriorityQueue to add the ability to update the priority
 * of an element that is already in the queue. This is useful for algorithms like
 * Dijkstra's shortest path that need to update priorities during execution.
 *
 * @param <E> the type of elements in this queue
 */
public interface AdjustablePriorityQueue<E> extends java.util.Collection<E> {
    
    /**
     * Inserts an element with a given priority into the queue.
     * 
     * @param element the element to insert
     * @param priority the priority value (lower values have higher priority)
     * @return true if the element was added successfully
     * 
     * Time complexity: O(log n)
     */
    boolean insert(E element, int priority);
    
    /**
     * Removes and returns the element with the minimum priority.
     * 
     * @return the element with minimum priority, or null if queue is empty
     * 
     * Time complexity: O(log n)
     */
    E extractMin();
    
    /**
     * Returns the element with minimum priority without removing it.
     * 
     * @return the element with minimum priority, or null if queue is empty
     * 
     * Time complexity: O(1)
     */
    E peekMin();
    
    /**
     * Updates the priority of an element already in the queue.
     * 
     * @param element the element whose priority to update
     * @param newPriority the new priority value
     * @return true if the element was found and updated, false otherwise
     * 
     * Time complexity: O(log n)
     */
    boolean updatePriority(E element, int newPriority);
    
    /**
     * Returns the priority of an element in the queue.
     * 
     * @param element the element to query
     * @return the priority of the element, or Integer.MAX_VALUE if not found
     * 
     * Time complexity: O(1) expected
     */
    int getPriority(E element);
    
    /**
     * Checks if a specific element is in the queue.
     * 
     * @param element the element to check
     * @return true if the element is in the queue, false otherwise
     * 
     * Time complexity: O(1) expected
     */
    boolean contains(Object element);
    
    /**
     * Returns the current number of elements in the queue.
     * 
     * @return the number of elements
     * 
     * Time complexity: O(1)
     */
    int size();
    
    /**
     * Removes all elements from the queue.
     * 
     * Time complexity: O(n)
     */
    void clear();
}

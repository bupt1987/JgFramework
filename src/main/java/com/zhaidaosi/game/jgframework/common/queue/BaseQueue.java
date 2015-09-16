package com.zhaidaosi.game.jgframework.common.queue;

/**
 * 可快速定位的FIFO队列
 */
public class BaseQueue<E> {

    private BaseQueueElement<E> start = null;
    private BaseQueueElement<E> end = null;
    private final Object lock = new Object();
    private long putCount = 0;
    private long takeCount = 0;
    private long size = 0;

    /**
     * 获取队列头
     */
    public BaseQueueElement<E> getStart() {
        return start;
    }

    /**
     * 从队列尾插入元素
     */
    public BaseQueueElement<E> put(E value) {
        if (value == null) {
            return null;
        }
        BaseQueueElement<E> element = new BaseQueueElement<E>(value);
        synchronized (lock) {
            if (start == null) {
                start = element;
                end = element;
            } else {
                element.setBefore(end);
                end.setNext(element);
                end = element;
            }
            putCount++;
            size++;
            element.setNo(putCount);
        }
        return element;
    }

    /**
     * 从队列头弹出元素
     */
    public BaseQueueElement<E> take() {
        if (start == null) {
            return null;
        }
        BaseQueueElement<E> element;
        synchronized (lock) {
            element = start;
            if (start == end) {
                start = null;
                end = null;
                putCount = 0;
                takeCount = 0;
            } else {
                start = start.getNext();
                start.setBefore(null);
                takeCount++;
            }
            size--;
            element.reset();
        }
        return element;
    }

    /**
     * 删除一个元素
     */
    public boolean remove(BaseQueueElement<E> element) {
        BaseQueueElement<E> after = null;
        synchronized (lock) {
            if (element == start && element == end) {
                start = null;
                end = null;
                putCount = 0;
                takeCount = 0;
            } else if (element == start) {
                start = start.getNext();
                start.setBefore(null);
                takeCount++;
            } else if (element == end) {
                end = element.getBefore();
                end.setNext(null);
            } else {
                // 判断是否在队列中
                if (element.getBefore().getNext() != element || element.getNext().getBefore() != element) {
                    return false;
                }
                after = element.getNext();
                after.setBefore(element.getBefore());
                element.getBefore().setNext(after);
            }
            size--;
            element.reset();
            if (after != null) {
                do {
                    after.setNo(after.getNo() - 1);
                    after = after.getNext();
                } while (after != null);
            }
        }
        return true;
    }

    /**
     * 查找元素所在的位置
     */
    public long findIndex(BaseQueueElement<E> element) {
        if (element == null) {
            return -1;
        }
        synchronized (lock) {
            return element.getNo() - takeCount;
        }
    }

    /**
     * 返回队列长度
     */
    public long size() {
        synchronized (lock) {
            return size;
        }
    }

    /**
     * 删除队列所有元素
     */
    public void clear() {
        synchronized (lock) {
            while (start != null) {
                BaseQueueElement<E> element = start;
                start = start.getNext();
                element.reset();
                element = null;
            }
            start = null;
            end = null;
            putCount = 0;
            takeCount = 0;
            size = 0;
        }
    }

}

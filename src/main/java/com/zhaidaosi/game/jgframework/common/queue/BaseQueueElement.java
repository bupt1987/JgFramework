package com.zhaidaosi.game.jgframework.common.queue;

public class BaseQueueElement<E> {

    private E value;
    private BaseQueueElement<E> before;
    private BaseQueueElement<E> next;
    private long No;

    public BaseQueueElement(E value) {
        this.value = value;
    }

    public void setValue(E value) {
        this.value = value;
    }

    public E getValue() {
        return value;
    }

    public BaseQueueElement<E> getBefore() {
        return before;
    }

    public void setBefore(BaseQueueElement<E> before) {
        this.before = before;
    }

    public BaseQueueElement<E> getNext() {
        return next;
    }

    public void setNext(BaseQueueElement<E> next) {
        this.next = next;
    }

    public void setNo(long No) {
        this.No = No;
    }

    public long getNo() {
        return No;
    }

    public void reset() {
        before = null;
        next = null;
        No = 0;
    }

}

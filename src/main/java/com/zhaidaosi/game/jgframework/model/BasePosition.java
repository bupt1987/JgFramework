package com.zhaidaosi.game.jgframework.model;

import com.zhaidaosi.game.jgframework.model.area.IBaseArea;

public class BasePosition {

    private int x;
    private int y;
    private int z;
    private IBaseArea area;

    public BasePosition(IBaseArea area) {
        this.area = area;
    }

    public BasePosition(int x, int y, int z, IBaseArea area) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.area = area;
    }

    public BasePosition(int x, int y, IBaseArea area) {
        this.x = x;
        this.y = y;
        this.area = area;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public IBaseArea getArea() {
        return area;
    }

    public void setArea(IBaseArea area) {
        this.area = area;
    }

}

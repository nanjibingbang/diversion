package com.liou.test.entity;

import java.io.Serializable;

/**
 * Content :
 *
 * @author liou 2018-01-24.
 */
public class Param implements Serializable {
    private String str;
    private double i;
    private char c;
    private boolean b;

    public Param(String str, double i, char c, boolean b) {
        this.str = str;
        this.i = i;
        this.c = c;
        this.b = b;
    }

    public String getStr() {
        return str;
    }

    public void setStr(String str) {
        this.str = str;
    }

    public double getI() {
        return i;
    }

    public void setI(double i) {
        this.i = i;
    }

    public char getC() {
        return c;
    }

    public void setC(char c) {
        this.c = c;
    }

    public boolean isB() {
        return b;
    }

    public void setB(boolean b) {
        this.b = b;
    }
}

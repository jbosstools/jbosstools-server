/*******************************************************************************
 * Copyright (c) 2007 Jeff Mesnil
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.jboss.tools.jmx.ui.test.interactive;

import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;

public class WritableAttributes extends StandardMBean implements
        WritableAttributesMBean {

    private boolean booleanValue = true;

    private byte byteValue = 0;

    private char charValue = 'a';

    private short shortValue = 0;

    private int intValue = 0;

    private long longValue = 0;

    private float floatValue = 0;

    private double doubleValue = 0;

    private String stringWithNewlines = "first line\nsecond line\nthird line"; //$NON-NLS-1$

    public WritableAttributes() throws NotCompliantMBeanException {
        super(WritableAttributesMBean.class);
    }

    public boolean isBoolean() {
        return booleanValue;
    }

    public void setBoolean(boolean value) {
        this.booleanValue = value;
    }

    public byte getByte() {
        return byteValue;
    }

    public void setByte(byte value) {
        this.byteValue = value;
    }

    public char getChar() {
        return charValue;
    }

    public void setChar(char value) {
        this.charValue = value;
    }

    public short getShort() {
        return shortValue;
    }

    public void setShort(short value) {
        this.shortValue = value;
    }

    public int getInt() {
        return intValue;
    }

    public void setInt(int value) {
        this.intValue = value;
    }

    public long getLong() {
        return longValue;
    }

    public void setLong(long value) {
        this.longValue = value;
    }

    public float getFloat() {
        return floatValue;
    }

    public void setFloat(float value) {
        this.floatValue = value;
    }

    public double getDouble() {
        return doubleValue;
    }

    public void setDouble(double value) {
        this.doubleValue = value;
    }

    public String getStringWithNewlines() {
        return stringWithNewlines;
    }

    public void setStringWithNewlines(String value) {
        this.stringWithNewlines = value;
    }
}

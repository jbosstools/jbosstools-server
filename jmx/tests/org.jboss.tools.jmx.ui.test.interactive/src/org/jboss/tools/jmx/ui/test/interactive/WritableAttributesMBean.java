/*******************************************************************************
 * Copyright (c) 2007 Jeff Mesnil
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.jboss.tools.jmx.ui.test.interactive;

public interface WritableAttributesMBean {
    boolean isBoolean();

    void setBoolean(boolean value);

    byte getByte();

    void setByte(byte value);

    char getChar();

    void setChar(char value);

    short getShort();

    void setShort(short value);

    int getInt();

    void setInt(int value);

    long getLong();

    void setLong(long value);

    float getFloat();

    void setFloat(float value);

    double getDouble();

    void setDouble(double value);

    String getStringWithNewlines();

    void setStringWithNewlines(String value);
}

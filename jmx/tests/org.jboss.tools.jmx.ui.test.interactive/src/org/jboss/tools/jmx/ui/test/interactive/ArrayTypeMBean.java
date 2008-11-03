/*******************************************************************************
 * Copyright (c) 2006 Jeff Mesnil
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.jboss.tools.jmx.ui.test.interactive;

public interface ArrayTypeMBean {

    // attributes
    boolean[] getBooleans();

    byte[] getBytes();

    char[] getChars();

    short[] getShorts();

    int[] getInts();

    long[] getLongs();

    float[] getFloats();

    double[] getDoubles();

    String[] getStrings();

    Object[] getObjects();

    Object[][] getMultiDimensionalObjects();

    // operations
    boolean[] booleansOp(boolean[] b);

    byte[] bytesOp(byte[] b);

    char[] charsOp(char[] c);

    short[] shortsOp(short[] s);

    int[] intsOp(int[] i);

    long[] longsOp(long[] l);

    float[] floatsOp(float[] f);

    double[] doublesOp(double[] d);

    Object[] objectsOp(Object[] o);

    Object[][] multiDimensionalObjectsOp(Object[][] o);

}

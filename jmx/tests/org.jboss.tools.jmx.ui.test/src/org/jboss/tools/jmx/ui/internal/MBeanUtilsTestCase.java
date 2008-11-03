/*******************************************************************************
 * Copyright (c) 2007 Jeff Mesnil
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.jboss.tools.jmx.ui.internal;

import org.jboss.tools.jmx.ui.internal.MBeanUtils;

import junit.framework.TestCase;

public class MBeanUtilsTestCase extends TestCase {

    public void testNullValue() throws Exception {
        assertNull(MBeanUtils.getValue(null, "whatever")); //$NON-NLS-1$
        assertNull(MBeanUtils.getValue("whatever", null)); //$NON-NLS-1$
        assertNull(MBeanUtils.getValue(null, null));
    }

    public void testNonPrimitiveType() throws Exception {
        String value = "any value"; //$NON-NLS-1$
        assertEquals(value, MBeanUtils.getValue(value, "java.util.Vector")); //$NON-NLS-1$
    }

    public void testBooleanValue() throws Exception {
        assertEquals(Boolean.TRUE, MBeanUtils.getValue("true", "boolean")); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(Boolean.FALSE, MBeanUtils.getValue("false", "boolean")); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(Boolean.FALSE, MBeanUtils.getValue("whatever", "boolean")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public void testByteValue() throws Exception {
        assertEquals((byte) 0, MBeanUtils.getValue("0", "byte")); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals((byte) 1, MBeanUtils.getValue("1", "byte")); //$NON-NLS-1$ //$NON-NLS-2$
        try {
            MBeanUtils.getValue("whatever", "byte"); //$NON-NLS-1$ //$NON-NLS-2$
            fail();
        } catch (NumberFormatException e) {

        }
    }

    public void testCharValue() throws Exception {
        assertEquals('a', MBeanUtils.getValue("a", "char")); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals('o', MBeanUtils.getValue("only take the first char", //$NON-NLS-1$
                "char")); //$NON-NLS-1$
    }

    public void testShortValue() throws Exception {
        assertEquals((short) 1, MBeanUtils.getValue("1", "short")); //$NON-NLS-1$ //$NON-NLS-2$
        try {
            MBeanUtils.getValue("not a short", "short"); //$NON-NLS-1$ //$NON-NLS-2$
            fail();
        } catch (NumberFormatException e) {

        }
    }

    public void testIntValue() throws Exception {
        assertEquals(1, MBeanUtils.getValue("1", "int")); //$NON-NLS-1$ //$NON-NLS-2$
        try {
            MBeanUtils.getValue("not a int", "int"); //$NON-NLS-1$ //$NON-NLS-2$
            fail();
        } catch (NumberFormatException e) {

        }
    }

    public void testLongValue() throws Exception {
        assertEquals((long) 1, MBeanUtils.getValue("1", "long")); //$NON-NLS-1$ //$NON-NLS-2$
        try {
            MBeanUtils.getValue("not a long", "long"); //$NON-NLS-1$ //$NON-NLS-2$
            fail();
        } catch (NumberFormatException e) {

        }
    }

    public void testFloatValue() throws Exception {
        assertEquals(1.0f, MBeanUtils.getValue("1", "float")); //$NON-NLS-1$ //$NON-NLS-2$
        try {
            MBeanUtils.getValue("not a float", "float"); //$NON-NLS-1$ //$NON-NLS-2$
            fail();
        } catch (NumberFormatException e) {

        }
    }

    public void testDoubleValue() throws Exception {
        assertEquals(1.0, MBeanUtils.getValue("1", "double")); //$NON-NLS-1$ //$NON-NLS-2$
        try {
            MBeanUtils.getValue("not a double", "double"); //$NON-NLS-1$ //$NON-NLS-2$
            fail();
        } catch (NumberFormatException e) {

        }
    }

}

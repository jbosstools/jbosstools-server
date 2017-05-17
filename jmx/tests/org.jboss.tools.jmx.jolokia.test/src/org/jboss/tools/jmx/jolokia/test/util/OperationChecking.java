/*
 * Copyright 2009-2013 Roland Huss
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.tools.jmx.jolokia.test.util;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.management.*;
import javax.management.openmbean.TabularData;

/**
 * @author roland
 * @since Jun 30, 2009
 */
public class OperationChecking implements OperationCheckingMBean,MBeanRegistration {

    private int counter = 0;
    private String domain;

    public OperationChecking(String pDomain) {
        domain = pDomain;
    }

    @Override
	public void reset() {
        counter = 0;
    }
    
    @Override
    public int fetchNumber(String arg) {
        if ("inc".equals(arg)) {
            return counter++;
        } else {
            throw new IllegalArgumentException("Invalid arg " + arg);
        }
    }

    @Override
    public boolean nullArgumentCheck(String arg1,Object arg2) {
        return arg1 == null && arg2 == null;
    }

    @Override
    public boolean emptyStringArgumentCheck(String arg1) {
        return arg1 != null && arg1.length() == 0;
    }

    @Override
    public String arrayArguments(String[] args, String extra) {
        return args[0];
    }

    @Override
    public Object objectArrayArg(Object[] args) {
        if (args == null) {
            return null;
        } else {
            return args[0];
        }
    }

    @Override
    public Object listArgument(List arg) {
        if (arg == null) {
            return null;
        }
        return arg.get(0);
    }

    @Override
    @SuppressWarnings("squid:S2447")
    public Boolean booleanArguments(boolean arg1, Boolean arg2) {
        if (arg2 == null) {
            return null;
        }
        return arg1 && arg2;
    }

    @Override
    public Map mapArgument(Map arg) {
        return arg;
    }

    @Override
    public int intArguments(int arg1, Integer arg2) {
        if (arg2 == null) {
            return -1;
        }
        return arg1 + arg2;
    }

    @Override
    public double doubleArguments(double arg1, Double arg2) {
        if (arg2 == null) {
            return -1.0;
        }
        return arg1 + arg2;
    }

    @Override
    public BigDecimal addBigDecimal(int first,BigDecimal second) {
        return second.add(BigDecimal.valueOf(first));
    }

    @Override
    public TabularData update(String name, TabularData data) {
        return data;
    }

    @Override
    public int overloadedMethod() {
        return 0;
    }

    @Override
    public int overloadedMethod(String arg) {
        return 1;
    }

    @Override
    public int overloadedMethod(String arg, int arg2) {
        return 2;
    }

    @Override
    public int overloadedMethod(String[] arg) {
        return 3;
    }

    @Override
    public ObjectName preRegister(MBeanServer server, ObjectName name) throws Exception {
        return new ObjectName(domain + ":type=operation");

    }

    @Override
    public void postRegister(Boolean registrationDone) {
    	/* Do nothing */
    }

    @Override
    public void preDeregister() throws Exception {
    	/* Do nothing */
    }

    @Override
    public void postDeregister() {
    	/* Do nothing */
    }

    @Override
    public String echo(String pEcho) {
        return pEcho;
    }

    @Override
    public TimeUnit findTimeUnit(TimeUnit unit) {
        return unit;
    }

	@Override
    public Set<String> setOfResult() {
    	return new HashSet<String>(Arrays.asList("value1", "value2"));
    }
}

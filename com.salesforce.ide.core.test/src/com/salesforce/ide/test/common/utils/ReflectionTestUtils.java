/*******************************************************************************
 * Copyright (c) 2014 Salesforce.com, inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Salesforce.com, inc. - initial API and implementation
 ******************************************************************************/
package com.salesforce.ide.test.common.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectionTestUtils {

	public static Method findMethodInClass(Object instance, String methodName,
			Class<?>[] params) throws NoSuchMethodException {
		Method method = findMethodInClass(instance.getClass(), methodName,
				params);
		method.setAccessible(true);
		return method;
	}

	private static Method findMethodInClass(Class<?> clazz, String methodName,
			Class<?>[] params) throws NoSuchMethodException {
		try {
			return clazz.getDeclaredMethod(methodName, params);
		}

		catch (NoSuchMethodException e) {
			// if we don't have a superclass, continue
			if (clazz.getSuperclass().equals(Object.class)) {
				throw e;
			}

			// check in the superclass
			return findMethodInClass(clazz.getSuperclass(), methodName, params);
		}
	}

	@SuppressWarnings( { "unchecked" })
	public static <T> T executeMethodInClass(Object instance,
			String methodName, Class<?>[] params, Object[] values)
			throws Exception {
		Method method = findMethodInClass(instance, methodName, params);
		Object o = method.invoke(instance, values);

		return (T) o;
	}

	@SuppressWarnings( { "unchecked" })
	public static <T> T findFieldInClass(Object instance, String fieldName)
			throws NoSuchFieldException {
		Field field = findFieldInClass(instance.getClass(), fieldName);
		field.setAccessible(true);

		try {
			return (T) field.get(instance);
		} catch (IllegalAccessException e) {
			throw new NoSuchFieldException();
		}
	}

	private static Field findFieldInClass(Class<?> clazz, String fieldName)
			throws NoSuchFieldException {
		try {
			return clazz.getDeclaredField(fieldName);
		}

		catch (NoSuchFieldException e) {
			// if we don't have a superclass, continue
			if (clazz.getSuperclass().equals(Object.class)) {
				throw e;
			}

			// check in the superclass
			return findFieldInClass(clazz.getSuperclass(), fieldName);
		}
	}
}

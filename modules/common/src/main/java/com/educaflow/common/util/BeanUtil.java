package com.educaflow.common.util;

import java.lang.reflect.Method;

public class BeanUtil {
    private static Method getSetMethod(Class<?> clazz, String nombrePropiedad) {
        try {
            String nombreMetodo = "set" + TextUtil.toFirstsLetterToUpperCase(nombrePropiedad);
            Method method=ReflectionUtil.getMethod(clazz, nombreMetodo, null, null, null);
            if (method.getParameterTypes().length != 1) {
                throw new RuntimeException("El método set solo puede tener un parámetro de entrada."+method.getParameterTypes().length + " " + nombrePropiedad + " en la clase " + clazz.getName());
            }
            return method;
        } catch (Exception ex2) {
            throw new RuntimeException("No se encontró el métodoset en " + nombrePropiedad + " en la clase " + clazz.getName(), ex2);
        }
    }

    private static Method getGetMethod(Class<?> clazz, String nombrePropiedad) {
        try {
            String nombreMetodo = "get" + TextUtil.toFirstsLetterToUpperCase(nombrePropiedad);
            Method method=ReflectionUtil.getMethod(clazz, nombreMetodo, null, null, null);
            if (method.getParameterTypes().length != 0) {
                throw new RuntimeException("El método get no puede tener parámetros de entrada."+method.getParameterTypes().length + " "+ nombrePropiedad + " en la clase " + clazz.getName());
            }
            return method;
        } catch (Exception ex) {
            try {
                String nombreMetodo = "is" + TextUtil.toFirstsLetterToUpperCase(nombrePropiedad);
                Method method=ReflectionUtil.getMethod(clazz, nombreMetodo, null, null, null);
                if (method.getParameterTypes().length != 0) {
                    throw new RuntimeException("El método is no puede tener parámetros de entrada."+method.getParameterTypes().length + " "+ nombrePropiedad + " en la clase " + clazz.getName());
                }
                return method;
            } catch (Exception ex2) {
                throw new RuntimeException("No se encontró el método get o is en " + nombrePropiedad + " en la clase " + clazz.getName(), ex2);
            }
        }
    }
}

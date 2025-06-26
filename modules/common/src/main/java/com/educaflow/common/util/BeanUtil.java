package com.educaflow.common.util;

import com.axelor.db.Model;
import org.modelmapper.ModelMapper;

import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;

public class BeanUtil {
    public static  <T extends Model> T cloneEntity(Class<T> modelClass, Object entity) {
        try {
            ModelMapper mapper = new ModelMapper();
            T model = modelClass.getDeclaredConstructor().newInstance();
            model = mapper.map(entity, modelClass);

            return (T) model;
        } catch (Exception ex) {
            throw new RuntimeException("Error al crear el modelo: " + modelClass.getName(), ex);
        }
    }
    public static  <T extends Model> void copyEntity(Class<T> modelClass, Object sourceEntity, Object targetEntity) {
        try {
            copySimpleAndModelProperties(sourceEntity,targetEntity,modelClass);
        } catch (Exception ex) {
            throw new RuntimeException("Error al crear el modelo: " + modelClass.getName(), ex);
        }
    }

    public static  <T extends Model> void copyMapToEntity(Class<T> modelClass, Map<String,Object> sourceEntity, Object targetEntity) {
        try {
            copyMapToObject(sourceEntity,targetEntity,modelClass);
        } catch (Exception ex) {
            throw new RuntimeException("Error al crear el modelo: " + modelClass.getName(), ex);
        }
    }


    private static void copySimpleAndModelProperties( Object source, Object destination,Class beanClass)
            throws ReflectiveOperationException {

        if (source == null || destination == null) {
            throw new IllegalArgumentException("Los objetos de origen y destino no pueden ser nulos.");
        }
        if (!beanClass.isInstance(source) || !beanClass.isInstance(destination)) {
            throw new IllegalArgumentException("Los objetos no son instancias de la clase proporcionada.");
        }

        for (Method setter : beanClass.getMethods()) {
            if (setter.getName().startsWith("set") && setter.getParameterTypes().length == 1) {
                String propertyName = setter.getName().substring(3);
                String getterName = "get" + propertyName;
                String isGetterName = "is" + propertyName;

                try {
                    Method getter = null;
                    try {
                        getter = beanClass.getMethod(getterName);
                    } catch (NoSuchMethodException e) {
                        try {
                            getter = beanClass.getMethod(isGetterName);
                        } catch (NoSuchMethodException ignored) {
                            continue;
                        }
                    }

                    // Asegurarse de que el tipo de retorno del getter coincide con el tipo del parámetro del setter
                    if (getter.getReturnType().equals(setter.getParameterTypes()[0]) == false) {
                        throw new RuntimeException("No coinciden los tipos de " + getter.getName() + " y " + setter.getName());
                    }

                    Class<?> propertyType = getter.getReturnType();
                    Object value = getter.invoke(source);


                    // 1. Tipos básicos (primitivos, String, Enum, Date/java.time)
                    if (propertyType.isPrimitive() ||
                            String.class.equals(propertyType) ||
                            Number.class.isAssignableFrom(propertyType) ||
                            propertyType.isEnum() ||
                            Date.class.isAssignableFrom(propertyType) || // java.util.Date
                            (propertyType.getPackage() != null && propertyType.getPackage().getName().startsWith("java.time")) || // java.time.*
                            Boolean.class.equals(propertyType) ||
                            Character.class.equals(propertyType)
                    ) {
                        setter.invoke(destination, value);
                    } else if (Model.class.isAssignableFrom(propertyType)) {
                        Object destinationModel = getter.invoke(destination);
                        if ((value == null) && (destinationModel == null)) {
                            //No hacer nada
                        } else if ((value == null) && (destinationModel != null)) {
                            setter.invoke(destination, (Object) null);
                        } else if ((value != null) && (destinationModel == null)) {
                            Model sourceModel = (Model) value;
                            Model newDestinationModel = AxelorDBUtil.getRepository((Class) getter.getReturnType()).find(sourceModel.getId());
                            setter.invoke(destination, newDestinationModel);
                        } else if ((value != null) && (destinationModel != null)) {
                            copySimpleAndModelProperties(value, destinationModel, propertyType);
                        } else {
                            throw new RuntimeException("Error de lógica");
                        }
                        // INICIO DEL CÓDIGO AÑADIDO/MODIFICADO
                    } else if (byte[].class.equals(propertyType)) {
                            // Si es un array de bytes, simplemente copia la referencia.
                            // Si necesitas una copia profunda, deberías clonar el array:
                            // setter.invoke(destination, value != null ? ((byte[]) value).clone() : null);

                            setter.invoke(destination, value);

                    } else if (Model.class.isAssignableFrom(propertyType) && (hasOneToOne(beanClass,propertyName))) {
                        throw new RuntimeException("No implementado");
                    } else if (Collection.class.isAssignableFrom(propertyType) && value != null) {

                        throw new RuntimeException("No implementado");
                    } else if (Map.class.isAssignableFrom(propertyType) || propertyType.isArray()) {
                        continue;
                    } else {
                        continue;
                    }

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private static void copyMapToObject( Map<String,Object> source, Object destination,Class beanClass) {

        try {

            if (source == null || destination == null) {
                throw new IllegalArgumentException("Los objetos de origen y destino no pueden ser nulos.");
            }
            if (!beanClass.isInstance(destination)) {
                throw new IllegalArgumentException("Los objetos no son instancias de la clase proporcionada.");
            }

            for (Method setter : beanClass.getMethods()) {
            if (setter.getName().startsWith("set") && setter.getParameterTypes().length == 1) {
                final String upperPropertyName = setter.getName().substring(3);
                String getterName = "get" + upperPropertyName;
                String isGetterName = "is" + upperPropertyName;

                String propertyName = TextUtil.toFirstsLetterToLowerCase(upperPropertyName);


                if (!source.containsKey(propertyName)) {
                    continue;
                }
                Method tempGetter = null;
                try {
                    tempGetter = beanClass.getMethod(getterName);
                } catch (NoSuchMethodException e) {
                    try {
                        tempGetter = beanClass.getMethod(isGetterName);
                    } catch (NoSuchMethodException ignored) {
                        continue;
                    }
                }
                final Method getter = tempGetter;


                if (!getter.getReturnType().equals(setter.getParameterTypes()[0])) {
                    throw new RuntimeException("No coinciden los tipos de " + getter.getName() + " y " + setter.getName());
                }

                Class<?> propertyType = getter.getReturnType();

                Object value = source.get(propertyName);
                System.out.println(beanClass.getSimpleName() + "." + propertyName + "=" + value);
                if (value == null) {
                    continue;
                } else if (propertyType.isPrimitive() ||
                        String.class.equals(propertyType) ||
                        Number.class.isAssignableFrom(propertyType) ||
                        propertyType.isEnum() ||
                        Date.class.isAssignableFrom(propertyType) || // java.util.Date
                        (propertyType.getPackage() != null && propertyType.getPackage().getName().startsWith("java.time")) || // java.time.*
                        Boolean.class.equals(propertyType) ||
                        Character.class.equals(propertyType)
                ) {
                    if (setter.getParameterTypes()[0] == Long.class) {
                        value = ((Integer) value).longValue();
                    }
                    if (setter.getParameterTypes()[0] == LocalDateTime.class) {
                        OffsetDateTime offsetDateTime = OffsetDateTime.parse((String) value);
                        value = offsetDateTime.toLocalDateTime();
                    }
                    try {
                        setter.invoke(destination, value);
                    } catch (Exception ex) {
                        throw new RuntimeException(propertyName + " " + value + "\n" + ex.getMessage());
                    }
                } else if (Model.class.isAssignableFrom(propertyType)) {
                    Object destinationModel = getter.invoke(destination);
                    if ((value == null) && (destinationModel == null)) {
                        //No hacer nada
                    } else if ((value == null) && (destinationModel != null)) {
                        setter.invoke(destination, (Object) null);
                    } else if ((value != null) && (destinationModel == null)) {

                        Long id;
                        if (Model.class.isAssignableFrom(value.getClass())) {
                            Model sourceModel = (Model) value;
                            id = sourceModel.getId();
                        } else {
                            Map<String, Object> sourceModel = (Map<String, Object>) value;
                            id = ((Integer) sourceModel.get("id")).longValue();
                        }
                        Model newDestinationModel = AxelorDBUtil.getRepository((Class) getter.getReturnType()).find(id);
                        setter.invoke(destination, newDestinationModel);
                    } else if ((value != null) && (destinationModel != null)) {
                        if (Model.class.isAssignableFrom(value.getClass())) {
                            copySimpleAndModelProperties(value, destinationModel, propertyType);
                        } else {
                            copyMapToObject((Map<String, Object>) value, destinationModel, propertyType);
                        }

                        copyMapToObject((Map<String, Object>) value, destinationModel, propertyType);
                    } else {
                        throw new RuntimeException("Error de lógica");
                    }
                    // INICIO DEL CÓDIGO AÑADIDO/MODIFICADO
                } else if (byte[].class.equals(propertyType)) {
                    // Si es un array de bytes, simplemente copia la referencia.
                    // Si necesitas una copia profunda, deberías clonar el array:
                    // setter.invoke(destination, value != null ? ((byte[]) value).clone() : null);
                    setter.invoke(destination, value);

                } else if (Model.class.isAssignableFrom(propertyType) && (hasOneToOne(beanClass, propertyName))) {
                    throw new RuntimeException("No implementado");
                } else if (List.class.isAssignableFrom(propertyType) && (hasOneToMany(beanClass, propertyName))) {
                    String mappedBy = getMappedByInOneToMany(beanClass, propertyName);
                    List<Map<String, Object>> sourceCollection = (List<Map<String, Object>>) value;
                    List<Model> destinationCollection = (List<Model>) getter.invoke(destination);
                    String mapperBy = getMappedByInOneToMany(beanClass, propertyName);
                    String mapperByMethodName = "set" + TextUtil.toFirstsLetterToUpperCase(mapperBy);
                    Class<? extends Model> oneToManyClass = (Class<? extends Model>) ((ParameterizedType) getter.getGenericReturnType()).getActualTypeArguments()[0];
                    Method mapperByMethod = ReflectionUtil.getMethod(oneToManyClass, mapperByMethodName, void.class, null, new Class<?>[]{beanClass});

                    if (sourceCollection != null) {
                        sourceCollection.stream().forEach((model) -> {
                            model.put(mappedBy, destination);
                        });
                    }


                    if ((sourceCollection == null) && (destinationCollection == null)) {
                        //No hacer nada
                    } else if ((sourceCollection == null) && (destinationCollection != null)) {
                        setter.invoke(destination, (Collection<Model>) null);
                    } else if ((sourceCollection != null) && (destinationCollection == null)) {
                        destinationCollection = new ArrayList<>();
                        for (Map<String, Object> mapModel : sourceCollection) {
                            Long idLong = (Long) mapModel.get("id");

                            if (idLong == null) {
                                System.out.println("Z");
                                Model newOneToManyObject = oneToManyClass.getDeclaredConstructor().newInstance();
                                copyMapToObject(mapModel, newOneToManyObject, oneToManyClass);
                                destinationCollection.add(newOneToManyObject);
                            } else {
                                System.out.println("E");
                                if (getIdModelInCollection(destinationCollection, idLong) == null) {
                                    throw new RuntimeException("Existe el id en la nueva lista pero no existía en la original:" + idLong);
                                }
                            }
                        }

                        setter.invoke(destination, destinationCollection);
                    } else if ((sourceCollection != null) && (destinationCollection != null)) {
                        System.out.println("A " + sourceCollection.size() + " " + destinationCollection.size());
                        for (Iterator<Model> iter = destinationCollection.iterator(); iter.hasNext(); ) {
                            Model model = iter.next();
                            Map<String, Object> sourceModel = getIdMapInCollection(sourceCollection, model.getId().intValue());
                            if (sourceModel == null) {
                                System.out.println("B");
                                iter.remove();
                            } else {
                                System.out.println("C");
                                copyMapToObject(sourceModel, model, oneToManyClass);
                            }
                        }

                        for (Map<String, Object> mapModel : sourceCollection) {
                            Long idLong = (Long) mapModel.get("id");

                            if (idLong == null) {
                                System.out.println("D");
                                Model newOneToManyObject = oneToManyClass.getDeclaredConstructor().newInstance();
                                copyMapToObject(mapModel, newOneToManyObject, oneToManyClass);
                                destinationCollection.add(newOneToManyObject);
                            } else {
                                System.out.println("E");
                                if (getIdModelInCollection(destinationCollection, idLong) == null) {
                                    throw new RuntimeException("Existe el id en la nueva lista pero no existía en la original:" + idLong);
                                }
                            }
                        }

                        destinationCollection.stream().forEach((model) -> {
                            try {
                                mapperByMethod.invoke(model, destination);
                            } catch (Exception e) {
                                throw new RuntimeException("Error al invocar el método", e);
                            }
                        });

                    } else {
                        throw new RuntimeException("Error de lógica");
                    }


                } else if (Map.class.isAssignableFrom(propertyType) || propertyType.isArray()) {
                    continue;
                } else {
                    continue;
                }


            }
        }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private static boolean hasManyToOne(Class<?> clazz, String nombrePropiedad) {
        try {
            Field campo = clazz.getDeclaredField(nombrePropiedad);
            return campo.isAnnotationPresent(ManyToOne.class);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("En el campo " + nombrePropiedad, e);
        }
    }
    private static boolean hasOneToOne(Class<?> clazz, String nombrePropiedad) {
        try {
            Field campo = clazz.getDeclaredField(nombrePropiedad);
            return campo.isAnnotationPresent(OneToOne.class);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("En el campo " + nombrePropiedad, e);
        }
    }

    private static boolean hasOneToMany(Class<?> clazz, String nombrePropiedad) {
        try {
            Field campo = clazz.getDeclaredField(nombrePropiedad);
            return campo.isAnnotationPresent(OneToMany.class);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("En el campo " + nombrePropiedad, e);
        }
    }

    private static String getMappedByInOneToMany(Class<?> clazz, String nombrePropiedad) {
        try {
            Field campo = clazz.getDeclaredField(nombrePropiedad);
            return campo.getAnnotation(OneToMany.class).mappedBy();
        } catch (Exception e) {
            throw new RuntimeException("En el campo " + nombrePropiedad, e);
        }
    }


    private static Map<String, Object> getIdMapInCollection(List<Map<String, Object>> list, long id) {
        return list.stream().filter(model -> ((Integer) model.get("id")) == id).findFirst().orElse(null);
    }

    private static Model getIdModelInCollection(List<Model> list, long id) {
        return list.stream().filter(model -> model.getId() == id).findFirst().orElse(null);
    }
}

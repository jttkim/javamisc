package org.javamisc.jee.entitycrud;

import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.InvocationTargetException;

import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import javax.persistence.Id;
import javax.persistence.Entity;
import javax.persistence.Column;
import javax.persistence.OneToOne;
import javax.persistence.OneToMany;
import javax.persistence.ManyToOne;
import javax.persistence.ManyToMany;

import static org.javamisc.Util.genericTypecast;


/**
 * Bean utilities to support the entitycrud package, mostly reflection
 * stuff.
 */
public class BeanUtil
{
  /**
   * BeanUtil is not to be instantiated, just provides static methods.
   */
  private BeanUtil()
  {
  }


  /**
   * Construct an instance of a class.
   *
   * @param c the class to be instantiated
   * @return an instance of the class, obtained by invoking the parameterless constructor of the class
   */
  public static Object constructDefaultInstance(Class<?> c) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException
  {
    Constructor defaultConstructor = c.getConstructor();
    Object o = defaultConstructor.newInstance();
    return (o);
  }


  /**
   * Construct an instance of a class specified by a name.
   *
   * @param className the canonical name of the class to be instantiated
   * @return an instance of the class, obtained by invoking the parameterless constructor of the class
   */
  public static Object constructDefaultInstance(String className) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException
  {
    return (constructDefaultInstance(Class.forName(className)));
  }


  /**
   * Extract the name of a property from the name of an accessor or mutator method.
   *
   * @param methodName the name of the accessor or mutator method
   * @return the name of the property
   */
  public static String extractPropertyName(String methodName)
  {
    int i = 0;
    if (methodName.startsWith("get") || methodName.startsWith("set"))
    {
      i = 3;
    }
    else if (methodName.startsWith("is"))
    {
      i = 2;
    }
    else
    {
      throw new IllegalArgumentException(String.format("not an accessor or mutator name: %s", methodName));
    }
    String propertyName = methodName.substring(i, i + 1).toLowerCase() + methodName.substring(i + 1);
    return (propertyName);
  }


  /**
   * Extract the name of a property from an accessor or mutator method.
   *
   * @param method the accessor or mutator method
   * @return the name of the property
   */
  public static String extractPropertyName(Method method)
  {
    return (extractPropertyName(method.getName()));
  }


  /**
   * Compute the accessor name corresponding to a property.
   *
   * <p>This method prefixes the property name with {@code get} and
   * converts the first character of the property to upper case. There
   * is no provision for boolean accessors prefixed with {@code is}.</p>
   *
   * @param propertyName the name of the property
   * @return the corresponding accessor name
   */
  public static String makeAccessorName(String propertyName)
  {
    // FIXME: cannot generate isSomething style boolean accessors
    return ("get" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1));
  }


  /**
   * Compute the mutator name corresponding to a property.
   *
   * <p>This method prefixes the property name with {@code set} and
   * converts the first character of the property to upper case.</p>
   *
   * @param propertyName the name of the property
   * @return the corresponding mutator name
   */
  public static String makeMutatorName(String propertyName)
  {
    return ("set" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1));
  }


  /**
   * Determine whether a method is an accessor method.
   *
   * <p>Currently checks are (1) whether the method's name starts with
   * {@code get} or {@code is} and (2) whether the method takes no
   * parameters.</p>
   *
   * @param method the method to be checked
   * @return {@code true} if the method is considered to be an accessor method.
   */
  public static boolean isAccessor(Method method)
  {
    String methodName = method.getName();
    if (!methodName.startsWith("get") && !methodName.startsWith("is"))
    {
      return (false);
    }
    Class parameterTypes[] = method.getParameterTypes();
    if (parameterTypes.length != 0)
    {
      return (false);
    }
    else
    {
      return (true);
    }
  }


  /**
   * Determine whether a method is a mutator method.
   *
   * <p>Currently checks are (1) whether the method's name starts with
   * {@code set} and (2) whether the method takes exactly one parameter.</p>
   *
   * @param method the method to be checked
   * @return {@code true} if the method is considered to be an mutator method.
   */
  public static boolean isMutator(Method method)
  {
    String methodName = method.getName();
    if (!methodName.startsWith("set"))
    {
      return (false);
    }
    Class parameterTypes[] = method.getParameterTypes();
    if (parameterTypes.length != 1)
    {
      return (false);
    }
    else
    {
      return (true);
    }
  }


  /**
   * Find the accessor method for a given property.
   *
   * <p>Accessors found by this method take no parameter. The method
   * returns the first accessor it finds.</p>
   *
   * @param entityClass the entity class within which the accessor is searched
   * @param propertyName the name of the property
   * @return the accessor method, or {@code null} if no mutator was found
   */
  public static Method findAccessor(Class<?> entityClass, String propertyName)
  {
    for (Method method : entityClass.getMethods())
    {
      if (isAccessor(method))
      {
        if (propertyName.equals(extractPropertyName(method)))
        {
          return (method);
        }
      }
    }
    return (null);
  }


  /**
   * Find the mutator method for a given property.
   *
   * <p>Mutators found by this method take exactly one parameter. The
   * method returns the first mutator it finds.</p>
   *
   * @param entityClass the entity class within which the mutator is searched
   * @param propertyName the name of the property
   * @return the mutator method, or {@code null} if no mutator was found
   */
  // FIXME: really ought to check for property type as well
  public static Method findMutator(Class<?> entityClass, String propertyName)
  {
    for (Method method : entityClass.getMethods())
    {
      if (isMutator(method))
      {
        if (propertyName.equals(extractPropertyName(method)))
        {
          return (method);
        }
      }
    }
    return (null);
  }


  /**
   * Determine the type of a property.
   *
   * <p>This method operates by trying to find an accessor method for
   * the property and returns that accessor's return type.</p>
   *
   * @param entityClass the entity class for which to determine the property type
   * @param propertyName the property's name
   * @return the type of the property, or {@code null} if the entity class has no accessor for the specified property
   */
  public static Class<?> findPropertyType(Class<?> entityClass, String propertyName)
  {
    Method accessor = findAccessor(entityClass, propertyName);
    if (accessor == null)
    {
      return (null);
    }
    return (accessor.getReturnType());
  }


  /**
   * Determine the type of a property.
   *
   * <p>This method operates by trying to find an accessor method for
   * the property and returns that accessor's return type.</p>
   *
   * @param entity the entity for which to determine the property type
   * @param propertyName the property's name
   * @return the type of the property, or {@code null} if the entity class has no accessor for the specified property
   */
  public static Class<?> findPropertyType(Object entity, String propertyName)
  {
    return (findPropertyType(entity.getClass(), propertyName));
  }


  /**
   * Find the names of all properties of an entity class.
   *
   * <p>Properties found by this method are both accessible and
   * mutatable.</p>
   *
   * @param entityClass the entity class
   * @return a set containing the names of the properties
   */
  public static Set<String> findPropertyNameSet(Class<?> entityClass)
  {
    HashSet<String> propertyNameSet = new HashSet<String>();
    for (Method method : entityClass.getMethods())
    {
      if (isAccessor(method))
      {
        String propertyName = extractPropertyName(method.getName());
        if (findMutator(entityClass, propertyName) != null)
        {
          propertyNameSet.add(propertyName);
        }
      }
    }
    return (propertyNameSet);
  }


  /**
   * Find the names of all properties of an entity.
   *
   * <p>Properties found by this method are both accessible and
   * mutatable.</p>
   *
   * @param entity the entity
   * @return a set containing the names of the properties
   */
  public static Set<String> findPropertyNameSet(Object entity)
  {
    return (findPropertyNameSet(entity.getClass()));
  }


  /**
   * Find properties of an entity class that are constrained to be
   * unique by a {@code @Column} annotation or that have an {@code @Id}
   * annotation.
   *
   * @param entityClass the entity class
   * @return a list of names of the entity class' unique properties
   */
  public static Set<String> findUniquePropertyNameSet(Class<?> entityClass)
  {
    HashSet<String> uniquePropertyNameSet = new HashSet<String>();
    for (String propertyName : findPropertyNameSet(entityClass))
    {
      Method accessor = findAccessor(entityClass, propertyName);
      Method mutator = findMutator(entityClass, propertyName);
      if ((accessor.getAnnotation(Id.class) != null) || (mutator.getAnnotation(Id.class) != null))
      {
        uniquePropertyNameSet.add(propertyName);
      }
      else
      {
        Column columnAnnotation = accessor.getAnnotation(Column.class);
        if (columnAnnotation == null)
        {
          columnAnnotation = mutator.getAnnotation(Column.class);
        }
        if (columnAnnotation != null && columnAnnotation.unique())
        {
          uniquePropertyNameSet.add(propertyName);
        }
      }
    }
    return (uniquePropertyNameSet);
  }


  /**
   * Determine whether an class represents a collection of entities.
   *
   * <p>A collection of entities is defined as an instance of {@code
   * java.util.Collection} with an element type that is annotated
   * {@code @javax.persistence.Entity}.</p>
   *
   * <p>Seems to be impossible due to restrictions caused by type erasure :-(((</p>
   *
   * @param objClass the class to be checked
   * @return {@code} true if the object is a collection of entities
   */
  public static boolean isEntityCollectionClass(Class<?> objClass)
  {
    throw new RuntimeException("not implemented due to erasure problems");
    /*
    if (!(objClass instanceof ParameterizedType))
    {
      return (false);
    }
    return (true);
    */
  }


  /**
   * Determine whether an object is a collection of entities.
   *
   * <p>A collection of entities is defined as an instance of {@code
   * java.util.Collection} with an element type that is annotated
   * {@code @javax.persistence.Entity}.</p>
   *
   * @param obj the object to be checked
   * @return {@code} true if the object is a collection of entities
   */
  public static boolean isEntityCollection(Object obj)
  {
    return (isEntityCollectionClass(obj.getClass()));
  }


  /**
   * Compute a map of property names to classes that the entity has
   * associations with.
   *
   * @param entityClass the entity for which associations are to be found.
   * @return a map with property names as keys and classes as values.
   */
  public static Map<String, Class<?>> findAssociationPropertyMap(Class<?> entityClass)
  {
    HashMap<String, Class<?>> associationPropertyMap = new HashMap<String, Class<?>>();
    for (String propertyName : findPropertyNameSet(entityClass))
    {
      Method accessor = findAccessor(entityClass, propertyName);
      Method mutator = findMutator(entityClass, propertyName);
      if ((accessor.getAnnotation(OneToOne.class) != null) || (accessor.getAnnotation(ManyToOne.class) != null) || (mutator.getAnnotation(OneToOne.class) != null) || (mutator.getAnnotation(ManyToOne.class) != null))
      {
        // System.err.println(String.format("Util.findAssociationPropertyMap: %s: using simple returnType for to-one association", propertyName));
        associationPropertyMap.put(propertyName, accessor.getReturnType());
      }
      else if ((accessor.getAnnotation(OneToMany.class) != null) || (accessor.getAnnotation(ManyToMany.class) != null) || (mutator.getAnnotation(OneToMany.class) != null) || (mutator.getAnnotation(ManyToMany.class) != null))
      {
        // System.err.println(String.format("Util.findAssociationPropertyMap: %s: analysing generic returnType for to-many association", propertyName));
        Type rawToManyType = accessor.getGenericReturnType();
        if (!(rawToManyType instanceof ParameterizedType))
        {
          throw new IllegalArgumentException(String.format("illegal type for to-many association: %s", rawToManyType.toString()));
        }
        ParameterizedType toManyType = (ParameterizedType) rawToManyType;
        Type[] actualTypeList = toManyType.getActualTypeArguments();
        if (actualTypeList.length != 1)
        {
          throw new IllegalArgumentException("cannot deal with collections that do not have exactly one type parameter");
        }
        Type associatedType = actualTypeList[0];
        if (!(associatedType instanceof Class<?>))
        {
          throw new IllegalArgumentException("cannot deal with association types that are not classes.");
        }
        Class<?> associatedClass = genericTypecast(associatedType);
        associationPropertyMap.put(propertyName, associatedClass);
      }
    }
    return (associationPropertyMap);
  }


  /**
   * Compute a map of property names to classes that the entity has
   * associations with.
   *
   * @param entity the entity for which associations are to be found.
   * @return a map with property names as keys and classes as values.
   */
  public static Map<String, Class<?>> findAssociationPropertyMap(Object entity)
  {
    Class<?> entityClass = entity.getClass();
    return findAssociationPropertyMap(entityClass);
  }


  /**
   * Find properties of an entity that are constrained to be
   * unique by a {@code @Column} annotation or that have an {@code @Id}
   * annotation.
   *
   * @param entity the entity
   * @return a list of names of the entity's unique properties
   */
  public static Set<String> findUniquePropertyNameSet(Object entity)
  {
    return (findUniquePropertyNameSet(entity.getClass()));
  }


  /**
   * Get a property from an entity (or generally a bean like object).
   *
   * @param entity the entity (or general object) to get the property from
   * @param propertyName the name of the property to get
   * @return the property's value
   */
  public static Object getProperty(Object entity, String propertyName) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException
  {
    String accessorName = makeAccessorName(propertyName);
    Class<?> entityClass = entity.getClass();
    Method accessorMethod = entityClass.getMethod(accessorName);
    Object property = accessorMethod.invoke(entity);
    return (property);
  }


  /**
   * Set a property on an entity (or generally a bean like object).
   *
   * @param entity the entity (or general object) to get the property from
   * @param propertyName the name of the property to get
   * @param propertyValue the value to set
   */
  public static void setProperty(Object entity, String propertyName, Object propertyValue) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException
  {
    String mutatorName = makeMutatorName(propertyName);
    Class<?> entityClass = entity.getClass();
    Method mutatorMethod = null;
    if (propertyValue != null)
    {
      Class<?> valueClass = propertyValue.getClass();
      mutatorMethod = entityClass.getMethod(mutatorName, valueClass);
    }
    else
    {
      // FIXME: just find any method taking one parameter -- cannot use parameter type now
      for (Method method : entityClass.getMethods())
      {
        if (mutatorName.equals(method.getName()) && (method.getParameterTypes().length == 1))
        {
          mutatorMethod = method;
          break;
        }
      }
    }
    // FIXME: ignoring the return value, assuming method returns void
    mutatorMethod.invoke(entity, propertyValue);
  }


  /**
   * Set a property based on a {@code String} value on an entity (or
   * generally a bean like object).
   *
   * <p>This method checks the type of the property and converts the
   * {@code String} accordingly. Currently supported types are:</p>
   * <table>
   * <tr><td>{@code String}</td><td>no conversion</td></tr>
   * <tr><td>{@code Integer}</td><td>conversion by {@code Integer.parseInt}</td></tr>
   * <tr><td>{@code Double}</td><td>conversion by {@code Double.parseDouble}</td></tr>
   * </table>
   *
   * @param entity the object to get the property from
   * @param propertyName the name of the property to get
   * @param propertyValueString the value to set, as a string
   */
  public static void setPropertyFromString(Object entity, String propertyName, String propertyValueString) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException
  {
    Class<?> valueClass = findPropertyType(entity.getClass(), propertyName);
    Object propertyValue = null;
    if (String.class.isAssignableFrom(valueClass))
    {
      valueClass = String.class;
      propertyValue = propertyValueString;
    }
    else if (Integer.class.isAssignableFrom(valueClass))
    {
      valueClass = Integer.class;
      propertyValue = new Integer(Integer.parseInt(propertyValueString));
    }
    else if (Double.class.isAssignableFrom(valueClass))
    {
      valueClass = Double.class;
      propertyValue = new Double(Double.parseDouble(propertyValueString));
    }
    else
    {
      throw new IllegalArgumentException("property type not (yet) supported");
    }
    setProperty(entity, propertyName, propertyValue);
  }
}

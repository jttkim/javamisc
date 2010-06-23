package org.javamisc.jee.entitycrud;

import java.lang.reflect.InvocationTargetException;

import java.util.List;
import java.util.Set;


public interface EntityAccess
{
  /**
   * Determine whether a class is an entity class.
   *
   * @param aClass the class to be tested
   * @return {@code true} if the class is an entity class
   */
  boolean isEntityClass(Class<?> aClass);

  /**
   * Determine whether an object is an instance of an entity class.
   *
   * @param aClass the class to be tested
   * @return {@code true} if the class is an entity class
   */
  boolean isEntityInstance(Object obj);
  <EntityClass> EntityClass findEntity(Class<EntityClass> entityClass, Integer id);
  <EntityClass> List<EntityClass> findEntityList(Class<EntityClass> entityClass);
  boolean updateEntity(Class<?> entityClass, Integer entityId, Set<EntityOperation> entityOperationSet) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException;
  boolean removeEntity(Class<?> entityClass, Integer entityId);
}

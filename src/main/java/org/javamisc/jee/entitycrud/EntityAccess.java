package org.javamisc.jee.entitycrud;

import java.lang.reflect.InvocationTargetException;

import java.util.List;
import java.util.Set;


public interface EntityAccess
{
  boolean isEntityClass(Class<?> aClass);
  boolean isEntityInstance(Object obj);
  <EntityClass> EntityClass findEntity(Class<EntityClass> entityClass, Integer id);
  <EntityClass> List<EntityClass> findEntityList(Class<EntityClass> entityClass);
  boolean updateEntity(Class<?> entityClass, Integer entityId, Set<EntityOperation> entityOperationSet) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException;
  boolean removeEntity(Class<?> entityClass, Integer entityId);
}

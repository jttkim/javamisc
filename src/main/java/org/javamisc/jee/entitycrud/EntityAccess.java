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
   * @param obj the object to be tested
   * @return {@code true} if the class is an entity class
   */
  boolean isEntityInstance(Object obj);

  /**
   * Fetch entities in collections mapping to-many associations.
   *
   * @param entity the entity for which to fetch collections
   */
  void fetchCollections(Object entity);

  /**
   * Find an entity by its id.
   *
   * <p>The {@code id} object must be suitable as a key for the entity
   * class.</p>
   *
   * @param entityClass the entity class
   * @param <EntityClass> the entity class
   * @param id the id of the entity
   * @return the entity
   */
  <EntityClass> EntityClass findEntity(Class<EntityClass> entityClass, Object id);

  /**
   * Find a list of all persisted entities of the entity class.
   *
   * @param entityClass the entity class
   * @param <EntityClass> the entity class
   * @return the list of all persisted entities
   */
  <EntityClass> List<EntityClass> findEntityList(Class<EntityClass> entityClass);
  boolean updateEntity(Class<?> entityClass, Integer entityId, Set<EntityOperation> entityOperationSet) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException;
  boolean removeEntity(Class<?> entityClass, Integer entityId);
}

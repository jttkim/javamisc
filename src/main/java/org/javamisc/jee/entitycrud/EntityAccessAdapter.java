package org.javamisc.jee.entitycrud;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.Map;

import javax.persistence.PersistenceContext;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import static org.javamisc.Util.genericTypecast;


public class EntityAccessAdapter implements EntityAccess
{
  @PersistenceContext
  protected EntityManager entityManager;

  /**
   * Determine whether a class is an entity class.
   *
   * <p>This method relies on the {@code javax.persistence.Entity} annotation.</p>
   *
   * @param aClass the class to be checked
   * @return {@code true} if the class is an entity class
   */
  public boolean isEntityClass(Class<?> aClass)
  {
    return (aClass.getAnnotation(Entity.class) != null);
  }


  /**
   * Determine whether an object is an instance of an entity class.
   *
   * <p>This method relies on {@see isEntityClass}.</p>
   *
   * @param obj the object to be checked
   * @return {@code true} if the object is an entity instance
   */
  public boolean isEntityInstance(Object obj)
  {
    return (isEntityClass(obj.getClass()));
  }


  /**
   * Check whether a method is an accessor for a collection mapping an
   * association.
   */
  private static boolean isCollectionAccessor(Method method)
  {
    // FIXME: should really check persistence annotations @ManyTo*
    String methodName = method.getName();
    Class<?>[] parameterTypes = method.getParameterTypes();
    if (parameterTypes.length != 0)
    {
      return (false);
    }
    else
    {
      return (Collection.class.isAssignableFrom(method.getReturnType()));
    }
  }


  /**
   * Fetch entities in collections mapping to-many associations.
   *
   * @param entity the entity for which to fetch collections
   */
  private void fetchCollections(Object entity)
  {
    Class<?> entityClass = entity.getClass();
    for (Method method : entityClass.getMethods())
    {
      if (this.isCollectionAccessor(method))
      {
	try
	{
	  Object setObject = method.invoke(entity);
	  Collection<?> set = genericTypecast(setObject);
	  for (Object o : set)
	  {
	    o.toString();
	  }
	}
	catch (IllegalAccessException e)
	{
	  System.err.println(String.format("AccessBean.fetchCollections: caught %s\n", e.toString()));
	}
	catch (InvocationTargetException e)
	{
	  System.err.println(String.format("AccessBean.fetchCollections: caught %s\n", e.toString()));
	}
      }
    }
  }


  public <EntityClass> EntityClass findEntity(Class<EntityClass> entityClass, Integer id)
  {
    EntityClass entity = this.entityManager.find(entityClass, id);
    if (entity != null)
    {
      fetchCollections(entity);
    }
    return (entity);
  }


  public <EntityClass> List<EntityClass> findEntityList(Class<EntityClass> entityClass)
  {
    // FIXME: hack!!!!! string assembly of query!!!
    // using a class' simple name provides some amount of sanity hopefully, but this is not good
    Query query = this.entityManager.createQuery(String.format("SELECT e FROM %s e", entityClass.getSimpleName()));
    List<EntityClass> entityList = genericTypecast(query.getResultList());
    for (Object entity : entityList)
    {
      fetchCollections(entity);
    }
    return (entityList);
  }


  public boolean updateEntity(Class<?> entityClass, Integer entityId, Set<EntityOperation> entityOperationSet) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException
  {
    // FIXME: propertyMap may need some sanitising, especially it must not contain an id entry (!!)
    Object entity = BeanUtil.constructDefaultInstance(entityClass);
    if (entityId != null)
    {
      entity = this.entityManager.find(entity.getClass(), entityId);
    }
    boolean success = true;
    for (EntityOperation entityOperation : entityOperationSet)
    {
      System.err.println(String.format("EntityAccessAdapter.updateEntity: applying %s\n", entityOperation.toString()));
      success = success && entityOperation.apply(entity, this);
    }
    if (entityId == null)
    {
      this.entityManager.persist(entity);
    }
    return (success);
  }


  public boolean removeEntity(Class<?> entityClass, Integer id)
  {
    Object entity = this.entityManager.find(entityClass, id);
    if (entity == null)
    {
      return (false);
    }
    this.entityManager.remove(entity);
    return (true);
  }
}

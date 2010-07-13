package org.javamisc.jee.entitycrud;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import java.util.Collection;

import static org.javamisc.Util.genericTypecast;


public class EntityLinkOperation extends EntityOperation
{
  // FIXME: much duplicated code with EntityUnlinkOperation
  private Integer associatedEntityId;


  public EntityLinkOperation(String propertyName, Integer associatedEntityId)
  {
    super(propertyName);
    this.associatedEntityId = associatedEntityId;
  }


  public boolean apply(Object entity, EntityAccess entityAccess) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException
  {
    System.err.println(String.format("EntityLinkOperation.apply: id = %s", this.associatedEntityId));
    Class<?> propertyType = BeanUtil.findPropertyType(entity.getClass(), this.propertyName);
    // FIXME: clumsy and perhaps not so safe way to get at associated entity type
    Class<?> associatedEntityType = BeanUtil.findAssociationPropertyMap(entity).get(this.propertyName);
    System.err.println(String.format("associated entity type for %s: %s, property type: %s", this.propertyName, associatedEntityType.toString(), propertyType.toString()));
    Object associatedEntity = entityAccess.findEntity(associatedEntityType, associatedEntityId);
    if (associatedEntity == null)
    {
      System.err.println(String.format("EntityLinkOperation: no associated entity of class %s with id %d", associatedEntityType.toString(), this.associatedEntityId.intValue()));
      return (false);
    }
    if (Collection.class.isAssignableFrom(propertyType))
    {
      // FIXME: no bidirectional setup of association -- need to get at mappedBy element ... ??
      Collection associationCollection = genericTypecast(BeanUtil.getProperty(entity, propertyName));
      System.err.println(String.format("Access.updateEntity: before add: %d associated entities", associationCollection.size()));
      associationCollection.add(associatedEntity);
      System.err.println(String.format("Access.updateEntity: after add: %d associated entities", associationCollection.size()));
    }
    else
    {
      BeanUtil.setProperty(entity, this.propertyName, associatedEntity);
    }
    return (true);
  }


  public String toString()
  {
    return (String.format("EntityLinkOperation: name = %s, value = %d", this.propertyName, this.associatedEntityId.intValue()));
  }
}

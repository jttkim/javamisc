package org.javamisc.jee.entitycrud;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;


public class EntitySetPropertyFromStringOperation extends EntityOperation
{
  private String propertyValue;


  public EntitySetPropertyFromStringOperation(String propertyName, String propertyValue)
  {
    super(propertyName);
    this.propertyValue = propertyValue;
  }


  public boolean apply(Object entity, EntityAccess entityAccess) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException
  {
    System.err.println(String.format("setting property %s to value \"%s\"", this.propertyName, this.propertyValue));
    Class<?> propertyType = BeanUtil.findPropertyType(entity.getClass(), this.propertyName);
    if (String.class.isAssignableFrom(propertyType))
    {
      BeanUtil.setProperty(entity, this.propertyName, this.propertyValue);
      return (true);
    }
    else if (Integer.class.isAssignableFrom(propertyType))
    {
      BeanUtil.setProperty(entity, this.propertyName, new Integer(Integer.parseInt(this.propertyValue)));
      return (true);
    }
    else if (Double.class.isAssignableFrom(propertyType))
    {
      BeanUtil.setProperty(entity, this.propertyName, new Double(Double.parseDouble(this.propertyValue)));
      return (true);
    }
    else if (entityAccess.isEntityClass(propertyType))
    {
      // FIXME: assuming that entity IDs are Integers
      Integer associatedEntityId = new Integer(Integer.parseInt(this.propertyValue));
      Object associatedEntity = entityAccess.findEntity(propertyType, associatedEntityId);
      // FIXME: setting up association unidirectionally only
      BeanUtil.setProperty(entity, this.propertyName, associatedEntity);
      return (true);
    }
    else
    {
      System.err.println(String.format("EntitySetPropertyFromStringOperation.apply: failed to set property %s on entity %s\n", this.propertyName, entity.getClass().toString()));
      return (false);
    }
  }


  public String toString()
  {
    return (String.format("EntitySetPropertyFromStringOperation: name = %s, value = %s", this.propertyName, this.propertyValue));
  }
}

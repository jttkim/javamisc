package org.javamisc.jee.entitycrud;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;


public class EntitySetPropertyOperation extends EntityOperation
{
  private Object propertyValue;


  public EntitySetPropertyOperation(String propertyName, Object propertyValue)
  {
    super(propertyName);
    this.propertyValue = propertyValue;
  }


  public boolean apply(Object entity, EntityAccess entityAccess) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException
  {
    System.err.println(String.format("EntitySetPropertyOperation: setting property %s to value %s\n", this.propertyName, this.propertyValue.toString()));
    BeanUtil.setProperty(entity, this.propertyName, this.propertyValue);
    // FIXME: should check whether property was set successfully
    return (true);
  }


  public String toString()
  {
    return (String.format("EntitySetPropertyOperation: name = %s, value = %s", this.propertyName, this.propertyValue.toString()));
  }
}

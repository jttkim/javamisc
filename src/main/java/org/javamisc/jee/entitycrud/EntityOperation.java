package org.javamisc.jee.entitycrud;

import java.lang.reflect.InvocationTargetException;

import java.io.Serializable;


public abstract class EntityOperation implements Serializable
{
  protected String propertyName;

  protected static final long serialVersionUID = 1;


  protected EntityOperation(String propertyName)
  {
    this.propertyName = propertyName;
  }


  public abstract boolean apply(Object entity, EntityAccess entityAccess) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException;
}

package org.javamisc.jee.entitycrud;

import org.junit.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.Set;
import java.util.HashSet;


class DummyCrudAction extends CrudAction
{
  public DummyCrudAction()
  {
    super(null);
  }


  public Class<?> findEntityClass()
  {
    return (Object.class);
  }


  public boolean isHidden(Class<?> entityClass, String propertyName)
  {
    return (false);
  }


  public boolean isReadOnly(Class<?> entityClass, String propertyName)
  {
    return (false);
  }
}


public class EntityCrudTest
{
  @Test
  public void testPropertyNameUtils()
  {
    Assert.assertTrue("something".equals(BeanUtil.extractPropertyName("getSomething")));
    Assert.assertTrue("something".equals(BeanUtil.extractPropertyName("setSomething")));
    Assert.assertTrue("something".equals(BeanUtil.extractPropertyName("isSomething")));
  }


  @Test
  public void testConstructInstance() throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException
  {
    Object o = BeanUtil.constructDefaultInstance(String.class);
    Assert.assertTrue(o instanceof String);
    o = BeanUtil.constructDefaultInstance(Exception.class);
    Assert.assertTrue(o instanceof Exception);
    o = BeanUtil.constructDefaultInstance("java.lang.String");
    Assert.assertTrue(o instanceof String);
    o = BeanUtil.constructDefaultInstance("java.lang.Exception");
    Assert.assertTrue(o instanceof Exception);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNeitherAccessorNorMutator()
  {
    BeanUtil.extractPropertyName("blah");
  }


  @Test
  public void testAccessorUtils() throws Exception
  {
    Method method = Object.class.getMethod("getClass");
    Assert.assertTrue(BeanUtil.isAccessor(method));
    Assert.assertFalse(BeanUtil.isMutator(method));
    Assert.assertEquals(BeanUtil.extractPropertyName(method), "class");
    String propertyName = "blah";
    Assert.assertEquals(propertyName, BeanUtil.extractPropertyName(BeanUtil.makeAccessorName(propertyName)));
  }


  @Test
  public void testEntityOperation()
  {
    EntitySetPropertyOperation entitySetPropertyOperation = new EntitySetPropertyOperation("testName", "hello");
    EntitySetPropertyFromStringOperation entitySetPropertyFromStringOperation = new EntitySetPropertyFromStringOperation("testName", "hello");
    EntityLinkOperation entityLinkOperation = new EntityLinkOperation("testName", new Integer(1));
    EntityUnlinkOperation entityUnlinkOperation = new EntityUnlinkOperation("testName", new Integer(1));
    Assert.assertTrue(entityUnlinkOperation != null);
  }


  @Test
  public void testCrudAction()
  {
    CrudAction crudAction = new DummyCrudAction();
    crudAction.setEntityId("4711");
    Assert.assertEquals(crudAction.getEntityId(), "4711");
    crudAction.setEntityClassName("String");
    Assert.assertEquals(crudAction.getEntityClassName(), "String");
  }
}


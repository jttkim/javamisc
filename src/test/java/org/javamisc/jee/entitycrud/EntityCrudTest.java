package org.javamisc.jee.entitycrud;

import org.junit.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.Set;
import java.util.HashSet;


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
}


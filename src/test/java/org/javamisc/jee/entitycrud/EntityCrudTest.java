package org.javamisc.jee.entitycrud;

import org.junit.*;

import java.util.Set;
import java.util.HashSet;


public class EntityCrudTest
{
  @Test
  public void testGenericTypecast()
  {
    Assert.assertTrue("something".equals(BeanUtil.extractPropertyName("getSomething")));
    Assert.assertTrue("something".equals(BeanUtil.extractPropertyName("setSomething")));
    Assert.assertTrue("something".equals(BeanUtil.extractPropertyName("isSomething")));
  }
}


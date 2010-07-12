package org.javamisc.jee.entitycrud;

import org.junit.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.Set;
import java.util.HashSet;

import javax.persistence.Id;
import javax.persistence.Entity;
import javax.persistence.Column;
import javax.persistence.OneToOne;
import javax.persistence.OneToMany;
import javax.persistence.ManyToOne;
import javax.persistence.ManyToMany;


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


@Entity
class DummyEntity
{
  private Integer id;
  private String name;
  private Double x;

  @Id
  public Integer getId()
  {
    return (this.id);
  }


  public void setId(Integer id)
  {
    this.id = id;
  }


  @Column(unique = true)
  public String getName()
  {
    return (this.name);
  }


  public void setName(String name)
  {
    this.name = name;
  }


  public Double getX()
  {
    return (this.x);
  }


  public void setX(Double x)
  {
    this.x = x;
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


  @Test
  public void testEntityAccessAdapter()
  {
    EntityAccessAdapter entityAccessAdapter = new EntityAccessAdapter();
    Assert.assertFalse(entityAccessAdapter.isEntityInstance("blah"));
  }


  @Test
  public void testSetProperty() throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException
  {
    DummyEntity e = new DummyEntity();
    Integer id = new Integer(4711);
    BeanUtil.setPropertyFromString(e, "id", String.format("%d", id.intValue()));
    Assert.assertEquals(id, BeanUtil.getProperty(e, "id"));
    String name = "Humpty Dumpty";
    BeanUtil.setPropertyFromString(e, "name", name);
    Assert.assertEquals(name, BeanUtil.getProperty(e, "name"));
    Double x = new Double(47.11);
    BeanUtil.setPropertyFromString(e, "x", String.format("%e", x.doubleValue()));
    Assert.assertEquals(x, BeanUtil.getProperty(e, "x"));
  }


  @Test
  public void testFindUnique()
  {
    Set uniquePropertyNameSet = BeanUtil.findUniquePropertyNameSet(DummyEntity.class);
    Assert.assertTrue(uniquePropertyNameSet.contains("id"));
    Assert.assertTrue(uniquePropertyNameSet.contains("name"));
    Assert.assertFalse(uniquePropertyNameSet.contains("x"));
  }
}


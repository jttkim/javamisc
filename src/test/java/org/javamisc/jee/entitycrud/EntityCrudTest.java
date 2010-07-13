package org.javamisc.jee.entitycrud;

import org.junit.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import javax.persistence.Id;
import javax.persistence.Entity;
import javax.persistence.Column;
import javax.persistence.OneToOne;
import javax.persistence.OneToMany;
import javax.persistence.ManyToOne;
import javax.persistence.ManyToMany;

import static org.javamisc.Util.genericTypecast;


class DummyCrudAction extends CrudAction
{
  public DummyCrudAction(EntityAccess entityAccess)
  {
    super(entityAccess);
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
  private DummyEntity dummyDaddy;
  private Collection<DummyEntity> dummyKidCollection;


  public DummyEntity()
  {
    super();
    this.dummyKidCollection = new HashSet<DummyEntity>();
  }


  public DummyEntity(Integer id)
  {
    this();
    this.id = id;
    name = String.format("dummy %d", id.intValue());
  }


  public DummyEntity(Integer id, String name, double x)
  {
    this(id);
    if (name != null)
    {
      this.name = name;
    }
    this.x = x;
  }


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
    if (this.name == null)
    {
      System.err.println("*** DummyEntity with null name ***");
    }
    else
    {
      System.err.printf("DummyEntity.getName: returning %s\n", this.name);
    }
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


  @ManyToOne
  public DummyEntity getDummyDaddy()
  {
    return (this.dummyDaddy);
  }


  public void setDummyDaddy(DummyEntity dummyDaddy)
  {
    this.dummyDaddy = dummyDaddy;
  }


  @OneToMany(mappedBy = "dummyDaddy")
  public Collection<DummyEntity> getDummyKidCollection()
  {
    return (this.dummyKidCollection);
  }


  public void setDummyKidCollection(Collection<DummyEntity> dummyKidCollection)
  {
    this.dummyKidCollection = dummyKidCollection;
  }
}


class DummyEntityAccess extends EntityAccessAdapter
{
  private Map<Integer, DummyEntity> dummyEntityMap;


  public DummyEntityAccess()
  {
    this.dummyEntityMap = new HashMap<Integer, DummyEntity>();
    DummyEntity d = new DummyEntity(new Integer(1), "daddy", 1000000.0);
    this.dummyEntityMap.put(d.getId(), d);
  }


  @Override
  public <EntityClass> EntityClass findEntity(Class<EntityClass> entityClass, Object id)
  {
    Integer entityId = (Integer) id;
    DummyEntity d;
    if (!this.dummyEntityMap.containsKey(entityId))
    {
      d = new DummyEntity(entityId, null, 0.0);
      this.dummyEntityMap.put(entityId, d);
    }
    else
    {
      d = this.dummyEntityMap.get(entityId);
    }
    if (entityId.intValue() != 1)
    {
      DummyEntity dummyDaddy = this.dummyEntityMap.get(new Integer(1));
      d.setDummyDaddy(dummyDaddy);
      dummyDaddy.getDummyKidCollection().add(d);
    }
    return ((EntityClass) d);
  }


  @Override
  public <EntityClass> List<EntityClass> findEntityList(Class<EntityClass> entityClass)
  {
    ArrayList<DummyEntity> l = new ArrayList<DummyEntity>(this.dummyEntityMap.values());
    ArrayList<EntityClass> ll = genericTypecast(l);
    return (ll);
  }


  @Override
  public boolean updateEntity(Class<?> entityClass, Integer entityId, Set<EntityOperation> entityOperationSet) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException
  {
    DummyEntity d;
    if (this.dummyEntityMap.containsKey(entityId))
    {
      d = this.dummyEntityMap.get(entityId);
    }
    else
    {
      d = new DummyEntity(entityId, null, 0.0);
    }
    for (EntityOperation op : entityOperationSet)
    {
      if (!op.apply(d, this))
      {
	return (false);
      }
    }
    this.dummyEntityMap.put(d.getId(), d);
    return (true);
  }


  @Override
  public boolean removeEntity(Class<?> entityClass, Integer entityId)
  {
    return (this.dummyEntityMap.remove(entityId) != null);
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


  @Test
  public void testEntityAccess()
  {
    DummyEntityAccess a = new DummyEntityAccess();
    DummyEntity d;
    for (int i = 2; i < 5; i ++)
    {
      d = a.findEntity(DummyEntity.class, new Integer(i));
    }
    d = a.findEntity(DummyEntity.class, new Integer(1));
    // nothing really to assert here
    a.fetchCollections(d);
  }


  @Test
  public void testCrudAction() throws Exception
  {
    CrudAction crudAction = new DummyCrudAction(new DummyEntityAccess());
    crudAction.setEntityId("4711");
    Assert.assertEquals(crudAction.getEntityId(), "4711");
    crudAction.setEntityClassName("String");
    Assert.assertEquals(crudAction.getEntityClassName(), "String");
    crudAction.setEntityClassName("DummyEntity");
    crudAction.getEntityHtml();
    crudAction.setEntityId("1");
    crudAction.getEntityHtml();
  }
}


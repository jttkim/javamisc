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
    // FIXME: might check that this.entityClassName equals "DummyEntity"
    return (DummyEntity.class);
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
  private DummyEntity daddy;
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
  public DummyEntity getDaddy()
  {
    return (this.daddy);
  }


  public void setDaddy(DummyEntity daddy)
  {
    this.daddy = daddy;
  }


  @OneToMany(mappedBy = "daddy")
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
      DummyEntity daddy = this.dummyEntityMap.get(new Integer(1));
      d.setDaddy(daddy);
      daddy.getDummyKidCollection().add(d);
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
  private static EntityAccess makeEntityAccess()
  {
    EntityAccess entityAccess = new DummyEntityAccess();
    DummyEntity d;
    for (int i = 2; i < 5; i ++)
    {
      d = entityAccess.findEntity(DummyEntity.class, new Integer(i));
    }
    return (entityAccess);
  }


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
  public void testEntityAccess()
  {
    EntityAccess a = makeEntityAccess();
    DummyEntity d;
    for (int i = 2; i < 5; i ++)
    {
      d = a.findEntity(DummyEntity.class, new Integer(i));
    }
    d = a.findEntity(DummyEntity.class, new Integer(1));
    // fetchCollections has side effects only, so nothing to assert here
    a.fetchCollections(d);
    Assert.assertFalse(a.isEntityInstance("blah"));
  }


  @Test
  public void testEntityOperation() throws Exception
  {
    EntityAccess entityAccess = makeEntityAccess();
    DummyEntity entity = entityAccess.findEntity(DummyEntity.class, new Integer(1));
    String name = "Humpty Dumpty";
    EntitySetPropertyOperation entitySetPropertyOperation = new EntitySetPropertyOperation("name", name);
    entitySetPropertyOperation.apply(entity, entityAccess);
    Assert.assertEquals(entity.getName(), name);
    Double x = new Double(15.08);
    EntitySetPropertyFromStringOperation entitySetPropertyFromStringOperation = new EntitySetPropertyFromStringOperation("x", x.toString());
    entitySetPropertyFromStringOperation.apply(entity, entityAccess);
    Assert.assertEquals(entity.getX(), x);
    Integer kidId = new Integer(3);
    DummyEntity kid = entityAccess.findEntity(DummyEntity.class, kidId);
    Integer daddyId = new Integer(2);
    EntityLinkOperation entityLinkOperation = new EntityLinkOperation("daddy", daddyId);
    entityLinkOperation.apply(kid, entityAccess);
    Assert.assertEquals(kid.getDaddy().getId(), daddyId);
    EntityUnlinkOperation entityUnlinkOperation = new EntityUnlinkOperation("daddy", daddyId);
    entityUnlinkOperation.apply(kid, entityAccess);
    Assert.assertEquals(kid.getDaddy(), null);
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
  public void testCrudAction() throws Exception
  {
    CrudAction crudAction = new DummyCrudAction(makeEntityAccess());
    crudAction.setEntityId("4711");
    Assert.assertEquals(crudAction.getEntityId(), "4711");
    crudAction.setEntityClassName("String");
    Assert.assertEquals(crudAction.getEntityClassName(), "String");
    crudAction.setEntityId(null);
    crudAction.setEntityClassName("DummyEntity");
    crudAction.getEntityHtml();
    crudAction.setEntityId("1");
    crudAction.getEntityHtml();
    crudAction.setCrudOp("form");
    crudAction.getEntityHtml();
    crudAction.setEntityId(null);
    crudAction.getEntityHtml();
    crudAction.setEntityId("3");
    crudAction.setCrudOp("delete");
    Assert.assertEquals("success", crudAction.execute());
    // FIXME: put together dummy request to test entity operations
  }
}


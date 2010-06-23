package org.javamisc.jee.entitycrud;

import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

import javax.naming.NamingException;
import javax.naming.InitialContext;

import javax.servlet.http.HttpServletRequest;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.ActionContext;
import org.apache.struts2.interceptor.SessionAware;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.dispatcher.SessionMap;

import static org.javamisc.Util.genericTypecast;
import static org.javamisc.jee.entitycrud.BeanUtil.constructDefaultInstance;
import static org.javamisc.jee.entitycrud.BeanUtil.extractPropertyName;
import static org.javamisc.jee.entitycrud.BeanUtil.getProperty;
import static org.javamisc.jee.entitycrud.BeanUtil.findUniquePropertyNameSet;
import static org.javamisc.jee.entitycrud.BeanUtil.findPropertyType;
import static org.javamisc.jee.entitycrud.BeanUtil.findPropertyNameSet;


public abstract class CrudAction extends ActionSupport implements ServletRequestAware
{
  protected int verbosityLevel;
  protected String entityClassName;
  protected String entityId;
  protected String crudOp;
  protected HttpServletRequest servletRequest;
  protected EntityAccess entityAccess;

  protected static final String crudParameterPrefix = "_crud_";


  public CrudAction(EntityAccess entityAccess)
  {
    super();
    this.entityAccess = entityAccess;
  }


  public static String htmlEscape(String s)
  {
    return(s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;"));
  }


  protected void logInfo(String message)
  {
    if (this.verbosityLevel > 0)
    {
      this.LOG.info(message);
    }
  }


  /**
   * Find the entity class based on the entityClassName property.
   */
  public abstract Class<?> findEntityClass() throws ClassNotFoundException;


  protected Object newEntity() throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException
  {
    if (this.entityClassName == null)
    {
      return (null);
    }
    else
    {
      return (constructDefaultInstance(this.findEntityClass()));
    }
  }


  protected Object retrieveEntity() throws ClassNotFoundException
  {
    if (this.entityClassName == null)
    {
      this.logInfo("Cannot retrieve entity: no entity class name");
      return (null);
    }
    else if (this.entityId == null)
    {
      this.logInfo(String.format("cannot retrieve entity %s: no entity id", this.entityClassName));
      return (null);
    }
    else
    {
      this.logInfo(String.format("entity name: %s, id: %s", this.entityClassName, this.entityId));
      Class<?> entityClass = this.findEntityClass();
      // FIXME: assuming retrieval by Integer id
      Object entity = this.entityAccess.findEntity(entityClass, new Integer(Integer.parseInt(this.entityId, 10)));
      return (entity);
    }
  }


  /**
   * Provide the text to be displayed in links to an entity.
   *
   * <p>This implementation provides a concatenation of name-value
   * pairs of all unique properties of the entity.</p>
   *
   * @param entity the entity
   * @return text for the link to the entity
   */
  public String entityLinkText(Object entity) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException
  {
    String s = "";
    String glue = "";
    for (String propertyName : findUniquePropertyNameSet(entity))
    {
      s += String.format("%s%s: %s", glue, propertyName, getProperty(entity, propertyName).toString());
      glue = ", ";
    }
    return (s);
  }


  protected String entityHtmlLink(Object entity) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException
  {
    // FIXME: just assuming that id is the id property, should really look for persistence annotation @Id
    // FIXME: hard-coded assumption that action name is "crud"
    Object id = getProperty(entity, "id");
    String s = String.format("<a href=\"crud?entityClassName=%s&entityId=%s\">%s</a>", htmlEscape(entity.getClass().getSimpleName()), htmlEscape(id.toString()), htmlEscape(entityLinkText(entity)));
    return (s);
  }


  protected String entityHtmlLinkList(Collection<?> entityCollection) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException
  {
    if (entityCollection.size() == 0)
    {
      return ("");
    }
    String s = "<ul>\n";
    for (Object entity : entityCollection)
    {
      s += String.format("<li>%s</li>\n", entityHtmlLink(entity));
    }
    s += "</ul>\n";
    return (s);
  }


  /**
   * Produce a simple menu of links.
   */
  protected String crudMenu()
  {
    String s = "<hr/>\n";
    s += "[";
    if (this.entityId != null)
    {
      s += String.format("<a href=\"crud?entityClassName=%s&entityId=%s\">show</a>", this.entityClassName, this.entityId);
      s += "|";
      s += String.format("<a href=\"crud?entityClassName=%s&entityId=%s&crudOp=form\">edit</a>", this.entityClassName, this.entityId);
      s += "|";
    }
    s += String.format("<a href=\"crud?entityClassName=%s&crudOp=form\">new <code>%s</code></a>", this.entityClassName, htmlEscape(this.entityClassName));
    s += "|";
    s += String.format("<a href=\"crud?entityClassName=%s\">list all <code>%s</code></a>", this.entityClassName, htmlEscape(this.entityClassName));
    s += "]\n";
    return (s);
  }


  protected String propertyHtmlTableRow(Object entity, String propertyName) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException
  {
    Object property = getProperty(entity, propertyName);
    Class<?> propertyType = findPropertyType(entity, propertyName);
    String s = "<tr>";
    s += String.format("<td>%s</td>", htmlEscape(propertyName));
    s += "<td>";
    if (property == null)
    {
      s += htmlEscape("<null>");
    }
    else if (Collection.class.isAssignableFrom(propertyType))
    {
      // FIXME: ought to check whether this is a set of entities
      Collection<?> propertyCollection = genericTypecast(property);
      s += entityHtmlLinkList(propertyCollection);
    }
    else if (this.entityAccess.isEntityInstance(property))
    {
      s += entityHtmlLink(property);
    }
    else
    {
      // FIXME: probably not suitable for all property types...
      s += htmlEscape(property.toString());
    }
    s += "</td>";
    s += "</tr>\n";
    return (s);
  }


  protected static List<String> orderedPropertyList(Class<?> entityClass)
  {
    Set<String> propertyNameSet = findPropertyNameSet(entityClass);
    CrudConfig crudConfig = entityClass.getAnnotation(CrudConfig.class);
    String[] propertyOrder = {"*"};
    if (crudConfig != null)
    {
      propertyOrder = crudConfig.propertyOrder();
    }
    List<String> orderedPropertyList = new ArrayList<String>();
    boolean addRemainingProperties = false;
    for (String propertyName : propertyOrder)
    {
      if ("*".equals(propertyName))
      {
	addRemainingProperties = true;
	break;
      }
      if (propertyNameSet.contains(propertyName))
      {
	propertyNameSet.remove(propertyName);
	orderedPropertyList.add(propertyName);
      }
    }
    if (addRemainingProperties)
    {
      orderedPropertyList.addAll(propertyNameSet);
    }
    return (orderedPropertyList);
  }


  protected String entityHtmlTable(Object entity) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException
  {
    List<String> propertyList = orderedPropertyList(entity.getClass());
    String s = "<table>\n";
    for (String propertyName : propertyList)
    {
      s += propertyHtmlTableRow(entity, propertyName);
    }
    s += "</table>\n";
    s += this.crudMenu();
    return (s);
  }


  /**
   * Determine whether a property should be hidden from CRUD views.
   *
   * @param entityClass the entity class
   * @param propertyName the name of the property to be checked
   * @return {@code true} if the property should be hidden
   */
  public abstract boolean isHidden(Class<?> entityClass, String propertyName);



  /**
   * Determine whether a property should be read only.
   *
   * <p>Read only properties cannot be updated via CRUD forms.</p>
   *
   * @param entityClass the entity class
   * @param propertyName the name of the property to be checked
   * @return {@code true} if the property should be read only
   */
  public abstract boolean isReadOnly(Class<?> entityClass, String propertyName);


  protected static String crudFormParameterName(String propertyName)
  {
    return (crudParameterPrefix + propertyName);
  }


  protected static boolean isEditableType(Class<?> c)
  {
    return (Integer.class.isAssignableFrom(c) || Double.class.isAssignableFrom(c) || String.class.isAssignableFrom(c));
  }


  protected String propertyHtmlFormRow(Object entity, String propertyName) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException
  {
    String s = "";
    Object property = getProperty(entity, propertyName);
    Class<?> propertyType = findPropertyType(entity, propertyName);
    if (Collection.class.isAssignableFrom(propertyType))
    {
      s += "<tr>";
      s += String.format("<td>%s</td>", htmlEscape(propertyName));
      s += "<td>";
      if (property == null)
      {
	s += "<strong><code>%s</code>: null collection</strong>";
      }
      else
      {
	// using multi-value for adding removing: first value is
	// command ("add" or "remove"), second value is id of entity
	// to be associated / disassociated
	s += String.format("<select name = %s>", crudFormParameterName(propertyName));
	s += "<option name=\"add\">add</option>";
	s += "<option name=\"remove\">remove</option>";
	s += "</select>";
	s += String.format("<input name=\"%s\" value=\"\"/>", crudFormParameterName(propertyName));
	Collection<?> collection = (Collection<?>) property;
	s += entityHtmlLinkList(collection);
      }
      s += "</td>";
      s += "</tr>\n";
    }
    else if (this.entityAccess.isEntityClass(propertyType))
    {
      s += "<tr>";
      s += String.format("<td>id of %s</td>", htmlEscape(propertyName));
      s += "<td>";
      String propertyValue = "";
      if (property != null)
      {
	Integer associatedEntityId = (Integer) getProperty(property, "id");
	propertyValue = associatedEntityId.toString();
      }
      s += String.format("<input name=\"%s\" value=\"%s\"/>", crudFormParameterName(propertyName), propertyValue);
      if (property != null)
      {
	s += "<br/>";
	s += entityHtmlLink(property);
      }
      s += "</td>";
      s += "</tr>\n";
    }
    else if (isEditableType(propertyType))
    {
      s += "<tr>";
      s += String.format("<td>%s</td>", htmlEscape(propertyName));
      s += "<td>";
      String propertyValue = "";
      if (property != null)
      {
	propertyValue = property.toString();
      }
      if (isReadOnly(entity.getClass(), propertyName))
      {
	s += htmlEscape(propertyValue);
      }
      else
      {
	s += String.format("<input name=\"%s\" value=\"%s\"/>", crudFormParameterName(propertyName), propertyValue);
      }
      s += "</td>";
      s += "</tr>\n";
    }
    else
    {
      s = propertyHtmlTableRow(entity, propertyName);
    }
    return (s);
  }


  protected String entityHtmlForm(Object entity) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException
  {
    // FIXME: consider introducing an IsacrodiEntity interface declaring getId (and setId), so that we can use typecasts to get at ids.
    Object id = getProperty(entity, "id");
    List<String> propertyNameList = orderedPropertyList(entity.getClass());
    // FIXME: order properties?
    String s = "<form method=\"post\" action=\"crud\">\n";
    s += "<input type=\"hidden\" name=\"crudOp\" value=\"update\"/>";
    s += String.format("<input type=\"hidden\" name=\"entityClassName\" value=\"%s\"/>", entity.getClass().getSimpleName());
    if (id != null)
    {
      s += String.format("<input type=\"hidden\" name=\"entityId\" value=\"%s\"/>", id.toString());
    }
    s += "<table>\n";
    for (String propertyName : propertyNameList)
    {
      if (!isHidden(entity.getClass(), propertyName))
      {
	if (isReadOnly(entity.getClass(), propertyName))
	{
	  s += propertyHtmlTableRow(entity, propertyName);
	}
	else
	{
	  s += propertyHtmlFormRow(entity, propertyName);
	}
      }
    }
    s += "<tr><td><input type=\"submit\"/></td><td></td></tr>\n";
    s += "</table>\n";
    s += "</form>\n";
    if (id != null)
    {
      s += "<form method=\"post\" action=\"crud\">\n";
      s += "<input type=\"hidden\" name=\"crudOp\" value=\"delete\"/>";
      s += String.format("<input type=\"hidden\" name=\"entityClassName\" value=\"%s\"/>", entity.getClass().getSimpleName());
      s += String.format("<input type=\"hidden\" name=\"entityId\" value=\"%s\"/>", id.toString());
      s += String.format("<input type=\"submit\" value=\"Delete %s\"/>", id.toString());
      s += "</form>\n";
    }
    s += this.crudMenu();
    return (s);
  }


  protected String entitySetHtmlList() throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, NoSuchMethodException
  {
    Class<?> entityClass = this.findEntityClass();
    List<?> entityList = this.entityAccess.findEntityList(entityClass);
    String s;
    if (entityList.size() == 0)
    {
      s = (String.format("<p>no entities of class <code>%s</code> found</p>\n", htmlEscape(entityClassName)));
    }
    else
    {
      s = entityHtmlLinkList(entityList);
    }
    s += this.crudMenu();
    return (s);
  }


  public String getEntityHtml() throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, InstantiationException
  {
    Object entity = this.retrieveEntity();
    if (entity != null)
    {
      if (this.crudOp != null)
      {
	if  ("form".equals(this.crudOp))
	{
	  return (entityHtmlForm(entity));
	}
      }
      return (entityHtmlTable(entity));
    }
    else if (this.entityClassName != null)
    {
      if ("form".equals(this.crudOp))
      {
	entity = this.newEntity();
	if (entity != null)
	{
	  return (entityHtmlForm(entity));
	}
      }
      return (entitySetHtmlList());
    }
    else
    {
      return ("<strong>no entity class name specified</strong>");
    }
  }


  public void setEntityClassName(String entityClassName)
  {
    this.entityClassName = entityClassName;
  }


  public String getEntityClassName()
  {
    return (this.entityClassName);
  }


  public void setEntityId(String entityId)
  {
    this.entityId = entityId;
  }


  public String getCrudOp()
  {
    return (this.crudOp);
  }


  public void setCrudOp(String crudOp)
  {
    this.crudOp = crudOp;
  }


  public void setVerbosityLevel(int verbosityLevel)
  {
    this.verbosityLevel = verbosityLevel;
  }


  public int getVerbosityLevel()
  {
    return (this.verbosityLevel);
  }


  public String getEntityId()
  {
    this.logInfo("returning entity id " + this.entityId);
    return (this.entityId);
  }


  public void setServletRequest(HttpServletRequest servletRequest)
  {
    this.servletRequest = servletRequest;
  }


  public HttpServletRequest getServletRequest()
  {
    return (this.servletRequest);
  }


  public Set<EntityOperation> getEntityOperationSet()
  {
    Set<EntityOperation> entityOperationSet = new HashSet<EntityOperation>();
    Map<String, String[]> parameterMap = genericTypecast(this.servletRequest.getParameterMap());
    for (String parameterName : parameterMap.keySet())
    {
      if (parameterName.startsWith(crudParameterPrefix))
      {
	// FIXME: should use extractor method
	String crudParameterName = parameterName.substring(crudParameterPrefix.length());
	// note that multi-value parameters are used for adding and removing from to-many sets
	String[] v = parameterMap.get(parameterName);
	// weed out empty strings -- this really should be a proper translation from form parameters to sanitised commands for the session bean
	if (v.length == 1)
	{
	  if (v[0].length() > 0)
	  {
	    entityOperationSet.add(new EntitySetPropertyFromStringOperation(crudParameterName, v[0]));
	  }
	}
	else if (v.length == 2)
	{
	  if ("add".equals(v[0]) || "add".equals(v[1]))
	  {
	    int idIndex = 0;
	    if ("add".equals(v[0]))
	    {
	      idIndex = 1;
	    }
	    if (v[idIndex].length() > 0)
	    {
	      entityOperationSet.add(new EntityLinkOperation(crudParameterName, new Integer(Integer.parseInt(v[idIndex]))));
	    }
	  }
	  else if ("remove".equals(v[0]) || "remove".equals(v[1]))
	  {
	    int idIndex = 0;
	    if ("add".equals(v[0]))
	    {
	      idIndex = 1;
	    }
	    if (v[idIndex].length() > 0)
	    {
	      entityOperationSet.add(new EntityUnlinkOperation(crudParameterName, new Integer(Integer.parseInt(v[idIndex]))));
	    }
	  }
	  else
	  {
	    throw new IllegalArgumentException(String.format("found neither add nor remove command for parameter \"%s\"", crudParameterName));
	  }
	}
	else
	{
	  throw new IllegalArgumentException(String.format("found %d values for parameter \"%s\"", v.length, crudParameterName));
	}
      }
    }
    return (entityOperationSet);
  }


  public void updateEntity() throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException
  {
    this.logInfo("updating entity");
    Set<EntityOperation> entityOperationSet = this.getEntityOperationSet();
    Integer id = null;
    if (this.entityId != null)
    {
      id = new Integer(Integer.parseInt(this.entityId));
    }
    this.entityAccess.updateEntity(this.findEntityClass(), id, entityOperationSet);
  }


  public void deleteEntity() throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException
  {
    if (this.entityClassName == null)
    {
      this.LOG.error("trying to delete an entity but no class name specified");
      return;
    }
    if (this.entityId == null)
    {
      this.LOG.error(String.format("trying to delete an entity of class %s without specifying an id", this.entityClassName));
      return;
    }
    Integer id = new Integer(Integer.parseInt(this.entityId));
    this.entityAccess.removeEntity(this.findEntityClass(), id);
    this.entityId = null;
  }


  public String execute() throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException
  {
    if ((this.crudOp != null) && this.crudOp.equals("update"))
    {
      this.updateEntity();
    }
    else if ((this.crudOp != null) && this.crudOp.equals("delete"))
    {
      this.deleteEntity();
    }
    return (SUCCESS);
  }
}

package org.javamisc;


/**
 * Utilities to support the entitycrud package, mostly reflection stuff.
 */
public class Util
{
  /**
   * Util is not to be instantiated.
   */
  private Util()
  {
  }


  /**
   * Helper method to cast values to parameterised types without
   * triggering an "unchecked" warning.
   *
   * <p>Idea found at
   * <a href="http://weblogs.java.net/blog/emcmanus/archive/2007/03/getting_rid_of.html">Eammon
   * McManus's blog</a>
   * </p>
   */
  @SuppressWarnings("unchecked")
  public static <T> T genericTypecast(Object o)
  {
    return ((T) o);
  }
}

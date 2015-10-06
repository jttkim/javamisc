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
   * Produce a string representation of a {@code String}, representing
   * a {@code null} reference as "&lt;null&gt;".
   *
   * <p>It is safe to call this method without checking that the
   * parameter is not {@code null}.</p>
   *
   * <p>Notice, however, that a string that contains the value {@code
   * &lt;null&gt;} has a representation that cannot be distinguished
   * from that of a {@code null} reference.</p>
   *
   * @param s the string variable to be represented
   * @return the representation
   */
  public static String safeStr(String s)
  {
    if (s == null)
    {
      return ("<null>");
    }
    else
    {
      return (s);
    }
  }


  /**
   * Split a string into words and trim leading and trailing whitespace.
   *
   * @param s the string to be split
   * @param regex the delimiting regular expression
   * @return an array containing the trimmed words
   */
  public static String[] splitTrim(String s, String regex)
  {
    String w[] = s.split(regex);
    for (int i = 0; i < w.length; i++)
    {
      w[i] = w[i].trim();
    }
    return (w);
  }


  /**
   * Produce a string representation of an {@code Integer},
   * representing a {@code null} reference as "&lt;null&gt;".
   *
   * <p>It is safe to call this method without checking that the
   * parameter is not {@code null}.</p>
   *
   * @param i the variable to be represented
   * @return the representation
   */
  public static String safeStr(Integer i)
  {
    if (i == null)
    {
      return ("<null>");
    }
    else
    {
      return (i.toString());
    }
  }


  /**
   * Produce a string representation of an {@code Boolean},
   * representing a {@code null} reference as "&lt;null&gt;".
   *
   * <p>It is safe to call this method without checking that the
   * parameter is not {@code null}.</p>
   *
   * @param b the variable to be represented
   * @return the representation
   */
  public static String safeStr(Boolean b)
  {
    if (b == null)
    {
      return ("<null>");
    }
    else
    {
      return (b.toString());
    }
  }


  /**
   * Produce a string representation of a {@code Double},
   * representing a {@code null} reference as "&lt;null&gt;".
   *
   * <p>It is safe to call this method without checking that the
   * parameter is not {@code null}.</p>
   *
   * @param d the variable to be represented
   * @return the representation
   */
  public static String safeStr(Double d)
  {
    if (d == null)
    {
      return ("<null>");
    }
    else
    {
      return (d.toString());
    }
  }


  /**
   * Helper method to cast values to parameterised types without
   * triggering an "unchecked" warning.
   *
   * <p>Idea found at
   * <a href="http://weblogs.java.net/blog/emcmanus/archive/2007/03/getting_rid_of.html">Eammon
   * McManus's blog</a>
   * </p>
   *
   * @param <T> the type to cast the object to
   * @param o the object
   * @return the object, cast to {@code T}
   */
  @SuppressWarnings("unchecked")
  public static <T> T genericTypecast(Object o)
  {
    return ((T) o);
  }
}

package org.javamisc;

import org.junit.*;

import java.util.Set;
import java.util.HashSet;


public class JavamiscTest
{
  @Test
  public void testGenericTypecast()
  {
    String testString = "this is a test";
    HashSet<String> testSet = new HashSet<String>();
    Object o = testSet;
    Set<String> castSet = Util.genericTypecast(o);
    castSet.add(testString);
    Assert.assertTrue(castSet.contains(testString));
  }


  @Test
  public void testSafeStr()
  {
    String s = null;
    Assert.assertEquals(Util.safeStr(s), "<null>");
    s = "blah";
    Assert.assertEquals(Util.safeStr(s), s);
    Integer i = null;
    Assert.assertEquals(Util.safeStr(i), "<null>");
    i = new Integer(4711);
    Assert.assertEquals(Util.safeStr(i), i.toString());
    Boolean b = null;
    Assert.assertEquals(Util.safeStr(b), "<null>");
    b = new Boolean(false);
    Assert.assertEquals(Util.safeStr(b), b.toString());
    Double d = null;
    Assert.assertEquals(Util.safeStr(d), "<null>");
    d = new Double(47.11);
    Assert.assertEquals(Util.safeStr(d), d.toString());
  }


  @Test
  public void splitTrimTest()
  {
    String s = "  this, stuff, was ,   written   , in, Monteria";
    String[] w = Util.splitTrim(s, ",");
    Assert.assertEquals(w[0], "this");
    Assert.assertEquals(w[1], "stuff");
    Assert.assertEquals(w[2], "was");
    Assert.assertEquals(w[3], "written");
    Assert.assertEquals(w[4], "in");
    Assert.assertEquals(w[5], "Monteria");
  }
}

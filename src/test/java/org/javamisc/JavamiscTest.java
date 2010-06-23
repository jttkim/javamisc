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
}


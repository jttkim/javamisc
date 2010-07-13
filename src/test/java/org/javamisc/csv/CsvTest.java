package org.javamisc.csv;

import org.junit.*;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.BufferedReader;

import java.util.Set;
import java.util.HashSet;
import java.util.Random;


public class CsvTest
{
  String xyData;


  private static int xyFunction(int x)
  {
    return (2 * x + 3);
  }


  private static String csvTextFunction(int i)
  {
    String text = "this is just some test";
    if (i % 4 == 1)
    {
      text = "\"this is quoted text\"";
    }
    else if (i % 4 == 2)
    {
      text = "\"this has \"\"escaped\"\" quotes\"";
    }
    else if (i % 4 == 3)
    {
      text = "this \"a\"ppears\"\" to have an escaped quote";
    }
    return (text);
  }


  private static String plainTextFunction(int i)
  {
    String text = "this is just some test";
    if (i % 4 == 1)
    {
      text = "this is quoted text";
    }
    else if (i % 4 == 2)
    {
      text = "this has \"escaped\" quotes";
    }
    else if (i % 4 == 3)
    {
      text = "this appears to have an escaped quote";
    }
    return (text);
  }


  @Before
  public void setUp()
  {
    Random rng = new Random(4711);
    this.xyData = "i,x,y,s\n";
    for (int i = 0; i < 10; i++)
    {
      int x = rng.nextInt(10);
      this.xyData += String.format("%d,%d,%d,%s\n", i, x, xyFunction(x), csvTextFunction(i));
    }
  }


  private BufferedReader getXYReader()
  {
    return new BufferedReader(new StringReader(this.xyData));
  }


  @Test
  public void testCsvTable() throws IOException
  {
    BufferedReader xyReader = this.getXYReader();
    CsvTable t = new CsvTable(new CsvReader(xyReader));
    Assert.assertEquals(t.getColumnNameList()[0], "i");
    Assert.assertEquals(t.getColumnNameList()[1], "x");
    Assert.assertEquals(t.getColumnNameList()[2], "y");
    Assert.assertEquals(t.getColumnNameList()[3], "s");
    while (t.next())
    {
      Assert.assertEquals(t.getString(1), t.getString("x"));
      Assert.assertEquals(t.getString(2), t.getString("y"));
      int i = Integer.parseInt(t.getString("i"));
      int x = Integer.parseInt(t.getString("x"));
      int y = Integer.parseInt(t.getString("y"));
      String s = t.getString("s");
      Assert.assertEquals(y, xyFunction(x));
      Assert.assertEquals(s, plainTextFunction(i));
    }
  }
}


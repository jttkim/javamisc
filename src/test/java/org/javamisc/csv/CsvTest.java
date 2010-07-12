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


  @Before
  public void setUp()
  {
    Random rng = new Random(4711);
    this.xyData = "x,y\n";
    for (int i = 0; i < 10; i++)
    {
      int x = rng.nextInt(10);
      this.xyData += String.format("%d,%d\n", x, xyFunction(x));
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
    Assert.assertEquals(t.getColumnNameList()[0], "x");
    Assert.assertEquals(t.getColumnNameList()[1], "y");
    while (t.next())
    {
      Assert.assertEquals(t.getString(0), t.getString("x"));
      Assert.assertEquals(t.getString(1), t.getString("y"));
      int x = Integer.parseInt(t.getString("x"));
      int y = Integer.parseInt(t.getString("y"));
      Assert.assertEquals(y, xyFunction(x));
    }
  }
}


package org.javamisc.csv;

import java.io.IOException;

import java.util.HashMap;


/**
 * Access a headed table stored in a CSV file (or other stream).
 *
 * <p>The first line in the CSV file is expected to contain column
 * names, it is an error if these names are not unique. Subsequent
 * lines are interpreted as rows of the table. This class is modelled
 * after the JDBC {@code ResultSet} interface in that row elements can
 * be accessed by column index or column name, and a {@code next}
 * method advances to the next row.</p>
 *
 * <p>{@code CsvTable} currently treats all cell content as strings,
 * but future versions may include facilities to determine column
 * types.</p>
 *
 * <p>This class is from grninfo and should be factored out to prevent
 * further duplications</p>
 *
 * @author Jan T. Kim <j.kim@uea.ac.uk>
 */
public class CsvTable
{
  protected CsvReader csvReader;
  protected String[] columnName;
  protected HashMap<String, Integer> columnIndexHash;
  protected String[] currentRow;


  /**
   * Constructor.
   *
   * @param csvReader the {@code csvReader} providing access to this table
   *
   * @throws IOException if the header row contains duplicate column names
   */
  public CsvTable(CsvReader csvReader) throws IOException
  {
    this.csvReader = csvReader;
    this.columnName = this.csvReader.nextLine();
    if (this.columnName == null)
    {
      throw new IOException("no header row");
    }
    for (int i = 0; i < this.columnName.length; i++)
    {
      for (int j = i + 1; j < this.columnName.length; j++)
      {
	if (this.columnName[i].equals(this.columnName[j]))
	{
	  throw new IOException(String.format("malformed header row: duplicate column name %s for columns %d and %d", this.columnName[i], i, j));
	}
      }
    }
    this.columnIndexHash = new HashMap<String, Integer>();
    for (int i = 0; i < this.columnName.length; i++)
    {
      this.columnIndexHash.put(this.columnName[i], new Integer(i));
    }
    this.currentRow = null;
  }


  /**
   * Get the column names in an array.
   *
   * <p>This array should be read only. Changing the array has
   * undefined effects.</p>
   *
   * @return an array containing the column names
   */
  public String[] getColumnNameList()
  {
    return (this.columnName);
  }


  /**
   * Advance to next row.
   *
   * @return {@code true} if there was a next row, {@code false} otherwise
   */
  public boolean next() throws IOException
  {
    this.currentRow = this.csvReader.nextLine();
    if (this.currentRow == null)
    {
      return (false);
    }
    int currentRowLength = this.currentRow.length;
    if (currentRowLength != this.columnName.length)
    {
      this.currentRow = null;
      throw new IOException(String.format("line %d: number of columns is %d (expected: %d)", this.csvReader.getLineNumber(), currentRow.length, this.columnName.length));
    }
    return (true);
  }


  /**
   * Access column element by index.
   *
   * @param columnIndex the column index
   *
   * @throws IllegalStateException if there is no current column
   * @throws IllegalArgumentException if the column index is out of range
   */
  public String getString(int columnIndex)
  {
    if (this.currentRow == null)
    {
      throw new IllegalStateException("no current row");
    }
    if (columnIndex >= this.columnName.length)
    {
      throw new IllegalArgumentException(String.format("column index %d is out of range [0, %d[", columnIndex, this.columnName.length));
    }
    return (this.currentRow[columnIndex]);
  }


  /**
   * Access column element by index.
   *
   * @param columnName the column's name
   *
   * @throws IllegalStateException if there is no current column
   * @throws IllegalArgumentException if the column name does not exist
   */
  public String getString(String columnName)
  {
    Integer columnIndex = this.columnIndexHash.get(columnName);
    if (columnIndex == null)
    {
      throw new IllegalArgumentException(String.format("no column named \"%s\"", columnName));
    }
    return (this.getString(columnIndex.intValue()));
  }
}

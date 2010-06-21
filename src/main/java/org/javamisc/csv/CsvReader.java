package org.javamisc.csv;

import java.io.BufferedReader;
import java.io.IOException;

import java.util.ArrayList;


/**
 * Class for reading comma-separated values (CSVs).
 *
 * <p>Values are provided line by line. Multi-line values are
 * currently not supported. All values are strings and may be empty.
 * Concatenation of quoted strings is supported. However, if the
 * escaped quote is not two (or more) consecutive quotes, this will
 * take precedence, i.e. two consecutive quotes will not be
 * interpreted as an empty quoted string, or as the border between two
 * quoted strings that are to be concatenated.</p>
 *
 * <p>This class is from grninfo and should be factored out to prevent
 * further duplications</p>
 *
 * @author Jan T. Kim <j.kim@uea.ac.uk>
 */
public class CsvReader
{
  private BufferedReader in;
  private String quoteChar;
  private String escapedQuote;
  private String separator;
  private int lineNumber;


  /**
   * Constructor.
   *
   * @param in the input from which to read lines
   * @param quoteChar the quote character
   * @param escapedQuote the escaped quote
   * @param separator the value separator
   */
  public CsvReader(BufferedReader in, String quoteChar, String escapedQuote, String separator)
  {
    this.in = in;
    this.quoteChar = quoteChar;
    this.escapedQuote = escapedQuote;
    this.separator = separator;
    this.lineNumber = 0;
  }


  /**
   * Constructor.
   *
   * <p>Constructs a {@code CsvReader} with quote char {@code "}
   * (double quote), escaped quote {@code ""} (two consecutive double
   * quotes), and separator {@code ,} (comma). This is suitable for
   * CSV generated by OpenOffice.org 3.1.</p>
   *
   * @param in the input from which to read lines
   */
  public CsvReader(BufferedReader in)
  {
    this(in, "\"", "\"\"", ",");
  }


  /**
   * Get the number of the last line read.
   *
   * @return the number of the last line read
   */
  public int getLineNumber()
  {
    return (this.lineNumber);
  }


  /**
   * Get the next line of values.
   *
   * @return the next line of values
   *
   * @throws IOException if a line ends inside a quoted string, or another I/O error occurs
   */
  public String[] nextLine() throws IOException
  {
    String line = this.in.readLine();
    if (line == null)
    {
      return (null);
    }
    this.lineNumber++;
    ArrayList<String> valueList = new ArrayList<String>();
    boolean quoteState = false;
    String value = "";
    while (line.length() > 0)
    {
      if (quoteState)
      {
	if (line.length() == 0)
	{
	  throw new IOException(String.format("line %d: end of line encountered in quoted string", this.lineNumber));
	}
	else if (line.startsWith(this.escapedQuote))
	{
	  value += this.quoteChar;
	  line = line.substring(this.escapedQuote.length());
	}
	else if (line.startsWith(this.quoteChar))
	{
	  quoteState = false;
	  line = line.substring(this.quoteChar.length());
	}
	else
	{
	  value += line.substring(0, 1);
	  line = line.substring(1);
	}
      }
      else
      {
	if (line.startsWith(this.separator))
	{
	  valueList.add(value);
	  value = "";
	  line = line.substring(this.separator.length());
	}
	else if (line.startsWith(this.quoteChar))
	{
	  quoteState = true;
	  line = line.substring(this.quoteChar.length());
	}
	else
	{
	  value += line.substring(0, 1);
	  line = line.substring(1);
	}
      }
    }
    valueList.add(value);
    return (valueList.toArray(new String[0]));
  }
}

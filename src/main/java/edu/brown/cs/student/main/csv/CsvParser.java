package edu.brown.cs.student.main.csv;

import static java.util.Collections.emptyList;

import edu.brown.cs.student.main.csv.creatorfromrow.*;
import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * A class for parsing through Reader objects to generate a list of CreatorFromRow objects
 *
 * @author Christina Peng
 */
public class CsvParser<T> {

  private final BufferedReader myBufferReader;
  private final CreatorFromRow<T> myParsedObject;

  public CsvParser(Reader reader, CreatorFromRow<T> parsedObject) {
    myBufferReader = new BufferedReader(reader);
    myParsedObject = parsedObject;
  }

  /**
   * parse() uses the Reader object to create a CreatorFromRow object for each row
   *
   * @return a list of CreatorFromRow objects
   * @throws IOException for failed or interrupted I/O operations
   * @throws FactoryFailureException for rows that cannot be parsed into a CreatorFromRow object
   */
  public List<T> parse() throws IOException, FactoryFailureException {
    String line = myBufferReader.readLine();
    List<T> parsedObjects = new ArrayList<>(emptyList());
    while (line != null) {
      String[] parsedLine = regexSplitCSVRow.split(line);
      String[] processedLine =
          Arrays.stream(parsedLine).map(CsvParser::postprocess).toArray(String[]::new);
      List<String> row = Arrays.asList(processedLine);
      T parsedObject = myParsedObject.create(row);
      parsedObjects.add(parsedObject);
      line = myBufferReader.readLine();
    }
    myBufferReader.close();
    return parsedObjects;
  }

  public static final Pattern regexSplitCSVRow =
      Pattern.compile(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*(?![^\\\"]*\\\"))");

  /**
   * Elimiate a single instance of leading or trailing double-quote, and replace pairs of double
   * quotes with singles.
   *
   * @param arg the string to process
   * @return the postprocessed string
   */
  public static String postprocess(String arg) {
    return arg
        // Remove extra spaces at beginning and end of the line
        .trim()
        // Remove a beginning quote, if present
        .replaceAll("^\"", "")
        // Remove an ending quote, if present
        .replaceAll("\"$", "")
        // Replace double-double-quotes with double-quotes
        .replaceAll("\"\"", "\"");
  }
}

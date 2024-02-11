package edu.brown.cs.student.main.csv;

import edu.brown.cs.student.main.csv.creatorfromrow.*;
import java.io.*;
import java.util.List;

/**
 * A class for searching through files based on a key word and optional column identifier
 *
 * @author Christina Peng
 */
public class CsvSearch {

  private static CsvParser<List<String>> MY_PARSER;
  private static CreatorFromRow<List<String>> MY_PARSED_OBJECT;

  public CsvSearch(CsvParser<List<String>> parser, CreatorFromRow<List<String>> parsedObject) {
    MY_PARSER = parser;
    MY_PARSED_OBJECT = parsedObject;
  }

  /**
   * Searches through a file for a target word
   *
   * @param filename name of the file to search
   * @param toSearch target word being searched
   * @throws IOException for failed or interrupted I/O operations
   * @throws FactoryFailureException for rows that cannot be parsed into a CreatorFromRow object
   */
  public void search(String filename, String toSearch) throws IOException, FactoryFailureException {
    tryOpenFile(filename);
    List<List<String>> rows = parseFile(filename);
    for (List<String> row : rows) {
      for (String word : row) {
        if (word.equals(toSearch)) {
          System.out.println(row);
          break;
        }
      }
    }
  }

  /**
   * Searches through a file for a target word based on a column index
   *
   * @param filename name of the file to search
   * @param toSearch target word being searched
   * @param columnIndex integer indicating column index to search from
   * @throws IOException for failed or interrupted I/O operations
   * @throws FactoryFailureException for rows that cannot be parsed into a CreatorFromRow object
   */
  public void search(String filename, String toSearch, Integer columnIndex)
      throws IOException, FactoryFailureException {
    tryOpenFile(filename);
    List<List<String>> rows = parseFile(filename);
    searchByColumnIndex(rows, toSearch, columnIndex);
  }

  /**
   * Searches through a file for a target word based on a header
   *
   * @param filename name of the file to search
   * @param toSearch target word being searched
   * @param header string indicating the header to search from
   * @throws IOException for failed or interrupted I/O operations
   * @throws FactoryFailureException for rows that cannot be parsed into a CreatorFromRow object
   */
  public void search(String filename, String toSearch, String header)
      throws IOException, FactoryFailureException {
    tryOpenFile(filename);
    List<List<String>> rows = parseFile(filename);
    List<String> firstRow = rows.get(0);
    for (int i = 0; i < firstRow.size(); i++) {
      if (firstRow.get(i).equals(header)) {
        searchByColumnIndex(rows, toSearch, i);
      }
    }
  }

  /**
   * throws SecurityException if attempting to access a file that's not in the data directory
   *
   * @param filename name of the file to parse
   * @throws SecurityException when file doesn't belong to data directory
   * @throws IOException for failed or interrupted I/O operations
   */
  private void tryOpenFile(String filename) throws IOException {
    File file = new File(filename);
    boolean fileInDataDirectory =
        file.getCanonicalPath().startsWith(System.getProperty("user.dir") + "/data/");
    if (!fileInDataDirectory) {
      throw new SecurityException("Error: attempting to access restricted file!");
    }
  }

  /**
   * parses a file into List<List<String>>
   *
   * @param filename name of the file to parse
   * @throws IOException for failed or interrupted I/O operations
   * @throws FactoryFailureException for rows that cannot be parsed into a CreatorFromRow object
   */
  public static List<List<String>> parseFile(String filename)
      throws IOException, FactoryFailureException {
    FileReader fileReader = new FileReader(filename);
    MY_PARSER = new CsvParser<List<String>>(fileReader, MY_PARSED_OBJECT);
    List<List<String>> rows = MY_PARSER.parse();
    if (rows.isEmpty()) {
      System.err.println("Note: This file is empty!");
    }
    return rows;
  }

  /**
   * Searches through a file for a target word based on a column index
   *
   * @param file name of the file to search
   * @param toSearch target word being searched
   * @param columnIndex integer indicating column index to search from
   * @throws IndexOutOfBoundsException for invalid column indexes
   */
  public static void searchByColumnIndex(
      List<List<String>> file, String toSearch, Integer columnIndex) {
    try {
      for (List<String> row : file) {
        if (row.get(columnIndex).equals(toSearch)) {
          System.out.println(row);
        }
      }
    } catch (IndexOutOfBoundsException e) {
      throw new IndexOutOfBoundsException("Error: searching using an invalid index!");
    }
  }
}

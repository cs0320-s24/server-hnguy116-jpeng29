package edu.brown.cs.student.csvtests;

import static edu.brown.cs.student.main.csv.CsvSearch.*;
import static org.junit.jupiter.api.Assertions.*;

import edu.brown.cs.student.main.csv.*;
import edu.brown.cs.student.main.csv.creatorfromrow.*;
import java.io.*;
import java.util.List;
import org.junit.jupiter.api.*;

public class SearchTest {

  private CreatorFromRow<List<String>> row;
  private ByteArrayOutputStream outputStream;
  String rootPath = System.getProperty("user.dir") + "/data/";

  @BeforeEach
  public void setUp() {
    row = new ParsedObject();
    outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));
  }

  @AfterEach
  public void tearDown() {
    System.setOut(System.out);
  }

  /** Search checks the entire file if not given a column identifier */
  @Test
  public void testSearchAll() throws IOException, FactoryFailureException {
    CsvParser<List<String>> parser =
        new CsvParser<List<String>>(new StringReader("Billy, Bob, Joe, Had, A, Time"), row);
    CsvSearch searcher = new CsvSearch(parser, row);
    searcher.search(rootPath + "census/income_by_race.csv", "White");
    System.setOut(System.out);
    String printedOutput = outputStream.toString();

    assertTrue(
        printedOutput.contains(
            "[1, White, 2020, 2020, 85359, 6432, Bristol County, RI, 05000US44001, bristol-county-ri]"));
    assertTrue(
        printedOutput.contains(
            "[1, White, 2013, 2013, 54104, 869, Providence County, RI, 05000US44007, providence-county-ri]"));
  }

  /** Search can match rows given a column index */
  @Test
  public void testSearchByColumnNum() throws IOException, FactoryFailureException {
    CsvParser<List<String>> parser =
        new CsvParser<List<String>>(new StringReader("Billy, Bob, Joe, Had, A, Time"), row);
    CsvSearch searcher = new CsvSearch(parser, row);
    searcher.search(rootPath + "census/income_by_race.csv", "White", 1);
    System.setOut(System.out);
    String printedOutput = outputStream.toString();

    assertTrue(
        printedOutput.contains(
            "[1, White, 2020, 2020, 85359, 6432, Bristol County, RI, 05000US44001, bristol-county-ri]"));
    assertTrue(
        printedOutput.contains(
            "[1, White, 2013, 2013, 54104, 869, Providence County, RI, 05000US44007, providence-county-ri]"));
  }

  /** Search does not output a matching row if the column index does not match */
  @Test
  public void searchRestrictsColumnNum() throws IOException, FactoryFailureException {
    CsvParser<List<String>> parser =
        new CsvParser<>(new StringReader("Billy, Bob, Joe, Had, A, Time"), row);
    CsvSearch searcher = new CsvSearch(parser, row);
    searcher.search(rootPath + "census/income_by_race.csv", "White", 0);
    System.setOut(System.out);
    String printedOutput = outputStream.toString();

    assertEquals(printedOutput, "");
    assertFalse(
        printedOutput.contains(
            "[1, White, 2020, 2020, 85359, 6432, Bristol County, RI, 05000US44001, bristol-county-ri]"));
    assertFalse(
        printedOutput.contains(
            "[1, White, 2013, 2013, 54104, 869, Providence County, RI, 05000US44007, providence-county-ri]"));
  }

  /** Search can search by header. */
  @Test
  public void testSearchByHeader() throws IOException, FactoryFailureException {
    CsvParser<List<String>> parser =
        new CsvParser<>(new StringReader("Billy, Bob, Joe, Had, A, Time"), row);
    CsvSearch searcher = new CsvSearch(parser, row);
    searcher.search(rootPath + "census/income_by_race.csv", "White", "Race");
    System.setOut(System.out);
    String printedOutput = outputStream.toString();

    assertTrue(
        printedOutput.contains(
            "[1, White, 2020, 2020, 85359, 6432, Bristol County, RI, 05000US44001, bristol-county-ri]"));
    assertTrue(
        printedOutput.contains(
            "[1, White, 2013, 2013, 54104, 869, Providence County, RI, 05000US44007, providence-county-ri]"));
    assertFalse(
        printedOutput.contains(
            "[2, Black, 2020, 2020, 45849, 6614, Washington County, RI, 05000US44009, washington-county-ri]"));
  }

  /** Search does not output a matching row if the header does not match. */
  @Test
  public void searchRestrictsByHeader() throws IOException, FactoryFailureException {
    CsvParser<List<String>> parser =
        new CsvParser<>(new StringReader("Billy, Bob, Joe, Had, A, Time"), row);
    CsvSearch searcher = new CsvSearch(parser, row);
    searcher.search(rootPath + "census/income_by_race.csv", "White", "Geography");
    System.setOut(System.out);
    String printedOutput = outputStream.toString();

    assertFalse(
        printedOutput.contains(
            "[1, White, 2020, 2020, 85359, 6432, Bristol County, RI, 05000US44001, bristol-county-ri]"));
    assertFalse(
        printedOutput.contains(
            "[1, White, 2013, 2013, 54104, 869, Providence County, RI, 05000US44007, providence-county-ri]"));
    assertEquals(printedOutput, "");
  }

  /** Search searches by the exact value and not substrings. */
  @Test
  public void testSearchByExactValue() throws IOException, FactoryFailureException {
    CsvParser<List<String>> parser =
        new CsvParser<>(new StringReader("Billy, Bob, Joe, Had, A, Time"), row);
    CsvSearch searcher = new CsvSearch(parser, row);
    searcher.search(rootPath + "census/income_by_race.csv", "White", "Race");
    System.setOut(System.out);
    String printedOutput = outputStream.toString();

    assertFalse(
        printedOutput.contains(
            "[8, White Non-Hispanic, 2020, 2020, 86463, 7051, Bristol County, RI, 05000US44001, bristol-county-ri]"));
  }

  /** Search is case-sensitive. */
  @Test
  public void testSearchCaseSensitive() throws IOException, FactoryFailureException {
    CsvParser<List<String>> parser =
        new CsvParser<>(new StringReader("Billy, Bob, Joe, Had, A, Time"), row);
    CsvSearch searcher = new CsvSearch(parser, row);
    searcher.search(rootPath + "census/income_by_race.csv", "white", "Race");
    System.setOut(System.out);
    String printedOutput = outputStream.toString();

    assertFalse(
        printedOutput.contains(
            "[1, White, 2020, 2020, 85359, 6432, Bristol County, RI, 05000US44001, bristol-county-ri]"));
  }

  /** Search can search on files with no headers. */
  @Test
  public void testSearchNoHeaders() throws IOException, FactoryFailureException {
    CsvParser<List<String>> parser =
        new CsvParser<>(new StringReader("Billy, Bob, Joe, Had, A, Time"), row);
    CsvSearch searcher = new CsvSearch(parser, row);
    searcher.search(rootPath + "numbers/no_header.csv", "100");
    System.setOut(System.out);
    String printedOutput = outputStream.toString();

    assertTrue(printedOutput.contains("[100, 2, 100, 2]"));
    assertFalse(printedOutput.contains("[9, 22, 12, 35]"));
  }

  /** Search searches through each duplicate header if there are any. */
  @Test
  public void testSearchDuplicateHeaders() throws IOException, FactoryFailureException {
    CsvParser<List<String>> parser =
        new CsvParser<>(new StringReader("Billy, Bob, Joe, Had, A, Time"), row);
    CsvSearch searcher = new CsvSearch(parser, row);
    searcher.search(rootPath + "numbers/duplicate_header.csv", "2", "Num");
    System.setOut(System.out);
    String printedOutput = outputStream.toString();

    assertTrue(printedOutput.contains("[1, 2, Normal Row]"));
    assertTrue(printedOutput.contains("[2, 3, Reverse Normal Row]"));
    assertFalse(printedOutput.contains("[Secret, Comment, 2]"));
  }

  /** searchByColumnIndex can search for a matching row in the given column index. */
  @Test
  public void testSearchByColumnIndex() {
    searchByColumnIndex(
        List.of(List.of("col0", "col1", "oops"), List.of("oops", "col1", "col2")), "oops", 2);
    System.setOut(System.out);
    String printedOutput = outputStream.toString();

    assertTrue(printedOutput.contains("[col0, col1, oops]"));
    assertFalse(printedOutput.contains("[oops, col1, col2]"));
  }

  /** searchByColumnIndex throws an IndexOutOfBoundsException for invalid column indexes */
  @Test
  public void testInvalidColumnIndex() {
    assertThrows(
        IndexOutOfBoundsException.class,
        () -> {
          searchByColumnIndex(List.of(List.of("col0", "col1", "word")), "word", -1);
        });
  }

  /** Search does not throw an exception for invalid headers */
  @Test
  public void testInvalidHeader() throws IOException, FactoryFailureException {
    CsvParser<List<String>> parser =
        new CsvParser<>(new StringReader("Billy, Bob, Joe, Had, A, Time"), row);
    CsvSearch searcher = new CsvSearch(parser, row);
    searcher.search(rootPath + "stars/stardata.csv", "Casey", "NULL");
    System.setOut(System.out);
    String printedOutput = outputStream.toString();

    assertEquals(printedOutput, "");
  }

  /**
   * Search throws SecurityException if trying to search through a file outside the data directory
   */
  @Test
  public void testSearchOutsideFile() {
    CsvParser<List<String>> parser =
        new CsvParser<>(new StringReader("Billy, Bob, Joe, Had, A, Time"), row);
    CsvSearch searcher = new CsvSearch(parser, row);
    assertThrows(
        SecurityException.class,
        () -> {
          searcher.search(System.getProperty("user.dir") + "/htmlReport/index.html", "2");
        });
    assertThrows(
        SecurityException.class,
        () -> {
          searcher.search(
              System.getProperty("user.dir") + "/data/../target/.mvn-classpath", "2", 1);
        });
    assertThrows(
        SecurityException.class,
        () -> {
          searcher.search(
              System.getProperty("user.dir") + "/data/../target/pom.xml.bak", "2", "Header");
        });
  }

  /** parseFile converts a file into List<List<String>> */
  @Test
  public void testParseFile() throws IOException, FactoryFailureException {
    List<List<String>> result = parseFile(rootPath + "numbers/no_header.csv");
    assertEquals(result, List.of(List.of("100", "2", "100", "2"), List.of("9", "22", "12", "35")));

    List<List<String>> starResult = parseFile(rootPath + "stars/stardata.csv");
    assertEquals(starResult.get(0), List.of("StarID", "ProperName", "X", "Y", "Z"));
    assertEquals(starResult.get(5), List.of("4", "Bailee", "79.62896", "0.01164", "-101.53103"));
  }

  /** parseFile throws a FileNotFoundException when given an invalid filename */
  @Test
  public void testFileNotFound() {
    assertThrows(
        FileNotFoundException.class,
        () -> {
          parseFile(rootPath + "nonExistent.csv");
        });
  }
}

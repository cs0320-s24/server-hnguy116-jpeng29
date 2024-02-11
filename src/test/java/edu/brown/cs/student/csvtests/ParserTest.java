package edu.brown.cs.student.csvtests;

import static edu.brown.cs.student.main.csv.CsvParser.*;
import static org.junit.jupiter.api.Assertions.*;

import edu.brown.cs.student.main.csv.*;
import edu.brown.cs.student.main.csv.creatorfromrow.*;
import edu.brown.cs.student.main.csv.objects.*;
import java.io.*;
import java.util.List;
import org.junit.jupiter.api.*;

public class ParserTest<T> {

  /** Tests from the provided code snippet */
  @Test
  public void testBasic() {
    String line = "Hello, World, 123, ABC";
    String[] result = regexSplitCSVRow.split(line);

    assertEquals(4, result.length);
    assertEquals("Hello", result[0].trim());
    assertEquals("World", result[1].trim());
    assertEquals("123", result[2].trim());
    assertEquals("ABC", result[3].trim());
  }

  @Test
  public void testQuotes() {
    String line = "\"Providence, RI\", 123.456, \"Telson, Nim\"";
    String[] result = regexSplitCSVRow.split(line);

    assertEquals(3, result.length);
    assertEquals("Providence, RI", result[0].trim().replaceAll("\"", ""));
    assertEquals("123.456", result[1].trim().replaceAll("\"", ""));
    assertEquals("Telson, Nim", result[2].trim().replaceAll("\"", ""));
  }

  @Test
  public void testQuotesWithEscaping() {
    String line =
        "\"Regular expressions are \"\"fun\"\"\", \"However, sometimes they are \"\"useful\"\"\"";
    String[] result = regexSplitCSVRow.split(line);

    assertEquals(2, result.length);
    assertEquals("Regular expressions are fun", result[0].trim().replaceAll("\"", ""));
    assertEquals("However, sometimes they are useful", result[1].trim().replaceAll("\"", ""));
    assertEquals("Regular expressions are \"fun\"", postprocess(result[0]));
    assertEquals("However, sometimes they are \"useful\"", postprocess(result[1]));
  }

  /** Tests I wrote for CsvParser */
  private CreatorFromRow<List<String>> row;

  private CreatorFromRow<Star> star;
  private CreatorFromRow<Person> person;
  String rootPath = System.getProperty("user.dir") + "/data/";

  @BeforeEach
  public void setUp() {
    row = new ParsedObject();
    star = new StarObject();
    person = new PersonObject();
  }

  /** Parser reads and splits a line of strings into List<List<String>> */
  @Test
  public void testParser() throws IOException, FactoryFailureException {
    CsvParser<List<String>> parser =
        new CsvParser<List<String>>(new StringReader("Billy, Bob, Joe, Had, A, Time"), row);
    List<List<String>> result = parser.parse();

    assertEquals(1, result.size());
    assertEquals("Billy", result.get(0).get(0));
    assertEquals("Bob", result.get(0).get(1));
    assertEquals("Joe", result.get(0).get(2));
    assertEquals("Had", result.get(0).get(3));
    assertEquals("A", result.get(0).get(4));
    assertEquals("Time", result.get(0).get(5));
  }

  /** Parser works on strings with multiple rows */
  @Test
  public void testMultipleRows() throws IOException, FactoryFailureException {
    CsvParser<List<String>> parser = new CsvParser<>(new StringReader("1,2,3\n4,5,6"), row);
    List<List<String>> result = parser.parse();

    assertEquals(2, result.size());
    assertEquals("1", result.get(0).get(0));
    assertEquals("2", result.get(0).get(1));
    assertEquals("3", result.get(0).get(2));
    assertEquals("4", result.get(1).get(0));
    assertEquals("5", result.get(1).get(1));
    assertEquals("6", result.get(1).get(2));
  }

  /** Parser works on large files */
  @Test
  public void testParserOnLargeFile() throws IOException, FactoryFailureException {
    CsvParser<List<String>> parser =
        new CsvParser<List<String>>(new FileReader(rootPath + "census/income_by_race.csv"), row);
    List<List<String>> result = parser.parse();

    assertEquals(324, result.size());
    assertEquals("ID Race", result.get(0).get(0));
    assertEquals("Bristol County, RI", result.get(1).get(6));
    assertEquals("2013", result.get(323).get(2));
  }

  /** Parser works on files with malformed rows */
  @Test
  public void testParserOnMalformed() throws IOException, FactoryFailureException {
    CsvParser<List<String>> parser =
        new CsvParser<List<String>>(
            new FileReader(rootPath + "malformed/malformed_signs.csv"), row);
    List<List<String>> result = parser.parse();

    assertEquals(13, result.size());
    assertEquals("Star Sign", result.get(0).get(0));
    assertEquals("Libra", result.get(7).get(0));
    assertEquals("Gabi", result.get(5).get(1));
  }

  /** Parser works on files without headers */
  @Test
  public void testParserNoHeaders() throws IOException, FactoryFailureException {
    CsvParser<List<String>> parser =
        new CsvParser<List<String>>(new FileReader(rootPath + "numbers/no_header.csv"), row);
    List<List<String>> result = parser.parse();

    assertEquals(2, result.size());
    assertEquals("100", result.get(0).get(0));
    assertEquals("35", result.get(1).get(3));
    assertEquals(List.of("9", "22", "12", "35"), result.get(1));
  }

  /** Parser works on different CreatorFromRow classes like Star */
  @Test
  public void testParserOnStar() throws IOException, FactoryFailureException {
    CsvParser<Star> parser =
        new CsvParser<Star>(new StringReader("1,Andreas,282.0,0.00449,5.36884"), star);
    List<Star> result = parser.parse();

    assertEquals(1, result.size());
    assertEquals(1, result.get(0).myStarID);
    assertEquals("Andreas", result.get(0).myProperName);
    assertEquals(282.0, result.get(0).myX);
    assertEquals(0.00449, result.get(0).myY);
    assertEquals(5.36884, result.get(0).myZ);
  }

  /** Parser works on different CreatorFromRow classes like Person */
  @Test
  public void testParserOnMultiplePersons() throws IOException, FactoryFailureException {
    CsvParser<Person> parser =
        new CsvParser<Person>(new StringReader("Leo, English, 20\nEmily, 12"), person);
    List<Person> result = parser.parse();

    assertEquals(2, result.size());
    assertEquals("Leo", result.get(0).myName);
    assertEquals("English", result.get(0).myConcentration);
    assertEquals(20, result.get(0).myAge);
    assertEquals("Emily", result.get(1).myName);
    assertEquals(12, result.get(1).myAge);
  }

  /** Parser throws FactoryFailureException when given row cannot be parsed into the object */
  @Test
  public void testFactoryFailure() throws IOException, FactoryFailureException {
    CsvParser<Person> peopleParser =
        new CsvParser<Person>(new StringReader("Leo, English"), person);
    assertThrows(FactoryFailureException.class, peopleParser::parse);

    CsvParser<Star> starParser = new CsvParser<Star>(new StringReader("Jupiter, 100\nMars"), star);
    assertThrows(FactoryFailureException.class, starParser::parse);
  }

  /** Parser has no restriction on which directory files come from */
  @Test
  public void testParserOutsideFile() throws IOException, FactoryFailureException {
    CsvParser<List<String>> parser =
        new CsvParser<List<String>>(
            new FileReader(System.getProperty("user.dir") + "/htmlReport/index.html"), row);
    assertDoesNotThrow(parser::parse);
  }
}

package edu.brown.cs.student.main.csv.creatorfromrow;

import java.util.List;

/**
 * A ParsedObject class that implements the CreatorFromRow interface
 *
 * @author Christina Peng
 */
public class ParsedObject implements CreatorFromRow<List<String>> {
  public ParsedObject() {}

  public List<String> create(List<String> stringList) {
    return stringList;
  }
}

package edu.brown.cs.student.main.csv.creatorfromrow;

import edu.brown.cs.student.main.csv.objects.Star;
import java.util.List;

/**
 * A Star class that implements the CreatorFromRow interface
 *
 * @author Christina Peng
 */
public class StarObject implements CreatorFromRow<Star> {

  /**
   * creates a Star object from a list of strings
   *
   * @param stringList strings to use for constructing Star
   * @throws FactoryFailureException for list of strings that cannot be turned into Star
   */
  public Star create(List<String> stringList) throws FactoryFailureException {
    try {
      return new Star(
          Integer.parseInt(stringList.get(0)),
          stringList.get(1),
          Double.parseDouble(stringList.get(2)),
          Double.parseDouble(stringList.get(3)),
          Double.parseDouble(stringList.get(4)));
    } catch (Exception e) {
      throw new FactoryFailureException("Error while creating Star from row: ", stringList);
    }
  }
}

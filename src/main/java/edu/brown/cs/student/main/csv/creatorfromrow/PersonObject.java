package edu.brown.cs.student.main.csv.creatorfromrow;

import edu.brown.cs.student.main.csv.objects.Person;
import java.util.List;

/**
 * A Person class that implements the CreatorFromRow interface
 *
 * @author Christina Peng
 */
public class PersonObject implements CreatorFromRow<Person> {

  /**
   * creates a Person object from a list of strings
   *
   * @param stringList strings to use for constructing Person
   * @throws FactoryFailureException for list of strings that cannot be turned into Person
   */
  public Person create(List<String> stringList) throws FactoryFailureException {
    try {
      if (stringList.size() == 2) {
        return new Person(stringList.get(0), Integer.parseInt(stringList.get(1)));
      } else {
        return new Person(
            stringList.get(0), stringList.get(1), Integer.parseInt(stringList.get(2)));
      }
    } catch (Exception e) {
      throw new FactoryFailureException("Error while creating object from row: ", stringList);
    }
  }
}

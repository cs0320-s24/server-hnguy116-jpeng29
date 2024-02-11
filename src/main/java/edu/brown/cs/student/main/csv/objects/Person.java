package edu.brown.cs.student.main.csv.objects;

/**
 * A Person object class
 *
 * @author Christina Peng
 */
public class Person {
  public String myName;
  public String myConcentration;
  public int myAge;

  public Person(String name, String concentration, int age) {
    myName = name;
    myConcentration = concentration;
    myAge = age;
  }

  public Person(String name, int age) {
    myName = name;
    myAge = age;
  }
}

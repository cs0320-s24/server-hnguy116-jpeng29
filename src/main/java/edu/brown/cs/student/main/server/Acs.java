package edu.brown.cs.student.main.server;

public class Acs {
  private String name;
  private String state;
  private String county;
  private int stateCode;
  private int countyCode;

  public Acs() {}

  @Override
  public String toString() {
    return this.county
        + " in "
        + this.state
        + " has "
        + "percentage of households with broadband "
        + "access. \n Data retrieved at: "
        + "time data was retrieved";
  }
}

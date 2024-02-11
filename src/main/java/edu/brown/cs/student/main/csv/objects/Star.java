package edu.brown.cs.student.main.csv.objects;

/**
 * A Star object class
 *
 * @author Christina Peng
 */
public class Star {
  public int myStarID;
  public String myProperName;
  public double myX;
  public double myY;
  public double myZ;

  public Star(int starID, String properName, double x, double y, double z) {
    myStarID = starID;
    myProperName = properName;
    myX = x;
    myY = y;
    myZ = z;
  }
}

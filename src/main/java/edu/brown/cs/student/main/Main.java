package edu.brown.cs.student.main;

import edu.brown.cs.student.main.csv.*;
import edu.brown.cs.student.main.csv.creatorfromrow.*;
import java.io.*;
import java.util.*;

public final class Main {

  /**
   * The initial method called when execution begins.
   *
   * @param args An array of command line arguments
   */
  // public args;
  public static void main(String[] args) {
    new Main(args).run();
  }

  private Main(String[] args) {
    CreatorFromRow<List<String>> parsedObject = new ParsedObject();
    CsvParser<List<String>> parser =
        new CsvParser<List<String>>(new StringReader("Billy"), parsedObject);
    CsvSearch csvSearch = new CsvSearch(parser, parsedObject);
    String rootPath = System.getProperty("user.dir") + "/data/";
    String welcomeMessage =
        """
            Welcome to Search, your program for searching through files based on a search term.
            Enter a Search command by specifying the filename as the first argument,
            the search term as the second argument, and a header or column index to search
            from as an optional third argument.

            Note: Only files in your data directory can be accessed.

            """;
    System.out.print(welcomeMessage);
    Scanner scanner = new Scanner(System.in);
    while (true) {
      System.out.print("Enter a Search command (or 'exit' to quit): ");
      String command = scanner.nextLine();
      if (command.equalsIgnoreCase("exit")) {
        break;
      }
      String[] commandParts = command.split("\\s+");
      if (commandParts.length < 2) {
        System.out.println("Invalid command. Please provide a valid search command.");
        continue;
      }
      String filename = rootPath + commandParts[0];
      String toSearch = commandParts[1];
      try {
        if (commandParts.length == 2) {
          csvSearch.search(filename, toSearch);
        } else {
          try {
            Integer columnIndex = Integer.parseInt(commandParts[2]);
            csvSearch.search(filename, toSearch, columnIndex);
          } catch (NumberFormatException e) {
            StringBuilder header = new StringBuilder(commandParts[2]);
            if (commandParts.length > 3) {
              for (int i = 3; i < commandParts.length; i++) {
                header.append(" ").append(commandParts[i]);
              }
            }
            csvSearch.search(filename, toSearch, header.toString());
          }
        }
      } catch (FactoryFailureException e) {
        System.err.println("Factory Failure Exception: " + e.getMessage());
        System.exit(1);
      } catch (FileNotFoundException e) {
        System.err.println("File not found: " + filename);
        System.exit(1);
      } catch (IOException e) {
        System.err.println("IO Exception: " + e.getMessage());
        System.exit(1);
      } catch (SecurityException e) {
        System.err.println("Restricted file: " + filename);
        System.exit(1);
      } catch (IndexOutOfBoundsException e) {
        System.err.println("Invalid column identifier: " + commandParts[2]);
        System.exit(1);
      }
      System.out.println("Search results have been generated.");
    }
  }

  private void run() {
    System.out.println(
        "Your horoscope for this project:\n"
            + "Entrust in the Strategy pattern, and it shall give thee the sovereignty to "
            + "decide and the dexterity to change direction in the realm of thy code.");
  }
}

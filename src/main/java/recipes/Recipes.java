// Copyright (c) 2021 Promineo Tech

package recipes;

import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import recipes.entity.Recipe;
import recipes.exception.DbException;
import recipes.service.RecipeService;

/**
 * This class contains the "main" method - the entry point to a Java
 * application.
 * 
 * @author Promineo
 *
 */
public class Recipes {
  private Scanner scanner = new Scanner(System.in);
  private RecipeService recipeService = new RecipeService();

  /* This is the list of available operations */
  // @formatter:off
  private List<String> operations = List.of(
      "1) Create and populate all tables",
      "2) Add a recipe"
  );
  // @formatter:on

  /**
   * Entry point for a Java application.
   * 
   * @param args Command line arguments. Ignored.
   */
  public static void main(String[] args) {
    new Recipes().displayMenu();
  }

  /**
   * This method displays the menu selections (available operations), gets the
   * user menu selection, and acts on that selection.
   */
  private void displayMenu() {
    boolean done = false;

    while (!done) {
      try {
        int operation = getOperation();

        switch (operation) {
          case -1:
            done = exitMenu();
            break;

          case 1:
            createTables();
            break;

          case 2:
            addRecipe();
            break;

          default:
            System.out.println("\n" + operation + " is not valid. Try again.");
            break;
        }
      } catch (Exception e) {
        System.out.println("\nError: " + e.toString() + " Try again.");
      }
    }
  }

  /**
   * Add a recipe (without ingredients, steps, or categories).
   */
  private void addRecipe() {
    /* Get the recipe input from the user. */
    String name = getStringInput("Enter the recipe name");
    String notes = getStringInput("Enter the recipe notes");
    Integer numServings = getIntInput("Enter number of servings");
    Integer prepMinutes = getIntInput("Enter prep time in minutes");
    Integer cookMinutes = getIntInput("Enter cook time in minutes");

    LocalTime prepTime = minutesToLocalTime(prepMinutes);
    LocalTime cookTime = minutesToLocalTime(cookMinutes);

    /* Create a recipe object from the user input. */
    Recipe recipe = new Recipe();

    recipe.setRecipeName(name);
    recipe.setNotes(notes);
    recipe.setNumServings(numServings);
    recipe.setPrepTime(prepTime);
    recipe.setCookTime(cookTime);

    /*
     * Add the recipe to the recipe table. This will throw an unchecked
     * exception if there is an error. The exception is picked up by the
     * exception handler in displayMenu(). This keeps the code very clean and
     * readable.
     */
    Recipe dbRecipe = recipeService.addRecipe(recipe);
    System.out.println("You added this recipe:\n" + dbRecipe);
  }

  /**
   * Convert from an Integer minute value to a LocalTime object.
   * 
   * @param numMinutes The number of minutes. This may be {@code null}, in which
   *        case the number of minutes is set to zero.
   * @return A LocalTime object containing the number of hours and minutes.
   */
  private LocalTime minutesToLocalTime(Integer numMinutes) {
    int min = Objects.isNull(numMinutes) ? 0 : numMinutes;
    int hours = min / 60;
    int minutes = min % 60;

    return LocalTime.of(hours, minutes);
  }

  /**
   * This is called to programmatically drop all the tables, recreate them and
   * populate them with data. It resets the table data to a known, initial
   * state.
   */
  private void createTables() {
    recipeService.createAndPopulateTables();
    System.out.println("\nTables created and populated!");
  }

  /**
   * This is called when the user is exiting the menu. It simply returns
   * {@code true}, which will cause the menu to exit. This is not the best
   * approach, as there is no guarantee what the caller will do with the return
   * value. It does, however, keep the switch statement in the menu code
   * cleaner.
   * 
   * @return {@code true} to cause the menu to exit.
   */
  private boolean exitMenu() {
    System.out.println("\nExiting the menu. TTFN!");
    return true;
  }

  /**
   * This prints the available options and then asks the user to input a menu
   * selection. If the user enters nothing, -1 is returned, which will cause the
   * application to exit.
   * 
   * @return The user's menu selection, or -1 if the user pressed Enter without
   *         making a selection.
   */
  private int getOperation() {
    printOperations();

    Integer op =
        getIntInput("\nEnter an operation number (press Enter to quit)");

    return Objects.isNull(op) ? -1 : op;
  }

  /**
   * Print the available menu operations to the console, with each operation on
   * a separate line.
   */
  private void printOperations() {
    System.out.println();
    System.out.println("Here's what you can do:");

    /*
     * Every List has a forEach method. The forEach method takes a Consumer as a
     * parameter. This uses a Lambda expression to supply the Consumer. Consumer
     * is a functional interface that has a single abstract method accept. The
     * accept method returns nothing (void) and accepts a single parameter. This
     * Lambda expression returns nothing (System.out.println has a void return
     * value). It has a single parameter (op), which meets the requirements of
     * Consumer.accept().
     */
    operations.forEach(op -> System.out.println("   " + op));
  }

  /**
   * Get the user's input and convert it to an Integer.
   * 
   * @param prompt The prompt that is printed to the console.
   * @return This will return {@code null} if the user presses Enter without
   *         entering anything. It returns an Integer if the user enters a valid
   *         integer.
   * @throws DbException [Unchecked exception] Thrown if the input cannot be
   *         converted to an Integer.
   */
  private Integer getIntInput(String prompt) {
    String input = getStringInput(prompt);

    if (Objects.isNull(input)) {
      return null;
    }

    try {
      return Integer.parseInt(input);
    } catch (NumberFormatException e) {
      throw new DbException(input + " is not a valid number.");
    }
  }

  /**
   * Get the user's input and convert it to a Double.
   * 
   * @param prompt The prompt that is printed to the console.
   * @return This will return {@code null} if the user presses Enter without
   *         entering anything. It returns a Double if the user enters a valid
   *         Double.
   * @throws DbException [Unchecked exception] Thrown if the input cannot be
   *         converted to a Double.
   */
  @SuppressWarnings("unused")
  private Double getDoubleInput(String prompt) {
    String input = getStringInput(prompt);

    if (Objects.isNull(input)) {
      return null;
    }

    try {
      return Double.parseDouble(input);
    } catch (NumberFormatException e) {
      throw new DbException(input + " is not a valid number.");
    }
  }

  /**
   * This prints the prompt to the console and collects the user's input. If the
   * user's input is all blank or empty, {@code null} is returned.
   * 
   * @param prompt The prompt that is printed to the console.
   * @return {@code null} if the input is empty or blank. Otherwise the trimmed
   *         input is returned.
   */
  private String getStringInput(String prompt) {
    System.out.print(prompt + ": ");
    String line = scanner.nextLine();

    return line.isBlank() ? null : line.trim();
  }

}

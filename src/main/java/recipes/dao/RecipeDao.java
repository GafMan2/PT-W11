// Copyright (c) 2022 Promineo Tech

package recipes.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalTime;
import java.util.List;
import provided.util.DaoBase;
import recipes.entity.Recipe;
import recipes.exception.DbException;

/**
 * This class performs CRUD (Create, Read, Update and Delete) operations on
 * tables in the recipe schema. Connections are obtained from
 * {@link DbConnection#getConnection()}. All operations within a transaction
 * must be made on the same connection. The strategy is to use try-with-resource
 * to ensure that resources are always closed properly. The approach looks like
 * this:
 * 
 * <pre>
 * String sql = "...";
 * 
 * try(Connection conn = DbConnection.getConnection()) {
 *   try(PreparedStatement stmt = conn.prepareStatement(sql)) {
 *     setParameter(stmt, 1, parm1, Parm1.class);
 *     ...
 *     
 *     try(ResultSet rs = stmt.executeQuery()) {
 *       while(rs.next) {
 *         <em>Object<em> value = extract(rs, <em>Object</em>.class);
 *         // Where <em>Object</em> is the actual entity type: Recipe, etc.
 *       }
 *     }
 *     
 *     commitTransaction(conn);
 *     return value;
 *   }
 *   catch(Exception e) {
 *     rollbackTransaction(conn);
 *     throw new DbException(e);
 *   }
 * catch(SQLException e) {
 *   throw new DbException(e);
 * }
 * </pre>
 * 
 * @author Promineo
 *
 */
@SuppressWarnings("unused")
public class RecipeDao extends DaoBase {
  private static final String CATEGORY_TABLE = "category";
  private static final String INGREDIENT_TABLE = "ingredient";
  private static final String RECIPE_TABLE = "recipe";
  private static final String RECIPE_CATEGORY = "recipe_category";
  private static final String STEP_TABLE = "step";
  private static final String UNIT_TABLE = "unit";

  /**
   * Insert a recipe into the recipe table. This uses a
   * {@link PreparedStatement} so that typed parameters can be passed into the
   * statement, allowing validation and safety checks to be performed. This will
   * mitigate against SQLInjection attacks.
   * 
   * @param recipe The recipe to be inserted.
   * @return The recipe with the primary key value.
   */
  public Recipe insertRecipe(Recipe recipe) {
    /*
     * Note that the primary key (recipe_id) is not included in the list of
     * fields in the insert statement. MySQL will set the correct primary key
     * value when the row is inserted.
     */
    // @formatter:off
    String sql = ""
        + "INSERT INTO " + RECIPE_TABLE + " "
        + "(recipe_name, notes, num_servings, prep_time, cook_time) "
        + "VALUES "
        + "(?, ?, ?, ?, ?)";
    // @formatter:on

    try (Connection conn = DbConnection.getConnection()) {
      startTransaction(conn);

      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        setParameter(stmt, 1, recipe.getRecipeName(), String.class);
        setParameter(stmt, 2, recipe.getNotes(), String.class);
        setParameter(stmt, 3, recipe.getNumServings(), Integer.class);
        setParameter(stmt, 4, recipe.getPrepTime(), LocalTime.class);
        setParameter(stmt, 5, recipe.getCookTime(), LocalTime.class);

        /*
         * Insert the row. Statement.executeUpdate() performs inserts,
         * deletions, and modifications. It does all operations that do not
         * return result sets.
         */
        stmt.executeUpdate();

        /*
         * Call a method in the base class to get the last insert ID (primary
         * key value) in the given table.
         */
        Integer recipeId = getLastInsertId(conn, RECIPE_TABLE);

        commitTransaction(conn);

        /*
         * Set the recipe ID primary key to the value obtained by
         * getLastInsertId(). This does not fill in the createdAt field. To get
         * that value we would need to do a fetch on the recipe row.
         */
        recipe.setRecipeId(recipeId);
        return recipe;
      } catch (Exception e) {
        rollbackTransaction(conn);
        throw new DbException(e);
      }
    } catch (SQLException e) {
      throw new DbException(e);
    }
  }

  /**
   * This method takes a list of SQL statements, which will be executed as a
   * batch.
   * 
   * @param sqlBatch A list of SQL statements that are executed in order.
   */
  public void executeBatch(List<String> sqlBatch) {
    try (Connection conn = DbConnection.getConnection()) {
      startTransaction(conn);

      try (Statement stmt = conn.createStatement()) {
        /*
         * Add each SQL line to the Statement so they can be executed as a
         * batch.
         */
        for (String sql : sqlBatch) {
          stmt.addBatch(sql);
        }

        stmt.executeBatch();
        commitTransaction(conn);

      } catch (Exception e) {
        rollbackTransaction(conn);
        throw new DbException(e);
      }
    } catch (SQLException e) {
      throw new DbException(e);
    }
  }
}

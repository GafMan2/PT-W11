package projects.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import projects.entity.Category;
import projects.entity.Material;
import projects.entity.Project;
import projects.entity.Step;
import projects.exception.DbException;
import provided.util.DaoBase;


public class ProjectDao extends DaoBase {

	private static final String PROJECT_CATEGORY_TABLE = "project_category";
    private static final String MATERIAL_TABLE = "material";
    private static final String STEP_TABLE = "step";
    private static final String CATEGORY_TABLE = "category";
    private static final String PROJECT_TABLE = "project";
	
    public Project insertProject(Project project) {  
        // @formatter:off
    	String sql = ""
    	        + "INSERT INTO " + PROJECT_TABLE + " "
    	        + "(project_name, estimated_hours, actual_hours, difficulty, notes) "
    	        + "VALUES "
    	        + "(?, ?, ?, ?, ?)";
    	// @formatter:on		
    			
        try (Connection conn = DbConnection.getConnection()) {
            startTransaction(conn);

            try(PreparedStatement stmt = conn.prepareStatement(sql)) {
                setParameter(stmt, 1, project.getProjectName(), String.class);
                setParameter(stmt, 2, project.getEstimatedHours(), BigDecimal.class);
                setParameter(stmt, 3, project.getActualHours(), BigDecimal.class);
                setParameter(stmt, 4, project.getDifficulty(), Integer.class);
                setParameter(stmt, 5, project.getNotes(), String.class);
                stmt.executeUpdate();

                Integer projectId = getLastInsertId(conn, PROJECT_TABLE);
                commitTransaction(conn);

                project.setProjectId(projectId);
                return project;
              }
              catch(Exception e) {
                rollbackTransaction(conn);
                throw new DbException(e);
              }
            }
            catch(SQLException e) {
              throw new DbException(e);
            }
          }

    // Added a new method fetchProjectById 
    // I wrote a SQL statement using a wild card to return all columns
    // from the Project table in the row project_id.
    public Optional<Project> fetchProjectById(Integer projectId) {
        String sql = "SELECT * FROM " + PROJECT_TABLE + " WHERE project_ID = ?";

        try (Connection conn = DbConnection.getConnection()) {
            startTransaction(conn);  // used a try/catch block to obtain a connection
            
           // Something new this time, inside our try block we created a variable of type project
           // and set it to null and then we return the project object as an optional object 
           // using optional.ofNullable 
           // If a project is found with the provided ID, it will be wrapped in an Optional.ofNullable(project).
           // and if no project is found, an empty Optional will be returned.
            try {
                Project project = null;
            
            try(PreparedStatement stmt = conn.prepareStatement(sql)) {
            	setParameter(stmt, 1, projectId, Integer.class);

            try(ResultSet rs = stmt.executeQuery()) {
            	if(rs.next()) {
                    project = extract(rs, Project.class);
                  }
                }
              }

            if(Objects.nonNull(project)) {  
                project.getMaterials().addAll(fetchMaterialsForProject(conn, projectId));
                project.getSteps().addAll(fetchStepsForProject(conn, projectId));
                project.getCategories().addAll(fetchCategoriesForProject(conn, projectId));
              }

            commitTransaction(conn);

            return Optional.ofNullable(project);
            }
            catch (Exception e) {  // similar to our last method our catch block handles the SQL exception
                rollbackTransaction(conn);
                throw new DbException(e);
            }
        } catch (SQLException e) {  
            throw new DbException(e); // and throws a new DBException object as a parameter
        }
    }
	
    public List<Project> fetchAllProjects() {
        String sql = "SELECT * FROM " + PROJECT_TABLE + " ORDER BY project_name";

        try(Connection conn = DbConnection.getConnection()) {
          startTransaction(conn);

          try(PreparedStatement stmt = conn.prepareStatement(sql)) {
            try(ResultSet rs = stmt.executeQuery()) {
              List<Project> projects = new LinkedList<>();

              while(rs.next()) {
                projects.add(extract(rs, Project.class));

                Project project = new Project();
                
                project.setActualHours(rs.getBigDecimal("actual_hours"));
                project.setDifficulty(rs.getObject("difficulty", Integer.class));
                project.setEstimatedHours(rs.getBigDecimal("estimated_hours"));
                project.setNotes(rs.getString("notes"));
                project.setProjectId(rs.getObject("project_id", Integer.class));
                project.setProjectName(rs.getString("project_name"));
                
              }

              return projects;
            }
          }
          catch(Exception e) {
            rollbackTransaction(conn);
            throw new DbException(e);
          }
        }
        catch(SQLException e) {
          throw new DbException(e);
        }
      }
// I wrote 3 methods to return materials, steps and categories as a list
// The connection is passed as a parameter (Connection conn) so we don't have to do 
// multiple database calls using DBconnection.getconnection each time.
    
// We're also throwing a SQLException in each method declaration because our method call
// is within our try/catch block.  When these methods are run they will return a list from 
// Materials, Steps and Categories tables
    
    private List<Category> fetchCategoriesForProject(Connection conn, Integer projectId) throws SQLException {
		String sql = "SELECT c.* FROM " + CATEGORY_TABLE + " c " + "JOIN " + PROJECT_CATEGORY_TABLE
				+ " pc USING (category_id) " + "WHERE project_id = ?";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			setParameter(stmt, 1, projectId, Integer.class);

			try (ResultSet rs = stmt.executeQuery()) {
				List<Category> categories = new LinkedList<>();

				while (rs.next()) {
					categories.add(extract(rs, Category.class));
				}
				return categories;
			}
		}
	}

    private List<Step> fetchStepsForProject(Connection conn, Integer projectId) throws SQLException {
    	String sql = "SELECT * FROM " + STEP_TABLE + " WHERE project_ID = ?";
    	
    	try(PreparedStatement stmt = conn.prepareStatement(sql)){
    		setParameter(stmt, 1, projectId, Integer.class);
    		
    		try(ResultSet rs = stmt.executeQuery()){
    			List<Step> steps = new LinkedList<>();
    			
    			while(rs.next()) {
    				steps.add(extract(rs, Step.class));
    			}
    			return steps;
    			}
    		}
    	}
    private List<Material> fetchMaterialsForProject(Connection conn, Integer projectID)
    	      throws SQLException {
    	    String sql = "SELECT * FROM " + MATERIAL_TABLE + " WHERE project_ID = ?";

    	    try(PreparedStatement stmt = conn.prepareStatement(sql)) {
    	      setParameter(stmt, 1, projectID, Integer.class);

    	      try(ResultSet rs = stmt.executeQuery()) {
    	        List<Material> materials = new LinkedList<>();

    	        while(rs.next()) {
    	          materials.add(extract(rs, Material.class));
    	        }
    	        return materials;
    	      }
    	    }
    	  }

	public boolean modifyProjectDetails(Project project) { // wrote the modifyProjectDetails method
		
		//@formatter:off
		String sql = ""  // Wrote the SQL statement to modify the project details
				+ "UPDATE " + PROJECT_TABLE + " SET "
				+ "project_name = ?, "
				+ "estimated_hours = ?, "
				+ "actual_hours = ?, "
				+ "difficulty = ?, "
				+ "notes = ? "
				+ "WHERE project_id = ?";
		//@formatter:on	
		
		try (Connection conn = DbConnection.getConnection()) { // within the try/catch block we establish a connection first
			startTransaction(conn);
			try (PreparedStatement stmt = conn.prepareStatement(sql)) { // set all of our parameters in the prepared statement
				setParameter(stmt, 1, project.getProjectName(), String.class);
				setParameter(stmt, 2, project.getEstimatedHours(), BigDecimal.class);
				setParameter(stmt, 3, project.getActualHours(), BigDecimal.class);
				setParameter(stmt, 4, project.getDifficulty(), Integer.class);
				setParameter(stmt, 5, project.getNotes(), String.class);
				setParameter(stmt, 6, project.getProjectId(), Integer.class);

				boolean updatedProject = stmt.executeUpdate() == 1;
				commitTransaction(conn);  // commit the transaction 

				return updatedProject; // and return the results: true if 1 or false if 0

			} catch (Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}

		} catch (SQLException e) {
			throw new DbException(e);
		}
	}

public boolean deleteProject(Integer projectId) { // Added delete project method

	String sql = "DELETE FROM " + PROJECT_TABLE + " WHERE project_id = ?"; // Wrote the DELETE SQL statement

	try (Connection conn = DbConnection.getConnection()) { // In the try/catch block it obtains a connection
		startTransaction(conn);							   // starts the transaction
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			setParameter(stmt, 1, projectId, Integer.class);

			boolean deletedProject = stmt.executeUpdate() == 1;  

			commitTransaction(conn); // commits the transaction
			return deletedProject; // returns true from the menu if execute update returns 1

		} catch (Exception e) {
			rollbackTransaction(conn); // rollsback a transaction
			throw new DbException(e);
		}

	} catch (SQLException e) {
		throw new DbException(e);
	}
}
}

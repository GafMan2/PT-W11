package projects.service;

import java.util.List;
import projects.dao.ProjectDao;
import projects.entity.Project;
import projects.exception.DbException;

public class ProjectService {
  private ProjectDao projectDao = new ProjectDao();
  
public Project addProject(Project project) {
  return projectDao.insertProject(project);
  }

public List<Project> fetchAllProjects() {
	  return projectDao.fetchAllProjects();
  }

public Project fetchProjectById(Integer projectId) { 
    return projectDao.fetchProjectById(projectId).orElseThrow(() -> new projects.exception.DbException(
        "Project with project ID=" + projectId + " does not exist."));

	}

public void modifyProjectDetails(Project project) { // in this method I called the projectDao.modifyProjectDetails
	if(!projectDao.modifyProjectDetails(project)) { // and passed the project object as a parameter.  The Dao method returns a boolean
		throw new DbException("Project with ID=" + project.getProjectId() + " does not exist."); //if it's false it indicates
	}																							// project does not exist.
  }

public void deleteProject(Integer projectId) { //
	if(!projectDao.deleteProject(projectId)) {
		throw new DbException("Project with ID = " + projectId + " does not exist.");
	}
}


}


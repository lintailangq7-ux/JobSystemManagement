package model;

import java.util.List;

import DAO.StudentDAO;

public class StudentLogic {
	public List<ModelStudent> execute(){
		StudentDAO DAO =  new StudentDAO();
		List<ModelStudent> StuList = DAO.findAll();
		return StuList;
	}
}

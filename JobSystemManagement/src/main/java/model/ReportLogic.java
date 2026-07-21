package model;

import java.util.List;

import DAO.EmploymentDAO;

public class ReportLogic {
	public List<ModelEmployment> execute(){
		EmploymentDAO DAO =  new EmploymentDAO();
		List<ModelEmployment> list = DAO.findAll();
		return list;
	}
}

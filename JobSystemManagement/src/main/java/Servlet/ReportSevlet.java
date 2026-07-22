package Servlet;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import model.ModelEmployment;
import model.ModelStudent;
import model.ReportLogic;
import model.StudentLogic;

/**
 * Servlet implementation class StudentNewSevlet
 */
@WebServlet("/ReportSevlet")
public class ReportSevlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ReportLogic ReLogic = new ReportLogic();
		List<ModelEmployment> list = ReLogic.execute();
		StudentLogic StuLogic = new StudentLogic();
		List<ModelStudent> StuList = StuLogic.execute();
		request.setAttribute("list",list);
		
		request.setAttribute("StuList", StuList);	//あとで消す
		RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/Report.jsp");
		dispatcher.forward(request, response);
		System.out.println();
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}

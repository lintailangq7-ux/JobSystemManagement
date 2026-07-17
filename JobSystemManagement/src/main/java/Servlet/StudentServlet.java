package Servlet;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import model.ModelStudent;
import model.StudentLogic;
 
/**
* Servlet implementation class StudentServlet
*/
@jakarta.servlet.annotation.WebServlet("/Student")
public class StudentServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
 
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		StudentLogic StuLogic = new StudentLogic();
		List<ModelStudent> StuList = StuLogic.execute();
		request.setAttribute("StuList",StuList);
		RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/StudentList.jsp");
		dispatcher.forward(request, response);
	}
 
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}
 
}
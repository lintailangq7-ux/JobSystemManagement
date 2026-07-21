package Servlet;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import DAO.StudentDAO;
import model.StudentList;
@WebServlet("/ListofExamStudents")
public class ListofExamStudents extends HttpServlet {
	 private static final long serialVersionUID = 1L;

	    public ListofExamStudents() {
	        super();
}
	    protected void doGet(HttpServletRequest request, HttpServletResponse response)
	            throws ServletException, IOException {

	        // 企業IDを受け取る
	        String companyId = request.getParameter("companyId");

	        StudentDAO dao = new StudentDAO();

	        // 企業ごとの受験学生一覧を取得
	        List<StudentList> studentList = dao.findByCompanyId(companyId);

	        // 企業名を取得（今回は仮）
	        String companyName = "○○株式会社";

	        // JSPへ渡す
	        request.setAttribute("companyName", companyName);
	        request.setAttribute("studentList", studentList);

	        RequestDispatcher rd =
	                request.getRequestDispatcher("/jsp/ListofExamStudents.jsp");
	        rd.forward(request, response);
	    }

	    protected void doPost(HttpServletRequest request, HttpServletResponse response)
	            throws ServletException, IOException {
	        doGet(request, response);
	    }
}

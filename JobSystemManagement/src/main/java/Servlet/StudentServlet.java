package Servlet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import DAO.StudentDAO;
import model.ModelStudent;
import model.StudentLogic;
 

@jakarta.servlet.annotation.WebServlet("/StudentServlet")
public class StudentServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
 
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 
		String gakusekiNoParam = request.getParameter("gakusekiNo");
		List<ModelStudent> StuList;
 
		if (gakusekiNoParam != null && !gakusekiNoParam.trim().isEmpty()) {
			// ---- 特定の1人だけを表示するモード ----
			StuList = new ArrayList<>();
 
			try {
				int gakusekiNo = Integer.parseInt(gakusekiNoParam.trim());
				StudentDAO stuDAO = new StudentDAO();
				ModelStudent student = stuDAO.findByGakusekiNo(gakusekiNo);
 
				if (student != null) {
					StuList.add(student);
				} else {
					request.setAttribute("emg", "学籍番号 " + gakusekiNo + " の学生が見つかりませんでした");
				}
			} catch (NumberFormatException e) {
				request.setAttribute("emg", "学籍番号の形式が不正です");
			}
 
		} else {
			// ---- 全件表示モード（従来通り） ----
			StudentLogic StuLogic = new StudentLogic();
			StuList = StuLogic.execute();
		}
 
		request.setAttribute("StuList", StuList);
		RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/StudentList.jsp");
		dispatcher.forward(request, response);
	}
 
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}
 
}
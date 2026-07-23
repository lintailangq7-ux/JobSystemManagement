package Servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import model.ModelStudent;
import model.StudentChukan;
import model.StudentLogic;
/**
 * Servlet implementation class StudentNewSevlet
 */
@WebServlet("/StudentNewSevlet")
public class StudentNewSevlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/GTouroku.jsp");
		dispatcher.forward(request, response);
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		int gakusekiNo = Integer.parseInt(request.getParameter("studentNo"));          // 学籍番号*
		String gakusekiNoNum = request.getParameter("studentNo");          // 学籍番号*
		String className = request.getParameter("className");						   // クラス*
		String name = request.getParameter("studentName");   				               // 氏名*
		int attendanceNo = Integer.parseInt(request.getParameter("attendanceNo"));        // 出席番号*
		int zaisekiJokyo = Integer.parseInt(request.getParameter("schoolStatus"));        // 在籍状況*
		String attendanceNoS = request.getParameter("attendanceNo");        // 出席番号*
		String zaisekiJokyoS = request.getParameter("schoolStatus");        // 在籍状況*
		String kenNaiGaiKibo = request.getParameter("area");   	  							// 県内外の希望*
		String seibetsu = request.getParameter("sex");          								// 性別*
		int assen = Integer.parseInt(request.getParameter("status"));   						//あっせん*
		String assenS = request.getParameter("status");   						//あっせん*
		String biko = request.getParameter("memo");            								  // 備考*
		List<StudentChukan> list = new ArrayList<>();
		list.add(null);
		list.add(null);
		String forword ="/jsp/GTouroku.jsp";
		String resurt =null;
		
		if(className.isEmpty()){
			request.setAttribute("emg", "クラスが入力されていません");
			forword ="/jsp/GTouroku.jsp";
			resurt = "false";
		}else if(gakusekiNoNum.isEmpty()) {
			request.setAttribute("emg", "学籍番号が入力されていません");
			forword ="/jsp/GTouroku.jsp";
			resurt = "false";			
		}else if(name.isEmpty()) {	
			request.setAttribute("emg", "氏名が入力されていません");
			forword ="/jsp/GTouroku.jsp";
			resurt = "false";
		}else if(attendanceNoS.isEmpty()){
			request.setAttribute("emg", "が入力されていません");
			forword ="/jsp/GTouroku.jsp";
			resurt = "false";			
		}else if(zaisekiJokyoS.isEmpty()){
			request.setAttribute("emg", "が入力されていません");
			forword ="/jsp/GTouroku.jsp";
			resurt = "false";								
		}else if(kenNaiGaiKibo.isEmpty()){
			request.setAttribute("emg", "が入力されていません");
			forword ="/jsp/GTouroku.jsp";
			resurt = "false";
		}else if(seibetsu.isEmpty()){
			request.setAttribute("emg", "が入力されていません");
			forword ="/jsp/GTouroku.jsp";
			resurt = "false";
		}else if(assenS.isEmpty()){
			request.setAttribute("emg", "が入力されていません");
			forword ="/jsp/GTouroku.jsp";
			resurt = "false";
		}else if(biko.isEmpty()){
			request.setAttribute("emg", "が入力されていません");
			forword ="/jsp/GTouroku.jsp";
			resurt = "false";		
			
			request.setAttribute("emg", "入力されていない欄があります");
			forword ="/jsp/GTouroku.jsp";
			resurt = "false";
		}
		if(resurt==null) {
			StudentLogic StuLog = new StudentLogic();
			List<ModelStudent> Stulist = StuLog.execute();
			if(Stulist != null) {
				for(ModelStudent M:Stulist) {
					if(M.getGakusekiNo()==gakusekiNo){
						request.setAttribute("emg", "学籍番号はもう登録されています");
						forword ="/jsp/GTouroku.jsp";
						resurt = "false";
					}
				}
			}
			
		}
		if(resurt==null) {
			ModelStudent MD = new ModelStudent(gakusekiNo,className,name,attendanceNo,zaisekiJokyo,kenNaiGaiKibo,seibetsu,assen,biko,list);
		}
		request.setAttribute("emg","ooooooo");
		RequestDispatcher dispatcher = request.getRequestDispatcher(forword);
		dispatcher.forward(request, response);
	}

}
//int gakusekiNo;           // 学籍番号
//String className;         // クラス
//String name;              // 氏名
//int attendanceNo;         // 出席番号
//int zaisekiJokyo;         // 在籍状況
//String kenNaiGaiKibo;     // 県内外の希望
//String seibetsu;          // 性別
//int assen;String biko;              // 備考


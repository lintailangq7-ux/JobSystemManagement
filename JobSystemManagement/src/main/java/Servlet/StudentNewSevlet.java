package Servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import DAO.StudentChukanDAO;
import DAO.StudentDAO;
import model.ModelStudent;
import model.StudentChukan;
import model.StudentLogic;

@WebServlet("/StudentNewSevlet")
public class StudentNewSevlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/GTouroku.jsp");
		dispatcher.forward(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    String gakusekiNoNum = request.getParameter("studentNo");
	    String className = request.getParameter("className");
	    String name = request.getParameter("studentName");
	    String attendanceNoS = request.getParameter("attendanceNo");
	    String zaisekiJokyoS = request.getParameter("schoolStatus");
	    String kenNaiGaiKibo = request.getParameter("area");
	    String seibetsu = request.getParameter("sex");
	    String assenS = request.getParameter("status");
	    String biko = request.getParameter("memo");
	    String job1 = request.getParameter("job1");
	    String job2 = request.getParameter("job2");
	    String job3 = request.getParameter("job3");

	    List<StudentChukan> list = new ArrayList<>();
	    list.add(null);
	    list.add(null);

	    String forword = "/jsp/GTouroku.jsp";
	    String resurt = null;

	    List<String> emptyFields = new ArrayList<>();
	    if (className.isEmpty())      emptyFields.add("クラス");
	    if (gakusekiNoNum.isEmpty())  emptyFields.add("学籍番号");
	    if (name.isEmpty())           emptyFields.add("氏名");
	    if (attendanceNoS.isEmpty())  emptyFields.add("出席番号");
	    if (zaisekiJokyoS.isEmpty())  emptyFields.add("在籍状況");
	    if (kenNaiGaiKibo.isEmpty())  emptyFields.add("県内外希望");
	    if (seibetsu.isEmpty())       emptyFields.add("性別");
	    if (assenS.isEmpty())         emptyFields.add("あっせん状況");

	    if (!emptyFields.isEmpty()) {
	        request.setAttribute("emg", String.join("、", emptyFields) + "が入力されていません");
	        resurt = "false";
	    }

	    if (resurt == null) {
	        List<String> lengthErrors = new ArrayList<>();
	        if (className.length() != 4)  lengthErrors.add("クラスは4文字で入力してください");
	        if (biko.length() > 100)      lengthErrors.add("備考は100文字以内で入力してください");
	        if (!lengthErrors.isEmpty()) {
	            request.setAttribute("emg", String.join("\n", lengthErrors));
	            resurt = "false";
	        }
	    }

	    int gakusekiNo = 0;
	    if (resurt == null) {
	        try {
	            gakusekiNo = Integer.parseInt(gakusekiNoNum);
	        } catch (NumberFormatException e) {
	            request.setAttribute("emg", "学籍番号は数値で入力してください");
	            resurt = "false";
	        }
	    }

	    if (resurt == null) {
	        StudentLogic StuLog = new StudentLogic();
	        List<ModelStudent> Stulist = StuLog.execute();
	        if (Stulist != null) {
	            for (ModelStudent M : Stulist) {
	                if (M.getGakusekiNo() == gakusekiNo) {
	                    request.setAttribute("emg", "学籍番号はもう登録されています");
	                    resurt = "false";
	                    break;
	                }
	            }
	        }
	    }

	    if (resurt == null) {
	        try {
	            int attendanceNo = Integer.parseInt(attendanceNoS);
	            int zaisekiJokyo = Integer.parseInt(zaisekiJokyoS);
	            int assen = Integer.parseInt(assenS);

	            ModelStudent MD = new ModelStudent(gakusekiNo, className, name, attendanceNo,
	                    zaisekiJokyo, kenNaiGaiKibo, seibetsu, assen, biko, list);

	            StudentDAO StuDAO = new StudentDAO();
	            boolean success = StuDAO.create(MD);

	            if (success) {
	                // 希望職種（中間テーブル）も登録
	                StudentChukanDAO chukanDao = new StudentChukanDAO();
	                chukanDao.updateAll(gakusekiNo, Arrays.asList(job1, job2, job3));

	                request.setAttribute("emg", "登録完了");

	                List<ModelStudent> updatedList = StuDAO.findAll();
	                request.setAttribute("StuList", updatedList);

	                forword = "/jsp/StudentList.jsp";
	            } else {
	                request.setAttribute("emg", "登録に失敗しました");
	            }
	        } catch (NumberFormatException e) {
	            request.setAttribute("emg", "出席番号・在籍状況・あっせん状況は数値で入力してください");
	        }
	    }

	    RequestDispatcher dispatcher = request.getRequestDispatcher(forword);
	    dispatcher.forward(request, response);
	}
}
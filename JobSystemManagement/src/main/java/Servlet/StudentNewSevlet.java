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

import DAO.StudentDAO;
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
	    System.out.println("★★★ 新しいコードが動いています ★★★");

	    String gakusekiNoNum = request.getParameter("studentNo");     // 学籍番号*
	    String className = request.getParameter("className");         // クラス*
	    String name = request.getParameter("studentName");             // 氏名*
	    String attendanceNoS = request.getParameter("attendanceNo");   // 出席番号*
	    String zaisekiJokyoS = request.getParameter("schoolStatus");   // 在籍状況*
	    String kenNaiGaiKibo = request.getParameter("area");           // 県内外の希望*
	    String seibetsu = request.getParameter("sex");                 // 性別*
	    String assenS = request.getParameter("status");                // あっせん*
	    String biko = request.getParameter("memo");                    // 備考*

	    List<StudentChukan> list = new ArrayList<>();
	    list.add(null);
	    list.add(null);

	    String forword = "/jsp/GTouroku.jsp";
	    String resurt = null;

	    // 未入力項目の名前を集めるリスト
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
	        String joined = String.join("、", emptyFields);
	        request.setAttribute("emg", joined + "が入力されていません");
	        resurt = "false";
	    }

	    // 文字数チェック（未入力チェックとは別枠。文字が入っている場合のみ判定）
	    if (resurt == null) {
	        List<String> lengthErrors = new ArrayList<>();
	        if (className.length() > 4)  lengthErrors.add("クラスは4文字以内で入力してください");
	        if (biko.length() > 100)     lengthErrors.add("備考は100文字以内で入力してください");

	        if (!lengthErrors.isEmpty()) {
	            request.setAttribute("emg", String.join("\n", lengthErrors));
	            resurt = "false";
	        }
	    }

	    // 学籍番号の数値チェック	
	    int gakusekiNo = 0;
	 // 文字数チェック（未入力チェックとは別枠。文字が入っている場合のみ判定）
	    if (resurt == null) {
	        List<String> lengthErrors = new ArrayList<>();
	        if (className.length() != 4)  lengthErrors.add("クラスは4文字で入力してください");
	        if (biko.length() > 100)      lengthErrors.add("備考は100文字以内で入力してください");

	        if (!lengthErrors.isEmpty()) {
	            request.setAttribute("emg", String.join("\n", lengthErrors));
	            resurt = "false";
	        }
	    }

	    // 重複チェック
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

	    // 残りの数値項目の変換とDB登録
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
	                request.setAttribute("emg", "登録完了");
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
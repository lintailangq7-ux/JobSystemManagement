package Servlet;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import DAO.CompanyDAO;
import model.Company;

/**
 * Servlet implementation class ListofEmployment
 */
@WebServlet("/ListofEmployment")
public class ListofEmployment extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ListofEmployment() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // JSPから検索文字を受け取る
		 

       String keyword = request.getParameter("keyword");
        

        //DAOで検索
        CompanyDAO dao = new CompanyDAO();
        List<Company> companyList;
        
        if (keyword == null || keyword.trim().isEmpty()) {
           companyList = dao.findAllCompany();      // 初回表示：全件取得
        } else {
            companyList = dao.search(keyword); // 検索時：条件に合うものだけ取得
        }
        System.out.println("keyword = " + keyword);
        System.out.println("件数 = " + companyList.size());

        // JSPへ渡す
       
        request.setAttribute("companyList", companyList);

        // 一覧画面へ戻る
       RequestDispatcher rd =request.getRequestDispatcher("/jsp/Employment/TecherEmplymentList.jsp");

        rd.forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}

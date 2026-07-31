package Servlet;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import DAO.CompanyDAO;
import model.Company;

@WebServlet("/CompanyUpdate")
public class CompanyUpdate extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String companyId = request.getParameter("companyId");

        if (companyId != null && !companyId.isEmpty()) {
            // 編集の場合：既存データを取得してフォームに渡す
            CompanyDAO dao = new CompanyDAO();
            Company company = dao.findById(companyId);
            request.setAttribute("company", company);
        }
        // companyIdが無ければ新規登録なので何もセットしない(空フォームになる)

        request.getRequestDispatcher("/jsp/Ktouroku.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String id = request.getParameter("companyId");
        String name = request.getParameter("companyName");
        String address = request.getParameter("address");
        String tel = request.getParameter("tel");
        String mail = request.getParameter("mail");
        String result = request.getParameter("result");

        Company company = new Company();
        company.setId(id);
        company.setName(name);
        company.setAddress(address);
        company.setTel(tel);
        company.setMail(mail);
        company.setJobtype(result);

        CompanyDAO dao = new CompanyDAO();

        if (id == null || id.isEmpty()) {
            // 新規登録
            dao.addCompany(company);
        } else {
            // 更新
            dao.updateCompany(company);
        }

        // 更新/登録が終わったら一覧画面に戻す
        response.sendRedirect(request.getContextPath() + "/ListofCompanies");
    }
}
package Servlet;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import DAO.CompanyDAO;

@WebServlet("/CompanyDeleteServlet")
public class CompanyDelete extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        String companyId = request.getParameter("companyId");
        CompanyDAO dao = new CompanyDAO();
        dao.deleteCompany(companyId);
        response.sendRedirect(
                request.getContextPath() + "/ListofCompanies");
    }

    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String companyId = request.getParameter("companyId");

        CompanyDAO dao = new CompanyDAO();
        dao.deleteCompany(companyId);

        response.sendRedirect(
                request.getContextPath() + "/ListofCompanies");
    }

}

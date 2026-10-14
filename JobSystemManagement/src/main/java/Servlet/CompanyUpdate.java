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
 
    // 新規登録フォーム（emg属性でダイアログ表示）
    private static final String REGISTER_JSP = "/jsp/Ktouroku.jsp";
    // 変更フォーム（errorMessage属性で表示）
    private static final String EDIT_JSP = "/jsp/Khenku.jsp";
 
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String companyId = request.getParameter("companyId");
        if (companyId != null && !companyId.isEmpty()) {
            // 編集の場合：既存データを取得してフォームに渡す
            CompanyDAO dao = new CompanyDAO();
            Company company = dao.findById(companyId);
            request.setAttribute("company", company);
            request.getRequestDispatcher(EDIT_JSP).forward(request, response);
        } else {
            // companyIdが無ければ新規登録
            request.getRequestDispatcher(REGISTER_JSP).forward(request, response);
        }
    }
 
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
 
        // ---- フォームによってinputのname属性が異なる（企業登録画面と企業変更画面で不一致）ため、
        //      両方に対応できるよう複数のパラメータ名を順に試す ----
        String id       = request.getParameter("companyId");
        String name     = param(request, "companyName", "name");
        String address  = request.getParameter("address");
        String tel      = request.getParameter("tel");
        String mail     = param(request, "mail", "email");
        String jobtype  = param(request, "result", "jobtype");
        String location = request.getParameter("location");
 
        boolean isEdit = (id != null && !id.isEmpty());
 
        System.out.println("勤務地：" + location);
 
        Company company = new Company();
        company.setId(id);
        company.setName(name);
        company.setAddress(address);
        company.setTel(tel);
        company.setMail(mail);
        company.setJobtype(jobtype);
        company.setLocation(location);
 
        // ---- 入力チェック：必須項目が空ならJSPに戻してエラー表示 ----
        if (name == null || name.trim().isEmpty()) {
            showError(request, response, company, isEdit, "企業名は必須です。入力してください。");
            return;
        }
        if (jobtype == null || jobtype.trim().isEmpty()) {
            showError(request, response, company, isEdit, "採用実績を選択してください。");
            return;
        }
 
        CompanyDAO dao = new CompanyDAO();
        try {
            if (!isEdit) {
                // 新規登録
                dao.addCompany(company);
            } else {
                // 更新
                dao.updateCompany(company);
            }
        } catch (IllegalArgumentException e) {
            // 桁数超過・数値変換エラーなど、入力内容に起因するエラー
            showError(request, response, company, isEdit, e.getMessage());
            return;
        } catch (RuntimeException e) {
            // DBへの登録・更新自体に失敗した場合
            e.printStackTrace();
            showError(request, response, company, isEdit,
                    "登録・更新に失敗しました。入力内容をご確認のうえ、もう一度お試しください。");
            return;
        }
 
        // 更新/登録が終わったら一覧画面に戻す
        response.sendRedirect(request.getContextPath() + "/ListofCompanies");
    }
 
    /**
     * primaryName を優先して取得し、値が無ければ fallbackName を試す。
     * 同じ項目でもJSPによってinputのname属性が異なるための救済措置。
     */
    private String param(HttpServletRequest request, String primaryName, String fallbackName) {
        String value = request.getParameter(primaryName);
        if (value != null && !value.trim().isEmpty()) {
            return value;
        }
        return request.getParameter(fallbackName);
    }
 
    /**
     * 入力エラー・登録エラー時に、入力済みの内容を保持したまま、
     * 呼び出し元と同じフォーム（新規登録 or 変更）へ戻す。
     * company / errorMessage / emg をリクエスト属性にセットし、
     * どちらのJSPでも表示できるようにしている。
     */
    private void showError(HttpServletRequest request, HttpServletResponse response,
            Company company, boolean isEdit, String message) throws ServletException, IOException {
        request.setAttribute("company", company);
        request.setAttribute("errorMessage", message);
        request.setAttribute("emg", message);
        String target = isEdit ? EDIT_JSP : REGISTER_JSP;
        request.getRequestDispatcher(target).forward(request, response);
    }
}
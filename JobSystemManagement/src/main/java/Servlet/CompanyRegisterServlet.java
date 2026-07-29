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

import DAO.CompanyDAO;
import model.ModelCompany;

@WebServlet("/CompanyRegisterServlet")
public class CompanyRegisterServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/Ktouroku.jsp");
        dispatcher.forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String name       = request.getParameter("name");
        String address    = request.getParameter("address");
        String tel        = request.getParameter("tel");
        String email      = request.getParameter("email");
        String saiyoS     = request.getParameter("saiyoJisseki"); // 採用実績（数値）
        String kinmuChi   = request.getParameter("kinmuchi");

        String forword = "/jsp/Ktouroku.jsp";
        String resurt = null;

        // ============ 必須項目チェック ============
        List<String> emptyFields = new ArrayList<>();
        if (name == null || name.isEmpty())       emptyFields.add("企業名");
        if (address == null || address.isEmpty()) emptyFields.add("住所");
        if (tel == null || tel.isEmpty())         emptyFields.add("電話番号");
        if (email == null || email.isEmpty())     emptyFields.add("メールアドレス");

        if (!emptyFields.isEmpty()) {
            request.setAttribute("emg", String.join("、", emptyFields) + "が入力されていません");
            resurt = "false";
        }

        // ============ 文字数チェック（DBのカラム桁数に合わせる） ============
        if (resurt == null) {
            List<String> lengthErrors = new ArrayList<>();
            if (name.length() > 50)     lengthErrors.add("企業名は50文字以内で入力してください");
            if (address.length() > 40)  lengthErrors.add("住所は40文字以内で入力してください");
            if (tel.length() > 11)      lengthErrors.add("電話番号は11文字以内で入力してください");
            if (email.length() > 254)   lengthErrors.add("メールアドレスは254文字以内で入力してください");
            if (kinmuChi != null && kinmuChi.length() > 30) lengthErrors.add("勤務地は30文字以内で入力してください");

            if (!lengthErrors.isEmpty()) {
                request.setAttribute("emg", String.join("\n", lengthErrors));
                resurt = "false";
            }
        }

        // ============ 電話番号は数字のみか ============
        if (resurt == null) {
            if (!tel.matches("^[0-9\\-]+$")) {
                request.setAttribute("emg", "電話番号は数字とハイフンのみで入力してください");
                resurt = "false";
            }
        }

        // ============ メールアドレスの簡易形式チェック ============
        if (resurt == null) {
            if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
                request.setAttribute("emg", "メールアドレスの形式が正しくありません");
                resurt = "false";
            }
        }

        // ============ 採用実績（int型・空欄なら0扱い） ============
        int saiyoJisseki = 0;
        if (resurt == null) {
            if (saiyoS != null && !saiyoS.isEmpty()) {
                try {
                    saiyoJisseki = Integer.parseInt(saiyoS);
                } catch (NumberFormatException e) {
                    request.setAttribute("emg", "採用実績は数値で入力してください");
                    resurt = "false";
                }
            }
        }

        // ============ 企業名の重複チェック ============
        CompanyDAO companyDAO = new CompanyDAO();
        if (resurt == null) {
            if (companyDAO.findByName(name) != null) {
                request.setAttribute("emg", "この企業名はすでに登録されています");
                resurt = "false";
            }
        }

        // ============ 登録処理 ============
        if (resurt == null) {
            // 企業IDを自動採番（例: C0001）
            String newKaishaId = companyDAO.generateNewKaishaId();

            ModelCompany MC = new ModelCompany();
            MC.setKaishaId(newKaishaId);
            MC.setKaishaName(name);
            MC.setAddress(address);
            MC.setTel(tel);
            MC.setEmail(email);
            MC.setSaiyoJisseki(saiyoJisseki);
            MC.setKinmuChi(kinmuChi);

            boolean success = companyDAO.create(MC);

            if (success) {
                request.setAttribute("emg", "登録完了");
                forword = "/jsp/ListofCompanies.jsp";
            } else {
                request.setAttribute("emg", "登録に失敗しました");
            }
        }

        RequestDispatcher dispatcher = request.getRequestDispatcher(forword);
        dispatcher.forward(request, response);
    }
}
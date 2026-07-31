package Servlet;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import DAO.CompanyDAO;
import DAO.EmploymentChukanDAO;
import DAO.EmploymentDAO;
import model.Company;
import model.EmploymentChukan;
import model.ModelEmployment;
import model.StudentDetail;

@WebServlet("/EmploymentCangeServlet")
public class EmploymentCangeServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter FMT_T =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    // =========================================================
    // 変更画面表示
    // =========================================================
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String shidoId = request.getParameter("shidoId");
        HttpSession session = request.getSession();

        EmploymentDAO eDAO = new EmploymentDAO();
        EmploymentChukanDAO ecDAO = new EmploymentChukanDAO();
        CompanyDAO cDAO = new CompanyDAO();

        ModelEmployment employment = eDAO.findById(shidoId);
        if (employment == null) {
            response.sendRedirect(request.getContextPath() + "/ListofEmployment");
            return;
        }

        Company company = cDAO.findById(employment.getKaishaId());
        List<EmploymentChukan> examList = ecDAO.findById(shidoId); // 全件取得

        request.setAttribute("mode", "edit");
        request.setAttribute("shidoId", shidoId);
        request.setAttribute("employment", employment);
        request.setAttribute("company", company);
        request.setAttribute("examList", examList);   // ← 複数行用

        // 左側の生徒情報
        Object detailObj = session.getAttribute("detail");
        if (detailObj instanceof StudentDetail) {
            request.setAttribute("student", ((StudentDetail) detailObj).getStudent());
        } else if (detailObj instanceof List) {
            // List<StudentDetail> の場合のフォールバック（必要に応じて）
            @SuppressWarnings("unchecked")
            List<StudentDetail> list = (List<StudentDetail>) detailObj;
            for (StudentDetail d : list) {
                if (d.getGuidanceList() != null) {
                    for (var g : d.getGuidanceList()) {
                        if (shidoId.equals(g.getShidoId())) {
                            request.setAttribute("student", d.getStudent());
                            break;
                        }
                    }
                }
            }
        }

        RequestDispatcher dispatcher =
                request.getRequestDispatcher("/jsp/Employment/Shenkou.jsp");
        dispatcher.forward(request, response);
    }

    // =========================================================
    // 変更処理（複数試験対応）
    // =========================================================
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        CompanyDAO cDAO = new CompanyDAO();
        EmploymentDAO eDAO = new EmploymentDAO();
        EmploymentChukanDAO ecDAO = new EmploymentChukanDAO();

        String shidoId     = request.getParameter("shidoId");
        String companyId   = request.getParameter("companyId");
        String companyName = request.getParameter("companyName");
        String offerStatus = request.getParameter("offerStatus");
        String acceptDate  = request.getParameter("acceptDate");
        String memo        = request.getParameter("memo");

        // ---- 入力チェック ----
        if (shidoId == null || shidoId.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/ListofEmployment");
            return;
        }

        if (companyName == null || companyName.trim().isEmpty()) {
            setErrorAndForward(request, response, shidoId, "企業名は必須です。");
            return;
        }

        Company company = cDAO.findByName(companyName);
        if (company == null) {
            // 企業IDが直接入力されている場合のフォールバック
            if (companyId != null && !companyId.isEmpty()) {
                company = cDAO.findById(companyId);
            }
            if (company == null) {
                setErrorAndForward(request, response, shidoId,
                        "入力された企業名「" + companyName + "」は登録されていません。");
                return;
            }
        }

        int offerInt = "1".equals(offerStatus) || "内".equals(offerStatus) ? 1 : 0;

        LocalDateTime acceptDateTime = parseDateTime(acceptDate);

        // ---- 就職情報テーブル更新 ----
        eDAO.updateGuidance(shidoId, company.getId(), acceptDateTime, offerInt, memo);

        // ---- 試験情報（複数）を処理 ----
        String[] examIds      = request.getParameterValues("examId");
        String[] examContents = request.getParameterValues("examContent");
        String[] examPlaces   = request.getParameterValues("examPlace");
        String[] examDates    = request.getParameterValues("examDateTime");
        String[] examSubmits  = request.getParameterValues("examSubmit");

        if (examContents != null) {
            // 既存の試験ID一覧を取得（削除判定用）
            List<EmploymentChukan> existingList = ecDAO.findById(shidoId);
            List<Integer> keepIds = new ArrayList<>();

            for (int i = 0; i < examContents.length; i++) {
                String content = examContents[i] != null ? examContents[i].trim() : "";
                String place   = examPlaces   != null && i < examPlaces.length   ? examPlaces[i]   : "";
                String dateStr = examDates    != null && i < examDates.length    ? examDates[i]    : "";
                String submit  = examSubmits  != null && i < examSubmits.length  ? examSubmits[i]  : "0";
                String idStr   = examIds      != null && i < examIds.length      ? examIds[i]      : "";

                // 空行はスキップ
                if (content.isEmpty() && (dateStr == null || dateStr.trim().isEmpty())) {
                    continue;
                }

                int submitInt = "1".equals(submit) || "済".equals(submit) ? 1 : 0;
                LocalDateTime examDateTime = parseDateTime(dateStr);

                if (idStr != null && !idStr.trim().isEmpty()) {
                    // ===== 更新 =====
                    int examId = Integer.parseInt(idStr.trim());
                    keepIds.add(examId);

                    EmploymentChukan ec = new EmploymentChukan();
                    ec.setShikenId(examId);
                    ec.setShidoId(shidoId);
                    ec.setShikenNaiyo(content);
                    ec.setShikenKaijo(place);
                    ec.setShikenNichiji(examDateTime);
                    ec.setTeishutsuShoruiJokyo(submitInt);

                    ecDAO.updateById(ec);   // ※DAOに examId で更新するメソッドが必要
                } else {
                    // ===== 新規追加 =====
                    EmploymentChukan ec = new EmploymentChukan(
                            shidoId, examDateTime, content, submitInt, place);
                    ecDAO.insert(ec);       // ※DAOに insert メソッドが必要
                }
            }

            // ===== 画面から消された行をDBから削除 =====
            if (existingList != null) {
                for (EmploymentChukan old : existingList) {
                    if (!keepIds.contains(old.getShikenId())) {
                        ecDAO.deleteById(old.getShikenId());  // ※DAOに必要
                    }
                }
            }
        }

        response.sendRedirect(request.getContextPath() + "/ListofEmployment");
    }

    // ---------------------------------------------------------
    // ユーティリティ
    // ---------------------------------------------------------
    private LocalDateTime parseDateTime(String str) {
        if (str == null || str.trim().isEmpty()) return null;
        str = str.trim().replace(" ", "T"); // "2025-09-20 10:00" → "2025-09-20T10:00"
        try {
            if (str.length() == 16) { // yyyy-MM-ddTHH:mm
                return LocalDateTime.parse(str, FMT_T);
            }
            return LocalDateTime.parse(str);
        } catch (DateTimeParseException e) {
            try {
                return LocalDateTime.parse(str, FMT);
            } catch (DateTimeParseException e2) {
                return null;
            }
        }
    }

    private void setErrorAndForward(HttpServletRequest request,
                                    HttpServletResponse response,
                                    String shidoId,
                                    String message)
            throws ServletException, IOException {
        request.setAttribute("mode", "edit");
        request.setAttribute("shidoId", shidoId);
        request.setAttribute("errorMessage", message);
        // 再表示用に最低限のデータを再セット（必要ならdoGetの処理を呼び出す）
        request.getRequestDispatcher("/jsp/Employment/Shenkou.jsp")
               .forward(request, response);
    }
}
package team.omok.omok_mini_project.controller;

import team.omok.omok_mini_project.domain.UserVO;
import team.omok.omok_mini_project.service.UserService;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("[INFO]login-doGet");
        request.getRequestDispatcher("/WEB-INF/views/login.jsp")
                .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("[INFO]login-doPost");
        request.setCharacterEncoding("utf-8");
        String id = request.getParameter("id");
        String pw = request.getParameter("pw");
        response.setContentType("text/html");

        try {
            UserVO user = userService.login(id, pw);

            HttpSession session = request.getSession();
            session.setAttribute("loginUser", user);

            response.sendRedirect("/omok/lobby/");

        } catch (IllegalArgumentException e) {

            System.out.println("[INFO]login-fail");
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/login.jsp")
                    .forward(request, response);
        }
    }
}
package team.omok.omok_mini_project.controller;

import team.omok.omok_mini_project.domain.Room;
import team.omok.omok_mini_project.domain.UserVO;
import team.omok.omok_mini_project.service.RoomService;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.List;

@WebServlet("/lobby/*")
public class LobbyServlet extends HttpServlet {
    private final RoomService roomService = new RoomService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("[INFO]lobby-doGet");

        String path = request.getPathInfo(); // null, /enter
        if ("/enter".equals(path)) {
            System.out.println("[INFO]lobby-doGet-enter");

            UserVO user = (UserVO) request.getSession().getAttribute("loginUser");
            String roomId = request.getParameter("roomId");

            roomService.enterRoom(roomId, user);

            response.sendRedirect("/omok/room?roomId=" + roomId);
            return;
        }

        // 기본: 로비 화면
        System.out.println("[INFO]lobby-doGet");
        List<Room> rooms = roomService.getWaitingRooms();
        request.setAttribute("rooms", rooms);
        request.getRequestDispatcher("/WEB-INF/views/lobby.jsp")
                .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getPathInfo(); // /create
        UserVO user = (UserVO) request.getSession().getAttribute("loginUser");

        if ("/create".equals(path)) {
            System.out.println("[INFO]lobby-doPost-create");
            Room room = roomService.createRoom(user.getId());
            response.sendRedirect("/omok/room?roomId=" + room.getRoomId());
        }

    }
}
package team.omok.omok_mini_project.util;

import team.omok.omok_mini_project.domain.UserVO;

import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

public class HttpSessionConfigurator extends ServerEndpointConfig.Configurator{
    @Override
    public void modifyHandshake(ServerEndpointConfig config,
                                HandshakeRequest request,
                                HandshakeResponse response) {

        HttpSession httpSession =
                (HttpSession) request.getHttpSession();

        if (httpSession != null) {
            UserVO vo = (UserVO) httpSession.getAttribute("loginUser");
            String userId = vo.getId();

            config.getUserProperties().put("user_id", userId);
        }
    }
}

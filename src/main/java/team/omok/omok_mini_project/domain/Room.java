package team.omok.omok_mini_project.domain;

import lombok.Data;
import team.omok.omok_mini_project.enums.RoomStatus;
import team.omok.omok_mini_project.game.Game;
import team.omok.omok_mini_project.manager.RoomManager;

import javax.websocket.Session;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 하나의 게임 방에 대해 모든 상태 저장 + 유일하게 상태 변경 할 수 있는 객체
 *
 * 방 ID
 * 참가자 목록
 * 관전자 목록
 * WebSocket 세션들
 * 현재 게임(Game) 참조
 *
 * (참고) 스레드 세이프해야하는 부분
 * * 게임 시작 조건 판단
 * * 플레이어 추가
 * * 게임 상태 변경
 * @see RoomManager
 */
@Data
public class Room {
    private static final int MAX_PLAYER = 2;

    private final String roomId;
    private final String ownerId;
    private final List<String> players = new ArrayList<>(MAX_PLAYER);           // 플레이어
    private final Set<Session> playerSessions = ConcurrentHashMap.newKeySet();        // 플레이어 세션
    private final Set<Session> spectators = ConcurrentHashMap.newKeySet();      // 관전자 세션
    private Game game;                              // 게임
    private RoomStatus status;                      // 방 상태: WAITING, READY, COUNTDOWN, PLAYING, END

    public Room(String roomId, String ownerId) {
        this.roomId = roomId;
        this.ownerId = ownerId;
        this.players.add(ownerId);              // 방장은 자동 입장
        status = RoomStatus.WAIT;
    }

    ////////////// 상태 관리 ///////////////
    private synchronized void updateStatus(RoomStatus nextStatus){
        if(this.status == nextStatus) return;
        this.status = nextStatus;

        switch (nextStatus){
            case WAIT:
                broadcast("{\"type\":\"ROOM_WAIT\",\"reason\":\"PLAYER_LEFT\"}");

                break;
            case READY:

                break;
            case COUNTDOWN:
                startCountdown();

                break;
            case PLAYING:

                break;
            case END:

                break;

            default:
                break;
        }
    }



    ////////////// 세션 관리 ///////////////

    public synchronized void addSession(String userId, Session session) {
        System.out.println("[INFO]Room-addSession: " + session);
        if(players.contains(userId)){
            playerSessions.add(session);
        }else{
            spectators.add(session);
        }

        if(isReady() && status == RoomStatus.WAIT){
            updateStatus(RoomStatus.READY);
        }
    }

    public synchronized void removeSession(String userId, Session session) {
        players.remove(userId);
        playerSessions.remove(session);
        spectators.remove(session);

        // 게임 도중 방 나간 경우
        if(!isReady() && status == RoomStatus.PLAYING){
            updateStatus(RoomStatus.END);
        }

        // 게임 시작 전에 방 나간 경우
        if(!isReady() && (status == RoomStatus.READY || status == RoomStatus.COUNTDOWN)){
            updateStatus(RoomStatus.WAIT);
        }
    }

    public synchronized void tryAddPlayer(String userId) {
        if (isFull()) {
            throw new IllegalStateException("방이 가득 찼습니다");
        }
        players.add(userId);
    }

    ////////////// 게임 흐름 제어 ///////////////

    public synchronized void tryStartGame() {
        if(status != RoomStatus.READY) {
            return;
        }
        updateStatus(RoomStatus.COUNTDOWN);
    }

    private void startCountdown() {
        System.out.println("[INFO]Room-startCountdown");

        new Thread(() -> {
            try {
                for (int i = 5; i >= 1; i--) {

                    if(status != RoomStatus.COUNTDOWN){
                        return;
                    }
                    broadcast("{\"type\":\"COUNTDOWN\",\"sec\":" + i + "}");
                    Thread.sleep(1000);
                }

                // 게임 시작
                if(isReady()){
                    startGame();
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private synchronized void startGame(){
        if(status != RoomStatus.COUNTDOWN) return;
        if(!isReady()) return;

        broadcast("{\"type\":\"GAME_START\"}");

        this.game = new Game(); // 게임은 추후 추가
        updateStatus(RoomStatus.PLAYING);
    }


    ////////////// 유틸 ///////////////

    public synchronized boolean isFull() {
        return players.size() >= MAX_PLAYER;
    }

    // 게임 시작 가능 조건
    private boolean isReady() {
        return players.size() == MAX_PLAYER && playerSessions.size() == MAX_PLAYER;
    }

    public void broadcast(String message) {
        for (Session s : playerSessions) {
            try {
                System.out.println("[INFO]Room-broadcast: "+ message);
                s.getBasicRemote().sendText(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



}



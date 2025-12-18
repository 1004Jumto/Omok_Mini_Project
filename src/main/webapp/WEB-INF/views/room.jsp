<%--
  Created by IntelliJ IDEA.
  User: leeyj
  Date: 25. 12. 14.
  Time: 오후 2:09
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="team.omok.omok_mini_project.domain.Room" %>
<%
  Room room = (Room) request.getAttribute("room");
%>

<html>
<head>
    <title>게임방</title>
</head>
<script>
  const roomId = "<%= room.getRoomId() %>";
  const socket = new WebSocket(
          "ws://" + location.host + "/omok/ws/game/" + roomId
  );

  socket.onopen = () => {
    console.log("WebSocket 연결 성공");
  };

  socket.onmessage = (event) => {
    const data = JSON.parse(event.data);
    console.log("RECEIVED:", data);

    switch (data.type) {
      case "COUNTDOWN":
        showCountdown(data.sec);
        break;
      case "GAME_START":
        startGameUI();
        break;
      case "ROOM_WAIT":
        status.innerText = "상대방을 기다리는 중...";
        countdown.innerText = "";
        break;
      case "MOVE":
        drawStone(data.x, data.y, data.color);

        // 상대가 두면 내 턴으로 변경
        currentTurn = data.color === "BLACK" ? "WHITE" : "BLACK";
        break;
    }
  };

  socket.onclose = () => {
    console.log("WebSocket 연결 종료");
  };

  function showCountdown(sec) {
    const status = document.getElementById("status");
    const countdown = document.getElementById("countdown");

    status.innerText = "게임 준비 중...";
    countdown.innerText = "시작까지 " + sec + "초";
  }


</script>
<body>

  <h2>방 입장</h2>
  <p>Room ID: <%= room.getRoomId() %></p>
  <p>Owner: <%= room.getOwnerId() %></p>

  <!-- 상태 표시 -->
  <p id="status">상대방을 기다리는 중...</p>

  <!-- 카운트다운 표시 -->
  <p id="countdown"></p>

  <!-- 오목판 영역 -->
  <div id="board" style="display:none;"></div>
  <script>
    let currentTurn = "BLACK";
    const BOARD_SIZE = 15;
    const boardState = Array.from({ length: BOARD_SIZE },
            () => Array(BOARD_SIZE).fill(null));

    function startGameUI() {
      const status = document.getElementById("status");
      const countdown = document.getElementById("countdown");
      const board = document.getElementById("board");

      status.innerText = "게임 시작!";
      countdown.innerText = "";

      board.style.display = "block";
      renderBoard();
    }

    function renderBoard() {
      const board = document.getElementById("board");
      board.innerHTML = "";
      board.style.display = "grid";
      board.style.gridTemplateColumns = `repeat(15, 30px)`;
      board.style.gridTemplateRows = `repeat(15, 30px)`;
      board.style.gridAutoFlow = "row";
      board.style.gap = "1px";
      board.style.width = `${15 * 31}px`;

      for (let y = 0; y < BOARD_SIZE; y++) {
        for (let x = 0; x < BOARD_SIZE; x++) {
          const cell = document.createElement("div");
          cell.style.width = "30px";
          cell.style.height = "30px";
          cell.style.border = "1px solid #333";
          cell.style.display = "flex";
          cell.style.alignItems = "center";
          cell.style.justifyContent = "center";
          cell.style.cursor = "pointer";
          cell.style.userSelect = "none";

          cell.dataset.x = x;
          cell.dataset.y = y;

          cell.onclick = () => {
            placeStone(x, y, cell);
          };

          board.appendChild(cell);
        }
      }
    }

    function placeStone(x, y, cell) {
      if (cell.innerText !== "") return;

      // 현재 턴의 돌 표시
      drawStone(x, y, currentTurn);

      const message = {
        type: "MOVE",
        x: x,
        y: y,
        color: currentTurn
      };

      socket.send(JSON.stringify(message));

      // 턴 전환
      currentTurn = currentTurn === "BLACK" ? "WHITE" : "BLACK";

    }

    function drawStone(x, y) {
      const index = y * BOARD_SIZE + x;
      const cell = document.getElementById("board").children[index];

      if (cell.innerText !== "") return;

      if (currentTurn === "BLACK") {
        cell.innerText = "●";
        cell.style.color = "black";
      } else {
        cell.innerText = "○";
        cell.style.color = "black";
      }

      cell.style.fontSize = "20px";
    }

  </script>
</body>
</html>

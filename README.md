# 프로젝트 개요

* JAVA 기반 웹 소켓을 사용하여 오목 게임을 구현하는 미니 프로젝트
 

## 개발환경

* IDE: intellij, eclipse
* 서버: tomcat9 + JDK17
* DB: postgreSQL
* 기술 스택: JAVA, JSP, Servlet, CSS, JS


## 웹 소켓 스터디

* [웹 소켓 위키](https://github.com/ShinHanSWT1/Omok_Mini_Project.wiki.git)


## 구조

* MVC 모델

| 아키텍쳐 설계1 | 아키텍쳐 설계2 |
|:--:|:--:|
|<img width="500" height="400" alt="image" src="https://github.com/user-attachments/assets/29a35e46-6df8-4c81-9824-c3b0de10b382" />|<img width="500" height="400" alt="image" src="https://github.com/user-attachments/assets/4c7efd47-b193-4a55-869f-013fb11b98fb" />|


### 패키지 구조

```java
 src/
 └── main/java/com/omok
     ├── controller/        ← Servlet, WebSocket Endpoint
     │     ├── LoginServlet.java
     │     ├── RegisterServlet.java
     │     ├── LobbyServlet.java
     │     ├── RoomServlet.java
     │     └── GameWebSocket.java
     │
     ├── service/           ← 비즈니스 로직
     │     ├── UserService.java
     │     ├── RoomService.java
     │     ├── GameService.java
     │     └── ResultService.java
     │
     ├── repository/        ← DAO + SQL
     │     ├── UserDAO.java
     │     ├── RoomDAO.java  
     │     └── ResultDAO.java
     |
     ├── manager/           ← 싱글톤으로 객체를 관리, 기능 수행
     │     ├── RoomManager.java
     │     ├── GameManager.java
     │
     ├── domain/            ← VO 
     │     ├── User.java
     │     ├── Room.java
     │     ├── Player.java
     │     ├── Board.java
     │     └── Move.java
     │
     ├── game/               ← 게임 로직 묶음
     │     ├── Board.java
     │     ├── Judge.java
     │     ├── Timer.java
     │     └── GameRule.java
     │
     └── util/
           ├── JsonUtil.java
           └── DBConnection.java
 
 
 webapp/
  ├── WEB-INF/
  │     └── views/
  │           ├── login.jsp
  │           ├── lobby.jsp
  │           ├── room.jsp
  │           └── game.jsp
  │
  └── static/
        ├── js/
        ├── css/
        └── img/

```


## 코드 플로우

* [웹 소켓을 이용한 오목_흐름도.drawio](https://drive.google.com/file/d/1A-UerBnSQWB8CKETi9cZNwWrvPYuyRjW/view)

|로그인| <img width="800" height="600" alt="image" src="https://github.com/user-attachments/assets/416b8c50-784a-44ff-bd72-92a8ad5d185c" />|
|:--:|:--:|
|로비|<img width="800" height="600" alt="image" src="https://github.com/user-attachments/assets/3ca4e088-7b1b-441c-9350-a43aa8c08d01" />|
|게임|<img width="6200" height="4448" alt="image" src="https://github.com/user-attachments/assets/dbdd5021-db4a-41f8-aaed-63349612a200" />|


 



# 팀원

<table>
  <tr>
     <td align="center">
      <a href="https://github.com/1004Jumto">
        <img width="100" height="100" alt="image" src="https://github.com/user-attachments/assets/3929ae8a-e024-4239-ac94-d952459e6667" /><br />
        <sub><b>예진</b></sub> 
    </td>
    <td align="center">
      <a href="https://github.com/namhunheo">
        <img width="100" height="100" alt="image" src="https://github.com/user-attachments/assets/30bdd0aa-c60d-4d69-8d01-58a10a7b49b4" /><br />
        <sub><b>남훈<b></sub> 
    </td>
    <td align="center">
      <a href="https://github.com/SSjunn">
        <img width="100" height="100" alt="image" src="https://github.com/user-attachments/assets/2294540a-8fd3-405c-8c6b-6397b17e5672" /><br />
        <sub><b>성준</b></sub> 
    </td>
    <td align="center">
      <a href="https://github.com/jms0324">
        <img width="100" height="100" alt="image" src="https://github.com/user-attachments/assets/60c3d8ab-af5c-4dea-941a-c72c2314b864" /><br />
        <sub><b>민석</b></sub> 
    </td>
    <td align="center">
      <a href="https://github.com/baiees">
        <img width="100" height="100" alt="image" src="https://github.com/user-attachments/assets/60ce9e04-58c9-441b-b5ce-bb0808e3714b" /><br />
        <sub><b>훈주</b></sub> 
    </td>
    <td align="center">
      <a href="https://github.com/wlaos">
        <img width="100" height="100" alt="image" src="https://github.com/user-attachments/assets/95757f6c-d0ef-46c1-92d0-6bfab2f9f30e" /> <br />
        <sub><b>재민</sub> 
    </td>
  </tr>
</table>  

 

# 추가 문서

- [프로젝트 전체 문서화](docs/project-documentation.md)
- [이력서용 역할 및 기술 문서](docs/resume-role-documentation.md)

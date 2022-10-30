# 01410.coroke.net

[01410.coroke.net](https://01410.coroke.net) 는 PC통신으로 불리우는 VT서비스의 UI를 체험해볼 수 있는 서비스입니다.

PC통신의 사용자 경험, 파란 화면에 표시되는 글자만으로 사람들과 연결되는 경험들이 역사의 뒤안길로 사라지지 않고 계속 보존될 수 있도록 01410.coroke.net 의 소스코드를 공개합니다.
소스코드를 활용하여 유사한 서비스를 만들어도 좋고, 더 다듬어주셔도 좋습니다.

# 주요 기능

- 소셜 로그인 (트위터 및 구글 계정)
- 글 쓰기
- 글 읽기

# 기술 사양

- Kotlin/Spring Boot
- Java 18
- HTML & CSS & JavaScript (Bundler 없음)

# 주요 설정

- application.properties 와 env.env 를 참조하여 DB에 연결하고, 트위터 및 구글의 OAuth 설정값을 추가합니다.

# 서버 기동 방법

- `./gradlew bootJar` 로 jar 컴파일을 한 뒤
- `java -jar terminal-1.0.0.jar` 로 서버를 가동합니다.
- 또는 Dockerfile 을 참조해 docker 컨테이너를 가동합니다.

# 권장사항

- src/main/resources/static 은 nginx 등에서 바로 제공토록 설정하는 것을 권장합니다. nginx-terminal.conf 설정파일을 참조하세요.

# 기여

- Issue 에 자유로이 제안을 해주셔도 좋고, 직접 PR을 제안하셔도 좋습니다. 
- 제안해주신 코드는 01410.coroke.net 서비스에 반영될 수 있습니다.

# 라이센스

MIT License 

# Credits

- [Lee JunHaeng aka rainygirl](https://rainygirl.com/)

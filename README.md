# URL-Shortner
![img](https://user-images.githubusercontent.com/54087826/257005548-576e042c-d576-45d7-9c81-47f1902d53f3.png)

## Links 
- [API Doc](https://documenter.getpostman.com/view/17357775/2s9XxtxFCN)

## Installation 
- Apache Zookeeper 3.8.2
- MongoDB 6.0.6
- Redis 7.0.11
- Java 17+ (It may work on lower version too, but it hasn’t been tested)
- Add the required credentials in https://github.com/ats1999/URL-Shortner/blob/main/url-shortner/src/main/resources/application.properties

> if you are not using IDE

- Install Maven
- `mvn package`
- `mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8080`

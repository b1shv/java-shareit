### ShareIt
#### _Сервис для шеринга вещей_
_Java 11, Spring Boot, Maven, Lombok, Hibernate, PostgreSQL, Mockito, Docker_

ShareIt позволяет пользователям:
* добавлять, редактировать и удалять вещи, которыми пользователи хотят поделиться;
* бронировать вещи других пользователей на определённое время;
* искать вещи по названиям и описаниям;
* делать запросы на вещи, которых нет в текущем каталоге;
* оставлять комментарии после бронирования.

Приложение состоит из двух модулей: server (основной модуль) и gateway (валидирует запросы к приложению и перенаправляет 
их в основной модуль).

### Установка и запуск приложения в Docker
Для запуска приложения на вашем компьютере должны быть установлены Java версии 11, Maven и Docker версии 1.13.1 или выше.
Последовательность действий:
* собрать jar-файлы server и gateway с помощью команды mvn package;
* запустить Docker Compose из корня проекта командой docker-compose up.

После того как Docker скачает образ базы данных, создаст образы сервисов и запустит контейнеры,
приложение будет доступно через порт 8080.
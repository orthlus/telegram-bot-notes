Телеграм бот, который поможет восстановить во времени все сообщения, отправленные ему. Время по МСК.

Полезно для быстрого трекинга задач.

Выгрузка пока только в .txt

Всю историю записей можно удалить.

Поднят здесь https://t.me/what_are_you_up_to_bot

Сборка:
1. `` mvn clean package ``
2. применить в бд скрипты из ./db (PostgreSQL)
3. `` docker-compose -f docker-compose-example.yml up -d ``
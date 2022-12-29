# habr-abbr-scanner

сканер, публикующий в телеграм канал новые посты с habr.com, в которых используется фича 
аббревиатуры (Строчные спойлеры) https://habr.com/ru/company/habr/blog/705856/

работающий канал - https://t.me/habr_abbr_scan

### подготовка telegram
1. создать бота в [BotFather](https://t.me/BotFather)
2. создать публичный канал
3. добавить бота в канал как админа
4. выполнить в браузере запрос

    https://api.telegram.org/bot000000:AAAA123AAAA/sendMessage?chat_id=@channel_nick&text=123

    где

    _channel_nick_ - публичная ссылка на канал (после _t.me/_)

    _000000:AAAA123AAAA_ - токен бота, полученный от BotFather
5. в ответ получить json, в котором будет id канала (long с минусом, _-995684641434541_)
    1. только после этого сделать канал приватным (если требуется)
6. подставить токен бота и id канала в env в docker-compose


### как запустить:

выполнить в бд миграцию из sql/, подставив свой пароль в _WITH PASSWORD ''_

для dev: сканирование схемы подключенной бд и генерация jooq классов

```bash
mvn clean jooq-codegen:generate
```
сборка

```bash
mvn clean package

docker build -t habr-abbr-scanner:latest .
docker image tag habr-abbr-scanner:latest habr/habr-abbr-scanner:latest
docker image push habr/habr-abbr-scanner:latest

```

запуск

```bash
docker-compose -f ./docker-compose-example-habr-abbr-scanner-bot.yml up -d
```
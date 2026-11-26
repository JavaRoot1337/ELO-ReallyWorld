# ELO

Плагин PvP-рейтинга, рангов и таблицы лидеров для Paper и Folia `1.21–26.3`.

## Совместимость

| Параметр | Значение |
| --- | --- |
| Ядро | Paper, Folia (`folia-supported: true`) |
| Minecraft | `1.21 – 26.3` |
| Java | `21+` |
| Опционально | `PlaceholderAPI`, `Duels` |

## Сборка

```powershell
.\gradlew.bat shadowJar
```

Итоговый JAR: `build/libs/ELO-26.2.0.jar`.

## Команды

| Команда | Действие | Право |
| --- | --- | --- |
| `/elo [ник]` | Статистика игрока | `elo.use` |
| `/elotop` | Топ игроков по ELO | `elo.top` |
| `/elo give <ник> <кол-во>` | Выдать ELO | `elo.admin` |
| `/elo take <ник> <кол-во>` | Списать ELO | `elo.admin` |
| `/elo set <ник> <кол-во>` | Установить ELO | `elo.admin` |
| `/elo reload` | Перезагрузить `config.yml` и `messages.yml` | `elo.admin` |

## Права

| Право | Назначение | По умолчанию |
| --- | --- | --- |
| `elo.use` | Доступ к `/elo` | всем |
| `elo.top` | Доступ к `/elotop` | всем |
| `elo.admin` | Управление рейтингом и reload | op |

## Конфигурация и БД

`plugins/ELO/config.yml`:

- `storage.type` — `sqlite` (WAL) или `mysql` (MySQL/MariaDB через HikariCP)
- `elo-win`, `elo-loss`, `elo-default`, `elo-min`, `elo-max`
- `kill-cooldown-sec` — анти-фарм кулдаун (`0` — выкл)
- `only-duels` — учитывать только матчи Duels
- `ranks` и `default-rank` — цвета `&`, `&#RRGGBB`, `&RRGGBB`

## Плейсхолдеры

Личные: `%elo_rating%`, `%elo_rank%`, `%elo_kills%`, `%elo_deaths%`, `%elo_kd%`

Топ: `%elo_top_name_<место>%`, `%elo_top_rating_<место>%`, `%elo_top_rank_<место>%`, `%elo_top_kills_<место>%`, `%elo_top_deaths_<место>%`, `%elo_top_kd_<место>%`

[English documentation](README.md)

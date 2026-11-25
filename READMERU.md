# ELO

Плагин на PvP-рейтинг для серверов Paper и Folia (`1.21 – 26.3`).

### Возможности
* Начисление и снятие ELO за PvP-убийства или победы в дуэлях (`only-duels` с поддержкой Duels).
* Защита от фарма (`kill-cooldown-sec`) и настраиваемые границы рейтинга (`elo-min`, `elo-max`).
* Гибкая система рангов с цветами `&` / `&#RRGGBB` / `&RRGGBB`.
* Хранилище SQLite (WAL) или MySQL/MariaDB через `HikariCP`, авто-миграция со старой таблицы `players`.
* Полная совместимость с Folia (`AsyncScheduler`, `GlobalRegionScheduler`, `EntityScheduler`) и Brigadier-команды Paper.
* Интеграция с PlaceholderAPI (`%elo_*%`, `%elo_top_*%`).

### Сборка
```bash
gradle shadowJar
```
Готовый плагин: `build/libs/ELO-<version>.jar` (Java 21+, Paper/Folia `1.21 – 26.3`).

### Команды
* `/elo [ник]` — статистика игрока (онлайн и оффлайн).
* `/elotop` — топ игроков по ELO.
* `/elo give <ник> <кол-во>` — выдать ELO.
* `/elo take <ник> <кол-во>` — списать ELO.
* `/elo set <ник> <кол-во>` — установить ELO.
* `/elo reload` — перезагрузить `config.yml` и `messages.yml`.

### Права
* `elo.use` — доступ к `/elo`.
* `elo.top` — доступ к `/elotop`.
* `elo.admin` — управление рейтингом и перезагрузка конфига.

### Плейсхолдеры (PlaceholderAPI)
Личные:
* `%elo_rating%`
* `%elo_rank%`
* `%elo_kills%`
* `%elo_deaths%`
* `%elo_kd%`

Топ (1–20+):
* `%elo_top_name_<место>%`
* `%elo_top_rating_<место>%`
* `%elo_top_rank_<место>%`
* `%elo_top_kills_<место>%`
* `%elo_top_deaths_<место>%`
* `%elo_top_kd_<место>%`

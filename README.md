# ELO

PvP rating plugin for Paper and Folia servers (`1.21 – 26.3`).

### Features
* ELO points for PvP kills or duel outcomes (`only-duels` with Duels hook).
* Anti-farm cooldown (`kill-cooldown-sec`) and configurable rating bounds (`elo-min`, `elo-max`).
* Dynamic ranks by ELO threshold with `&` / `&#RRGGBB` / `&RRGGBB` color formatting.
* Async SQLite (WAL) or MySQL/MariaDB storage via HikariCP, with automatic migration from legacy `players` table.
* Folia-native schedulers (`AsyncScheduler`, `GlobalRegionScheduler`, `EntityScheduler`) and Paper Brigadier commands.
* PlaceholderAPI expansion (`%elo_*%`, `%elo_top_*%`).

### Build
```bash
./gradlew shadowJar
# or with system Gradle:
gradle shadowJar
```
Output JAR: `build/libs/ELO-<version>.jar` (Java 21+, Paper/Folia `1.21 – 26.3`).

### Commands
* `/elo [player]` — View player stats (online or offline).
* `/elotop` — Top players leaderboard.
* `/elo give <player> <amount>` — Add ELO points.
* `/elo take <player> <amount>` — Subtract ELO points.
* `/elo set <player> <amount>` — Set exact ELO score.
* `/elo reload` — Reload `config.yml` and `messages.yml`.

### Permissions
* `elo.use` — Access to `/elo`.
* `elo.top` — Access to `/elotop`.
* `elo.admin` — Admin rating management and `/elo reload`.

### Placeholders (PlaceholderAPI)
Personal:
* `%elo_rating%`
* `%elo_rank%`
* `%elo_kills%`
* `%elo_deaths%`
* `%elo_kd%`

Global (Top 1–20+):
* `%elo_top_name_<pos>%`
* `%elo_top_rating_<pos>%`
* `%elo_top_rank_<pos>%`
* `%elo_top_kills_<pos>%`
* `%elo_top_deaths_<pos>%`
* `%elo_top_kd_<pos>%`

# ELO

PvP ELO rating, rank thresholds, and leaderboard for Paper and Folia `1.21–26.3`.

## Compatibility

| Parameter | Value |
| --- | --- |
| Core | Paper, Folia (`folia-supported: true`) |
| Minecraft | `1.21 – 26.3` |
| Java | `21+` |
| Optional | `PlaceholderAPI`, `Duels` |

## Build

```powershell
.\gradlew.bat shadowJar
```

Output JAR: `build/libs/ELO-26.2.0.jar`.

## Commands

| Command | Action | Permission |
| --- | --- | --- |
| `/elo [player]` | Show player ELO stats | `elo.use` |
| `/elotop` | Show top players leaderboard | `elo.top` |
| `/elo give <player> <amount>` | Add ELO points | `elo.admin` |
| `/elo take <player> <amount>` | Subtract ELO points | `elo.admin` |
| `/elo set <player> <amount>` | Set exact ELO score | `elo.admin` |
| `/elo reload` | Reload `config.yml` and `messages.yml` | `elo.admin` |

## Permissions

| Permission | Purpose | Default |
| --- | --- | --- |
| `elo.use` | Use `/elo` | all |
| `elo.top` | Use `/elotop` | all |
| `elo.admin` | Manage ratings and reload | op |

## Configuration & storage

`plugins/ELO/config.yml`:

- `storage.type` — `sqlite` (WAL) or `mysql` (MySQL/MariaDB via HikariCP)
- `elo-win`, `elo-loss`, `elo-default`, `elo-min`, `elo-max`
- `kill-cooldown-sec` — anti-farm cooldown (`0` disables)
- `only-duels` — count only Duels matches
- `ranks` & `default-rank` — `&` and `&#RRGGBB` / `&RRGGBB` formatting

## Placeholders

Personal: `%elo_rating%`, `%elo_rank%`, `%elo_kills%`, `%elo_deaths%`, `%elo_kd%`

Leaderboard: `%elo_top_name_<pos>%`, `%elo_top_rating_<pos>%`, `%elo_top_rank_<pos>%`, `%elo_top_kills_<pos>%`, `%elo_top_deaths_<pos>%`, `%elo_top_kd_<pos>%`

[Русская документация](READMERU.md)

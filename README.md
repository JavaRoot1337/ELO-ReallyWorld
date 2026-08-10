# ELO

A simple PvP rating plugin for ReallyWorld-like servers (1.16.x - 26.1.2).

### Features
* Gives and takes ELO points for player kills or duel outcomes (`only-duels`).
* Dynamic rank system based on ELO score.
* Stores data in a local SQLite database (zero lag or TPS drops at high online).
* Async SQL queries and leaderboard caching.
* Duels plugin integration.
* Full Tab Completion support for all commands and arguments.
* PlaceholderAPI support.

### Commands
* `/elo [player]` — View player stats.
* `/elotop` — Top 10 leaderboard.
* `/elo give <player> <amount>` — Add ELO points to a player.
* `/elo take <player> <amount>` — Remove ELO points from a player.
* `/elo set <player> <amount>` — Set a player's ELO score.
* `/elo reload` — Reload configuration files.

### Permissions
* `elo.use` — Access to `/elo`.
* `elo.top` — Access to `/elotop`.
* `elo.admin` — Access to admin management commands.

### Placeholders (PlaceholderAPI)
Personal:
* `%elo_rating%`
* `%elo_rank%`
* `%elo_kills%`
* `%elo_deaths%`
* `%elo_kd%`

Global (Top 1-20):
Replace `1` with any number from 1 to 20. Perfect for holograms!
* `%elo_top_name_1%`
* `%elo_top_rating_1%`
* `%elo_top_rank_1%`
* `%elo_top_kills_1%`
* `%elo_top_deaths_1%`
* `%elo_top_kd_1%`

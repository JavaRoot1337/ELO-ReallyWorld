# ELO

A simple PvP rating plugin for ReallyWorld-like servers.

### Features
* Gives and takes ELO points for player kills.
* Rank system based on ELO score.
* Stores data in a local SQLite database (zero lag or TPS drops, even with high online).
* Async queries and caching for the leaderboard.
* PlaceholderAPI support.

### Commands
* `/elo [player]` — View a player's stats.
* `/elotop` — Server Top 10 leaderboard.

### Placeholders (PlaceholderAPI)
Personal:
* `%elo_rating%`
* `%elo_rank%`
* `%elo_kills%`
* `%elo_deaths%`
* `%elo_kd%`

Global (Top 1-20):
Replace 1 with any number from 1 to 10. Perfect for holograms!
* `%elo_top_name_1%`
* `%elo_top_rating_1%`
* `%elo_top_rank_1%`
* `%elo_top_kills_1%`
* `%elo_top_deaths_1%`
* `%elo_top_kd_1%`

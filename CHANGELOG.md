# CHANGELOG
## 1.1.0
### Added
- New command *!setPronouns*. Users can set their preferred pronouns, that is used in bot's messages. Possible values: M - male, F - female. Default: M.
- Weekly Gold summary/ranking.
- Nice message :)
- Gold for Bot response.
### Changed
- Store user and messages data in database.
- *!status* command response uses embed message. Added info about version and author.
### Fixed
- Fixed *!usersGold* command response not showing, when response is longer then 2000 characters (Discord's limit).
## 1.0.1
### Changed
-  Removed partial counts - only one full count is allowed at given time.
### Fixed
-  Fixed gold not being reset at the start of full count.
## 1.0.0
- Bot released!
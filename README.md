
# Project Details
Sprint 2: Server
Team members: Christina Peng (jpeng29) & Havi Nguyen (hnguy116)
Repo: https://github.com/cs0320-s24/server-hnguy116-jpeng29.git
Est. completion time: 30 hr

# Design Choices
- Main class Server creates new instances of handlers depending on a path input by the user.
  Separate classes represent the varying functionalities of the server, such as loading, searching,
  and viewing a CSV, as well as making requests to an external API.
- The load, search, and view CSV handlers use a state object that they update, search, or
  serialize to return to user.
- The AcsHandler retrieves state codes once and stores them in a map. When the user makes a request,
  the map is searched to find the corresponding state code and its counties, and a call is made to
  api.census.gov to find the broadband access percentage.

# Errors/Bugs
- No known bugs 

# Tests
- Our program contains two testing folders: one for classes pertaining to our Parser and 
  Searcher, and another for classes concerning Server, and each respective handler. 

# How to
- Run the program:
- run "mvn package", then ./run
- click on the link printed to terminal
- at the end of the link in browser, add desired path (ex. "searchcsv") and
desired query parameters (ex. ?target=RI)
- Input a file: the program will only accept files in the data folder.
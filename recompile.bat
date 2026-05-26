@echo off
rem Необходим RetroMCP Cli - https://github.com/MCPHackers/RetroMCP-Java
echo Building...
java -jar RetroMCP-Java-CLI.jar build
timeout /t 3 /NOBREAK >nul 2>&1

echo -----------------
echo Done
pause

exit	
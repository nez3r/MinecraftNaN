@echo off
chcp 65001 > nul
cls
echo.

git status --short
echo.

:: Запрашиваем описание изменений
set /p commit_message="commit message: "

:: Если пользователь ничего не ввел, задаем стандартное описание
if "%commit_message%"=="" set commit_message="auto update"

echo.
echo [1/3] Добавление файлов...
git add .

echo [2/3] Фиксация изменений...
git commit -m "%commit_message%"


echo [3/3] Отправка на GitHub...
git push

echo.
pause

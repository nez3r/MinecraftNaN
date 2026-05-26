# Minecraft NaN - Creepypasta Features

## Реализованные функции

### 1. Переименование версии
- Все упоминания "Beta 1.5_01" заменены на "NaN"
- Изменено в:
  - Главное меню
  - Заголовок окна
  - Отчёты о крашах
  - Конфигурационные файлы
  - Debug информация

### 2. VHS эффект (постоянный)
Эффект старой VHS-кассеты применяется ко всему экрану:
- **Scanlines** - горизонтальные линии сканирования
- **Random noise** - случайные белые помехи
- **Glitch lines** - случайные горизонтальные полосы
- **Vignette** - затемнение по краям экрана
- **Chromatic aberration** - пульсирующий красноватый оттенок

Эффект работает везде: в игре, в меню, всегда активен.

### 3. Автоматическое сообщение об ошибке Herobrine
Через **5 минут** после загрузки мира появляются сообщения:
```
[FATAL ERROR] Float overflow in entity.Herobrine.posX
[FATAL ERROR] NaN detected in world renderer
[WARNING] Attempting to recover...
[WARNING] Entity 'Herobrine' removed from world
```

### 4. Команда /next - Мистические ивенты
В одиночной и многопользовательской игре используйте команду **/next** в чате для запуска последовательных мистических событий:

**Event 1: Strange whispers + Bedrock crosses**
- "I see you..."
- Появление 3-5 бедроковых крестов на высоте Y=90-100 над игроком

**Event 2: Reality glitch + Weather change**
- "Warning: GL11 is corrupted."
- Смена погоды на противоположную (дождь ↔ ясно)

**Event 3: Time anomaly + Netherrack with fire**
- "nope"
- Появление круга из незерака с огнём вокруг игрока

**Event 4: Entity warning**
- "Unknown entity detected at coordinates"

**Event 5: Memory corruption**
- "Stay away."

**Event 6: The watcher**
- "Something is watching you"

**Event 7: Save corruption**
- "you shouldn't be here"

**Event 8: Final message**
- "He is coming"

После 8-го ивента цикл начинается заново.

## Компиляция
Запустите `recompile.bat` для сборки проекта.

## Использование
1. Запустите игру
2. Создайте или загрузите одиночный мир
3. Через 5 минут появится автоматическое сообщение об ошибке Herobrine
4. Откройте чат (клавиша T) и введите команду **/next** для запуска мистических ивентов

## Технические детали
- VHS эффект реализован в `EntityRenderer.renderVHSEffect()`
- Таймер Herobrine запускается при загрузке мира
- Команда /next обрабатывается в `Minecraft.lineIsCommand()`
- Чат доступен в одиночной игре (убрана проверка на мультиплеер)
- Бедроковые кресты создаются в `spawnBedrockCrosses()`
- Смена погоды в `toggleWeather()`
- Незерак с огнём в `spawnNetherrackWithFire()`
- Все изменения внесены только в `src/`, `src_original/` не тронут

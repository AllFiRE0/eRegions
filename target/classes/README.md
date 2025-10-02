# eRegions

**Продвинутый плагин управления регионами с триггерами команд и визуальными эффектами**

---

## 📋 Содержание

1. [Команды](#команды)
2. [Права доступа](#права-доступа)
3. [PlaceholderAPI плейсхолдеры](#placeholderapi-плейсхолдеры)
4. [Конфигурация](#конфигурация)
5. [Триггеры команд](#триггеры-команд)
6. [Типы сообщений](#типы-сообщений)

---

## 🎮 Команды

### Основная команда
- **Команда:** `/eregion`
- **Алиасы:** `/ereg`, `/erg`

### Основные команды
```bash
/eregion create                    # Создать новый регион
/eregion cancel                    # Отменить создание региона
/eregion remove <region>           # Удалить регион
/eregion help                     # Показать справку
/eregion reload                   # Перезагрузить конфигурацию
```

### Управление участниками
```bash
/eregion member add <region> <player>     # Добавить участника
/eregion member remove <region> <player>  # Удалить участника
```

### Управление владельцами
```bash
/eregion owner add <region> <player>     # Добавить владельца
/eregion owner remove <region> <player>  # Удалить владельца
```

### Управление флагами
```bash
/eregion flag add <region> <flag> <value>    # Добавить флаг
/eregion flag remove <region> <flag>         # Удалить флаг
/eregion flags <region>                      # Показать все флаги региона
```

### Изменение региона
```bash
/eregion move <+/-distance>        # Переместить выделенную область
/eregion size <+/-distance>        # Изменить размер выделенной области
```

### Админские команды
```bash
/eregion admin flag <region> <flag> <value> <groups> [silent]
```
**Описание:** Админское управление флагами с поддержкой групп  
**Примеры:**
- `/eregion admin flag мойрегион pvp deny owners members`
- `/eregion admin flag мойрегион chest-access allow all silent`

---

## 🔐 Права доступа

### Основные права
- `eregions.*` - Все права eRegions *(по умолчанию: op)*
- `eregions.use` - Базовое использование eRegions *(по умолчанию: true)*
- `eregions.admin` - Админский доступ к eRegions *(по умолчанию: op)*
- `eregions.admin.flag` - Админское управление флагами с группами *(по умолчанию: op)*
- `eregions.view` - Просмотр границ регионов и эффектов *(по умолчанию: true)*
- `eregions.reload` - Перезагрузка конфигурации eRegions *(по умолчанию: op)*
- `eregions.help` - Просмотр справочной информации *(по умолчанию: true)*

### Права управления регионами
- `eregions.region.*` - Все права управления регионами *(по умолчанию: false)*
- `eregions.region.create` - Создание новых регионов *(по умолчанию: true)*
- `eregions.region.remove` - Удаление регионов *(по умолчанию: false)*
- `eregions.region.members` - Управление участниками регионов *(по умолчанию: false)*
- `eregions.region.owner` - Управление владельцами регионов *(по умолчанию: false)*
- `eregions.region.flag` - Управление флагами регионов *(по умолчанию: false)*
- `eregions.region.flags` - Просмотр флагов региона *(по умолчанию: false)*
- `eregions.region.move` - Перемещение регионов *(по умолчанию: false)*
- `eregions.region.size` - Изменение размера регионов *(по умолчанию: false)*
- `eregions.region.view` - Просмотр информации о регионе *(по умолчанию: true)*

---

## 📊 PlaceholderAPI плейсхолдеры

**Всего доступно 262 плейсхолдера**

### Плейсхолдеры выделения (3 штуки)
| Плейсхолдер | Описание | Пример |
|-------------|----------|--------|
| `%eregions_selection_pos1%` | Координаты первой точки выделения (x, y, z) | `100, 64, 200` |
| `%eregions_selection_pos2%` | Координаты второй точки выделения (x, y, z) | `110, 70, 210` |
| `%eregions_selection_size%` | Количество блоков в текущем выделении | `1000` |

### Плейсхолдеры информации о регионе (55 штук)
| Плейсхолдер | Описание | Пример |
|-------------|----------|--------|
| `%eregions_region_flags%` | Все флаги в текущем регионе | `pvp:deny, mob-spawning:allow` |
| `%eregions_region_flags_1%` до `%eregions_region_flags_50%` | Отдельные флаги по индексу (1-50) | `%eregions_region_flags_1% → pvp:deny` |
| `%eregions_region_viewing%` | Игрок имеет право eregions.region.view (true/false) | `true` |
| `%eregions_region_size%` | Количество блоков в текущем регионе | `1000` |
| `%eregions_region_creator%` | Имя создателя региона | `ИмяИгрока` |
| `%eregions_region_expelled%` | Имя последнего исключенного игрока | `ИмяИгрока` |

### Плейсхолдеры владельцев/участников текущего региона (102 штуки)
| Плейсхолдер | Описание | Пример | Разделитель |
|-------------|----------|--------|-------------|
| `%eregions_region_owners%` | Все владельцы текущего региона | `Игрок1, Игрок2, Игрок3` | `placeholders.owners-separator` |
| `%eregions_region_owners_1%` до `%eregions_region_owners_50%` | Отдельные владельцы по индексу (1-50) | `%eregions_region_owners_1% → Игрок1` | - |
| `%eregions_region_members%` | Все участники текущего региона | `Участник1, Участник2, Участник3` | `placeholders.members-separator` |
| `%eregions_region_members_1%` до `%eregions_region_members_50%` | Отдельные участники по индексу (1-50) | `%eregions_region_members_1% → Участник1` | - |

### Плейсхолдеры регионов игрока (102 штуки)
| Плейсхолдер | Описание | Пример | Разделитель |
|-------------|----------|--------|-------------|
| `%eregions_region_owned%` | Все регионы, которыми владеет игрок | `регион1, регион2, регион3` | `placeholders.owned-separator` |
| `%eregions_region_owned_1%` до `%eregions_region_owned_50%` | Отдельные регионы по индексу (1-50) | `%eregions_region_owned_1% → регион1` | - |
| `%eregions_region_membed%` | Все регионы, в которых игрок является участником | `регион4, регион5, регион6` | `placeholders.membered-separator` |
| `%eregions_region_membed_1%` до `%eregions_region_membed_50%` | Отдельные регионы по индексу (1-50) | `%eregions_region_membed_1% → регион4` | - |

---

## ⚙️ Конфигурация

### Основные настройки
```yaml
# Режим отладки
debug: false

# PlaceholderAPI интеграция
placeholders:
  enabled: true
  owners-separator: ", "
  members-separator: ", "
  owned-separator: ", "
  membered-separator: ", "
```

### Настройка разделителей
```yaml
placeholders:
  owners-separator: " | "      # Разделитель для владельцев
  members-separator: " & "     # Разделитель для участников
  owned-separator: " -> "      # Разделитель для регионов игрока
  membered-separator: " <- "   # Разделитель для регионов участника
```

---

## 🎯 Триггеры команд

Триггеры команд выполняют команды или отправляют сообщения при определенных событиях.

### Доступные события
- `region-created` - Регион создан
- `region-removed` - Регион удален
- `region-resized` - Размер региона изменен
- `region-moved` - Регион перемещен
- `owner-added` - Владелец добавлен
- `owner-removed` - Владелец удален
- `member-added` - Участник добавлен
- `member-removed` - Участник удален
- `flag-added` - Флаг добавлен
- `flag-removed` - Флаг удален

### Доступные плейсхолдеры
- `{player_name}` - Имя игрока, который вызвал событие
- `{region_name}` - Название региона
- `{size}` - Количество изменения размера
- `{flag_name}` - Название флага
- `{state_flag}` - Состояние флага (allow/deny)
- `{point_1}` - Координаты первой точки (x, y, z)
- `{point_2}` - Координаты второй точки (x, y, z)

### Примеры триггеров
```yaml
command-triggers:
  region-created:
    enabled: true
    message: "title! &aРегион {region_name} создан успешно!"
    
  region-removed:
    enabled: true
    message: "subtitle! &cРегион {region_name} удален!"
    
  region-resized:
    enabled: true
    message: "chat! &eРегион изменен в размере на {size} блоков. Новые координаты: {point_1} - {point_2}"
    
  region-moved:
    enabled: true
    message: "actionbar;3! &bРегион перемещен! Новые координаты: {point_1} - {point_2}"
```

---

## 💬 Типы сообщений

| Префикс | Описание | Пример |
|---------|----------|--------|
| `chat!` | Отправить сообщение в чат игрока | `chat! &aСообщение в чат` |
| `actionbar!` | Отправить сообщение в action bar (по умолчанию 1 секунда) | `actionbar! &bСообщение в action bar` |
| `actionbar;N!` | Отправить сообщение в action bar на N секунд | `actionbar;5! &bСообщение на 5 секунд` |
| `title!` | Отправить заголовок (по умолчанию 1 секунда) | `title! &eЗаголовок` |
| `title;N!` | Отправить заголовок на N секунд | `title;3! &eЗаголовок на 3 секунды` |
| `subtitle!` | Отправить подзаголовок (по умолчанию 1 секунда) | `subtitle! &6Подзаголовок` |
| `subtitle;N!` | Отправить подзаголовок на N секунд | `subtitle;2! &6Подзаголовок на 2 секунды` |
| `title!` + `%subtitle%` | Отправить заголовок и подзаголовок | `title! &aЗаголовок%subtitle%&6Подзаголовок` |
| `asConsole!` | Выполнить команду от имени консоли | `asConsole! say Регион создан игроком {player_name}!` |
| `asPlayer!` | Выполнить команду от имени игрока | `asPlayer! tp {player_name} 0 100 0` |

---

## 🔧 Устранение неполадок

### Частые проблемы

| Проблема | Решение |
|----------|---------|
| Плейсхолдеры не работают | Убедитесь, что PlaceholderAPI установлен и eRegions включен в PlaceholderAPI |
| title! и subtitle! не отображаются | Проверьте, используете ли вы Paper 1.21+ и доступен ли Adventure API |
| Триггеры команд не выполняются | Проверьте, включено ли событие и правильный ли формат сообщения |
| Права не работают | Убедитесь, что игрок имеет необходимый permission node |

### Режим отладки
- **Включение:** Установите `debug: true` в config.yml
- **Назначение:** Показывает подробное логирование для устранения неполадок
- **Расположение:** Консоль сервера и файлы логов

---

## 📦 Зависимости

### Обязательные
- WorldGuard
- WorldEdit

### Опциональные
- PlaceholderAPI
- Vault
- LuckPerms
- CMI
- SelectionVisualizer
- FastAsyncWorldEdit

### Совместимость
- **Версии Minecraft:** 1.16+
- **Версии Java:** Java 8+
- **Протестировано на:** Paper 1.21.7
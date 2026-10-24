# HeavenTPA

[![Paper](https://img.shields.io/badge/Paper-1.21.11-2b2b2b?logo=paper&logoColor=white)](https://papermc.io/)
[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)](https://adoptium.net/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Tests](https://img.shields.io/badge/tests-JUnit-informational)](src/test/java)

Телепорт-запросы для **Purpur/Paper 1.21.11**, в которые можно ответить **кликом
по кнопке в чате**, а не набором команды.

## Что умеет

- **`/tpa <ник>`** — запрос на телепорт к игроку.
- **Кнопки `[Принять]` / `[Отклонить]`** — прямо в сообщении получателю, клик = действие.
- **`/tpaccept` и `/tpdeny`** — обычные команды для тех, кто любит клавиатуру.
- **Истечение запроса** через `expire-seconds` (по умолчанию 60 секунд) — старые заявки не висят вечно.
- **Точная точка или безопасное место** — `general.exact: true` телепортирует в исходные координаты, `false` ищет безопасную площадку рядом.
- **Один запрос на пару** — повторная отправка заменяет предыдущий, спам не множит кнопки.
- **Essentials** — мягкая совместимость, если он установлен.

## Команды

| Команда | Что делает | Право |
|---|---|---|
| `/tpa <ник>` | Отправить запрос | `heaventpa.use` |
| `/tpaccept [ник]` | Принять запрос | `heaventpa.use` |
| `/tpdeny [ник]` | Отклонить запрос | `heaventpa.use` |
| `/tpareload` | Перечитать конфиг | `heaventpa.admin` |

Аргумент `[ник]` нужен, если к вам одновременно пришло несколько запросов.

## Права

| Право | По умолчанию |
|---|---|
| `heaventpa.use` | всем |
| `heaventpa.admin` | оператор |

## Конфиг

```yaml
general:
  prefix: '<gradient:#7AD9FF:#FFD76A>[ТП]</gradient> <gray>'
  sounds: true
  expire-seconds: 60    # сколько живёт запрос
  exact: true           # false = искать безопасное место рядом
```

## Установка

```bash
./gradlew build       # Windows: gradlew.bat build
# build/libs/HeavenTPA-1.0.1.jar -> plugins/
```

Нужен JDK 21. После копирования jar перезапустите сервер.

## Тесты

Логика заявок (срок жизни, замена, выбор получателя) вынесена из Bukkit и покрыта JUnit:

```bash
./gradlew test
```

## Лицензия

MIT — см. [LICENSE](LICENSE).

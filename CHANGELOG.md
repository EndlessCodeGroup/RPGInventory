## 2.3.0 (2019-04-29)

*No changes since 2.3.0-RC1.*

## 2.3.0-RC1 (2019-04-22)

25 changes total

### Fixed (6 changes)

- New inventories and backpacks serialization mechanism. Should fix issues with items loss.
- NPEs: in InventoryLocker, in CraftExtension, on RP loading.
- Skip wrong pets options instead of fail.
- Fixed messages with single quote.
- Shield slot.
- Can't remove pet after leave with killed pet.

### Added (5 changes)

- More logs. All plugin's logs you can found in `RPGInventory/logs/latest.log`.
- Understandable errors when you use unknown enum (item rarity, pet role, color, horse variant, etc.).
- Understandable errors when wrong texture passed.
- Friendly log message on durability parsing.
- Always show pets names.

### Changed (8 changes)

- Side features now disabled by default: slots buying, craft extensions, resource-pack.
- Removed unused options from config: `metrics`, `containers`.
- Plugin is enabled by default. First start message removed.
- Disable recipe book only if craft extensions are enabled.
- Removed unused lines from lang files: `message.fixhp`, `error.rp.force`.
- Better RP URL validation. Support of relative redirects.
- **BREAKING CHANGE:** Removed built-in placeholders, use PlaceholderAPI instead.
- **BREAKING CHANGE:** Removed slot type `MYPET`. When MyPet enabled `PET` slot works as `MYPET`.

### Other (6 changes)

- Improved compatibility with MC 1.13.
- Inspector updated to 0.8.1.
- Updated PlaceholderAPI integration.
- Updated Skills integration.
- Updated MyPet integration.
- bStats instead of MCStats.

<!-- Template:
## 2.X.X (YYYY-MM-DD)

X changes total

### Added (X changes)

### Changed (X changes)

### Fixed (X changes)

### Other (X changes)
-->

## 2.3.1-SNAPSHOT (2019-12-15)

X changes total

### Added (X changes)

- Support of 1.14 and 1.15
- Pet skin OCELOT replaced with CAT  
  Available cats types: TABBY (default), BLACK, RED, SIAMESE, BRITISH_SHORTHAIR, CALICO, PERSIAN, RAGDOLL, WHITE, JELLIE, ALL_BLACK
- Added COLLAR option for CAT pet skin

### Changed (X changes)

- Removed movement blocking on resource-pack loading
- Removed option `resource-pack.delay` from config
- Removed support of 1.13 and older

### Fixed (X changes)

- Fixed problem with items name and description encoding after inventory save  
- Fixed quickbar slots detection
- Fixed crashes on pets damage
- Fixed crashes from PlaceholderAPI
- Fixed plugin loading fail if MyPet initialization failed
- Fixed many NPE crashes
- Fixed kick when player accepted

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

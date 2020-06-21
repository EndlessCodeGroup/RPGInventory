<!-- Template:
## 2.X.X (YYYY-MM-DD)

X changes total

### Added (X changes)

### Changed (X changes)

### Fixed (X changes)

### Other (X changes)
-->

## 2.3.2-rc3 (2020-06-22)

4 changes total

### Fixed (4 changes)

- Add missing weapons and armor to default slot configs
- Fix crash when backpack size is more than 54
- Fix armor validation on right click and dispense
- Prevent damage pet with spectral arrow

## 2.3.2-rc2 (2020-06-06)

3 changes total

### Fixed (3 changes)

- Add GOLD**EN** tools to the default config
- Prevent equip restricted armor with right-click
- Prevent equip restricted armor with shift-click when `armor_slot_action` is `rpginv`

## 2.3.2-rc1 (2020-06-02)

5 changes total

### Added (3 changes)

- **Added option `textures-type`.**  
  Now you can switch textures type between Custom Model Data and damage.
  Available values:
  - `damage` (default) - https://www.spigotmc.org/wiki/custom-item-models-in-1-9-and-up/
  - `custom_model_data` - modern replacement for textures by damage for 1.14+
- Now 1.15 is fully supported
- Disallow to open RPGInventory in creative mode

### Changed (1 changes)

- Removed health info from default info slot

### Other (1 changes)

- Update Inspector to v0.9 - crash reports will be better

## 2.3.1 (2020-01-02)

2 changes total

### Fixed (2 changes)

- Crash when MyPet integration is enabled
- Crash when permissions plugin is not enabled

## 2.3.1-rc01 (2019-12-15)

13 changes total

### Added (3 changes)

- Support of 1.14 and 1.15
- Pet skin OCELOT replaced with CAT  
  Available cats types: TABBY (default), BLACK, RED, SIAMESE, BRITISH_SHORTHAIR, CALICO, PERSIAN, RAGDOLL, WHITE, JELLIE, ALL_BLACK
- Added COLLAR option for CAT pet skin

### Changed (3 changes)

- Removed movement blocking on resource-pack loading
- Removed option `resource-pack.delay` from config
- Removed support of 1.13 and older

### Fixed (7 changes)

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

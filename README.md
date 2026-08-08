# Composter Compatibilities

A lightweight mod that dynamically makes items compostable! Its main goal is to provide automatic out-of-the-box composter compatibility for food and items added by other mods, without needing any configuration.

## Features

- **Automatic Food Support:** Scans the item registry and automatically makes any food item compostable. The compost chance is scaled based on the food's nutrition value.
- **Smart Categorization:** Automatically detects and adds support for items that should be compostable, such as seeds, crops, drinks, and bonemealable plants.
- **Recipe Propagation:** If an item is crafted entirely from compostable ingredients, the mod will automatically make the crafted item compostable as well!
- **Meat Rejection:** Specifically ignores meat, fish, and recipes crafted with them, keeping the composter vegetarian-friendly as intended by vanilla gameplay.
- **Item Remainders:** If a compostable item is crafted with a bowl or a glass bottle (like a soup or a drink), composting it will correctly give you the empty bowl or bottle back instead of consuming it entirely!

## Compatibility

This mod works dynamically by analyzing item properties, tags, and crafting recipes at startup. It provides instant compatibility with almost any modded item, no data packs or configuration required!

## For Mod & Modpack Creators

Need to override the automatic behavior? You can use **item tags** to force items to be compostable (at a specific chance) or to prevent them from being composted entirely. No code or dependency required — just add a JSON file to your datapack or mod.

### Available Tags

| Tag | Effect |
|---|---|
| `c:not_compostable` | **Prevents** the item from being composted, even if it would normally be compostable |
| `c:compostable/chance_30` | Makes the item compostable with a **30%** chance |
| `c:compostable/chance_50` | Makes the item compostable with a **50%** chance |
| `c:compostable/chance_65` | Makes the item compostable with a **65%** chance |
| `c:compostable/chance_85` | Makes the item compostable with a **85%** chance |
| `c:compostable/chance_100` | Makes the item compostable with a **100%** chance |

> **Priority:** `c:not_compostable` always wins. If multiple `chance_*` tags are present, the highest one is used. Tag overrides are applied before all automatic detection.

### Example: Make an item compostable

Create a file at `data/c/tags/item/compostable/chance_65.json` in your datapack or mod resources (for 1.20.1 and older, use `tags/items/` instead of `tags/item/`):

```json
{
  "replace": false,
  "values": [
    "examplemod:rubber_duck"
  ]
}
```

### Example: Prevent an item from being composted

Create a file at `data/c/tags/item/not_compostable.json` (for 1.20.1 and older, use `tags/items/`):

```json
{
  "replace": false,
  "values": [
    "minecraft:rotten_flesh"
  ]
}
```
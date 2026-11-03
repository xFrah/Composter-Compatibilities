<div align="center">

![Composter Compatibilities](https://media.forgecdn.net/attachments/description/1602597/description_3637e704-5229-442d-bb7a-dc5b41965f15.png)

[![CurseForge Downloads](https://img.shields.io/curseforge/dt/1602597?logo=curseforge&label=CurseForge&color=F16436&style=for-the-badge)](https://www.curseforge.com/minecraft/mc-mods/composter-compatibilities)
[![Modrinth Downloads](https://img.shields.io/modrinth/dt/mNABp0x0?logo=modrinth&label=Modrinth&color=00AF5C&style=for-the-badge)](https://modrinth.com/mod/composter-compatibilities)

[![Fabric](https://img.shields.io/badge/Fabric-DBD0B4?style=for-the-badge&logo=fabric&logoColor=1f1f1f)](https://fabricmc.net/)
[![NeoForge](https://img.shields.io/badge/NeoForge-E05D26?style=for-the-badge&logo=forge&logoColor=white)](https://neoforged.net/)

[![1.20.1](https://img.shields.io/badge/1.20.1-3C8527?style=for-the-badge&logo=minecraft&logoColor=white)](https://modrinth.com/mod/composter-compatibilities/versions)
[![1.21.1](https://img.shields.io/badge/1.21.1-3C8527?style=for-the-badge&logo=minecraft&logoColor=white)](https://modrinth.com/mod/composter-compatibilities/versions)
[![1.21.11](https://img.shields.io/badge/1.21.11-3C8527?style=for-the-badge&logo=minecraft&logoColor=white)](https://modrinth.com/mod/composter-compatibilities/versions)
[![26.1.x](https://img.shields.io/badge/26.1.x-3C8527?style=for-the-badge&logo=minecraft&logoColor=white)](https://modrinth.com/mod/composter-compatibilities/versions)
[![26.2](https://img.shields.io/badge/26.2-3C8527?style=for-the-badge&logo=minecraft&logoColor=white)](https://modrinth.com/mod/composter-compatibilities/versions)
[![26.3](https://img.shields.io/badge/26.3-3C8527?style=for-the-badge&logo=minecraft&logoColor=white)](https://modrinth.com/mod/composter-compatibilities/versions)

</div>

A lightweight mod that dynamically makes items compostable! Its main goal is to provide automatic out-of-the-box composter compatibility for food and items added by other mods, without needing any configuration.

## Features
- Any food/vegetable is now compostable (except food that includes meat)
- Drinks (in bottles or in buckets) are now compostable, and the composter will spit out the container after it's done!
- Items made of all compostable items become compostable too.
- Any bone-mealable item/block becomes compostable too.
- Tags for making items compostable (or not compostable)
- Meat and rotten meat can be compostable, if set in config.
- Compatibility with JEI/REI/EMI, as they show the compostability of items.

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
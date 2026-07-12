package com.fdimo.compostercompat;

import net.minecraft.core.registries.BuiltInRegistries;

// This class is part of the common project meaning it is shared between all supported loaders. Code written here can only
// import and access the vanilla codebase, libraries used by vanilla, and optionally third party libraries that provide
// common compatible binaries. This means common code can not directly use loader specific concepts such as Forge events
// however it will be compatible with all supported mod loaders.
public class CommonClass {

    // The loader specific projects are able to import and use any code from the
    // common project. This allows you to
    // write the majority of your code here and load it from your loader specific
    // projects. This example has some
    // code that gets invoked by the entry point of the loader specific projects.
    public static void init() {
    }

    public static final boolean DEBUG = false; // Set to false to disable console spam

    public static void debugLog(String message, Object... args) {
        if (DEBUG) {
            Constants.LOG.info(message, args);
        }
    }

    public static java.util.List<String> crossVersionGetTags(net.minecraft.world.item.ItemStack stack) {
        return stack.tags().map(tagKey -> tagKey.location().toString()).toList();
    }

    public static net.minecraft.world.item.Item crossVersionGetRemainder(net.minecraft.world.item.Item item) {
        net.minecraft.world.item.ItemStackTemplate remainderTemplate = item.getCraftingRemainder();
        if (remainderTemplate != null) {
            return remainderTemplate.create().getItem();
        }
        net.minecraft.world.item.component.UseRemainder useRemainder = item.components()
                .get(net.minecraft.core.component.DataComponents.USE_REMAINDER);
        if (useRemainder != null) {
            return useRemainder.convertInto().item().value();
        }
        return null;
    }

    public static java.util.List<net.minecraft.world.item.crafting.Ingredient> crossVersionGetIngredients(net.minecraft.world.item.crafting.Recipe<?> recipe) {
        return recipe.placementInfo().ingredients();
    }

    public static net.minecraft.world.item.Item getHeuristicRemainder(net.minecraft.world.item.Item item) {
        String name = BuiltInRegistries.ITEM.getKey(item).getPath();
        if (name.contains("bottle") && item != net.minecraft.world.item.Items.GLASS_BOTTLE
                && item != net.minecraft.world.item.Items.EXPERIENCE_BOTTLE) {
            return net.minecraft.world.item.Items.GLASS_BOTTLE;
        } else if (name.contains("bowl") && item != net.minecraft.world.item.Items.BOWL) {
            return net.minecraft.world.item.Items.BOWL;
        } else if (name.contains("bucket") && item != net.minecraft.world.item.Items.BUCKET) {
            return net.minecraft.world.item.Items.BUCKET;
        }
        return null;
    }

    public static class CompostData {
        public final float probability;
        public final net.minecraft.world.item.Item remainder;
        public final String reason;

        public CompostData(float probability, net.minecraft.world.item.Item remainder, String reason) {
            this.probability = probability;
            this.remainder = remainder;
            this.reason = reason;
        }
    }

    public static net.minecraft.world.item.ItemStack getRecipeResult(
            net.minecraft.world.item.crafting.Recipe<?> recipe, net.minecraft.core.RegistryAccess registryAccess) {
        for (net.minecraft.world.item.crafting.display.RecipeDisplay display : recipe.display()) {
            if (display.result() instanceof net.minecraft.world.item.crafting.display.SlotDisplay.ItemStackSlotDisplay stackSlot) {
                return stackSlot.stack().create().getItem().getDefaultInstance();
            }
        }
        return net.minecraft.world.item.ItemStack.EMPTY;
    }

    public static CompostData getCompostResult(net.minecraft.world.level.ItemLike itemLike,
            net.minecraft.core.RegistryAccess registryAccess,
            net.minecraft.world.item.crafting.RecipeManager recipeManager) {
        return getCompostResult(new net.minecraft.world.item.ItemStack(itemLike));
    }

    public static CompostData getCompostResult(net.minecraft.world.item.ItemStack stack) {
        if (net.minecraft.world.level.block.ComposterBlock.COMPOSTABLES.containsKey(stack.getItem())) {
            float existingValue = net.minecraft.world.level.block.ComposterBlock.COMPOSTABLES.getFloat(stack.getItem());
            if (DEBUG) {
                debugLog("Composter mapped {} using existing vanilla/mod value: {}", stack.getHoverName().getString(),
                        existingValue);
            }
            return new CompostData(existingValue, null, "Existing Vanilla/Mod Value");
        }

        java.util.List<String> tags = crossVersionGetTags(stack);
        // Tag log removed from here, moving to rejection cases

        String itemName = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().toLowerCase();
        boolean isSeedLike = matchesAnyKeyword(itemName, tags, "seed", "seeds", "sapling", "saplings");
        if (isSeedLike) {
            if (DEBUG) {
                debugLog("Composter mapped {} (Sapling/Seed) to 0.3 chance", stack.getHoverName().getString());
            }
            return new CompostData(0.3f, null, "Sapling or Seed Tag Match");
        }

        // Search for a substring across all tags lazily
        boolean isCrop = isCrop(itemName, tags);
        if (isCrop) {
            if (DEBUG) {
                debugLog("Composter mapped {} (Crop) to 0.65 chance", stack.getHoverName().getString());
            }
            return new CompostData(0.65f, null, "Crop Keyword Match");
        }

        boolean isDrink = isDrink(itemName, tags);
        if (isDrink) {
            if (DEBUG) {
                debugLog("Composter mapped {} (Drink) to 0.2 chance", stack.getHoverName().getString());
            }
            return new CompostData(0.2f, null, "Drink Keyword Match");
        }

        boolean isMeat = isMeatLike(itemName, tags);

        if (isMeat) {
            if (DEBUG) {
                debugLog("Composter rejected item {} (Meat). Tags: {}", stack.getHoverName().getString(), tags);
            }
            return new CompostData(-1.0f, null, "Meat");
        }
        
        if (stack.getItem() instanceof net.minecraft.world.item.BlockItem blockItem) {
            net.minecraft.world.level.block.Block block = blockItem.getBlock();
            
            if (block instanceof net.minecraft.world.level.block.LeavesBlock || block instanceof net.minecraft.world.level.block.SaplingBlock || block instanceof net.minecraft.world.level.block.TallGrassBlock || block instanceof net.minecraft.world.level.block.SeagrassBlock) {
                if (DEBUG) {
                    debugLog("Composter mapped {} (Leaves/Sapling/Grass) to 0.3 chance", stack.getHoverName().getString());
                }
                return new CompostData(0.3f, null, "Leaves/Sapling/Grass Block Match");
            }
            if (block instanceof net.minecraft.world.level.block.FlowerBlock || block instanceof net.minecraft.world.level.block.TallFlowerBlock) {
                if (DEBUG) {
                    debugLog("Composter mapped {} (Flower) to 0.65 chance", stack.getHoverName().getString());
                }
                return new CompostData(0.65f, null, "Flower Block Match");
            }
            if (block instanceof net.minecraft.world.level.block.MushroomBlock || block instanceof net.minecraft.world.level.block.NetherFungusBlock) {
                if (DEBUG) {
                    debugLog("Composter mapped {} (Mushroom/Fungus) to 0.65 chance", stack.getHoverName().getString());
                }
                return new CompostData(0.65f, null, "Mushroom/Fungus Block Match");
            }
            if (block instanceof net.minecraft.world.level.block.VineBlock || block instanceof net.minecraft.world.level.block.LilyPadBlock || block instanceof net.minecraft.world.level.block.HangingRootsBlock || block instanceof net.minecraft.world.level.block.NetherRootsBlock) {
                if (DEBUG) {
                    debugLog("Composter mapped {} (Vine/Roots/Lily) to 0.5 chance", stack.getHoverName().getString());
                }
                return new CompostData(0.5f, null, "Vine/Roots/Lily Block Match");
            }

            if (block instanceof net.minecraft.world.level.block.BonemealableBlock) {
                boolean isFullBlock = block.defaultBlockState().isCollisionShapeFullBlock(
                        net.minecraft.world.level.EmptyBlockGetter.INSTANCE, net.minecraft.core.BlockPos.ZERO);
                if (!isFullBlock) {
                    if (DEBUG) {
                        debugLog("Composter mapped {} (Bonemealable Block) to 0.65 chance", stack.getHoverName().getString());
                    }
                    return new CompostData(0.65f, null, "Bonemealable Block Match");
                } else if (DEBUG) {
                    debugLog("Composter rejected {} (Bonemealable Block, but is a full block)", stack.getHoverName().getString());
                }
            }
        }

        net.minecraft.world.food.FoodProperties food = stack.get(net.minecraft.core.component.DataComponents.FOOD);

        if (food != null || tags.contains("c:foods")) {
            int nutrition = food != null ? food.nutrition() : 4; // Default to 4 (medium) if it only has the tag
            float probability;
            if (nutrition <= 2) {
                probability = 0.3f; // e.g., Melon Slice, Sweet Berries
            } else if (nutrition <= 4) {
                probability = 0.65f; // e.g., Apple, Carrot
            } else if (nutrition <= 6) {
                probability = 0.85f; // e.g., Baked Potato, Bread
            } else {
                probability = 1.0f; // High nutrition foods
            }
            if (DEBUG) {
                debugLog("Composter mapped {} with nutrition {} to {} chance", stack.getHoverName().getString(),
                        nutrition, probability);
            }
            return new CompostData(probability, null, "Food Item or c:foods Tag");
        }

        if (DEBUG) {
            debugLog("Composter rejected item {}. Tags: {}", stack.getHoverName().getString(), tags);
        }
        return new CompostData(-1.0f, null, "Not Compostable");
    }

    private static final java.util.Map<String, java.util.regex.Pattern> REGEX_PATTERNS = new java.util.concurrent.ConcurrentHashMap<>();

    private static boolean matchesAnyKeyword(String itemName, java.util.List<String> tags, String... keywords) {
        for (String keyword : keywords) {
            java.util.regex.Pattern pattern = REGEX_PATTERNS.computeIfAbsent(keyword,
                    k -> java.util.regex.Pattern
                            .compile("(?<![a-zA-Z])" + java.util.regex.Pattern.quote(k) + "(?![a-zA-Z])"));
            if (pattern.matcher(itemName).find()) {
                return true;
            }
            for (String tag : tags) {
                if (pattern.matcher(tag).find()) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isMeatLike(String itemName, java.util.List<String> tags) {
        java.util.List<String> filteredTags = tags.stream()
                .filter(tag -> !tag.endsWith("_food"))
                .toList();

        boolean isMeat = matchesAnyKeyword(itemName, filteredTags,
                "meat", "meats", "chicken", "chickens", "pork", "beef", "mutton",
                "flesh", "fish", "fishes", "salmon", "cod", "rabbit", "rabbits", "ham", "bacon", "burger");

        if (!isMeat) {
            String[] meatKeywords = { "meat", "chicken", "pork", "beef", "mutton", "flesh", "fish", "salmon", "rabbit",
                    "burger", "bacon" };
            for (String keyword : meatKeywords) {
                if (itemName.contains(keyword)) {
                    return true;
                }
            }
        }
        return isMeat;
    }

    private static boolean isCrop(String itemName, java.util.List<String> tags) {
        boolean isCrop = matchesAnyKeyword(itemName, tags, "crop", "crops");
        if (!isCrop) {
            String[] cropKeywords = { "crop" };
            for (String keyword : cropKeywords) {
                if (itemName.contains(keyword)) {
                    return true;
                }
            }
        }
        return isCrop;
    }

    private static boolean isDrink(String itemName, java.util.List<String> tags) {
        boolean isDrink = matchesAnyKeyword(itemName, tags, "drink", "drinks", "milk", "juice", "tea", "coffee");
        if (!isDrink) {
            String[] drinkKeywords = { "drink", "milk", "juice", "tea", "coffee" };
            for (String keyword : drinkKeywords) {
                if (itemName.contains(keyword)) {
                    return true;
                }
            }
        }
        return isDrink;
    }

    public static final java.util.Map<net.minecraft.world.item.Item, CompostData> COMPOSTER_CACHE = new java.util.concurrent.ConcurrentHashMap<>();

    public static void rebuildComposterMap() {
        debugLog("Rebuilding Vanilla Composter Map with Tag values...");
        boolean warnedUnbound = false;
        for (net.minecraft.world.item.Item item : BuiltInRegistries.ITEM) {
            net.minecraft.world.item.ItemStack stack;
            try {
                stack = new net.minecraft.world.item.ItemStack(item);
            } catch (NullPointerException e) {
                // Item components are not yet bound (e.g., called before data packs are fully
                // loaded, or a broken modded item). Skip this item.
                if (!warnedUnbound) {
                    Constants.LOG.warn(
                            "rebuildComposterMap: skipping {} and potentially others — components not bound yet",
                            BuiltInRegistries.ITEM.getKey(item));
                    warnedUnbound = true;
                }
                continue;
            }
            CompostData compostResult = getCompostResult(stack);
            float probability = compostResult.probability;

            if (probability > 0) {
                synchronized (net.minecraft.world.level.block.ComposterBlock.COMPOSTABLES) {
                    net.minecraft.world.level.block.ComposterBlock.COMPOSTABLES.put(item, probability);
                }

                net.minecraft.world.item.Item remainder = crossVersionGetRemainder(item);
                if (remainder == null) {
                    remainder = getHeuristicRemainder(item);
                }

                // ALSO populate the client-side cache so Mixin predictions work!
                if (!COMPOSTER_CACHE.containsKey(item)) {
                    COMPOSTER_CACHE.put(item, new CompostData(probability, remainder, compostResult.reason));
                }
            }
        }
    }

    public static void populateComposterCache(net.minecraft.world.item.crafting.RecipeManager recipeManager,
            net.minecraft.core.RegistryAccess registryAccess) {
        debugLog("Populating Eager Composter Cache...");

        // Ensure the vanilla map is fully rebuilt before we populate the cache!
        rebuildComposterMap();

        java.util.List<net.minecraft.world.level.ItemLike> itemsToRemove = new java.util.ArrayList<>();

        for (net.minecraft.world.level.ItemLike itemLike : net.minecraft.world.level.block.ComposterBlock.COMPOSTABLES
                .keySet()) {
            net.minecraft.world.item.Item item = itemLike.asItem();
            float probability = net.minecraft.world.level.block.ComposterBlock.COMPOSTABLES.getFloat(itemLike);
            String reason = "Existing Vanilla/Mod Value";
            if (COMPOSTER_CACHE.containsKey(item)) {
                reason = COMPOSTER_CACHE.get(item).reason;
            }
            net.minecraft.world.item.Item remainder = null;
            boolean craftedWithMeat = false;

            // 1. Check for standard crafting remainder
            remainder = crossVersionGetRemainder(item);

            if (remainder == null) {
                for (net.minecraft.world.item.crafting.RecipeHolder<?> recipeHolder : recipeManager.getRecipes()) {
                    net.minecraft.world.item.crafting.Recipe<?> recipe = recipeHolder.value();
                    net.minecraft.world.item.ItemStack result = getRecipeResult(recipe, registryAccess);

                    if (result != null && !result.isEmpty() && result.is(item)) {
                        for (net.minecraft.world.item.crafting.Ingredient ingredient : crossVersionGetIngredients(recipe)) {
                            if (remainder == null) {
                                if (ingredient.test(
                                        new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.BOWL))) {
                                    remainder = net.minecraft.world.item.Items.BOWL;
                                } else if (ingredient.test(new net.minecraft.world.item.ItemStack(
                                        net.minecraft.world.item.Items.GLASS_BOTTLE))) {
                                    remainder = net.minecraft.world.item.Items.GLASS_BOTTLE;
                                }
                            }

                            if (!craftedWithMeat) {
                                for (net.minecraft.core.Holder<net.minecraft.world.item.Item> ingHolder : ingredient.items().toList()) {
                                    net.minecraft.world.item.Item ingItem = ingHolder.value();
                                    net.minecraft.world.item.ItemStack ingredientStack = new net.minecraft.world.item.ItemStack(ingItem);
                                    if (ingredientStack.isEmpty())
                                        continue;
                                    String ingName = BuiltInRegistries.ITEM.getKey(ingItem).getPath()
                                            .toLowerCase();
                                    java.util.List<String> ingTags = crossVersionGetTags(ingredientStack);
                                    if (isMeatLike(ingName, ingTags)) {
                                        craftedWithMeat = true;
                                        break;
                                    }
                                }
                            }
                        }
                        if (remainder != null && craftedWithMeat)
                            break;
                    }
                }
            }

            if (craftedWithMeat) {
                reason = "Crafted with Meat";
                probability = -1.0f;
                itemsToRemove.add(itemLike);
            } else {
                // 3. Heuristic fallback for uncraftable items (like Ominous Bottle)
                if (remainder == null) {
                    remainder = getHeuristicRemainder(item);
                }
            }

            if (remainder != null) {
                debugLog("Final remainder for item {}: {}", item, remainder);
            }

            COMPOSTER_CACHE.put(item, new CompostData(probability, remainder, reason));
            synchronized (net.minecraft.world.level.block.ComposterBlock.COMPOSTABLES) {
                if (!net.minecraft.world.level.block.ComposterBlock.COMPOSTABLES.containsKey(item) && probability > 0) {
                    net.minecraft.world.level.block.ComposterBlock.COMPOSTABLES.put(item, probability);
                }
            }
        }

        for (net.minecraft.world.level.ItemLike itemToRemove : itemsToRemove) {
            net.minecraft.world.level.block.ComposterBlock.COMPOSTABLES.removeFloat(itemToRemove);
        }

        // --- Recipe Propagation Logic ---
        debugLog("Starting Recipe-Based Composter Propagation...");
        for (int iteration = 0; iteration < 5; iteration++) {
            boolean changed = false;

            for (net.minecraft.world.item.crafting.RecipeHolder<?> recipeHolder : recipeManager.getRecipes()) {
                net.minecraft.world.item.crafting.Recipe<?> recipe = recipeHolder.value();
                net.minecraft.world.item.ItemStack resultStack = getRecipeResult(recipe, registryAccess);

                if (resultStack == null || resultStack.isEmpty())
                    continue;
                net.minecraft.world.item.Item resultItem = resultStack.getItem();

                // If it's already compostable, we don't need to do anything
                if (net.minecraft.world.level.block.ComposterBlock.COMPOSTABLES.containsKey(resultItem)) {
                    continue;
                }

                boolean hasIngredients = false;
                boolean allIngredientsCompostable = true;
                float highestProbability = 0.0f;

                for (net.minecraft.world.item.crafting.Ingredient ingredient : crossVersionGetIngredients(recipe)) {
                    if (ingredient.isEmpty())
                        continue;
                    hasIngredients = true;

                    boolean ingredientIsCompostable = false;
                    float ingredientProb = 0.0f;

                    // If at least one valid item for this ingredient slot is compostable, we
                    // consider the slot satisfied.
                    for (net.minecraft.core.Holder<net.minecraft.world.item.Item> ingHolder : ingredient.items().toList()) {
                        net.minecraft.world.item.Item ingItem = ingHolder.value();
                        if (net.minecraft.world.level.block.ComposterBlock.COMPOSTABLES.containsKey(ingItem)) {
                            ingredientIsCompostable = true;
                            float prob = net.minecraft.world.level.block.ComposterBlock.COMPOSTABLES.getFloat(ingItem);
                            if (prob > ingredientProb) {
                                ingredientProb = prob;
                            }
                        }
                    }

                    if (!ingredientIsCompostable) {
                        allIngredientsCompostable = false;
                        break;
                    }

                    if (ingredientProb > highestProbability) {
                        highestProbability = ingredientProb;
                    }
                }

                if (hasIngredients && allIngredientsCompostable) {
                    synchronized (net.minecraft.world.level.block.ComposterBlock.COMPOSTABLES) {
                        net.minecraft.world.level.block.ComposterBlock.COMPOSTABLES.put(resultItem, highestProbability);
                    }
                    COMPOSTER_CACHE.put(resultItem,
                            new CompostData(highestProbability, null, "Derived from Compostable Recipe"));
                    debugLog("Propagated compostability to {} with chance {} (Iteration {})",
                            BuiltInRegistries.ITEM.getKey(resultItem), highestProbability, iteration + 1);
                    changed = true;
                }
            }

            if (!changed) {
                break;
            }
        }

        Constants.LOG.info("Successfully populated Composter Cache with {} items!", COMPOSTER_CACHE.size());
    }
}
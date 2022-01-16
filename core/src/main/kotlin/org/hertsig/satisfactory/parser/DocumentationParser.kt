package org.hertsig.satisfactory.parser

import com.google.gson.GsonBuilder
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import org.hertsig.satisfactory.data.*
import java.io.Reader
import java.time.Duration
import java.time.temporal.ChronoUnit

object DocumentationParser {
    private val gson = GsonBuilder().create()
    private val ingredientRegex = Regex("ItemClass=BlueprintGeneratedClass'\"([^\"]+)\"',Amount=(\\d+)")

    fun parse(reader: Reader): Pair<Collection<Item>, List<Recipe>> {
        val rawIitems = mutableListOf<RawItem>()
        val rawRecipes = mutableListOf<RawRecipe>()
        gson.newJsonReader(reader).use {
            it.readList {
                lateinit var name: String
                it.readObject { key -> when (key) {
                    "NativeClass" -> name = it.nextString()
                    "Classes" -> when (name) {
                        "Class'/Script/FactoryGame.FGItemDescAmmoTypeColorCartridge'",
                        "Class'/Script/FactoryGame.FGItemDescAmmoTypeProjectile'",
                        "Class'/Script/FactoryGame.FGItemDescAmmoTypeInstantHit'",
                        "Class'/Script/FactoryGame.FGItemDescriptorNuclearFuel'",
                        "Class'/Script/FactoryGame.FGItemDescriptorBiomass'",
                        "Class'/Script/FactoryGame.FGConsumableDescriptor'",
                        "Class'/Script/FactoryGame.FGEquipmentDescriptor'",
                        "Class'/Script/FactoryGame.FGResourceDescriptor'",
                        "Class'/Script/FactoryGame.FGItemDescriptor'" -> rawIitems += parseItems(it)
                        "Class'/Script/FactoryGame.FGRecipe'" -> rawRecipes += parseRecipes(it)
                        else -> it.skipValue()
                    }
                    else -> throw IllegalArgumentException("Invalid key $key")
                } }
            }
            require(it.peek() == JsonToken.END_DOCUMENT)
        }

        val items = rawIitems.associate { it.className!! to parseItem(it) }
        val recipes = rawRecipes.mapNotNull { parseRecipe(it, items) }
        return Pair(items.values, recipes)
    }

    private fun parseRecipe(it: RawRecipe, items: Map<String, Item>): Recipe? {
        val building = parseBuilding(it.producedIn!!) ?: return null
        return Recipe(
            it.displayName!!.removePrefix("Alternate: "),
            building,
            parseRecipeData(it.ingredients!!, items),
            parseRecipeData(it.product!!, items),
            it.displayName!!.startsWith("Alternate:"),
            Duration.of(it.duration!!, ChronoUnit.SECONDS),
        )
    }

    private fun parseRecipeData(ingredients: String, items: Map<String, Item>): List<RecipeData> {
        return ingredientRegex.findAll(ingredients)
            .map { RecipeData(it.groups[2]!!.value.toInt(), items.getValue(it.groups[1]!!.value.substringAfterLast('.'))) }
            .toList()
    }

    private fun parseBuilding(producedIn: String) = producedIn.removeSurrounding("(", ")")
        .split(",")
        .firstNotNullOfOrNull { when (it) {
            "/Game/FactoryGame/Buildable/Factory/ConstructorMk1/Build_ConstructorMk1.Build_ConstructorMk1_C" -> Building.CONSTRUCTOR
            "/Game/FactoryGame/Buildable/Factory/AssemblerMk1/Build_AssemblerMk1.Build_AssemblerMk1_C" -> Building.ASSEMBLER
            "/Game/FactoryGame/Buildable/Factory/ManufacturerMk1/Build_ManufacturerMk1.Build_ManufacturerMk1_C" -> Building.MANUFACTURER
            "/Game/FactoryGame/Buildable/Factory/SmelterMk1/Build_SmelterMk1.Build_SmelterMk1_C" -> Building.SMELTER
            "/Game/FactoryGame/Buildable/Factory/FoundryMk1/Build_FoundryMk1.Build_FoundryMk1_C" -> Building.FOUNDRY
            "/Game/FactoryGame/Buildable/Factory/Packager/Build_Packager.Build_Packager_C" -> Building.PACKAGER
            "/Game/FactoryGame/Buildable/Factory/OilRefinery/Build_OilRefinery.Build_OilRefinery_C" -> Building.REFINERY
            "/Game/FactoryGame/Buildable/Factory/Blender/Build_Blender.Build_Blender_C" -> Building.BLENDER
            "/Game/FactoryGame/Buildable/Factory/HadronCollider/Build_HadronCollider.Build_HadronCollider_C" -> Building.PARTICLE_ACCELERATOR
            "",
            "/Script/FactoryGame.FGBuildGun",
            "/Script/FactoryGame.FGBuildableAutomatedWorkBench",
            "/Game/FactoryGame/Equipment/BuildGun/BP_BuildGun.BP_BuildGun_C",
            "/Game/FactoryGame/Buildable/-Shared/WorkBench/BP_WorkshopComponent.BP_WorkshopComponent_C",
            "/Game/FactoryGame/Buildable/-Shared/WorkBench/BP_WorkBenchComponent.BP_WorkBenchComponent_C",
            "/Game/FactoryGame/Buildable/Factory/Converter/Build_Converter.Build_Converter_C",
            -> null
            else -> throw IllegalArgumentException("Unrecognized building $producedIn")
        } }

    private fun parseItem(it: RawItem) = Item(
        it.displayName!!,
        it.description.orEmpty(),
        parseStackSize(it.stackSize),
        it.resourceSinkPoints ?: 0)

    private fun parseStackSize(stackSize: String?): StackSize? = when (stackSize) {
        "SS_HUGE" -> StackSize.HUGE
        "SS_BIG" -> StackSize.BIG
        "SS_MEDIUM" -> StackSize.MEDIUM
        "SS_SMALL" -> StackSize.SMALL
        "SS_ONE" -> StackSize.ONE
        "SS_FLUID" -> null
        else -> throw IllegalArgumentException("Unrecognized stack size $stackSize")
    }

    private fun parseItems(it: JsonReader): List<RawItem> {
        val items = mutableListOf<RawItem>()
        it.readList {
            val item = RawItem().also { items.add(it) }
            it.readObject { key ->
                when (key) {
                    "ClassName" -> item.className = it.nextString()
                    "mDisplayName" -> item.displayName = it.nextString()
                    "mDescription" -> item.description = it.nextString()
                    "mAbbreviatedDisplayName" -> item.abbreviatedDisplayName = it.nextString()
                    "mStackSize" -> item.stackSize = it.nextString()
                    "mCanBeDiscarded" -> item.canBeDiscarded = it.nextString().lowercase().toBooleanStrict()
                    "mForm" -> item.form = it.nextString()
                    "mSmallIcon" -> item.smallIcon = it.nextString()
                    "mPersistentBigIcon" -> item.bigIcon = it.nextString()
                    "mSubCategories" -> item.subcategories = it.nextString()
                    "mResourceSinkPoints" -> item.resourceSinkPoints = it.nextInt()
                    else -> it.skipValue()
                }
            }
        }
        return items
    }

    private fun parseRecipes(it: JsonReader): List<RawRecipe> {
        val recipes = mutableListOf<RawRecipe>()
        it.readList {
            val recipe = RawRecipe().also { recipes.add(it) }
            it.readObject { key ->
                when (key) {
                    "ClassName" -> recipe.className = it.nextString()
                    "FullName" -> recipe.fullName = it.nextString()
                    "mDisplayName" -> recipe.displayName = it.nextString()
                    "mIngredients" -> recipe.ingredients = it.nextString()
                    "mProduct" -> recipe.product = it.nextString()
                    "mManufactoringDuration" -> recipe.duration = it.nextLong()
                    "mProducedIn" -> recipe.producedIn = it.nextString()
                    "mVariablePowerConsumptionConstant" -> recipe.variablePowerConsumptionConstant = it.nextDouble()
                    "mVariablePowerConsumptionFactor" -> recipe.variablePowerConsumptionFactor = it.nextDouble()
                    else -> it.skipValue()
                }
            }
        }
        return recipes
    }

    private fun JsonReader.readList(handle: () -> Unit) {
        beginArray()
        while (peek() != JsonToken.END_ARRAY) {
            handle()
        }
        endArray()
    }

    private fun JsonReader.readObject(handle: (String) -> Unit) {
        beginObject()
        while (peek() != JsonToken.END_OBJECT) {
            require(peek() == JsonToken.NAME) { "Must consume value in handle lambda; left a ${peek()}" }
            handle(nextName())
        }
        endObject()
    }
}

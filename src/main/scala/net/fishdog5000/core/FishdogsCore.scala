/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 FishDog5000
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.fishdog5000.core

import java.util

import net.fishdog5000.core.basestuff.{IBaseBlock, IBaseItem}
import net.fishdog5000.core.handler._
import net.minecraft.block.material.Material
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.model.{ModelBakery, ModelResourceLocation}
import net.minecraft.entity.Entity
import net.minecraft.entity.effect.EntityLightningBolt
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.{Blocks, Items}
import net.minecraft.item.crafting.{CraftingManager, IRecipe}
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.server.MinecraftServer
import net.minecraft.util.{ChatComponentTranslation, IChatComponent, StatCollector}
import net.minecraft.world.World
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod.EventHandler
import net.minecraftforge.fml.common.event.{FMLInitializationEvent, FMLInterModComms, FMLPostInitializationEvent, FMLPreInitializationEvent, FMLServerStartingEvent}
import net.minecraftforge.fml.common.registry.GameRegistry
import net.minecraftforge.fml.common.{Mod, SidedProxy}
import net.minecraftforge.fml.relauncher.{Side, SideOnly}
import org.apache.logging.log4j.Level

//@formatter:off
@Mod(modid = FishdogsCore.MODID, name = "Fishdog5000's Core", version = FishdogsCore.FULL_VERSION, modLanguage = "scala")//@DEPEND@//(dependencies="required-after:Forge@[@FORGEVERSION@,)")
//@formatter:on
object FishdogsCore {
    final val BASE_VERSION = "@COREVERSION@"
    final val FULL_VERSION = "@FULLVERSION@"
    final val MODID = "fishdog5000score"
    final val MCVERSION = "@MCVERSION@"
    //var item: BaseItem = _
    //var item2: BaseItem = _
    var herobrineblock: BlockPowerBlock = _
    var registeredpower: Boolean = false

    @SidedProxy(clientSide = "net.fishdog5000.core.handler.ClientProxy", serverSide = "net.fishdog5000.core.handler.ServerProxy", modId = MODID)
    var proxy: CommonProxy = _

    @EventHandler
    def preInit(event: FMLPreInitializationEvent) = {
        Log.logger = event.getModLog
        Log.info("###################### FISHDOG5000'S CORE STARTING ######################")

        Log.info("attempting to add version checker support...")
        FMLInterModComms.sendRuntimeMessage(MODID, "VersionChecker", "addVersionCheck", CoreConstants.VERSIONS_URL)
        MinecraftForge.EVENT_BUS.register(FishdogsCoreEventHandler)

        //logger.info("preparing configuration file...")
        Log.info("registering a few basic things in the ore dictionary...")
        OreDictionaryHandler.registerItemOres()

    }

    @EventHandler
    def load(event: FMLInitializationEvent) = {
        Log.info("Initializing stuff...")
        proxy.init()
        proxy.registerRenderers()


        if (registeredpower) {
            herobrineblock = new BlockPowerBlock(Material.iron)

            registerBlock(herobrineblock, "tile.BlockPowerBlock", MODID)

            GameRegistry.addRecipe(new ItemStack(herobrineblock), "XXX", "***", "XXX",
                new Character('X'), Blocks.redstone_block, new Character('*'), Items.coal)
            GameRegistry.addRecipe(new ItemStack(herobrineblock), "XXX", "***", "XXX",
                new Character('X'), Blocks.redstone_block, new Character('*'), new ItemStack(Items.coal, 1, 1))
        }

        //item = new BaseItem(MODID + ":default", CreativeTabs.tabTransport, MODID)
        //GameRegistry.registerItem(item, MODID + ":default")
        //Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, 0, new ModelResourceLocation(MODID + ":default", "inventory"))

        //item2 = new BaseItem("item2", CreativeTabs.tabTransport, MODID).setUsesHDTextures()
        //GameRegistry.registerItem(item2, "item2")
        //Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item2, 0, new ModelResourceLocation(MODID + ":item2", "inventory"))

        //item2.setHDString(MODID + ":test1")
    }

    def registerBlock(block: IBaseBlock, name: String, MODID: String): Unit =
        registerBlock(block, name, MODID, 0)

    @EventHandler
    def postInit(event: FMLPostInitializationEvent) = {
        Log.info("###################### FISHDOG5000'S CORE READY ######################")
    }

    def flingEntity(xmotion: Double, ymotion: Double, zmotion: Double, entity: Entity, world: World): Unit =
        flingEntity(xmotion, ymotion, zmotion, entity, world, vset = true)

    def flingEntity(xmotion: Double, ymotion: Double, zmotion: Double, entity: Entity, world: World, vset: Boolean) = {
        if (world.isRemote && vset)
            entity.setVelocity(xmotion, ymotion, zmotion)

        entity.motionX = xmotion
        entity.motionY = ymotion
        entity.motionZ = zmotion
    }

    def registerPowerBlock() =
        registeredpower = true

    def registerBlock(block: IBaseBlock, MODID: String, meta: Int): Unit =
        registerBlock(block, block.getName, MODID, meta)

    def registerBlock(block: IBaseBlock, name: String, MODID: String, metadata: Int): Unit = {
        GameRegistry.registerBlock(block, name)
        proxy.registerBlockRenderer(block, metadata, name, MODID)
    }

    def registerBlock(block: IBaseBlock, MODID: String): Unit =
        registerBlock(block, block.getName, MODID, 0)

    def removeRecipe(result: ItemStack) = {
        val recipes: java.util.List[IRecipe] = CraftingManager.getInstance.getRecipeList
        var i = 0

        while (i < recipes.size) {
            val to_remove = recipes.get(i)
            val result_recipe = to_remove.getRecipeOutput
            if (ItemStack.areItemStacksEqual(result, result_recipe)) {
                recipes.remove(i)
                i -= 0
            }
            i += 1
        }
    }

    def removeRecipe(result: Item) = {
        val recipes: java.util.List[IRecipe] = CraftingManager.getInstance.getRecipeList
        var i = 0

        while (i < recipes.size) {
            val to_remove = recipes.get(i)
            val result_recipe = to_remove.getRecipeOutput
            if (result_recipe != null && result == result_recipe.getItem) {
                recipes.remove(i)
                i -= 0
            }
            i += 1
        }
    }

    def addInformation(name: String, player: EntityPlayer, list: java.util.List[String], adv: Boolean, precache: String, postcache: java.util.List[String]): java.util.List[String] = {
        val lore = StatCollector.translateToLocal(name + ".lore")
        if (precache.equals(lore)) {
            if (postcache != null)
                list.addAll(postcache)
            null
        }
        else {
            var i = 0
            var str = ""
            val listy: java.util.List[String] = new util.ArrayList[String]()
            while (i < lore.length) {
                if (lore(i) == '\\') {
                    i += 1
                    listy.add(str)
                    str = ""
                }
                else {
                    str += lore(i)
                }
                i += 1
            }
            listy.add(str)
            list.addAll(listy)
            listy
        }
    }

    @SideOnly(Side.SERVER)
    @EventHandler
    def serverLoad(event: FMLServerStartingEvent) =
        event.registerServerCommand(OreDictionaryLister)

    def lightning(chatmessage: String, x: Double, y: Double, z: Double, world: World, times: Int) {
        for (i <- 0 until times)
            world.addWeatherEffect(new EntityLightningBolt(world, x, y, z))
        if (chatmessage != null)
            chat(chatmessage)
    }

    def chat(msg: String): Unit =
        chat(new ChatComponentTranslation(msg))

    def chat(msg: IChatComponent) =
        MinecraftServer.getServer.getConfigurationManager.sendChatMsg(msg)

    def registerItem(item: IBaseItem, MODID: String): Unit = registerItem(item, item.getName, MODID, 0)

    def registerItem(item: IBaseItem, name: String, MODID: String): Unit = registerItem(item, name, MODID, 0)

    def registerItem(item: IBaseItem, name: String, MODID: String, metadata: Int): Unit = {
        GameRegistry.registerItem(item, name)
        proxy.registerItemRenderer(item, metadata, name, MODID)
    }

    def registerItem(item: IBaseItem, MODID: String, metadata: Int): Unit = registerItem(item, item.getName, MODID, metadata)

    @SideOnly(Side.CLIENT)
    def setItemMultitexture(item: IBaseItem, name: String, MODID: String, textures: Array[String], defaultTexture: String): MultiTextureModel =
        setItemMultitexture(item, name, MODID, 0, textures, defaultTexture)

    @SideOnly(Side.CLIENT)
    def setItemMultitexture(item: IBaseItem, name: String, MODID: String, meta: Int, textures: Array[String], defaultTexture: String): MultiTextureModel = {
        val loc = new ModelResourceLocation(MODID + ":" + name, "inventory")
        for (model <- textures)
            ModelBakery.registerItemVariants(item, new ModelResourceLocation(model, "inventory"))
        MultiTextureModels.create(loc, textures, defaultTexture)
    }

    @SideOnly(Side.CLIENT)
    def refreshResources() =
        Minecraft.getMinecraft.refreshResources()

}

object Log {
    private[core] var logger: org.apache.logging.log4j.Logger = _

    def info(msg: String) = logger.log(Level.INFO, msg)

    //def debug(msg: String) = logger.log(Level.DEBUG, msg)

    //def warn(msg: String) = logger.log(Level.WARN, msg)

    //def err(msg: String) = logger.log(Level.ERROR, msg)

}
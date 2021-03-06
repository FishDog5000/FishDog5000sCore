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
package net.fishdog5000.core.thirdparty.JEI

import javax.annotation.Nonnull

import mezz.jei.api._
import net.fishdog5000.core.Log
import net.fishdog5000.core.basestuff.DurabilityItemCrafting

@JEIPlugin
class CoreJEIPlugin extends IModPlugin {
    override def register(@Nonnull registry: IModRegistry) {
        Log.info("Initializing FishDog5000's Core JEI plugin...")
    }

    override def onRuntimeAvailable(@Nonnull jeiRuntime: IJeiRuntime) = {
        import scala.collection.JavaConversions._
        val recipeRegistry = jeiRuntime.getRecipeRegistry
        for (recipe <- DurabilityItemCrafting.recipes)
            recipeRegistry.addRecipe(recipe.parent)
        //DurabilityItemCrafting.recipes = null
    }
}

/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.ui.client.clickgui.newVer.element

import net.minecraft.client.renderer.GlStateManager
import net.minusmc.minusbounce.ui.client.clickgui.newVer.ColorManager
import net.minusmc.minusbounce.ui.client.clickgui.newVer.IconManager
import net.minusmc.minusbounce.ui.client.clickgui.newVer.extensions.animSmooth
import net.minusmc.minusbounce.ui.font.Fonts
import net.minusmc.minusbounce.utils.MouseUtils
import net.minusmc.minusbounce.utils.render.RenderUtils
import net.minusmc.minusbounce.utils.render.Stencil
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.abs

class SearchElement(private val xPos: Float, private val yPos: Float, val width: Float, val height: Float) {

    private var scrollHeight = 0F
    private var animScrollHeight = 0F
    private var lastHeight = 0F

    private val searchBox = SearchBox(0, xPos.toInt() + 2, yPos.toInt() + 2, width.toInt() - 4, height.toInt() - 2)

    fun drawBox(mouseX: Int, mouseY: Int, accentColor: Color): Boolean {
        RenderUtils.originalRoundedRect(xPos - 0.5F, yPos - 0.5F, xPos + width + 0.5F, yPos + height + 0.5F, 4F, ColorManager.buttonOutline.rgb)
        Stencil.write(true)
        RenderUtils.originalRoundedRect(xPos, yPos, xPos + width, yPos + height, 4F, ColorManager.textBox.rgb)
        Stencil.erase(true)
        if (searchBox.isFocused) {
            RenderUtils.newDrawRect(xPos, yPos + height - 1F, xPos + width, yPos + height, accentColor.rgb)
            searchBox.drawTextBox()
        } else if (searchBox.text.isEmpty()) {
            searchBox.text = "Search"
            searchBox.drawTextBox()
            searchBox.text = ""
        } else
            searchBox.drawTextBox()

        Stencil.dispose()
        GlStateManager.disableAlpha()
        RenderUtils.drawImage2(IconManager.search, xPos + width - 15F, yPos + 5F, 10, 10)
        GlStateManager.enableAlpha()
        return searchBox.text.isNotEmpty()
    }

    fun drawPanel(mX: Int, mY: Int, x: Float, y: Float, w: Float, h: Float, wheel: Int, ces: MutableList<CategoryElement>, accentColor: Color) {
        var mouseY = mY
        lastHeight = 0F
        for (ce in ces) {
            for (me in ce.moduleElements) {
                if (me.module.name.startsWith(searchBox.text, true))
                    lastHeight += me.animHeight + 40F
            }
        }
        if (lastHeight >= 10F) lastHeight -= 10F
        handleScrolling(wheel, h)
        drawScroll(x, y + 50F, w, h)
        Fonts.fontLarge.drawString("Search", x + 10F, y + 10F, -1)
        Fonts.fontSmall.drawString("Search", x - 170F, y - 12F, -1)
        RenderUtils.drawImage2(IconManager.back, x - 190F, y - 15F, 10, 10)
        var startY = y + 50F
        if (mouseY < y + 50F || mouseY >= y + h)
            mouseY = -1
        RenderUtils.makeScissorBox(x, y + 50F, x + w, y + h)
        GL11.glEnable(3089)
        for (ce in ces) {
            for (me in ce.moduleElements) {
                if (me.module.name.startsWith(searchBox.text, true)) {
                    startY += if (startY + animScrollHeight > y + h || startY + animScrollHeight + 40F + me.animHeight < y + 50F)
                        40F + me.animHeight
                    else
                        me.drawElement(mX, mouseY, x, startY + animScrollHeight, w, 40F, accentColor)
                }
            }
        }
        GL11.glDisable(3089)
    }

    private fun handleScrolling(wheel: Int, height: Float) {
        if (wheel != 0) {
            if (wheel > 0)
                scrollHeight += 50F
            else
                scrollHeight -= 50F
        }
        scrollHeight = if (lastHeight > height - 60F)
            scrollHeight.coerceIn(-lastHeight + height - 60, 0F)
        else
            0F
        animScrollHeight = animScrollHeight.animSmooth(scrollHeight, 0.5F)
    }

    private fun drawScroll(x: Float, y: Float, width: Float, height: Float) {
        if (lastHeight > height - 60F) {
            val last = (height - 60F) - (height - 60F) * ((height - 60F) / lastHeight)
            val multiply = last * abs(animScrollHeight / (-lastHeight + height - 60)).coerceIn(0F, 1F)
            RenderUtils.originalRoundedRect(x + width - 6F, y + 5F + multiply, x + width - 4F, y + 5F + (height - 60F) * ((height - 60F) / lastHeight) + multiply, 1F, 1358954495L.toInt())
        }
    }

    fun handleMouseClick(mX: Int, mY: Int, mouseButton: Int, x: Float, y: Float, w: Float, h: Float, ces: MutableList<CategoryElement>) {
        if (MouseUtils.mouseWithinBounds(mX, mY, x - 200F, y - 20F, x - 170F, y)) {
            searchBox.text = ""
            return
        }
        var mouseY = mY
        searchBox.mouseClicked(mX, mouseY, mouseButton)
        if (searchBox.text.isEmpty()) return
        if (mouseY < y + 50F || mouseY >= y + h)
            mouseY = -1
        var startY = y + 50F
        for (ce in ces)
            for (me in ce.moduleElements)
                if (me.module.name.startsWith(searchBox.text, true)) {
                    me.handleClick(mX, mouseY, x, startY + animScrollHeight, w, 40F)
                    startY += 40F + me.animHeight
                }
    }

    fun handleMouseRelease(mX: Int, mY: Int, mouseButton: Int, x: Float, y: Float, w: Float, h: Float, ces: MutableList<CategoryElement>) {
        var mouseY = mY
        if (searchBox.text.isEmpty()) return
        if (mouseY < y + 50F || mouseY >= y + h)
            mouseY = -1
        var startY = y + 50F
        for (ce in ces)
            for (me in ce.moduleElements)
                if (me.module.name.startsWith(searchBox.text, true)) {
                    me.handleRelease(mX, mouseY, x, startY + animScrollHeight, w, 40F)
                    startY += 40F + me.animHeight
                }
    }

    fun handleTyping(typedChar: Char, keyCode: Int, x: Float, y: Float, w: Float, h: Float, ces: MutableList<CategoryElement>): Boolean {
        searchBox.textboxKeyTyped(typedChar, keyCode)
        if (searchBox.text.isEmpty()) return false
        for (ce in ces)
            for (me in ce.moduleElements)
                if (me.module.name.startsWith(searchBox.text, true))
                    if (me.handleKeyTyped(typedChar, keyCode))
                        return true
        return false
    }

    fun isTyping(): Boolean = (searchBox.text.isNotEmpty())

}

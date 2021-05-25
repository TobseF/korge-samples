import com.soywiz.klock.milliseconds
import com.soywiz.kmem.*
import com.soywiz.korge.Korge
import com.soywiz.korge.input.mouse
import com.soywiz.korge.render.BatchBuilder2D
import com.soywiz.korge.resources.resourceBitmap
import com.soywiz.korge.view.addUpdater
import com.soywiz.korge.view.fast.FSprite
import com.soywiz.korge.view.fast.FSprites
import com.soywiz.korge.view.fast.fastForEach
import com.soywiz.korge.view.position
import com.soywiz.korge.view.text
import com.soywiz.korim.bitmap.effect.BitmapEffect
import com.soywiz.korim.bitmap.sliceWithSize
import com.soywiz.korim.font.DefaultTtfFont
import com.soywiz.korim.font.toBitmapFont
import com.soywiz.korim.format.readBitmap
import com.soywiz.korio.async.delay
import com.soywiz.korio.file.std.resourcesVfs
import com.soywiz.korio.lang.currentThreadId
import com.soywiz.korio.resources.ResourcesContainer
import kotlin.random.Random

// @TODO: We could autogenerate this via gradle
val ResourcesContainer.korge_png by resourceBitmap("korge.png")

class BunnyContainer(maxSize: Int) : FSprites(maxSize) {
    val speeds = FBuffer(maxSize * Float.SIZE_BYTES * 2).f32
    var FSprite.speedXf: Float get() = speeds[index * 2 + 0] ; set(value) { speeds[index * 2 + 0] = value }
    var FSprite.speedYf: Float get() = speeds[index * 2 + 1] ; set(value) { speeds[index * 2 + 1] = value }
    //var FSprite.tex: BmpSlice
}

/*
class Bunny(tex: BmpSlice) : FastSprite(tex) {
    var speedXf: Float = 0f
    var speedYf: Float = 0f
}
*/

// bunnymark ported from PIXI.js
// https://www.goodboydigital.com/pixijs/bunnymark/
// https://www.goodboydigital.com/pixijs/bunnymark/js/bunnyBenchMark.js
suspend fun main() = Korge(width = 800, height = 800, batchMaxQuads = BatchBuilder2D.MAX_BATCH_QUADS) {
	println("currentThreadId=$currentThreadId")
	delay(1.milliseconds)
	println("currentThreadId=$currentThreadId")
	println("ag.graphicExtensions=${ag.graphicExtensions}")
	println("ag.isFloatTextureSupported=${ag.isFloatTextureSupported}")
	println("ag.isInstancedSupported=${ag.isInstancedSupported}")
//suspend fun main() = Korge(width = 800, height = 600, bgcolor = Colors["#2b2b9b"]) {
	val wabbitTexture = resourcesVfs["bunnys.png"].readBitmap()

	val bunny1 = wabbitTexture.sliceWithSize(2, 47, 26, 37)
    val bunny2 = wabbitTexture.sliceWithSize(2, 86, 26, 37)
    val bunny3 = wabbitTexture.sliceWithSize(2, 125, 26, 37)
    val bunny4 = wabbitTexture.sliceWithSize(2, 164, 26, 37)
    val bunny5 = wabbitTexture.sliceWithSize(2, 2, 26, 37)

    val startBunnyCount = 2
    //val startBunnyCount = 1_000_000
    // val startBunnyCount = 200_000
    val bunnyTextures = listOf(bunny1, bunny2, bunny3, bunny4, bunny5)
    var currentTexture = bunny1

    val bunnys = BunnyContainer(800_000)
    addChild(bunnys.createView(wabbitTexture))

    val font = DefaultTtfFont.toBitmapFont(fontSize = 16.0, effect = BitmapEffect(dropShadowX = 1, dropShadowY = 1, dropShadowRadius = 1))
    val bunnyCountText = text("", font = font, textSize = 16.0, alignment = com.soywiz.korim.text.TextAlignment.TOP_LEFT).position(16.0, 16.0)


    val random = Random(0)

    fun addBunny(count: Int = 1) {
        for (n in 0 until kotlin.math.min(count, bunnys.available)) {
            bunnys.apply {
                val bunny = alloc()
                bunny.speedXf = random.nextFloat() * 1
                bunny.speedYf = (random.nextFloat() * 1) - 5
                bunny.setAnchor(.5f, 1f)
                //bunny.width = 10f
                //bunny.height = 20f
                //bunny.alpha = 0.3f + random.nextFloat() * 0.7f
                bunny.setTex(currentTexture)
                bunny.scale(0.5f + random.nextFloat() * 0.5f)
                bunny.radiansf = (random.nextFloat() - 0.5f)
            }
        }
        bunnyCountText.text = "(WIP) KorGE Bunnymark. Bunnies: ${bunnys.size}"
    }

    addBunny(startBunnyCount)

    val maxX = width.toFloat()
    val minX = 0f
    val maxY = height.toFloat()
    val minY = 0f
    val gravity = 0.5f // 1.5f

    mouse {
        up {
            currentTexture = bunnyTextures.random(random)
        }
    }

    addUpdater {
        if (views.input.mouseButtons != 0) {
            if (bunnys.size < 200_000) {
                addBunny(2_000)
            } else if (bunnys.size < bunnys.maxSize - 1000) {
                addBunny(4_000)
            }
        }
        bunnys.fastForEach { bunny ->
            bunny.x += bunny.speedXf
            bunny.y += bunny.speedYf
            bunny.speedYf += gravity

            if (bunny.x > maxX) {
                bunny.speedXf *= -1
                bunny.x = maxX
            } else if (bunny.x < minX) {
                bunny.speedXf *= -1
                bunny.x = minX
            }

            if (bunny.y > maxY) {
                bunny.speedYf *= -0.85f
                bunny.y = maxY
                bunny.radiansf = (random.nextFloat() - 0.5f) * 0.2f
                if (random.nextFloat() > 0.5) {
                    bunny.speedYf -= random.nextFloat() * 6
                }
            } else if (bunny.y < minY) {
                bunny.speedYf = 0f
                bunny.y = minY
            }
        }
    }
}


/*
suspend fun main() {
    //GLOBAL_CHECK_GL = true
    Korge(width = 512, height = 512, bgcolor = Colors["#2b2b2b"], clipBorders = false) {
        gameWindow.icon = korge_png.get().bmp.toBMP32().scaled(32, 32)
        val minDegrees = (-16).degrees
        val maxDegrees = (+16).degrees
        val image = image(korge_png) {
            //val image = image(resourcesVfs["korge.png"].readbitmapslice) {
            rotation = maxDegrees
            anchor(.5, .5)
            scale(.8)
            position(256, 256)
        }
        addChild(MyView())
        //bindLength(image::scaledWidth) { 100.vw }
        //bindLength(image::scaledHeight) { 100.vh }
        while (true) {
            image.tween(image::rotation[minDegrees], time = 1.seconds, easing = Easing.EASE_IN_OUT)
            image.tween(image::rotation[maxDegrees], time = 1.seconds, easing = Easing.EASE_IN_OUT)
        }
    }
}
*/

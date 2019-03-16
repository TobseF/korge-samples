package com.soywiz.korge.experimental.s3d

import com.soywiz.kds.*
import com.soywiz.kds.iterators.*
import com.soywiz.klock.*
import com.soywiz.korge.experimental.s3d.model.*
import com.soywiz.korma.geom.*
import com.soywiz.korma.interpolation.*

class Animator3D(val animations: List<Animation3D>, val rootView: View3D) {
	var currentTime = 0.milliseconds
	fun update(ms: Int) {
		//currentTime += ms.milliseconds * 0.1
		currentTime += ms.milliseconds
		animations.fastForEach { animation ->
			val keyFrames = animation.keyFrames
			val fseconds = keyFrames.seconds
			val ftransforms = keyFrames.transforms
			val ffloats = keyFrames.floats
			val aproperty = animation.property
			val elapsedTimeInAnimation = (currentTime % animation.totalTime)
			//genericBinarySearch(0, animation.keys.size) { animation.keys[it] }

			val n = keyFrames.findIndex(elapsedTimeInAnimation)
			if (n < 0) return@fastForEach

			val startTime = fseconds[n].seconds
			val endTime = fseconds.getOrNull(n + 1)?.seconds ?: startTime
			val ratio = (elapsedTimeInAnimation - startTime) / (endTime - startTime)
			val aview = rootView[animation.target]
			if (aview != null) {
				when (aproperty) {
					"transform" -> {
						if (ftransforms != null) {
							aview.transform.setToInterpolated(ftransforms[n], ftransforms.getCyclic(n + 1), ratio.toDouble())
						}
					}
					"location.X", "location.Y", "location.Z", "scale.X", "scale.Y", "scale.Z", "rotationX.ANGLE", "rotationY.ANGLE", "rotationZ.ANGLE" -> {
						if (ffloats != null) {
							val value = ratio.interpolate(ffloats[n], ffloats[n % ffloats.size]).toDouble()
							when (aproperty) {
								"location.X" -> aview.x = value
								"location.Y" -> aview.y = value
								"location.Z" -> aview.z = value
								"scale.X" -> aview.scaleX = value
								"scale.Y" -> aview.scaleY = value
								"scale.Z" -> aview.scaleZ = value
								"rotationX.ANGLE" -> aview.rotationX  = value.degrees
								"rotationY.ANGLE" -> aview.rotationY = value.degrees
								"rotationZ.ANGLE" -> aview.rotationZ = value.degrees
							}
						}
					}
					else -> {
						println("WARNING: animation.property=${animation.property} not implemented")
					}
				}
			}

			//animation.keyFrames.binarySearch { it.time.millisecondsInt }
			//println(animation)
		}
	}
}

data class Animation3D(val id: String, val target: String, val property: String, val keyFrames: Frames) : Library3D.Def() {
	val totalTime = keyFrames.totalTime

	class Frames(
		var seconds: FloatArray = floatArrayOf(),
		var interpolations: Array<String> = arrayOf(),
		var floats: FloatArray? = null,
		var matrices: Array<Matrix3D>? = null
	) {
		val transforms = matrices?.map { Transform3D().setMatrix(it) }?.toTypedArray()
		val totalFrames = seconds.size
		val totalTime = seconds.max()?.seconds ?: 0.seconds

		// @TODO: Binary Search
		fun findIndex(time: TimeSpan): Int {
			val elapsedSeconds = time.seconds
			for (n in 0 until totalFrames - 1) {
				if (elapsedSeconds >= seconds[n] && elapsedSeconds < seconds[n + 1]) {
					return n
				}
			}
			return totalFrames - 1
		}
	}

}

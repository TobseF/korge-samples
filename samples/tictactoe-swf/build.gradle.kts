import com.soywiz.korge.gradle.*
import com.soywiz.korge.gradle.util.*

apply<KorgeGradlePlugin>()

korge {
	id = "com.soywiz.samples.tictactoe"
	name = "tic-tac-toe"
	icon = projectDir["src/commonMain/resources/icon.png"]

	//dependencyMulti("com.soywiz:korge-swf:$korgeVersion")
	supportSwf()

	targetDefault()
}

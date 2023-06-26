package app.ministrylogbook.ui

sealed class AppGraph(val route: String) {

    object Home : AppGraph("home")

    object Intro : AppGraph("intro")

    object Settings : AppGraph("settings")

    object Share : AppGraph("share")

    object Backup : AppGraph("backup")

    override fun toString() = route
}

package app.ministrylogbook.ui

sealed class AppGraph(val route: String) {
    object Home : AppGraph("home")

    object Settings : AppGraph("settings")

    object Share : AppGraph("share")

    override fun toString(): String {
        return route
    }
}

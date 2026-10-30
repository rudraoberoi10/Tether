package com.tether.app.ui.navigation

object Routes {
    const val PERMISSIONS = "permissions"
    const val NAME = "name"
    const val HOME = "home"
    const val CREATE_CIRCLE = "create_circle"
    const val JOIN_CIRCLE = "join_circle"
    const val CIRCLE_DETAIL = "circle_detail/{circleId}"

    fun circleDetail(circleId: String): String = "circle_detail/$circleId"
}
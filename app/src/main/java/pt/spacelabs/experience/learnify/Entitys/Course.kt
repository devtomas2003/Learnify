package pt.spacelabs.experience.learnify.Entitys

data class Course(
    val Id: String,
    val FriendlyName: String,
    val title: String,
    val description: String,
    val poster: String,
    val UUID: String,
    val isEnable: Boolean
)
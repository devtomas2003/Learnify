package pt.spacelabs.experience.learnify.Entitys

data class ClassDetailInfo(
    val Id: String,
    val CourseId: String,
    val Name: String,
    val Description: String,
    val Path: String,
    val ImagePath: String,
    val UUID: String,
    val isEnable: Boolean
)
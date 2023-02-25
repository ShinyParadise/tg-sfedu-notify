package utils

import models.User
import java.io.File

class FileManager {
    private val outputFile = File("src/main/resources/users.txt")

    fun writeNewUser(user: User) {
        try {
            outputFile.writeText("${user.id} ${user.group}")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun changeUserGroup(user: User, newGroup: String) {
        outputFile.forEachLine { line ->
            if (line.contains(user.id)) line.replace(user.group, newGroup)
        }
    }
}

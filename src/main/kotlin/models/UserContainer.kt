package models

import utils.FileManager

interface UserContainer {
    fun addUsers(vararg user: User)
    fun updateUserGroup(userId: String, group: String)
    fun isUserWithAGroup(userId: String): Boolean
    fun findUser(userId: String): User?
}

class UserContainerImpl : UserContainer {
    private val users = mutableListOf<User>()

    private val fileManager: FileManager = FileManager()

    override fun addUsers(vararg user: User) {
        users.addAll(user)
    }

    override fun findUser(userId: String): User? {
        return users.find { it.id == userId }
    }

    override fun updateUserGroup(userId: String, group: String) {
        val existingUser = users.find { user -> (user.id == userId) }

        if (existingUser == null) {
            val newUser = User(id = userId, group = group)
            users.add(newUser)
            fileManager.writeNewUser(newUser)
        } else {
            existingUser.group = group
            fileManager.changeUserGroup(existingUser, group)
        }
    }

    override fun isUserWithAGroup(userId: String): Boolean {
        val existingUser = users.find { it.id == userId }
        existingUser?.let {
            return it.group.isNotBlank()
        }

        return false
    }
}

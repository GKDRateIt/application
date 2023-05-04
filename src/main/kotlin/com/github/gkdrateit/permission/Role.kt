package com.github.gkdrateit.permission

sealed class Role {
    abstract val permissions: Set<Permission>

    fun hasPermission(permission: Permission): Boolean {
        return this.permissions.contains(permission)
    }

    companion object {
        fun getRole(roleName: String): Role {
            return when (roleName.lowercase()) {
                "default" -> Default
                "member" -> Member
                "admin" -> Admin
                else -> throw Exception("No such role")
            }
        }
    }
}

object Default : Role() {
    override val permissions = setOf(
        Permission.COURSE_READ, Permission.REVIEW_READ, Permission.TEACHER_READ, Permission.USER_READ
    )

    override fun toString(): String {
        return "NoLogin"
    }
}

object Member : Role() {
    override val permissions = run {
        val p = Default.permissions.toHashSet()
        arrayOf(
            Permission.COURSE_CREATE, Permission.REVIEW_CREATE
        ).forEach {
            p.add(it)
        }
        return@run p.toSet()
    }

    override fun toString(): String {
        return "Member"
    }
}

object Admin : Role() {
    override val permissions = run {
        val p = HashSet<Permission>()
        Permission.values().forEach {
            p.add(it)
        }
        return@run p.toSet()
    }

    override fun toString(): String {
        return "Admin"
    }
}
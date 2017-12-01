package com.kissybnts.app

object EnvironmentVariableKeys {
    /**
     * All key has the prefix `ORG_GRADLE_PROJECT_`
     */
    object Gradle {
        private const val GRADLE_ENV = "ORG_GRADLE_PROJECT_"
        const val DB_HOST: String = "${GRADLE_ENV}DB_HOST"
        const val DB_PORT: String = "${GRADLE_ENV}DB_PORT"
        const val DB_NAME: String = "${GRADLE_ENV}DB_NAME"
        const val DB_USER: String = "${GRADLE_ENV}DB_USER"
        const val DB_PASS: String = "${GRADLE_ENV}DB_PASS"
    }
    const val JWT_SECRET_KEY: String = "JWT_SECRET_KEY"
    const val GITHUB_CLIENT_ID: String = "GITHUB_CLIENT_ID"
    const val GITHUB_CLIENT_SECRET: String = "GITHUB_CLIENT_SECRET"
}
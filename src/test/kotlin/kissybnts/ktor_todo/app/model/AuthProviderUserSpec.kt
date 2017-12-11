package kissybnts.ktor_todo.app.model

import kissybnts.ktor_todo.app.enumeration.AuthProvider
import org.amshove.kluent.shouldEqual
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

object AuthProviderUserSpec: Spek({
    given("GitHubUser") {
        on("toOAuthUser()") {
            val githubUser = GitHubUser(1, "kissybnts", "Masataka Kishida", "https://avatars0.githubusercontent.com/u/7589780?v=4")
            val nameNullGithubUser = GitHubUser(2, "kissybnts", null, "https://avatars0.githubusercontent.com/u/7589780?v=4")
            val providerCode = "test code"
            it("should return same value as GitHubUser in case of GitHubUser.name is not null") {
                val oautUser = githubUser.toOAuthUser(providerCode)
                oautUser.providerType shouldEqual AuthProvider.GitHub
                oautUser.providerId shouldEqual githubUser.id
                oautUser.name shouldEqual githubUser.name
                oautUser.imageUrl shouldEqual githubUser.avatarUrl
                oautUser.providerCode shouldEqual providerCode
            }

            it("name of returned user should equal with GitHubUser.login in case of GitHubUser.name is null") {
                val oauthUser = nameNullGithubUser.toOAuthUser(providerCode)
                oauthUser.name shouldEqual githubUser.login
            }
        }
    }
})
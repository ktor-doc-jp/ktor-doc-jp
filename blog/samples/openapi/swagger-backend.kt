package com.example

import java.util.*
import io.ktor.http.*
import io.ktor.swagger.experimental.*

class ConduitAPIServer(val myjwt: MyJWT) : SwaggerBaseServer, ConduitAPI {
    override suspend fun login(
        body: LoginUserRequest
    ): UserResponse {
        if (false) httpException(HttpStatusCode.Unauthorized)
        if (false) httpException(422, "Unexpected error")

        return UserResponse(
            user = User(
                email = "",
                token = "",
                username = "",
                bio = "",
                image = ""
            )
        )
    }

    override suspend fun createUser(
        body: NewUserRequest
    ): String {
        if (false) httpException(HttpStatusCode.Created)
        if (false) httpException(422, "Unexpected error")

        return ""
    }

    override suspend fun getCurrentUser(
    ): UserResponse {
        if (false) httpException(HttpStatusCode.Unauthorized)
        if (false) httpException(422, "Unexpected error")

        return UserResponse(
            user = User(
                email = "",
                token = "",
                username = "",
                bio = "",
                image = ""
            )
        )
    }

    override suspend fun updateCurrentUser(
        body: UpdateUserRequest
    ): UserResponse {
        if (false) httpException(HttpStatusCode.Unauthorized)
        if (false) httpException(422, "Unexpected error")

        return UserResponse(
            user = User(
                email = "",
                token = "",
                username = "",
                bio = "",
                image = ""
            )
        )
    }

    override suspend fun getProfileByUsername(
        username: String
    ): ProfileResponse {
        if (false) httpException(HttpStatusCode.Unauthorized)
        if (false) httpException(422, "Unexpected error")

        return ProfileResponse(
            profile = Profile(
                username = "",
                bio = "",
                image = "",
                following = false
            )
        )
    }

    override suspend fun followUserByUsername(
        username: String
    ): ProfileResponse {
        if (false) httpException(HttpStatusCode.Unauthorized)
        if (false) httpException(422, "Unexpected error")

        return ProfileResponse(
            profile = Profile(
                username = "",
                bio = "",
                image = "",
                following = false
            )
        )
    }

    override suspend fun unfollowUserByUsername(
        username: String
    ): ProfileResponse {
        if (false) httpException(HttpStatusCode.Unauthorized)
        if (false) httpException(422, "Unexpected error")

        return ProfileResponse(
            profile = Profile(
                username = "",
                bio = "",
                image = "",
                following = false
            )
        )
    }

    override suspend fun getArticlesFeed(
        limit: Int,
        offset: Int
    ): MultipleArticlesResponse {
        if (false) httpException(HttpStatusCode.Unauthorized)
        if (false) httpException(422, "Unexpected error")

        return MultipleArticlesResponse(
            articles = listOf(),
            articlesCount = 0
        )
    }

    override suspend fun getArticles(
        tag: String,
        author: String,
        favorited: String,
        limit: Int,
        offset: Int
    ): MultipleArticlesResponse {
        if (false) httpException(HttpStatusCode.Unauthorized)
        if (false) httpException(422, "Unexpected error")

        return MultipleArticlesResponse(
            articles = listOf(),
            articlesCount = 0
        )
    }

    override suspend fun createArticle(
        article: NewArticleRequest
    ): String {
        if (false) httpException(HttpStatusCode.Created)
        if (false) httpException(HttpStatusCode.Unauthorized)
        if (false) httpException(422, "Unexpected error")

        return ""
    }

    override suspend fun getArticle(
        slug: String
    ): SingleArticleResponse {
        if (false) httpException(422, "Unexpected error")

        return SingleArticleResponse(
            article = Article(
                slug = "",
                title = "",
                description = "",
                body = "",
                tagList = listOf(),
                createdAt = Date(),
                updatedAt = Date(),
                favorited = false,
                favoritesCount = 0,
                author = Profile(
                    username = "",
                    bio = "",
                    image = "",
                    following = false
                )
            )
        )
    }

    override suspend fun updateArticle(
        slug: String,
        article: UpdateArticleRequest
    ): SingleArticleResponse {
        if (false) httpException(HttpStatusCode.Unauthorized)
        if (false) httpException(422, "Unexpected error")

        return SingleArticleResponse(
            article = Article(
                slug = "",
                title = "",
                description = "",
                body = "",
                tagList = listOf(),
                createdAt = Date(),
                updatedAt = Date(),
                favorited = false,
                favoritesCount = 0,
                author = Profile(
                    username = "",
                    bio = "",
                    image = "",
                    following = false
                )
            )
        )
    }

    override suspend fun deleteArticle(
        slug: String
    ): Unit {
        if (false) httpException(HttpStatusCode.Unauthorized)
        if (false) httpException(422, "Unexpected error")

    }

    override suspend fun getArticleComments(
        slug: String
    ): MultipleCommentsResponse {
        if (false) httpException(HttpStatusCode.Unauthorized)
        if (false) httpException(422, "Unexpected error")

        return MultipleCommentsResponse(
            comments = listOf()
        )
    }

    override suspend fun createArticleComment(
        slug: String,
        comment: NewCommentRequest
    ): SingleCommentResponse {
        if (false) httpException(HttpStatusCode.Unauthorized)
        if (false) httpException(422, "Unexpected error")

        return SingleCommentResponse(
            comment = Comment(
                id = 0,
                createdAt = Date(),
                updatedAt = Date(),
                body = "",
                author = Profile(
                    username = "",
                    bio = "",
                    image = "",
                    following = false
                )
            )
        )
    }

    override suspend fun deleteArticleComment(
        slug: String,
        id: Int
    ): Unit {
        if (false) httpException(HttpStatusCode.Unauthorized)
        if (false) httpException(422, "Unexpected error")

    }

    override suspend fun createArticleFavorite(
        slug: String
    ): SingleArticleResponse {
        if (false) httpException(HttpStatusCode.Unauthorized)
        if (false) httpException(422, "Unexpected error")

        return SingleArticleResponse(
            article = Article(
                slug = "",
                title = "",
                description = "",
                body = "",
                tagList = listOf(),
                createdAt = Date(),
                updatedAt = Date(),
                favorited = false,
                favoritesCount = 0,
                author = Profile(
                    username = "",
                    bio = "",
                    image = "",
                    following = false
                )
            )
        )
    }

    override suspend fun deleteArticleFavorite(
        slug: String
    ): SingleArticleResponse {
        if (false) httpException(HttpStatusCode.Unauthorized)
        if (false) httpException(422, "Unexpected error")

        return SingleArticleResponse(
            article = Article(
                slug = "",
                title = "",
                description = "",
                body = "",
                tagList = listOf(),
                createdAt = Date(),
                updatedAt = Date(),
                favorited = false,
                favoritesCount = 0,
                author = Profile(
                    username = "",
                    bio = "",
                    image = "",
                    following = false
                )
            )
        )
    }

    override suspend fun getTags(
    ): TagsResponse {
        if (false) httpException(422, "Unexpected error")

        return TagsResponse(
            tags = listOf()
        )
    }
}

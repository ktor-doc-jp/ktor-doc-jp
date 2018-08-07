package com.example

import java.util.*
import io.ktor.swagger.experimental.*

/**
 * Conduit API
 * 
 * Conduit API
 */
interface ConduitAPI : SwaggerBaseApi {
    /**
     * Login for existing user
     * 
     * @param body Credentials to use
     * 
     * @return OK
     */
    @Path("/users/login")
    @Method("POST")
    suspend fun login(
        @Body("body") body: LoginUserRequest
    ): UserResponse

    /**
     * Register a new user
     * 
     * @param body Details of the new user to register
     * 
     * @return OK
     */
    @Path("/users")
    @Method("POST")
    suspend fun createUser(
        @Body("body") body: NewUserRequest
    ): String

    /**
     * Gets the currently logged-in user
     * 
     * @return OK
     */
    @Path("/users")
    @Method("GET")
    @Auth("Token")
    suspend fun getCurrentUser(
    ): UserResponse

    /**
     * Updated user information for current user
     * 
     * @param body User details to update. At least **one** field is required.
     * 
     * @return OK
     */
    @Path("/users")
    @Method("PUT")
    @Auth("Token")
    suspend fun updateCurrentUser(
        @Body("body") body: UpdateUserRequest
    ): UserResponse

    /**
     * Get a profile of a user of the system. Auth is optional
     * 
     * @param username Username of the profile to get
     * 
     * @return OK
     */
    @Path("/profiles/{username}")
    @Method("GET")
    suspend fun getProfileByUsername(
        @Path("username") username: String
    ): ProfileResponse

    /**
     * Follow a user by username
     * 
     * @param username Username of the profile you want to follow
     * 
     * @return OK
     */
    @Path("/profiles/{username}/follow")
    @Method("POST")
    @Auth("Token")
    suspend fun followUserByUsername(
        @Path("username") username: String
    ): ProfileResponse

    /**
     * Unfollow a user by username
     * 
     * @param username Username of the profile you want to unfollow
     * 
     * @return OK
     */
    @Path("/profiles/{username}/follow")
    @Method("DELETE")
    @Auth("Token")
    suspend fun unfollowUserByUsername(
        @Path("username") username: String
    ): ProfileResponse

    /**
     * Get most recent articles from users you follow. Use query parameters to limit. Auth is required
     * 
     * @param limit Limit number of articles returned (default is 20)
     * @param offset Offset/skip number of articles (default is 0)
     * 
     * @return OK
     */
    @Path("/articles/feed")
    @Method("GET")
    @Auth("Token")
    suspend fun getArticlesFeed(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): MultipleArticlesResponse

    /**
     * Get most recent articles globally. Use query parameters to filter results. Auth is optional
     * 
     * @param tag Filter by tag
     * @param author Filter by author (username)
     * @param favorited Filter by favorites of a user (username)
     * @param limit Limit number of articles returned (default is 20)
     * @param offset Offset/skip number of articles (default is 0)
     * 
     * @return OK
     */
    @Path("/articles")
    @Method("GET")
    suspend fun getArticles(
        @Query("tag") tag: String = "",
        @Query("author") author: String = "",
        @Query("favorited") favorited: String = "",
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): MultipleArticlesResponse

    /**
     * Create an article. Auth is required
     * 
     * @param article Article to create
     * 
     * @return OK
     */
    @Path("/articles")
    @Method("POST")
    @Auth("Token")
    suspend fun createArticle(
        @Body("article") article: NewArticleRequest
    ): String

    /**
     * Get an article. Auth not required
     * 
     * @param slug Slug of the article to get
     * 
     * @return OK
     */
    @Path("/articles/{slug}")
    @Method("GET")
    suspend fun getArticle(
        @Path("slug") slug: String
    ): SingleArticleResponse

    /**
     * Update an article. Auth is required
     * 
     * @param slug Slug of the article to update
     * @param article Article to update
     * 
     * @return OK
     */
    @Path("/articles/{slug}")
    @Method("PUT")
    @Auth("Token")
    suspend fun updateArticle(
        @Path("slug") slug: String,
        @Body("article") article: UpdateArticleRequest
    ): SingleArticleResponse

    /**
     * Delete an article. Auth is required
     * 
     * @param slug Slug of the article to delete
     * 
     * @return OK
     */
    @Path("/articles/{slug}")
    @Method("DELETE")
    @Auth("Token")
    suspend fun deleteArticle(
        @Path("slug") slug: String
    ): Unit

    /**
     * Get the comments for an article. Auth is optional
     * 
     * @param slug Slug of the article that you want to get comments for
     * 
     * @return OK
     */
    @Path("/articles/{slug}/comments")
    @Method("GET")
    suspend fun getArticleComments(
        @Path("slug") slug: String
    ): MultipleCommentsResponse

    /**
     * Create a comment for an article. Auth is required
     * 
     * @param slug Slug of the article that you want to create a comment for
     * @param comment Comment you want to create
     * 
     * @return OK
     */
    @Path("/articles/{slug}/comments")
    @Method("POST")
    @Auth("Token")
    suspend fun createArticleComment(
        @Path("slug") slug: String,
        @Body("comment") comment: NewCommentRequest
    ): SingleCommentResponse

    /**
     * Delete a comment for an article. Auth is required
     * 
     * @param slug Slug of the article that you want to delete a comment for
     * @param id ID of the comment you want to delete
     * 
     * @return OK
     */
    @Path("/articles/{slug}/comments/{id}")
    @Method("DELETE")
    @Auth("Token")
    suspend fun deleteArticleComment(
        @Path("slug") slug: String,
        @Path("id") id: Int
    ): Unit

    /**
     * Favorite an article. Auth is required
     * 
     * @param slug Slug of the article that you want to favorite
     * 
     * @return OK
     */
    @Path("/articles/{slug}/favorite")
    @Method("POST")
    @Auth("Token")
    suspend fun createArticleFavorite(
        @Path("slug") slug: String
    ): SingleArticleResponse

    /**
     * Unfavorite an article. Auth is required
     * 
     * @param slug Slug of the article that you want to unfavorite
     * 
     * @return OK
     */
    @Path("/articles/{slug}/favorite")
    @Method("DELETE")
    @Auth("Token")
    suspend fun deleteArticleFavorite(
        @Path("slug") slug: String
    ): SingleArticleResponse

    /**
     * Get tags. Auth not required
     * 
     * @return OK
     */
    @Path("/tags")
    @Method("GET")
    suspend fun getTags(
    ): TagsResponse
}

data class LoginUser(
    val email: String,
    val password: String
)
data class LoginUserRequest(
    val user: LoginUser
)
data class NewUser(
    val username: String,
    val email: String,
    val password: String
)
data class NewUserRequest(
    val user: NewUser
)
data class User(
    val email: String,
    val token: String,
    val username: String,
    val bio: String,
    val image: String
)
data class UserResponse(
    val user: User
)
data class UpdateUser(
    val email: String,
    val token: String,
    val username: String,
    val bio: String,
    val image: String
)
data class UpdateUserRequest(
    val user: UpdateUser
)
data class ProfileResponse(
    val profile: Profile
)
data class Profile(
    val username: String,
    val bio: String,
    val image: String,
    val following: Boolean
)
data class Article(
    val slug: String,
    val title: String,
    val description: String,
    val body: String,
    val tagList: List<String>,
    val createdAt: Date,
    val updatedAt: Date,
    val favorited: Boolean,
    val favoritesCount: Int,
    val author: Profile
)
data class SingleArticleResponse(
    val article: Article
)
data class MultipleArticlesResponse(
    val articles: List<Article>,
    val articlesCount: Int
)
data class NewArticle(
    val title: String,
    val description: String,
    val body: String,
    val tagList: List<String>
)
data class NewArticleRequest(
    val article: NewArticle
)
data class UpdateArticle(
    val title: String,
    val description: String,
    val body: String
)
data class UpdateArticleRequest(
    val article: UpdateArticle
)
data class Comment(
    val id: Int,
    val createdAt: Date,
    val updatedAt: Date,
    val body: String,
    val author: Profile
)
data class SingleCommentResponse(
    val comment: Comment
)
data class MultipleCommentsResponse(
    val comments: List<Comment>
)
data class NewComment(
    val body: String
)
data class NewCommentRequest(
    val comment: NewComment
)
data class TagsResponse(
    val tags: List<String>
)
data class GenericErrorModel(
    val errors: Any/*Unsupported {body=List<String>}*/
)

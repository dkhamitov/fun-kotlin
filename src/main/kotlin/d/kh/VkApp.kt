package d.kh

import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.UserActor
import com.vk.api.sdk.httpclient.HttpTransportClient
import com.vk.api.sdk.queries.wall.WallGetFilter


fun main(args: Array<String>) {
    val appId = System.getProperty("appId").toInt()
    val clientSecret = System.getProperty("clientSecret")
    val redirectUrl = System.getProperty("redirectUrl", "")
    val code = System.getProperty("code")
    val userId = System.getProperty("userId").toInt()

    val vk = VkApiClient(HttpTransportClient.getInstance())

    val authResponse = vk.oauth()
            .userAuthorizationCodeFlow(appId, clientSecret, redirectUrl, code)
            .execute()

    val actor = UserActor(appId, authResponse.accessToken)

    val wall = vk.wall().get(actor)
            .ownerId(userId)
            .count(100)
            .filter(WallGetFilter.ALL)
            .execute()

    println(wall)
}

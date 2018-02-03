package d.kh

import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.httpclient.HttpTransportClient

fun main(args: Array<String>) {
    val appId = System.getProperty("appId").toInt()
    val clientSecret = System.getProperty("clientSecret")
    val redirectUrl = System.getProperty("redirectUrl", "")
    val code = readString("Enter the code please")

    val vk = VkApiClient(HttpTransportClient.getInstance())

    val authResponse = vk.oauth()
            .userAuthorizationCodeFlow(appId, clientSecret, redirectUrl, code)
            .execute()

    println(authResponse.accessToken)
}

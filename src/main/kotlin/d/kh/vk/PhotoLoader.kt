package d.kh.vk

import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.html.HtmlPage
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput
import com.gargoylesoftware.htmlunit.html.HtmlTextInput
import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.UserActor
import com.vk.api.sdk.httpclient.HttpTransportClient
import com.vk.api.sdk.objects.photos.PhotoAlbumFull
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import java.io.File
import java.net.URI
import java.nio.file.Paths

/**
 * The code has been written using
 * https://vk.com/dev/methods
 * https://vk.com/dev/photos.get
 * https://vk.com/dev/Java_SDK
 * https://vk.com/dev/permissions
 */
fun main(args: Array<String>) {
    val appId = System.getProperty("appId")?.toInt() ?: readInt("Enter the appId")
    val user = readString("Enter your username (email/phone)")
    val pass = readPassword("Enter your password")
    val token = getToken(appId, user, pass) ?: readString("Access Token not received. Enter the token manually")
    val photoUser = readInt("Enter the User ID whose photos you want to download")

    val vk = VkApiClient(HttpTransportClient.getInstance())
    val actor = UserActor(appId, token)

    val albums = vk.photos()
            .getAlbums(actor)
            .needSystem(true)
            .ownerId(photoUser)
            .execute()
            .items
    val albumTitleById = albums.associateBy({ it.id.toString() }, { it.title })

    printlnAlbums(albums)

    if (readString("Shall we download? [Y|N]").equals("Y", ignoreCase = true)) {
        val albumToLoad = readString("Which album to download?", { albumTitleById.contains(it) })
        val albumFolder = readString("Where to download the album?")

        val photos = vk.photos()
                .get(actor)
                .ownerId(photoUser)
                .albumId(albumToLoad)
                .photoIds()
                .photoSizes(true)
                .execute()
                .items

        val urls = photos.map { it.sizes }
                .map { it.maxBy { it.type } }
                .map { it!!.src }

        savePhotos(urls, albumFolder, albumTitleById[albumToLoad]!!)
    }
}

private fun printlnAlbums(albums: Collection<PhotoAlbumFull>) {
    val titleIndent = albums.map({ it.title.trim().length }).max()
    val idIndent = albums.map({ it.id.toString().length }).max()
    println("Albums")
    albums.forEach { println("%-${titleIndent}s: %${idIndent}s: %s".format(it.title.trim(), it.id, it.size)) }
    println()
}

private fun savePhotos(urls: List<String>, root: String, album: String) {
    val urlsByCopies = urls.groupBy { it }
            .map { it.key to it.value.size }
            .sortedByDescending { it.second }
    val duplicates = urlsByCopies.takeWhile { it.second > 1 }
    if (duplicates.isNotEmpty()) {
        println("There are duplicates:")
        duplicates.forEach { println("${it.first} ${it.second} copies") }
    }
    println("${urlsByCopies.size} photos out of ${urls.size} will be downloaded")

    val folder = Paths.get(root, album).toFile()
    folder.mkdirs()

    val httpClient = HttpClients.createDefault()
    urlsByCopies.map { it.first }.forEachIndexed { i, url ->
        val file = File(folder, URI.create(url).path.substring(1).replace('/', '-'))
        httpClient.execute(HttpGet(url)).entity.writeTo(file.outputStream())
        println("$url downloaded (${i + 1} of ${urls.size})")
    }
}

private fun getToken(appId: Int, user: String, pass: String): String? {
    val webClient = WebClient()
    val url = "https://oauth.vk.com/authorize?client_id=$appId&scope=friends,photos&display=mobile&response_type=token"
    val page = webClient.getPage<HtmlPage>(url)

    val loginForm = page.forms.first()
    loginForm.getInputByName<HtmlTextInput>("email").valueAttribute = user
    loginForm.getInputByName<HtmlPasswordInput>("pass").valueAttribute = pass
    val respPage = loginForm.getInputByValue<HtmlSubmitInput>("Log in").click<HtmlPage>()
    return respPage.url.ref.split('&').map { it.split('=') }.map { it[0] to it[1] }.toMap()["access_token"]
}
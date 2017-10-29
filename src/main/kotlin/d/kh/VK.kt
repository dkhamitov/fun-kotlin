package d.kh

fun main(args: Array<String>) {
    val hello = listOf("VK", "Client", "is", "coming", "soon", "too", "many", "words")
    println(hello.joinToString(separator = " ", limit = 5, truncated = "!"))
}

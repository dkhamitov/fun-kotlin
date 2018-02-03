package d.kh

import java.util.Scanner

fun readString(prompt: String): String {
    println(prompt)
    return Scanner(System.`in`).next()
}

fun readInt(prompt: String): Int {
    println(prompt)
    return Scanner(System.`in`).nextInt()
}
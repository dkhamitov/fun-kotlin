package d.kh

import java.util.Scanner

fun readString(prompt: String): String {
    println(prompt)
    return Scanner(System.`in`).next()
}

fun readString(prompt: String, successCond: (String) -> Boolean): String {
    var value = readString(prompt)
    while (!successCond(value)) {
        value = readString("Wrong input! $prompt")
    }
    return value
}

fun readInt(prompt: String): Int {
    println(prompt)
    return Scanner(System.`in`).nextInt()
}
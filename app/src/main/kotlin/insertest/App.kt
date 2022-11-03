package insertest

class App {
    val greeting: String
        get() {
            return "Hello World!"
        }
}

fun main() {
    val ins23 = Insert("test0", 1)
    println(ins23.insert)
}

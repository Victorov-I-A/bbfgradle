// Original bug: KT-14803

class A {
    val a by lazy {
        val x = f<Int>()

        if (x == null) {
            throw IllegalStateException("Lol")
        }
        else {
            //
        }

        x
    }
}

fun <T> f(): T? = null 

fun h(x: Int) {}

fun main(args: Array<String>) {
    h(A().a) // Error Kotlin: Type mismatch: inferred type is Int? but Int was expected
}

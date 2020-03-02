// IGNORE_BACKEND_FIR: JVM_IR
// IGNORE_BACKEND: JS_IR
// TODO: muted automatically, investigate should it be ran for JS or not
// IGNORE_BACKEND: JS, NATIVE

// WITH_REFLECT

fun box(): String {
    class Local {
        fun result(s: String) = s
    }

    return Local::result.call(Local(), "OK")
}

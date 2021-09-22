// WITH_RUNTIME

// IGNORE_BACKEND: WASM
// Result::getOrNull contains cast (value as T). It gets inlined but type parameter is not updated. We loose information that it was
// a String and instead we treat it as Any. We then fail to assign it into temporary variable of type String?.
// WASM_MUTE_REASON: TYPE_ISSUES

fun f1(): () -> Result<String> {
    return {
        runCatching {
            "OK"
        }
    }
}

fun box(): String {
    val r = f1()()
    return r.getOrNull() ?: "fail: $r"
}
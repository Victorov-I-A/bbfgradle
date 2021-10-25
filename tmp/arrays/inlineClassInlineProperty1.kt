// TARGET_BACKEND: JVM
// !LANGUAGE: +InlineClasses
// MODULE: lib
// USE_OLD_INLINE_CLASSES_MANGLING_SCHEME
// FILE: A.kt

package a

inline class S(val value: String) {
    inline val k: String
        get() = value + "K"
}

// MODULE: main(lib)
// FILE: B.kt

fun box(): String {
    return a.S("O").k
}

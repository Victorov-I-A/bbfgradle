// TARGET_BACKEND: JVM
// MODULE: lib
// FILE: Foo.java

public class Foo {
    public class Inner {

    }

    public class Inner$ {
    }
}

// MODULE: main(lib)
// FILE: 1.kt

fun box(): String {
    Foo().Inner()
    Foo().`Inner$`()
    return "OK"
}

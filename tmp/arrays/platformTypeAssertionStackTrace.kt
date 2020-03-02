// IGNORE_BACKEND_FIR: JVM_IR
// TARGET_BACKEND: JVM

// FULL_JDK

package test

import java.util.*

fun box(): String {
    val a = ArrayList<String>() as AbstractList<String>
    a.add(null)
    try {
        val b: String = a[0]
        return "Fail: an exception should be thrown"
    } catch (e: NullPointerException) {
        val st = (e as java.lang.Throwable).getStackTrace()
        if (st.size < 5) {
            return "Fail: very small stack trace, should at least have current function and JUnit reflective calls: ${Arrays.toString(st)}"
        }
        val top = st[0]
        if (!(top.getClassName() == "test.PlatformTypeAssertionStackTraceKt" && top.getMethodName() == "box")) {
            return "Fail: top stack trace element should be PlatformTypeAssertionStackTraceKt.box() from default package, but was $top"
        }
        return "OK"
    }
}

// Auto-generated by GenerateSteppedRangesCodegenTestData. Do not edit!
// DONT_TARGET_EXACT_BACKEND: WASM
// KJS_WITH_FULL_RUNTIME
// WITH_RUNTIME
import kotlin.test.*

fun box(): String {
    val uintList = mutableListOf<UInt>()
    for (i in 10u downTo 1u step 2 step 3) {
        uintList += i
    }
    assertEquals(listOf(10u, 7u, 4u), uintList)

    val ulongList = mutableListOf<ULong>()
    for (i in 10uL downTo 1uL step 2L step 3L) {
        ulongList += i
    }
    assertEquals(listOf(10uL, 7uL, 4uL), ulongList)

    return "OK"
}
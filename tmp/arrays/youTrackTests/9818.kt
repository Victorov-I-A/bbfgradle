// Original bug: KT-10481

open class A {
    open var value: Int = 4
        protected set
}

class MutableA : A() {
    override var value: Int = 4
    public set
}

fun test(myA: A) {
    if (myA is MutableA) {
        myA.value = 5
    }
}

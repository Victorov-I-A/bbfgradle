class A() {

    override fun toString(): String {
        return "A"
    }
}


fun box() : String {
    val p = 1
    val s = "${p}${2}${3}${4L}${5.0}${6F}${7}${A()}"

    return "OK"
}

// 1 NEW java/lang/StringBuilder
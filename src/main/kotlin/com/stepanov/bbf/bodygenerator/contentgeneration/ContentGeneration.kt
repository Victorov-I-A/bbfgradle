package com.stepanov.bbf.bodygenerator.contentgeneration

import com.stepanov.bbf.bodygenerator.BodyScope

abstract class ContentGeneration(val scope: BodyScope, val depth: Int) {

    abstract fun generate(): String

    val randTypeExpr: String
        get() = Generation.createExpression(scope, depth)

    val boolExpr: String
        get() = Generation.createExpression(scope, depth, scope.typeTranslator["Boolean"]!!)
}

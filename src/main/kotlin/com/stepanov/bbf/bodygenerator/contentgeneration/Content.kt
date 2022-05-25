package com.stepanov.bbf.bodygenerator.contentgeneration

import com.stepanov.bbf.bodygenerator.BodyScope
import com.stepanov.bbf.bodygenerator.TypeTable.primitiveTypesTranslator
import com.stepanov.bbf.bodygenerator.contentgeneration.Generation.generateExpression


abstract class Content(val scope: BodyScope, val depth: Int) {

    abstract fun generate(): String

    val randTypeExpr: String
        get() = generateExpression(scope, depth)

    val boolExpr: String
        get() = generateExpression(scope, depth, primitiveTypesTranslator["Boolean"]!!)
}

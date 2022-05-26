package com.stepanov.bbf.bodygenerator.contentgeneration

import com.stepanov.bbf.bodygenerator.BodyScope
import com.stepanov.bbf.bodygenerator.Generation
import com.stepanov.bbf.bodygenerator.TypeTable.primitiveTypesTranslator
import com.stepanov.bbf.bodygenerator.Generation.generateExpression
import org.jetbrains.kotlin.types.KotlinType


abstract class Content(val scope: BodyScope, val depth: Int) {

    abstract fun generate(): String

    fun generateExpression(type: KotlinType): String = generateExpression(scope, depth, type)

    val boolExpr: String
        get() = generateExpression(scope, depth, primitiveTypesTranslator["Boolean"]!!)
}

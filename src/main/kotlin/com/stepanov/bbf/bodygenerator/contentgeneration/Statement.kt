package com.stepanov.bbf.bodygenerator.contentgeneration

import com.stepanov.bbf.bodygenerator.BodyScope
import com.stepanov.bbf.bodygenerator.Config.BlockSize
import com.stepanov.bbf.bodygenerator.TypeTable.getRandomType
import com.stepanov.bbf.bodygenerator.TypeTable.primitiveTypesTranslator
import com.stepanov.bbf.bodygenerator.Generation.generateContent
import com.stepanov.bbf.bodygenerator.Generation.generateExpression
import com.stepanov.bbf.bodygenerator.Generation.generateValue
import com.stepanov.bbf.bodygenerator.Generation.generateVariable
import com.stepanov.bbf.bodygenerator.TypeTable.arithmeticTypes
import com.stepanov.bbf.bodygenerator.TypeTable.getRandomNumericBeforeType
import com.stepanov.bbf.bodygenerator.Utils.randomContent
import org.jetbrains.kotlin.types.KotlinType


sealed class Statement(scope: BodyScope, depth: Int) : Content(scope, depth) {

    fun generateBlock(): String {
            val blockContent = mutableListOf<String>()
            if (scope.scopeTable.addLevel()) {
                for (i in 0 until BlockSize) {
                    blockContent += generateContent(scope, depth)
                }
                scope.scopeTable.deleteLevel()
            } else {
                for (i in 0 until BlockSize) {
                    blockContent += generateValue(scope, depth)
                }
            }
            return blockContent.joinToString(separator = "\n", prefix = "\n", postfix = "\n")
        }

    class For(scope: BodyScope, depth: Int) : Statement(scope, depth) {
        override fun generate(): String {
            return String.format("for (i in 0..1) {%s}", generateBlock())
        }
    }

    class While(scope: BodyScope, depth: Int) : Statement(scope, depth) {
        override fun generate(): String {
            return String.format("while (%s) {%s}", boolExpr, generateBlock())
        }
    }

    class DoWhile(scope: BodyScope, depth: Int) : Statement(scope, depth) {
        override fun generate(): String {
            return String.format("do {%s} while (%s)", generateBlock(), boolExpr)
        }
    }

    class SimpleAssignment(scope: BodyScope, depth: Int) : Statement(scope, depth) {
        override fun generate(): String {
            val randomType = getRandomType()
            val leftExpr = generateVariable(scope, depth, randomType)
            val rightExpr = generateExpression(scope, depth, randomType)

            return String.format("%s = %s", leftExpr, rightExpr)
        }
    }

    class OperatorAssignment(scope: BodyScope, depth: Int) : Statement(scope, depth) {
        private val operators = setOf("+=", "-=", "*=", "/=", "%=")
        private val randomResultType: KotlinType
            get() = primitiveTypesTranslator[arithmeticTypes.randomContent().simpleName]!!

        override fun generate(): String {
            val randomType = randomResultType
            val leftExpr = generateVariable(scope, depth, randomType)
            val rightExpr = generateExpression(scope, depth, randomType.getRandomNumericBeforeType())

            return String.format("%s %s %s", leftExpr, operators.random(), rightExpr)
        }
    }
}
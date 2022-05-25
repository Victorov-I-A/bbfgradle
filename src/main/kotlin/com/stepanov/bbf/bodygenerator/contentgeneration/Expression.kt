package com.stepanov.bbf.bodygenerator.contentgeneration

import com.stepanov.bbf.bodygenerator.BodyScope
import com.stepanov.bbf.bodygenerator.Config.BlockSize
import com.stepanov.bbf.bodygenerator.Config.WhenEntriesSize
import com.stepanov.bbf.bodygenerator.TypeTable.getRandomSimpleType
import com.stepanov.bbf.bodygenerator.TypeTable.getRandomType
import com.stepanov.bbf.bodygenerator.Utils.ProjectTools.rig
import com.stepanov.bbf.bodygenerator.contentgeneration.Generation.generateConstant
import com.stepanov.bbf.bodygenerator.contentgeneration.Generation.generateExpression
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.typeUtil.isUnsignedNumberType


sealed class Expression(scope: BodyScope, depth: Int) : Content(scope, depth) {

    fun generateBlock(type: KotlinType): String {
        val blockContent = mutableListOf<String>()
        if (scope.scopeTable.addLevel()) {
            for (i in 1 until BlockSize) {
                blockContent += generateExpression(scope, depth)
            }
            blockContent += generateExpression(scope, depth, type)
            scope.scopeTable.deleteLevel()
        } else {
            for (i in 1 until BlockSize) {
                blockContent += generateConstant(scope, depth)
            }
            blockContent += generateConstant(scope, depth, type)
        }
        return blockContent.joinToString(separator = "\n", prefix = "\n", postfix = "\n")
    }

    sealed class MultiTypeExpression(scope: BodyScope, depth: Int, val type: KotlinType) : Expression(scope, depth) {

        class Constant(scope: BodyScope, depth: Int, type: KotlinType) : MultiTypeExpression(scope, depth, type) {
            override fun generate(): String = rig.generateValueOfType(type)
        }

        class If(scope: BodyScope, depth: Int, type: KotlinType) : MultiTypeExpression(scope, depth, type) {
            override fun generate(): String {
                return String.format("if (%s) {%s} else {%s}", boolExpr, generateBlock(type), generateBlock(type))
            }
        }

        class When(scope: BodyScope, depth: Int, type: KotlinType) : MultiTypeExpression(scope, depth, type) {
            override fun generate(): String {
                val randomType = getRandomType()
                val whenEntries = List(WhenEntriesSize) {
                    if (it != WhenEntriesSize - 1)
                        String.format("%s -> {%s}", generateExpression(scope, depth, randomType), generateBlock(type))
                    else
                        String.format("else -> {%s}", generateBlock(type))
                }
                return String.format("when (%s) {%s}",
                    generateExpression(scope, depth, randomType),
                    whenEntries.joinToString(separator = "\n", prefix = "\n", postfix = "\n")
                )
            }
        }

        class Try(scope: BodyScope, depth: Int, type: KotlinType) : MultiTypeExpression(scope, depth, type) {
            override fun generate(): String {
                return String.format("try {%s} catch (e: Exception) {%s}", generateBlock(type), generateBlock(type))
            }
        }
    }

    sealed class SingleTypeExpression(scope: BodyScope, depth: Int) : Expression(scope, depth) {

        class Equality(scope: BodyScope, depth: Int) : SingleTypeExpression(scope, depth) {
            private val valueOperators = setOf("==", "!=")
            private val referenceOperators = setOf("===", "!==")

            override fun generate(): String  {
                val randomType = getRandomType()
                val operator = if (randomType.isUnsignedNumberType())
                    valueOperators.random()
                else
                    (valueOperators + referenceOperators).random()

                return String.format("(%s %s %s)",
                    generateExpression(scope, depth, randomType),
                    operator,
                    generateExpression(scope, depth, randomType)
                )
            }
        }

        class Comparison(scope: BodyScope, depth: Int) : SingleTypeExpression(scope, depth) {
            private val operators = setOf("<", ">", "<=", ">=")

            override fun generate(): String  {
                val randomType = getRandomSimpleType()

                return String.format("(%s %s %s)",
                    generateExpression(scope, depth, randomType),
                    operators.random(),
                    generateExpression(scope, depth, randomType)
                )
            }
        }

        class Disjunction(scope: BodyScope, depth: Int) : SingleTypeExpression(scope, depth) {
            override fun generate(): String = String.format("%s || %s", boolExpr, boolExpr)
        }

        class Conjunction(scope: BodyScope, depth: Int) : SingleTypeExpression(scope, depth) {
            override fun generate(): String = String.format("%s && %s", boolExpr, boolExpr)
        }
    }
}
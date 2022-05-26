package com.stepanov.bbf.bodygenerator.contentgeneration

import com.stepanov.bbf.bodygenerator.BodyScope
import com.stepanov.bbf.bodygenerator.Config.BlockSize
import com.stepanov.bbf.bodygenerator.Config.CollectionTypes
import com.stepanov.bbf.bodygenerator.Config.WhenEntriesSize
import com.stepanov.bbf.bodygenerator.TypeTable.getRandomPrimitiveType
import com.stepanov.bbf.bodygenerator.TypeTable.getRandomType
import com.stepanov.bbf.bodygenerator.Utils.ProjectTools.rig
import com.stepanov.bbf.bodygenerator.Generation.generateExpression
import com.stepanov.bbf.bodygenerator.Generation.generateValue
import com.stepanov.bbf.bodygenerator.TypeTable.arithmeticTypes
import com.stepanov.bbf.bodygenerator.TypeTable.getRandomNumericBeforeType
import com.stepanov.bbf.bodygenerator.TypeTable.primitiveTypesTranslator
import com.stepanov.bbf.bugfinder.util.name
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.typeUtil.isUnsignedNumberType


sealed class Expression(scope: BodyScope, depth: Int) : Content(scope, depth) {

    sealed class AnyTypeExpression(scope: BodyScope, depth: Int, val type: KotlinType) : Expression(scope, depth) {

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
                    blockContent += generateValue(scope, depth)
                }
                blockContent += generateValue(scope, depth, type)
            }
            return blockContent.joinToString(separator = "\n", prefix = "\n", postfix = "\n")
        }

        class Constant(scope: BodyScope, depth: Int, type: KotlinType) : AnyTypeExpression(scope, depth, type) {
            override fun generate(): String {
                var result: String
                do {
                    result = rig.generateValueOfType(type)
                } while (result == "" && type.name != "String")
                return result
            }
        }

        class If(scope: BodyScope, depth: Int, type: KotlinType) : AnyTypeExpression(scope, depth, type) {
            override fun generate(): String {
                return String.format("if (%s) {%s} else {%s}", boolExpr, generateBlock(type), generateBlock(type))
            }
        }

        class When(scope: BodyScope, depth: Int, type: KotlinType) : AnyTypeExpression(scope, depth, type) {
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

        class Try(scope: BodyScope, depth: Int, type: KotlinType) : AnyTypeExpression(scope, depth, type) {
            override fun generate(): String {
                return String.format("try {%s} catch (e: Exception) {%s}", generateBlock(type), generateBlock(type))
            }
        }
    }

    sealed class MultiTypeExpression(scope: BodyScope, depth: Int, val type: KotlinType) : Expression(scope, depth) {
        companion object {
            val possibleTypes = mapOf(
                Additive::class to CollectionTypes + arithmeticTypes,
                Multiplicative::class to arithmeticTypes
            )
        }

        class Additive(scope: BodyScope, depth: Int, type: KotlinType) : MultiTypeExpression(scope, depth, type) {
            private val operators = setOf("+", "-")

            override fun generate(): String {
                val leftExpr = generateExpression(scope, depth, type)
                val rightExpr = if (primitiveTypesTranslator.values.contains(type))
                    generateExpression(type.getRandomNumericBeforeType())
                else
                    generateExpression(type)

                return String.format("(%s %s %s)", leftExpr, operators.random(), rightExpr)
            }
        }

        class Multiplicative(scope: BodyScope, depth: Int, type: KotlinType) : MultiTypeExpression(scope, depth, type) {
            private val operators = setOf("*", "/", "%")

            override fun generate(): String {
                val leftExpr = generateExpression(scope, depth, type)
                val rightExpr = generateExpression(scope, depth, type.getRandomNumericBeforeType())

                return String.format("(%s %s %s)", leftExpr, operators.random(), rightExpr)
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
                val randomType = getRandomPrimitiveType()

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
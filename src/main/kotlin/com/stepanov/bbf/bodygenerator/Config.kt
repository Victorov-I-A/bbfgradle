package com.stepanov.bbf.bodygenerator

import com.stepanov.bbf.bodygenerator.contentgeneration.Declaration
import com.stepanov.bbf.bodygenerator.contentgeneration.Expression
import com.stepanov.bbf.bodygenerator.contentgeneration.Statement

object Config {

    const val BlockDepth = 2

    const val ExpressionDepth = 3

    val AllowedTypes = listOf(
        "Byte", "Short", "Int", "Long",
        "Float", "Double",
        "UByte", "UShort", "UInt", "ULong",
        "Boolean",
        "Char",
        "String"
    )

    val SingleTypeExpressions = listOf(
        "Boolean" to (Expression.Equality::class to 1),
        "Boolean" to (Expression.Comparison::class to 1),
        "Boolean" to (Expression.Disjunction::class to 1),
        "Boolean" to (Expression.Conjunction::class to 1)
    )

    val MultiTypeExpression = listOf(
        Expression.Constant::class to 1,
        Expression.If::class to 1,
        Expression.When::class to 1,
    )

    val Statements = listOf(
        Statement.For::class to 1,
        Statement.While::class to 1,
        Statement.DoWhile::class to 1
    )

    val Declarations = listOf(
        Declaration.PropertyDeclaration::class to 1
    )
}
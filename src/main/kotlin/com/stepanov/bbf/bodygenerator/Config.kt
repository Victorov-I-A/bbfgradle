package com.stepanov.bbf.bodygenerator

import com.stepanov.bbf.bodygenerator.contentgeneration.Declaration
import com.stepanov.bbf.bodygenerator.contentgeneration.Expression
import com.stepanov.bbf.bodygenerator.contentgeneration.Statement


object Config {

    const val BodySize = 2

    const val BlockSize = 2

    const val BlocksLevel = 2

    const val ExpressionDepth = 3

    const val PowerOfUserDefinedTypes = 10

    const val WhenEntriesSize = 2

    val CollectionTypes = mapOf(
        List::class to 1, MutableList::class to 2,
        Set::class to 2, MutableSet::class to 1,
        Map::class to 1, MutableMap::class to 2
    )

    val PrimitiveTypes = mapOf(
        Byte::class to 1, Short::class to 1, Int::class to 1, Long::class to 1,
        Float::class to 1, Double::class to 1,
        UByte::class to 1, UShort::class to 1, UInt::class to 1, ULong::class to 1,
        Boolean::class to 1,
        Char::class to 1,
        String::class to 1
    )

    val SingleTypeExpressions = listOf(
        Boolean::class to (Expression.SingleTypeExpression.Equality::class to 1),
        Boolean::class to (Expression.SingleTypeExpression.Comparison::class to 1),
        Boolean::class to (Expression.SingleTypeExpression.Disjunction::class to 1),
        Boolean::class to (Expression.SingleTypeExpression.Conjunction::class to 1)
    )

    val MultiTypeExpression = listOf(
        Expression.MultiTypeExpression.Constant::class to 1,
        Expression.MultiTypeExpression.If::class to 1,
        Expression.MultiTypeExpression.When::class to 1,
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
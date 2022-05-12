package com.stepanov.bbf.bodygenerator.contentgeneration

import com.stepanov.bbf.bodygenerator.BodyScope
import com.stepanov.bbf.bodygenerator.contentgeneration.Generation.createExpression
import com.stepanov.bbf.bugfinder.mutator.transformations.Factory
import org.jetbrains.kotlin.types.KotlinType

sealed class Declaration(scope: BodyScope, depth: Int, val type: KotlinType): ContentGeneration(scope, depth) {

    class PropertyDeclaration(scope: BodyScope, depth: Int, type: KotlinType, private val isVar: Boolean) : Declaration(scope, depth, type) {

        companion object {
            var propertyIndex: Int = 0
                get() {
                    field++
                    return field
                }
        }

        override fun generate(): String =
            Factory.psiFactory.createProperty(
                "property$propertyIndex",
                type.toString(),
                isVar,
                createExpression(scope, depth, type)
            ).let {
                scope.scopeTable.putProperty(it, type)
                it.text
            }
    }
}

package com.stepanov.bbf.bodygenerator.contentgeneration

import com.stepanov.bbf.bodygenerator.BodyScope
import com.stepanov.bbf.bodygenerator.TypeTable.userDefinedTypes
import com.stepanov.bbf.bodygenerator.Utils.generateUserDefinedTypePath
import com.stepanov.bbf.bodygenerator.contentgeneration.Generation.generateExpression
import com.stepanov.bbf.bugfinder.mutator.transformations.Factory
import org.jetbrains.kotlin.types.KotlinType


sealed class Declaration(scope: BodyScope, depth: Int, val type: KotlinType): Content(scope, depth) {

    class PropertyDeclaration(
        scope: BodyScope,
        depth: Int,
        type: KotlinType,
        private val isVar: Boolean
    ) : Declaration(scope, depth, type) {

        companion object {
            var propertyIndex: Int = 0
                get() {
                    field++
                    return field
                }
        }

        private val strType: String

        init {
            strType =
                if (userDefinedTypes.contains(type))
                    generateUserDefinedTypePath(type)
                else
                    type.toString()
        }

        override fun generate(): String =
            Factory.psiFactory.createProperty(
                "property$propertyIndex",
                strType,
                isVar,
                generateExpression(scope, depth, type)
            ).let {
                scope.scopeTable.putProperty(it, type)
                it.text
            }
    }
}

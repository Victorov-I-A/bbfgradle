package com.stepanov.bbf.bodygenerator

import com.intellij.psi.PsiElement
import com.stepanov.bbf.bodygenerator.Config.BlocksLevel
import com.stepanov.bbf.bodygenerator.Utils.ProjectTools.rig
import com.stepanov.bbf.bodygenerator.Utils.generatePath
import com.stepanov.bbf.bugfinder.mutator.transformations.util.ScopeCalculator
import com.stepanov.bbf.bugfinder.util.getFirstParentOfType
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.psi.KtClass


typealias ScopeComponent = ScopeCalculator.ScopeComponent

class BodyScope(val body: KtExpression, scopeCalculator: ScopeCalculator) {
    val scopeTable = ScopeTable(scopeCalculator.calcScope(body))

    fun getRandomPropertyByType(type: KotlinType): String? {
        val component = scopeTable.getComponentByType(type) ?: return null
        val expression = component.makeExpressionToInsertFromPsiElement(rig)!!.psiElement.text
        return if (scopeTable.outerScope.contains(component) &&
            component.psiElement.getFirstParentOfType<KtClass>() != body.getFirstParentOfType<KtClass>())
                (component.psiElement.getFirstParentOfType<KtClass>()?.generatePath() ?: "") + expression
        else
            expression
    }

    data class ScopeTable(
        val outerScope: List<ScopeComponent>,
        val innerScope: MutableList<MutableList<ScopeComponent>> = mutableListOf(mutableListOf())
    ) {
        private val maxLevel: Int = BlocksLevel
        private var curLevel: Int = 0
        fun putProperty(property: PsiElement, type: KotlinType) {
            innerScope.last().add(
                ScopeComponent(property, type.constructor.declarationDescriptor, type)
            )
        }

        fun addLevel(): Boolean {
            return if (curLevel == maxLevel)
                false
            else {
                innerScope.add(mutableListOf())
                curLevel++
                true
            }
        }

        fun deleteLevel(): Boolean {
            return if (curLevel == 0)
                false
            else {
                innerScope.removeLast()
                curLevel--
                true
            }
        }

        fun getComponentByType(type: KotlinType): ScopeComponent? = filterByType(type).randomOrNull()

        private fun filterByType(type: KotlinType): List<ScopeComponent> =
            (innerScope.flatten() + outerScope).filter { it.type == type }
    }
}
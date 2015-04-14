/*
 * Copyright 2010-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.idea.intentions

import org.jetbrains.kotlin.psi.JetNamedFunction
import com.intellij.openapi.editor.Editor
import org.jetbrains.kotlin.psi.JetTypeReference
import org.jetbrains.kotlin.idea.caches.resolve.analyze
import org.jetbrains.kotlin.idea.refactoring.changeSignature.runChangeSignature
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.idea.refactoring.changeSignature.JetChangeSignatureConfiguration
import org.jetbrains.kotlin.idea.refactoring.changeSignature.JetMethodDescriptor
import org.jetbrains.kotlin.idea.refactoring.changeSignature.modify

public class ConvertReceiverToParameterIntention: JetSelfTargetingOffsetIndependentIntention<JetTypeReference>(javaClass(), "Convert receiver to parameter") {
    override fun isApplicableTo(element: JetTypeReference): Boolean {
        return (element.getParent() as? JetNamedFunction)?.getReceiverTypeReference() == element
    }

    private fun configureChangeSignature(): JetChangeSignatureConfiguration {
        return object: JetChangeSignatureConfiguration {
            override fun configure(originalDescriptor: JetMethodDescriptor, bindingContext: BindingContext): JetMethodDescriptor {
                return originalDescriptor.modify { receiver = null }
            }
        }
    }

    override fun applyTo(element: JetTypeReference, editor: Editor) {
        val function = element.getParent() as? JetNamedFunction ?: return
        val context = function.analyze()
        val descriptor = context[BindingContext.DECLARATION_TO_DESCRIPTOR, function] as? FunctionDescriptor ?: return
        runChangeSignature(element.getProject(), descriptor, configureChangeSignature(), context, element, getText())
    }
}

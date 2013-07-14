/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 Jason Stedman
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.jasonstedman.extensions;

import java.util.ArrayList;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic.Kind;

/** 
 * This annotation processor examines methods marked {@link com.jasonstedman.extensions.AbstractForever}
 * 
 * @author Jason Stedman
 * @version 1.1
 * 
 */
@SupportedAnnotationTypes("com.jasonstedman.extensions.AbstractForever")
public class AbstractForeverAnnotationProcessor extends AbstractProcessor {

	@Override
	public boolean process(Set<? extends TypeElement> elements,
			RoundEnvironment roundEnv) {
		Set<? extends Element> rootElements = roundEnv.getRootElements();
		for (Element element : rootElements)
		{	
			TypeElement classElement = (TypeElement) element;
			// Ignore abstract subclasses
			if(!classElement.getModifiers().contains(Modifier.ABSTRACT)){
				
				ArrayList<ExecutableElement> abstractForeverMethods = new ArrayList<ExecutableElement>();
				
				// Climb the type hierarchy to the top and collect AbstractForever methods
				TypeMirror superClass = classElement.getSuperclass();
				while(superClass.getKind()!=TypeKind.NONE){
					TypeElement superClassElement = (TypeElement) processingEnv.getTypeUtils().
							asElement(superClass);
					collectAbstractForeverMethodsForClass(
							abstractForeverMethods, superClassElement);
					collectAbstractForeverMethodsForInterfaces(
							abstractForeverMethods, superClassElement);
					superClass = superClassElement.getSuperclass();
				}

				// Enforce all AbstractForever methods are implemented in the current class
				for(ExecutableElement abstractForeverMethod : abstractForeverMethods){
					boolean currentMethodIsOverridden = false;
					for(ExecutableElement implementedMethod : ElementFilter.methodsIn(
							classElement.getEnclosedElements())){
						if(isDeclaredMethodOverridingMethod(abstractForeverMethod, implementedMethod, classElement)){
							currentMethodIsOverridden = true;
							break;
						}
					}
					if(!currentMethodIsOverridden){
						error(classElement, "Class " + classElement.toString() + " does not override @AbstractForever method "+ abstractForeverMethod.toString() + " declared in superclass or interface "+abstractForeverMethod.getEnclosingElement().toString());
					}
				}
			}
		}
		return false;
	}

	private void collectAbstractForeverMethodsForInterfaces(
			ArrayList<ExecutableElement> abstractForeverMethods,
			TypeElement superClassElement) {
		for(TypeMirror intrface : superClassElement.getInterfaces()){
			TypeElement intrfaceElement = (TypeElement) processingEnv.getTypeUtils().asElement(intrface);
			collectAbstractForeverMethodsForClass(abstractForeverMethods,
					intrfaceElement);
		}
	}

	private void collectAbstractForeverMethodsForClass(
			ArrayList<ExecutableElement> abstractForeverMethods,
			TypeElement typeElement) {
		for(ExecutableElement e : ElementFilter.methodsIn(typeElement.getEnclosedElements())){
			if(e.getAnnotation(AbstractForever.class)!=null){
				abstractForeverMethods.add(e);
			}
		}
	}

	private boolean isDeclaredMethodOverridingMethod(ExecutableElement method,
			ExecutableElement declaredMethod, TypeElement type) {
		return processingEnv.getElementUtils().overrides(declaredMethod, method, type);
	}

	private void error(Element element, String message) {
		Messager messager = processingEnv.getMessager();
		messager.printMessage(
				Kind.ERROR, 
				message,
				element);
	}

}

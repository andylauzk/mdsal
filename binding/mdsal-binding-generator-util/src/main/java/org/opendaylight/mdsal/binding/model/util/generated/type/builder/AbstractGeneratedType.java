/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.util.generated.type.builder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.opendaylight.mdsal.binding.model.api.AnnotationType;
import org.opendaylight.mdsal.binding.model.api.Constant;
import org.opendaylight.mdsal.binding.model.api.Enumeration;
import org.opendaylight.mdsal.binding.model.api.GeneratedProperty;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.MethodSignature;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.type.builder.AnnotationTypeBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.EnumBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedPropertyBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.MethodSignatureBuilder;
import org.opendaylight.mdsal.binding.model.util.AbstractBaseType;

abstract class AbstractGeneratedType extends AbstractBaseType implements GeneratedType {

    private final Type parent;
    private final String comment;
    private final List<AnnotationType> annotations;
    private final List<Type> implementsTypes;
    private final List<Enumeration> enumerations;
    private final List<Constant> constants;
    private final List<MethodSignature> methodSignatures;
    private final List<GeneratedType> enclosedTypes;
    private final List<GeneratedProperty> properties;
    private final boolean isAbstract;

    public AbstractGeneratedType(final AbstractGeneratedTypeBuilder<?> builder) {
        super(builder.getPackageName(), builder.getName());
        this.parent = builder.getParent();
        this.comment = builder.getComment();
        this.annotations = toUnmodifiableAnnotations(builder.getAnnotations());
        this.implementsTypes = makeUnmodifiable(builder.getImplementsTypes());
        this.constants = makeUnmodifiable(builder.getConstants());
        this.enumerations = toUnmodifiableEnumerations(builder.getEnumerations());
        this.methodSignatures = toUnmodifiableMethods(builder.getMethodDefinitions());
        this.enclosedTypes = toUnmodifiableEnclosedTypes(builder.getEnclosedTypes(),
                builder.getEnclosedTransferObjects());
        this.properties = toUnmodifiableProperties(builder.getProperties());
        this.isAbstract = builder.isAbstract();
    }

    public AbstractGeneratedType(final Type parent, final String packageName, final String name, final String comment,
            final List<AnnotationTypeBuilder> annotationBuilders, final boolean isAbstract,
            final List<Type> implementsTypes, final List<GeneratedTypeBuilder> enclosedGenTypeBuilders,
            final List<GeneratedTOBuilder> enclosedGenTOBuilders, final List<EnumBuilder> enumBuilders,
            final List<Constant> constants, final List<MethodSignatureBuilder> methodBuilders,
            final List<GeneratedPropertyBuilder> propertyBuilders) {
        super(packageName, name);
        this.parent = parent;
        this.comment = comment;
        this.annotations = toUnmodifiableAnnotations(annotationBuilders);
        this.implementsTypes = makeUnmodifiable(implementsTypes);
        this.constants = makeUnmodifiable(constants);
        this.enumerations = toUnmodifiableEnumerations(enumBuilders);
        this.methodSignatures = toUnmodifiableMethods(methodBuilders);
        this.enclosedTypes = toUnmodifiableEnclosedTypes(enclosedGenTypeBuilders, enclosedGenTOBuilders);
        this.properties = toUnmodifiableProperties(propertyBuilders);
        this.isAbstract = isAbstract;
    }

    protected static final <T> List<T> makeUnmodifiable(final List<T> list) {
        switch (list.size()) {
        case 0:
            return Collections.emptyList();
        case 1:
            return Collections.singletonList(list.get(0));
        default:
            return Collections.unmodifiableList(list);
        }
    }

    private static List<GeneratedType> toUnmodifiableEnclosedTypes(final List<GeneratedTypeBuilder> enclosedGenTypeBuilders,
            final List<GeneratedTOBuilder> enclosedGenTOBuilders) {
        final ArrayList<GeneratedType> enclosedTypesList = new ArrayList<>(enclosedGenTypeBuilders.size() + enclosedGenTOBuilders.size());
        for (final GeneratedTypeBuilder builder : enclosedGenTypeBuilders) {
            if (builder != null) {
                enclosedTypesList.add(builder.toInstance());
            }
        }

        for (final GeneratedTOBuilder builder : enclosedGenTOBuilders) {
            if (builder != null) {
                enclosedTypesList.add(builder.toInstance());
            }
        }

        return makeUnmodifiable(enclosedTypesList);
    }

    protected static final List<AnnotationType> toUnmodifiableAnnotations(final List<AnnotationTypeBuilder> annotationBuilders) {
        final List<AnnotationType> annotationList = new ArrayList<>(annotationBuilders.size());
        for (final AnnotationTypeBuilder builder : annotationBuilders) {
            annotationList.add(builder.toInstance());
        }
        return makeUnmodifiable(annotationList);
    }

    protected final List<MethodSignature> toUnmodifiableMethods(final List<MethodSignatureBuilder> methodBuilders) {
        final List<MethodSignature> methods = new ArrayList<>(methodBuilders.size());
        for (final MethodSignatureBuilder methodBuilder : methodBuilders) {
            methods.add(methodBuilder.toInstance(this));
        }
        return makeUnmodifiable(methods);
    }

    protected final List<Enumeration> toUnmodifiableEnumerations(final List<EnumBuilder> enumBuilders) {
        final List<Enumeration> enums = new ArrayList<>(enumBuilders.size());
        for (final EnumBuilder enumBuilder : enumBuilders) {
            enums.add(enumBuilder.toInstance(this));
        }
        return makeUnmodifiable(enums);
    }

    protected final List<GeneratedProperty> toUnmodifiableProperties(final List<GeneratedPropertyBuilder> methodBuilders) {
        final List<GeneratedProperty> methods = new ArrayList<>(methodBuilders.size());
        for (final GeneratedPropertyBuilder methodBuilder : methodBuilders) {
            methods.add(methodBuilder.toInstance(this));
        }
        return makeUnmodifiable(methods);
    }

    @Override
    public final Type getParentType() {
        return this.parent;
    }

    @Override
    public final String getComment() {
        return this.comment;
    }

    @Override
    public final List<AnnotationType> getAnnotations() {
        return this.annotations;
    }

    @Override
    public final boolean isAbstract() {
        return this.isAbstract;
    }

    @Override
    public final List<Type> getImplements() {
        return this.implementsTypes;
    }

    @Override
    public final List<GeneratedType> getEnclosedTypes() {
        return this.enclosedTypes;
    }

    @Override
    public final List<Enumeration> getEnumerations() {
        return this.enumerations;
    }

    @Override
    public final List<Constant> getConstantDefinitions() {
        return this.constants;
    }

    @Override
    public final List<MethodSignature> getMethodDefinitions() {
        return this.methodSignatures;
    }

    @Override
    public final List<GeneratedProperty> getProperties() {
        return this.properties;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("GeneratedType [packageName=");
        builder.append(getPackageName());
        builder.append(", name=");
        builder.append(getName());
        if (this.parent != null) {
            builder.append(", parent=");
            builder.append(this.parent.getFullyQualifiedName());
        } else {
            builder.append(", parent=null");
        }
        builder.append(", comment=");
        builder.append(this.comment);
        builder.append(", annotations=");
        builder.append(this.annotations);
        builder.append(", enclosedTypes=");
        builder.append(this.enclosedTypes);
        builder.append(", enumerations=");
        builder.append(this.enumerations);
        builder.append(", constants=");
        builder.append(this.constants);
        builder.append(", methodSignatures=");
        builder.append(this.methodSignatures);
        builder.append("]");
        return builder.toString();
    }
}

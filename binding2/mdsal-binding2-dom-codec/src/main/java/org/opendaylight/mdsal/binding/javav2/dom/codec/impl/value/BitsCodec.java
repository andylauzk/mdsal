/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.dom.codec.impl.value;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.value.ValueTypeCodec.SchemaUnawareCodec;
import org.opendaylight.mdsal.binding.javav2.generator.util.JavaIdentifier;
import org.opendaylight.mdsal.binding.javav2.generator.util.JavaIdentifierNormalizer;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition.Bit;

@Beta
final class BitsCodec extends ReflectionBasedCodec implements SchemaUnawareCodec {

    private static final MethodType CONSTRUCTOR_INVOKE_TYPE = MethodType.methodType(Object.class, Boolean[].class);
    // Ordered by position
    private final Map<String, Method> getters;
    // Ordered by lexical name
    private final Set<String> ctorArgs;
    private final MethodHandle ctor;

    private BitsCodec(final Class<?> typeClass, final MethodHandle ctor, final Set<String> ctorArgs,
            final Map<String, Method> getters) {

        super(typeClass);
        this.ctor = Preconditions.checkNotNull(ctor);
        this.ctorArgs = ImmutableSet.copyOf(ctorArgs);
        this.getters = ImmutableMap.copyOf(getters);
    }

    static Callable<BitsCodec> loader(final Class<?> returnType, final BitsTypeDefinition rootType) {
        return () -> {
            final Map<String, Method> getters = new LinkedHashMap<>();
            final Set<String> ctorArgs = new TreeSet<>();

            for (Bit bit : rootType.getBits()) {
                final Method valueGetter = returnType.getMethod("is" + JavaIdentifierNormalizer
                        .normalizeSpecificIdentifier(bit.getName(), JavaIdentifier.CLASS));
                ctorArgs.add(bit.getName());
                getters.put(bit.getName(), valueGetter);
            }
            Constructor<?> constructor = null;
            for (Constructor<?> cst : returnType.getConstructors()) {
                if (!cst.getParameterTypes()[0].equals(returnType)) {
                    constructor = cst;
                }
            }

            final MethodHandle ctor = MethodHandles.publicLookup().unreflectConstructor(constructor)
                    .asSpreader(Boolean[].class, ctorArgs.size()).asType(CONSTRUCTOR_INVOKE_TYPE);
            return new BitsCodec(returnType, ctor, ctorArgs, getters);
        };
    }

    @Override
    public Object deserialize(Object input) {
        Preconditions.checkArgument(input instanceof Set);
        @SuppressWarnings("unchecked")
        final Set<String> casted = (Set<String>) input;

        /*
         * We can do this walk based on field set sorted by name,
         * since constructor arguments in Java Binding are sorted by name.
         *
         * This means we will construct correct array for construction
         * of bits object.
         */
        final Boolean args[] = new Boolean[ctorArgs.size()];
        int currentArg = 0;
        for (String value : ctorArgs) {
            args[currentArg++] = casted.contains(value);
        }

        try {
            return ctor.invokeExact(args);
        } catch (Throwable e) {
            throw new IllegalStateException("Failed to instantiate object for " + input, e);
        }
    }

    @Override
    public Object serialize(Object input) {
        final Collection<String> result = new ArrayList<>(getters.size());
        for (Entry<String, Method> valueGet : getters.entrySet()) {
            final Boolean value;
            try {
                value = (Boolean) valueGet.getValue().invoke(input);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new IllegalArgumentException("Failed to get bit " + valueGet.getKey(), e);
            }

            if (value) {
                result.add(valueGet.getKey());
            }
        }
        return result.size() == getters.size() ? getters.keySet() : ImmutableSet.copyOf(result);
    }
}

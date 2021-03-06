/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.spec.util;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;
import org.opendaylight.mdsal.binding.javav2.spec.structural.Augmentation;
import org.opendaylight.mdsal.binding.javav2.spec.structural.AugmentationHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Beta
abstract class AugmentationFieldGetter {

    private static final Logger LOG = LoggerFactory.getLogger(AugmentationFieldGetter.class);

    private static final String AUGMENTATION_FIELD = "augmentation";

    private static final AugmentationFieldGetter DUMMY = new AugmentationFieldGetter() {
        @Override
        protected Map<Class<? extends Augmentation<?>>, Augmentation<?>> getAugmentations(final Object input) {
            return Collections.emptyMap();
        }
    };

    private static final AugmentationFieldGetter AUGMENTATION_HOLDER_GETTER = new AugmentationFieldGetter() {

        @Override
        @SuppressWarnings({ "unchecked", "rawtypes" })
        protected Map<Class<? extends Augmentation<?>>, Augmentation<?>> getAugmentations(final Object input) {
            return (Map) ((AugmentationHolder<?>) input).augmentations();
        }
    };

    private static final LoadingCache<Class<?>, AugmentationFieldGetter> AUGMENTATION_GETTERS =
            CacheBuilder.newBuilder().weakKeys().build(new AugmentationGetterLoader());

    /**
     * Retrieves augmentations from supplied object.
     *
     * @param input
     *            - input Data object, from which augmentations should be
     *            extracted
     * @return Map of Augmentation class to augmentation
     */
    protected abstract Map<Class<? extends Augmentation<?>>, Augmentation<?>> getAugmentations(final Object input);

    public static AugmentationFieldGetter getGetter(final Class<? extends Object> clz) {
        if (AugmentationHolder.class.isAssignableFrom(clz)) {
            return AUGMENTATION_HOLDER_GETTER;
        }
        return AUGMENTATION_GETTERS.getUnchecked(clz);
    }

    private static final class AugmentationGetterLoader extends CacheLoader<Class<?>, AugmentationFieldGetter> {
        private static final MethodType GETTER_TYPE = MethodType.methodType(Map.class, Object.class);
        private static final Lookup LOOKUP = MethodHandles.lookup();

        @Override
        public AugmentationFieldGetter load(final Class<?> key) throws IllegalAccessException {
            final Field field;
            try {
                field = key.getDeclaredField(AUGMENTATION_FIELD);
                field.setAccessible(true);
            } catch (NoSuchFieldException | SecurityException e) {
                LOG.warn("Failed to acquire augmentation field {}, ignoring augmentations in class {}",
                        AUGMENTATION_FIELD, key, e);
                return DUMMY;
            }
            if (!Map.class.isAssignableFrom(field.getType())) {
                LOG.warn("Class {} field {} is not a Map, ignoring augmentations", key,
                        AUGMENTATION_FIELD);
                return DUMMY;
            }

            return new ReflectionAugmentationFieldGetter(LOOKUP.unreflectGetter(field).asType(GETTER_TYPE));
        }
    }

    private static final class ReflectionAugmentationFieldGetter extends AugmentationFieldGetter {
        private final MethodHandle fieldGetter;

        ReflectionAugmentationFieldGetter(final MethodHandle mh) {
            this.fieldGetter = Preconditions.checkNotNull(mh);
        }

        @Override
        protected Map<Class<? extends Augmentation<?>>, Augmentation<?>> getAugmentations(final Object input) {
            try {
                return (Map<Class<? extends Augmentation<?>>, Augmentation<?>>) this.fieldGetter.invokeExact(input);
            } catch (final Throwable e) {
                throw new IllegalStateException("Failed to access augmentation field on " + input, e);
            }
        }
    }
}

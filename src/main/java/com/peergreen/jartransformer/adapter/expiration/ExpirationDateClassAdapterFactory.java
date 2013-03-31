/**
 * Copyright 2013 Peergreen S.A.S.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.peergreen.jartransformer.adapter.expiration;

import org.objectweb.asm.ClassVisitor;

import com.peergreen.jartransformer.ClassAdapterFactory;

/**
 * Expiration date class adapter factory
 * @author Florent Benoit
 */
public class ExpirationDateClassAdapterFactory implements ClassAdapterFactory {

    /**
     * Expiration date.
     */
    private final long expirationDate;

    public ExpirationDateClassAdapterFactory(long expirationDate) {
        this.expirationDate = expirationDate;
    }

    @Override
    public ClassVisitor build(int apiLevel, ClassVisitor delegatingClassVisitor) {
       return new ExpirationDateClassAdapter(apiLevel, delegatingClassVisitor, expirationDate);
    }

}

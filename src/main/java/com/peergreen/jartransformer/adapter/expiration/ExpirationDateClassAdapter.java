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
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * Expiration date class adapter.
 * @author Florent Benoit
 */
public class ExpirationDateClassAdapter extends ClassVisitor implements Opcodes {

    private final long expirationDate;
    private boolean isClass = true;
    private boolean visitedStaticMethod = false;


    public ExpirationDateClassAdapter(int apiLevel, final ClassVisitor cv, long expirationDate) {
        super(apiLevel, cv);
        this.expirationDate = expirationDate;
    }



    /**
     * Visits the header of the class.
     *
     * @param version
     *            the class version.
     * @param access
     *            the class's access flags (see {@link Opcodes}). This parameter
     *            also indicates if the class is deprecated.
     * @param name
     *            the internal name of the class (see
     *            {@link Type#getInternalName() getInternalName}).
     * @param signature
     *            the signature of this class. May be <tt>null</tt> if the class
     *            is not a generic one, and does not extend or implement generic
     *            classes or interfaces.
     * @param superName
     *            the internal of name of the super class (see
     *            {@link Type#getInternalName() getInternalName}). For
     *            interfaces, the super class is {@link Object}. May be
     *            <tt>null</tt>, but only for the {@link Object} class.
     * @param interfaces
     *            the internal names of the class's interfaces (see
     *            {@link Type#getInternalName() getInternalName}). May be
     *            <tt>null</tt>.
     */
    @Override
    public void visit(int version, int access, String name, String signature,
            String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);


        // Enum class
        if ((access & ACC_ENUM) != 0) {
            this.isClass = false;
        } else if ((access & ACC_ANNOTATION) != 0) {
            // Annotation class
            this.isClass = false;
        } else if ((access & ACC_INTERFACE) != 0) {
            // interface class
            this.isClass = false;
        }

    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc,
            String signature, String[] exceptions) {
        if (isClass) {
            // Static initializer, add the expiration code
            if ("<clinit>".equals(name) && "()V".equals(desc)) {
                visitedStaticMethod = true;
                MethodVisitor mv =  super.visitMethod(access, name, desc, signature, exceptions);
                return new ExpirationDateMethodAdapter(mv, expirationDate);
            }
        }
        return super.visitMethod(access, name, desc, signature, exceptions);

    }

    /**
     * Visits the end of the class. This method, which is the last one to be
     * called, is used to inform the visitor that all the fields and methods of
     * the class have been visited.
     */
    @Override
    public void visitEnd() {
        if (isClass && !visitedStaticMethod) {
            // add static block if it doesn't exists
            MethodVisitor mv = cv.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
            mv = new ExpirationDateMethodAdapter(mv, expirationDate);
            mv.visitCode();
            mv.visitInsn(RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        super.visitEnd();
    }


}

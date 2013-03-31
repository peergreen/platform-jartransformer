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

import static org.objectweb.asm.Opcodes.ASM4;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.F_SAME;
import static org.objectweb.asm.Opcodes.IFLE;
import static org.objectweb.asm.Opcodes.IFLT;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.LCMP;
import static org.objectweb.asm.Opcodes.NEW;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Expiration date method adapter.
 * @author Florent Benoit
 */
public class ExpirationDateMethodAdapter extends MethodVisitor {

    /**
     * Expiration date.
     */
    private final long expirationDate;

    /**
     * Constructs a new AddMethodConstructorAdapter object.
     * @param mv the code visitor to which this adapter must delegate calls.
     */
    public ExpirationDateMethodAdapter(final MethodVisitor mv, long expirationDate) {
        super(ASM4, mv);
        this.expirationDate = expirationDate;
    }

    /**
     * Adds instruction just after the start of the method code.
     */
    @Override
    public void visitCode() {

        // Adds the following code
        // static {
        //    if (System.currentTimeMillis() < "NOW" || System.currentTimeMillis() > 1364376812157L) {
        //        throw new IllegalStateException("This Peergreen Server has expired. Please download a new version on http://www.peergreen.com");
        //    }
        // }

        mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J");
        mv.visitLdcInsn(System.currentTimeMillis());
        mv.visitInsn(LCMP);
        Label l1 = new Label();
        mv.visitJumpInsn(IFLT, l1);
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J");
        mv.visitLdcInsn(Long.valueOf(expirationDate));
        mv.visitInsn(LCMP);
        Label l2 = new Label();
        mv.visitJumpInsn(IFLE, l2);
        mv.visitLabel(l1);
        mv.visitFrame(F_SAME, 0, null, 0, null);
        mv.visitTypeInsn(NEW, "java/lang/IllegalStateException");
        mv.visitInsn(DUP);
        mv.visitLdcInsn("The Peergreen Server has expired. Please download a new version on http://www.peergreen.com");
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/IllegalStateException", "<init>", "(Ljava/lang/String;)V");
        mv.visitInsn(ATHROW);
        mv.visitLabel(l2);
        mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        super.visitCode();
    }


}

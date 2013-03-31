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
package com.peergreen.jartransformer;

import static org.objectweb.asm.Opcodes.ASM4;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

/**
 * Exemple :
 * JarTransformer jarTransformer = new JarTransformer();
 *  jarTransformer.addClassAdapterFactory(new ExpirationDateClassAdapterFactory(123));
 *  jarTransformer.transform(new File("/tmp/in.jar"), new File("/tmp/out.jar"));
 */

/**
 * Transforms a jar into a new jar by applying transformation on the given bytecode.
 * @author Florent Benoit
 */
public class JarTransformer {

    private static final int BUFFER = 4096;
    private static final String CLASS_EXTENSION = ".class";

    private final List<ClassAdapterFactory> classAdapterFactories;


    public JarTransformer() {
        this.classAdapterFactories = new ArrayList<>();
    }


    public void addClassAdapterFactory(ClassAdapterFactory classAdapterFactory) {
        classAdapterFactories.add(classAdapterFactory);
    }


    public void transform(File in, File out) throws JarTransformerException {
        // First, open the file
        try (ZipFile zipFile = new ZipFile(in); FileOutputStream fileOutputStream = new FileOutputStream(out); ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream)) {

            // Get all entries
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            // Scan each entry
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();

                byte[] transformedBytes = null;

                // Transform only class
                if (zipEntry.getName().endsWith(CLASS_EXTENSION)) {
                    // transform the bytecode
                    try (InputStream inputStream = zipFile.getInputStream(zipEntry)) {
                        ClassReader classReader = new ClassReader(inputStream);
                        ClassWriter cw = new CustomClassLoaderClassWriter(JarTransformer.class.getClassLoader());
                        ClassVisitor cv = null;
                        for (ClassAdapterFactory classAdapterFactory : classAdapterFactories) {
                            if (cv == null) {
                                cv = classAdapterFactory.build(ASM4, cw);
                            } else {
                                cv = classAdapterFactory.build(ASM4, cv);
                            }
                        }
                        classReader.accept(cv, 0);
                        transformedBytes = cw.toByteArray();
                    }
                }


                // Write entry to the output stream
                ZipEntry writingZipEntry = new ZipEntry(zipEntry.getName());
                zipOutputStream.putNextEntry(writingZipEntry);

                // transformed bytes ?
                if (transformedBytes != null) {
                    zipOutputStream.write(transformedBytes);
                } else {
                    // write normal stream
                    try (InputStream inputStream = zipFile.getInputStream(zipEntry)) {
                        byte[] b = new byte[BUFFER];

                        int len;
                        while ((len = inputStream.read(b, 0, b.length)) != -1) {
                            zipOutputStream.write(b, 0, len);
                        }

                    }

                }

            }
        } catch (IOException e) {
            throw new JarTransformerException(String.format("Unable to transform the jar file %s", in), e);
        }

    }

}

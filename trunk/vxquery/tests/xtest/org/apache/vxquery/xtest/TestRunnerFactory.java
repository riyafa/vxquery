/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.vxquery.xtest;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.vxquery.api.InternalAPI;
import org.apache.vxquery.compiler.expression.GlobalVariable;
import org.apache.vxquery.datamodel.XDMItem;
import org.apache.vxquery.datamodel.serialization.XMLSerializer;
import org.apache.vxquery.runtime.base.OpenableCloseableIterator;
import org.apache.vxquery.runtime.core.Deflater;
import org.apache.vxquery.xmlquery.ast.ModuleNode;
import org.apache.vxquery.xmlquery.query.Module;
import org.apache.vxquery.xmlquery.query.PrologVariable;

public class TestRunnerFactory {
    private List<ResultReporter> reporters;

    public TestRunnerFactory() {
        reporters = new ArrayList<ResultReporter>();
    }

    public void registerReporter(ResultReporter reporter) {
        reporters.add(reporter);
    }

    public Runnable createRunner(final TestCase testCase) {
        return new Runnable() {
            @Override
            public void run() {
                TestCaseResult res = new TestCaseResult(testCase);
                long start = System.currentTimeMillis();
                try {
                    InternalAPI iapi = new InternalAPI();
                    FileReader in = new FileReader(testCase.getXQueryFile());
                    ModuleNode ast;
                    try {
                        ast = iapi.parse(testCase.getXQueryDisplayName(), in);
                    } finally {
                        in.close();
                    }
                    Module module = iapi.compile(null, ast);
                    for (PrologVariable pVar : module.getPrologVariables()) {
                        GlobalVariable var = pVar.getVariable();
                        QName varName = var.getName();
                        iapi.bindExternalVariable(var, testCase.getExternalVariableBinding(varName));
                    }
                    OpenableCloseableIterator ri = iapi.execute(module);
                    ri.open();
                    XDMItem o;
                    Deflater deflater = new Deflater();
                    deflater.reset(ri);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(baos)), true);
                    XMLSerializer s = new XMLSerializer(out, false);
                    try {
                        while ((o = (XDMItem) deflater.next()) != null) {
                            s.item(o);
                        }
                    } finally {
                        out.flush();
                        deflater.close();
                    }
                    try {
                        res.result = baos.toString("UTF-8");
                        res.compare();
                    } catch (Exception e) {
                        System.err.println("Framework error");
                        e.printStackTrace();
                    }
                } catch (Throwable e) {
                    res.error = e;
                } finally {
                    long end = System.currentTimeMillis();
                    res.time = end - start;
                    for (ResultReporter r : reporters) {
                        try {
                            r.reportResult(res);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
    }

    public void registerReporters(Collection<ResultReporter> reporters) {
        this.reporters.addAll(reporters);
    }
}
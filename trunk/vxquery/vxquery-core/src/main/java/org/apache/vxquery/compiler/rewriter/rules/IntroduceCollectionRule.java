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
package org.apache.vxquery.compiler.rewriter.rules;

import org.apache.commons.lang3.mutable.Mutable;
import org.apache.vxquery.compiler.rewriter.VXQueryOptimizationContext;
import org.apache.vxquery.metadata.VXQueryCollectionDataSource;

import edu.uci.ics.hyracks.algebricks.common.exceptions.AlgebricksException;
import edu.uci.ics.hyracks.algebricks.core.algebra.base.ILogicalOperator;
import edu.uci.ics.hyracks.algebricks.core.algebra.base.IOptimizationContext;
import edu.uci.ics.hyracks.algebricks.core.algebra.operators.logical.AbstractLogicalOperator;
import edu.uci.ics.hyracks.algebricks.core.algebra.operators.logical.AssignOperator;
import edu.uci.ics.hyracks.algebricks.core.algebra.operators.logical.DataSourceScanOperator;
import edu.uci.ics.hyracks.algebricks.core.algebra.operators.logical.UnnestOperator;

public class IntroduceCollectionRule extends AbstractCollectionRule {
    /**
     * Find the default query plan created for collection and updated it to use parallelization.
     * The following is an example of of the operators we are looking with a constant for the collection name.
     * Search pattern: unnest <- assign [function-call: collection] <- assign [constant: string]
     */
    @Override
    public boolean rewritePre(Mutable<ILogicalOperator> opRef, IOptimizationContext context) throws AlgebricksException {
        VXQueryOptimizationContext vxqueryContext = (VXQueryOptimizationContext) context;
        String collectionName = getCollectionName(opRef);

        if (collectionName != null) {
            // Build the new operator and update the query plan.
            VXQueryCollectionDataSource ds = vxqueryContext.getCollectionDataSourceMap(collectionName);
            if (ds != null) {
                ds.setTotalDataSources(vxqueryContext.getCollectionDataSourceMapSize());

                // Known to be true because of collection name.
                AbstractLogicalOperator op = (AbstractLogicalOperator) opRef.getValue();
                UnnestOperator unnest = (UnnestOperator) op;
                AbstractLogicalOperator op2 = (AbstractLogicalOperator) unnest.getInputs().get(0).getValue();
                AssignOperator assign = (AssignOperator) op2;

                DataSourceScanOperator opNew = new DataSourceScanOperator(unnest.getVariables(), ds);
                opNew.getInputs().addAll(assign.getInputs());
                opRef.setValue(opNew);
                return true;
            }
        }
        return false;
    }

}
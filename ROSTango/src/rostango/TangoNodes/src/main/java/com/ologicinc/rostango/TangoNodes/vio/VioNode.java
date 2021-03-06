/*
 * Copyright (C) 2014 OLogic, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.ologicinc.rostango.TangoNodes.vio;

import com.motorola.atap.androidvioservice.VinsServiceHelper;

import org.ros.concurrent.CancellableLoop;
import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeMain;

/**
 * Created by rohan on 7/8/14.
 */
public class VioNode implements NodeMain {

    private VinsServiceHelper mVinsServiceHelper;

    private TangoOdomPublisher mTangoOdomPublisher;
    private TangoPosePublisher mTangoPosePublisher;
    private TangoTfPublisher mTangoTfPublisher;

    public VioNode(VinsServiceHelper vins){
        mVinsServiceHelper = vins;
    }

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("tango_vio");
    }

    @Override
    public void onStart(ConnectedNode node) {
        mTangoOdomPublisher = new TangoOdomPublisher(node);
        mTangoPosePublisher = new TangoPosePublisher(node);
        mTangoTfPublisher = new TangoTfPublisher(node);

        node.executeCancellableLoop(new CancellableLoop() {
           @Override
            protected void loop() throws InterruptedException {
               Thread.sleep(30);
               final double[] posState = mVinsServiceHelper.getStateInFullStateFormat();
               final double[] rotState = mVinsServiceHelper.getStateInUnityFormat();
               // Generate the TF message

               updateTranslation(posState);
               Thread.sleep(30);

               updateRoataion(rotState);
               Thread.sleep(30);

               mTangoOdomPublisher.publishOdom();
               mTangoPosePublisher.publishPose();
               mTangoTfPublisher.publishTransforms();
            }
        });
    }

    @Override
    public void onShutdown(Node node) {
        mVinsServiceHelper.shutdown();
    }

    @Override
    public void onShutdownComplete(Node node) {
    }

    @Override
    public void onError(Node node, Throwable throwable) {
    }

    public void updateTranslation(double[] state) {
        mTangoOdomPublisher.setPosePoint(state[5],-state[4], state[6]);
        mTangoPosePublisher.setPoint(state[5],-state[4], state[6]);
        mTangoTfPublisher.setTranslation(state[5],-state[4], state[6]);
    }

    public void updateRoataion(double[] state) {
        mTangoOdomPublisher.setPoseQuat(-state[2], state[0], -state[1], state[3]);
        mTangoPosePublisher.setQuat(-state[2],state[0],-state[1],state[3]);
        mTangoTfPublisher.setRotation(-state[2],state[0],-state[1],state[3]);
    }
}

package com.microsoft.applicationinsights.smoketestapp;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Predicate;
import com.microsoft.applicationinsights.internal.schemav2.Data;
import com.microsoft.applicationinsights.internal.schemav2.Envelope;
import com.microsoft.applicationinsights.internal.schemav2.RemoteDependencyData;
import com.microsoft.applicationinsights.internal.schemav2.RequestData;
import com.microsoft.applicationinsights.smoketest.AiSmokeTest;
import com.microsoft.applicationinsights.smoketest.DependencyContainer;
import com.microsoft.applicationinsights.smoketest.TargetUri;
import com.microsoft.applicationinsights.smoketest.UseAgent;
import com.microsoft.applicationinsights.smoketest.WithDependencyContainers;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@UseAgent
@WithDependencyContainers(
        @DependencyContainer(
                value = "mongo:4",
                portMapping = "27017",
                hostnameEnvironmentVariable = "MONGO")
)
public class MongoSmokeTest extends AiSmokeTest {

    @Test
    @TargetUri("/mongo")
    public void mongo() throws Exception {
        List<Envelope> rdList = mockedIngestion.waitForItems("RequestData", 1);
        List<Envelope> rddList = mockedIngestion.waitForItems("RemoteDependencyData", 1);

        Envelope rdEnvelope = rdList.get(0);
        Envelope rddEnvelope = rddList.get(0);

        RequestData rd = (RequestData) ((Data) rdEnvelope.getData()).getBaseData();
        RemoteDependencyData rdd = (RemoteDependencyData) ((Data) rddEnvelope.getData()).getBaseData();

        assertTrue(rd.getSuccess());
        assertEquals("MongoDB", rdd.getType());
        assertEquals("MongoDB", rdd.getName());
        assertEquals("find testdb.test", rdd.getData());
        assertTrue(rdd.getSuccess());

        assertSameOperationId(rdEnvelope, rddEnvelope);
    }

    private static void assertSameOperationId(Envelope rdEnvelope, Envelope rddEnvelope) {
        String operationId = rdEnvelope.getTags().get("ai.operation.id");
        String operationParentId = rdEnvelope.getTags().get("ai.operation.parentId");

        assertNotNull(operationId);
        assertNotNull(operationParentId);

        assertEquals(operationId, rddEnvelope.getTags().get("ai.operation.id"));
        assertEquals(operationParentId, rddEnvelope.getTags().get("ai.operation.parentId"));
    }
}

package com.k8s.network.controller.backend.controller;


import com.k8s.network.controller.backend.model.networking.NetworkPolicyPattern;
import com.k8s.network.controller.backend.model.networking.NetworkPolicyPeer;
import com.k8s.network.controller.backend.model.networking.NetworkPolicyRule;
import io.restassured.http.ContentType;
import io.restassured.specification.ResponseSpecification;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;

import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.List;

import static com.k8s.network.controller.backend.CommonConfiguration.BASE_URL;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

public class NetworkPolicyControllerTest {
    private static final String NETWORK_POLICY_PATH = "/networkPolicy";
    private static final String TEST_NAMESPACE = "default";

    @Test
    void getNetworkPolicy() {
        List<NetworkPolicyPattern> list = given()
                .baseUri(BASE_URL)
                .expect()
                .statusCode(HttpURLConnection.HTTP_OK)
                .when()
                .get(NETWORK_POLICY_PATH)
                .jsonPath()
                .getList(".", NetworkPolicyPattern.class);
        assertFalse(list.isEmpty());
        NetworkPolicyPattern testPolicy = list.stream()
                .filter(policy -> "test-network-policy".equals(policy.getName()))
                .findFirst()
                .orElseThrow(IllegalStateException::new);
        assertEquals("db", testPolicy.getPodSelectorLabels().get("role"));
    }

    @Test
    void createNetworkPolicy() {
        String policyName = "create-policy-test";
        delete(TEST_NAMESPACE, policyName, true);

        NetworkPolicyPattern testPolicy = createTestPolicy(TEST_NAMESPACE, policyName);
        NetworkPolicyPattern createdNetworkPolicy = given()
                .baseUri(BASE_URL)
                .body(testPolicy)
                .contentType(ContentType.JSON)
                .expect()
                .statusCode(HttpURLConnection.HTTP_OK)
                .when()
                .post(NETWORK_POLICY_PATH)
                .as(NetworkPolicyPattern.class);

        assertNotNull(createdNetworkPolicy);
    }

    @Test
    void updateNetworkPolicy() {
        String policyName = "update-policy-test";
        String updatedDescription = "Updated description";
        delete(TEST_NAMESPACE, policyName, true);

        NetworkPolicyPattern testPolicy = createTestPolicy(TEST_NAMESPACE, policyName);
        NetworkPolicyPattern createdNetworkPolicy = given()
                .baseUri(BASE_URL)
                .body(testPolicy)
                .contentType(ContentType.JSON)
                .expect()
                .statusCode(HttpURLConnection.HTTP_OK)
                .when()
                .post(NETWORK_POLICY_PATH)
                .as(NetworkPolicyPattern.class);
        createdNetworkPolicy.setDescription(updatedDescription);

        NetworkPolicyPattern updatedNetworkPolicy = given()
                .baseUri(BASE_URL)
                .body(createdNetworkPolicy)
                .contentType(ContentType.JSON)
                .expect()
                .statusCode(HttpURLConnection.HTTP_OK)
                .when()
                .post(NETWORK_POLICY_PATH)
                .as(NetworkPolicyPattern.class);
        assertEquals(updatedDescription, updatedNetworkPolicy.getDescription());
    }

    @Test
    void deleteNetworkPolicy() {
        String policyName = "delete-policy-test";
        delete(TEST_NAMESPACE, policyName, true);

        NetworkPolicyPattern testPolicy = createTestPolicy(TEST_NAMESPACE, policyName);
        given()
                .baseUri(BASE_URL)
                .body(testPolicy)
                .contentType(ContentType.JSON)
                .expect()
                .statusCode(HttpURLConnection.HTTP_OK)
                .when()
                .post(NETWORK_POLICY_PATH)
                .as(NetworkPolicyPattern.class);

        delete(TEST_NAMESPACE, policyName, false);
    }


    private static void delete(String namespace, String name, boolean skipNotFound) {
        given()
                .baseUri(BASE_URL)
                .queryParam("namespace", namespace)
                .queryParam("name", name)
                .expect()
                .statusCode(new Matcher<Integer>() {
                    @Override
                    public boolean matches(Object o) {
                        if(o instanceof Integer) {
                            int code = (Integer) o;
                            return code == HttpURLConnection.HTTP_OK ||
                                    (skipNotFound && code == HttpURLConnection.HTTP_NOT_FOUND);
                        }
                        return false;
                    }

                    @Override
                    public void describeMismatch(Object o, Description description) {

                    }

                    @Override
                    public void _dont_implement_Matcher___instead_extend_BaseMatcher_() {

                    }

                    @Override
                    public void describeTo(Description description) {

                    }
                })
                .when()
                .delete(NETWORK_POLICY_PATH);
    }

    private static NetworkPolicyPattern createTestPolicy(String namespace, String name) {
        NetworkPolicyRule egressRule = new NetworkPolicyRule();
        NetworkPolicyPeer peer = new NetworkPolicyPeer();
        peer.setNamespaceName(namespace);
        peer.setPodSelectorLabels(Collections.singletonMap("role", "backend"));
        egressRule.setSelector(peer);

        NetworkPolicyPattern testPolicy = new NetworkPolicyPattern();
        testPolicy.setName(name);
        testPolicy.setNamespace(namespace);
        testPolicy.setIssue("JIRA-1313");
        testPolicy.setDescription("Simple test policy");
        testPolicy.setPodSelectorLabels(Collections.singletonMap("role", "frontend"));
        testPolicy.setEgress(egressRule);

        return testPolicy;
    }
}

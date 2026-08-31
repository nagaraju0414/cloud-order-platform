package com.cloudorder.order.service;

import com.google.cloud.secretmanager.v1.AccessSecretVersionRequest;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class SecretService {

    public boolean secretExists() throws IOException {

        String projectId = "cloud-order-platform";
        String secretId = "order-service-secret";

        try (SecretManagerServiceClient client =
                     SecretManagerServiceClient.create()) {

            SecretVersionName secretVersion =
                    SecretVersionName.of(projectId, secretId, "latest");

            client.accessSecretVersion(
                    AccessSecretVersionRequest.newBuilder()
                            .setName(secretVersion.toString())
                            .build());

            return true;
        }
    }
}
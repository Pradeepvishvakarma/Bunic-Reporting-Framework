package com.bunic.reportingframework.user.model;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Document(collection = "User")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    private String userId;
    private String name;
    private String emailId;
    private String accessLevel;
    private Map<String, List<String>> entitlementAccess;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("User{");
        sb.append("userId='").append(userId).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", emailId='").append(emailId).append('\'');
        sb.append(", accessLevel='").append(accessLevel).append('\'');
        sb.append(", entitlementAccess=").append(entitlementAccess);
        sb.append('}');
        return sb.toString();
    }
}

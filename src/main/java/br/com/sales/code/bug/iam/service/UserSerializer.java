package br.com.sales.code.bug.iam.service;

import br.com.sales.code.bug.iam.domain.Permission;
import br.com.sales.code.bug.iam.domain.Role;
import br.com.sales.code.bug.iam.domain.User;
import br.com.sales.code.bug.iam.domain.exception.DomainException;
import br.com.sales.code.bug.iam.domain.exception.RepositoryPersistenceException;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserSerializer {

    private final ObjectSerializer serializer;

    public UserSerializer(ObjectSerializer serializer) {
        this.serializer = serializer;
    }

    public String serializeUsers(List<User> users) {

        var value = serializer.initList();

        if( users != null && !users.isEmpty()) {

            value += serializer.keepLine();
            var usersIterator = users.iterator();
            while(usersIterator.hasNext()) {

                var user = usersIterator.next();
                value += serializer.initObject();

                var idValue = serializer.buildFieldString("id", user.getId().toString());
                var usernameValue = serializer.buildFieldString("username", user.getUsername());
                var emailValue = serializer.buildFieldString("email", user.getEmail());
                var passValue = serializer.buildFieldString("password", user.getPasswordHash());
                var status = serializer.buildFieldString("status", user.getStatus().name());

                var rolesValue = serializer.buildFieldObject("roles", this.serializerRoles(user.getRoles()));

                value += serializer.linkObjects(idValue, usernameValue);
                value = serializer.linkObjects(value, emailValue);
                value = serializer.linkObjects(value, passValue);
                value = serializer.linkObjects(value, status);
                value = serializer.linkObjects(value, rolesValue);

                value = serializer.endObject(value);

                if (usersIterator.hasNext())
                    value = serializer.linkObjects(value, "\n");
            }
        }

        value += serializer.keepLine();

        return serializer.endList(value);
    }

    private String serializerRoles(Set<Role> roles) {

        if (roles == null || roles.isEmpty())
            return "[ ]";

        var setRolesJson = new HashSet<String>();

        for(Role role : roles) {

            var value = serializer.initObject();

            var nameValue = serializer.buildFieldString("name", role.getName());

            var permissionsValue = serializer.buildFieldObject("permissions", this.serializerPermissions(role.getPermissions()));

            value += serializer.linkObjects(nameValue, permissionsValue);

            setRolesJson.add(serializer.endObject(value));
        }

        return serializer.buildList(setRolesJson);
    }

    private String serializerPermissions(Set<Permission> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return "[ ]";
        }

        var setPermissionsValues = new HashSet<String>();
        var permissionsIterator = permissions.iterator();

        while (permissionsIterator.hasNext()) {
            var permissionValue = serializer.initObject();

            var value = permissionValue.concat(serializer.buildFieldString("value", permissionsIterator.next().value()));

            setPermissionsValues.add(serializer.endObject(value));
        }

        return serializer.buildList(setPermissionsValues);
    }

    public User deserializeUser(String json) {

        var idValue = this.extractValue(json, "id");
        var usernameValue = this.extractValue(json, "username");
        var emailValue = this.extractValue(json, "email");
        var passValue = this.extractValue(json, "password");
        var statusValue = this.extractValue(json, "status");
        var rolesValue = this.extractObjects(json, "roles");

        var roles = new HashSet<Role>();

        for (String roleValue : rolesValue) {
            var roleNameValue = this.extractValue(roleValue, "name");

            var rolePermissionsValue = this.extractObjects(roleValue, "permissions");
            var permissions = new HashSet<Permission>();
            for (String permissionValue : rolePermissionsValue) {
                var value = this.extractValue(permissionValue, "value");
                permissions.add(new Permission(value));
            }
            roles.add(new Role(roleNameValue, permissions));
        }

        try {
            return User.reconstructUser(idValue, usernameValue, emailValue, passValue, statusValue, roles);
        } catch (DomainException exception) {
            throw new RepositoryPersistenceException("O arquivo contém campos inválidos.", exception);
        }
    }

    private String extractValue(String json, String key) {

        String regex = "\"" + Pattern.quote(key) + "\"\\s*:\\s*\"([^\"]*)\"";

        Matcher matcher = Pattern.compile(regex)
                .matcher(json);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    public static List<String> extractObjects(String json, String key) {

        String keyPattern = "\"" + Pattern.quote(key) + "\"\\s*:\\s*";

        Matcher matcher = Pattern.compile(keyPattern)
                .matcher(json);

        if (!matcher.find()) {
            return List.of();
        }

        int start = matcher.end();

        while (start < json.length()
                && Character.isWhitespace(json.charAt(start))) {
            start++;
        }

        if (start >= json.length() || json.charAt(start) != '[') {
            return List.of();
        }

        List<String> objects = new ArrayList<>();

        int objectStart = -1;
        int depth = 0;

        boolean insideString = false;
        boolean escaped = false;

        for (int i = start + 1; i < json.length(); i++) {

            char current = json.charAt(i);

            if (escaped) {
                escaped = false;
                continue;
            }

            if (current == '\\' && insideString) {
                escaped = true;
                continue;
            }

            if (current == '"') {
                insideString = !insideString;
                continue;
            }

            if (insideString) {
                continue;
            }

            if (current == '{') {

                if (depth == 0) {
                    objectStart = i;
                }

                depth++;

            } else if (current == '}') {

                depth--;

                if (depth == 0 && objectStart != -1) {

                    objects.add(
                            json.substring(objectStart, i + 1)
                    );

                    objectStart = -1;
                }

            } else if (current == ']' && depth == 0) {
                break;
            }
        }

        return objects;
    }
}

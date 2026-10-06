package br.com.sales.code.bug.iam.service;

import java.util.Set;

public interface ObjectSerializer {
    String initObject();
    String linkObjects(String currentObject, String newObject);
    String buildFieldString(String key, String value);
    String buildFieldObject(String key, String value);
    String buildList(Set<String> values);
    String buildListString(Set<String> values);
    String endObject(String object);
    String initList();
    String endList(String value);
    String keepLine();
}

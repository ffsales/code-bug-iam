package br.com.sales.code.bug.iam.service;

import java.util.Set;

public class JsonSerializer implements ObjectSerializer {

    @Override
    public String initObject() {
        return " { ";
    }

    @Override
    public String linkObjects(String currentObject, String newObject) {
        return currentObject.concat(", ").concat(newObject);
    }

    @Override
    public String buildFieldString(String key, String value) {
        return "\"".concat(key)
                .concat("\"")
                .concat(":")
                .concat("\"")
                .concat(value)
                .concat("\"");
    }

    @Override
    public String buildFieldObject(String key, String value) {
        return "\"".concat(key)
                .concat("\"")
                .concat(":")
                .concat(value);
    }

    @Override
    public String buildList(Set<String> values) {

        var value = this.initList();

        var iterator = values.iterator();
        while (iterator.hasNext()) {

            value = value.concat(iterator.next());

            if(iterator.hasNext())
                value = this.linkObjects(value, " ");
        }

        return this.endList(value);
    }

    @Override
    public String buildListString(Set<String> values) {

        var value = this.initList();

        var iterator = values.iterator();
        while (iterator.hasNext()) {

            value = value.concat("\"").concat(iterator.next()).concat("\"");

            if(iterator.hasNext())
                value = this.linkObjects(value, " ");
        }

        return this.endList(value);
    }

    @Override
    public String endObject(String object) {
        return object.concat(" }");
    }

    @Override
    public String initList() {
        return "[";
    }

    @Override
    public String endList(String value) {
        return value.concat("]");
    }

    public String keepLine() {
        return "\n";
    }
}

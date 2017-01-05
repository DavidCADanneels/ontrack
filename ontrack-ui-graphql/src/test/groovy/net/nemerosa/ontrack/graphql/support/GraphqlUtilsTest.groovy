package net.nemerosa.ontrack.graphql.support

import org.junit.Test

import static net.nemerosa.ontrack.graphql.support.GraphqlUtils.lowerCamelCase

class GraphqlUtilsTest {

    @Test
    void 'Lower camel case'() {
        assert lowerCamelCase(null) == null
        assert lowerCamelCase("") == ""
        assert lowerCamelCase(" ") == ""
        assert lowerCamelCase("Abc") == "abc"
        assert lowerCamelCase("abc") == "abc"
        assert lowerCamelCase("AbcDef") == "abcdef"
        assert lowerCamelCase("abcDef") == "abcdef"
        assert lowerCamelCase("abc Def") == "abcDef"
        assert lowerCamelCase("Abc Def") == "abcDef"
        assert lowerCamelCase("Abc def") == "abcDef"
    }

    @Test
    void 'Lower camel case more complex cases'() {
        assert lowerCamelCase("prefix:suffix") == "prefixSuffix"
        assert lowerCamelCase("prefix: suffix") == "prefixSuffix"
    }

}

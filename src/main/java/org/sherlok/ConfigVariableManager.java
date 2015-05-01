package org.sherlok;

import static org.sherlok.utils.Create.list;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sherlok.mappings.BundleDef.EngineDef;

/**
 * Manage configuration variables for the engine definition.
 * 
 * This includes how resources are fetched, their lifetime on disk and where
 * they are stored.
 */
public class ConfigVariableManager {

    /**
     * Process the configuration variables in each value and return the
     * interpreted value.
     * 
     * <pre>
     * For example, with the following variables defined in the bundle of the
     * engine:
     *  - $bluima refers to a git repository
     *  - $chunker refers to the string 'opennlp/chunker'
     *  - $countries refers to the path 'file://config/resources/countries.txt'
     * then processing the value list:
     *  - "$bluima/$chunker/Chunker_Genia.bin.gz"
     *  - "$countries"
     * will return the following list:
     *  - "<a_path>/opennlp/chunker/Chunker_Genia.bin.gz"
     *  - "config/resources/countries.txt"
     * where <a_path> is some path to a local copy of bluima repository that  
     * Sherlok will download at runtime.
     * </pre>
     */
    static List<String> processConfigVariables(List<String> values,
            EngineDef engineDef) {
        List<String> processed = list();
        for (String value : values) {
            processed.add(processConfigVariables(value, engineDef));
        }

        return processed;
    }

    // Accepts variable starting by '$' and containing only alpha-numerical
    // characters (+ underscore). The variable can be accessed through the
    // named-capturing group "name".
    private static final Pattern VARIABLE_PATTERN = Pattern
            .compile("\\$(?<name>\\w+)");

    // Process configuration variables
    private static String processConfigVariables(String value,
            EngineDef engineDef) {
        Matcher matcher = VARIABLE_PATTERN.matcher(value);
        Map<String, String> config = engineDef.getBundle().getConfig();

        while (matcher.find()) {
            String name = matcher.group("name");
            String processed = processConfigVariable(name, config);

            // replace all occurrences of "$name" in the original string
            value = value.replace(matcher.group(), processed);
        }

        return value;
    }

    private static String processConfigVariable(String name,
            Map<String, String> config) {
        // TODO handle URLs here (e.g. file, git, ...)

        // fallback: basic substitution
        return config.get(name);
    }

}

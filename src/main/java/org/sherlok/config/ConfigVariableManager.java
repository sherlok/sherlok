package org.sherlok.config;

import static org.sherlok.utils.Create.list;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sherlok.mappings.Def;
import org.slf4j.Logger;

/**
 * Manage configuration variables for the engine definition.
 * 
 * This includes how resources are fetched, their lifetime on disk and where
 * they are stored.
 */
public class ConfigVariableManager {

    private static Logger LOG = getLogger(ConfigVariableManager.class);

    /**
     * Process the configuration variables in each value and return the
     * interpreted value. Works for both pipeline and engine definition
     * (actually for all class inheriting from Def).
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
     * 
     * @throws NoSuchVariableException
     *             when an unknown variable is used
     * @throws ProcessConfigVariableException
     *             if a variable could not be processed
     */
    public static List<String> processConfigVariables(List<String> values,
            Def def) throws NoSuchVariableException,
            ProcessConfigVariableException {
        List<String> processed = list();
        for (String value : values) {
            processed.add(processConfigVariables(value, def));
        }

        return processed;
    }

    // Accepts variable starting by '$' and containing only alpha-numerical
    // characters (+ underscore). The variable can be accessed through the
    // named-capturing group "name".
    private static final Pattern VARIABLE_PATTERN = Pattern
            .compile("\\$(?<name>\\w+)");

    // Process configuration variables
    private static String processConfigVariables(String value, Def def)
            throws NoSuchVariableException,
            ProcessConfigVariableException {
        Matcher matcher = VARIABLE_PATTERN.matcher(value);
        Map<String, ConfigVariable> config = def.getConfigVariables();

        while (matcher.find()) {
            String name = matcher.group("name");
            String processed = processConfigVariable(name, config);

            // replace all occurrences of "$name" in the original string
            value = value.replace(matcher.group(), processed);
        }

        return value;
    }

    private static String processConfigVariable(String name,
            Map<String, ConfigVariable> config) throws NoSuchVariableException,
            ProcessConfigVariableException {
        ConfigVariable var = config.get(name);
        if (var == null) {
            LOG.debug("unknown variable " + name);
            throw new NoSuchVariableException(name);
        }
        return var.getProcessedValue();
    }

}

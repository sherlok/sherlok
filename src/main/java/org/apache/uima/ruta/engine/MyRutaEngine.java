/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.uima.ruta.engine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.apache.uima.UIMAFramework;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.AnalysisComponent;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceConfigurationException;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceManager;
import org.apache.uima.ruta.FilterManager;
import org.apache.uima.ruta.RutaBlock;
import org.apache.uima.ruta.RutaModule;
import org.apache.uima.ruta.RutaStream;
import org.apache.uima.ruta.extensions.IEngineLoader;
import org.apache.uima.ruta.extensions.IRutaExtension;
import org.apache.uima.ruta.extensions.RutaEngineLoader;
import org.apache.uima.ruta.extensions.RutaExternalFactory;
import org.apache.uima.ruta.parser.RutaLexer;
import org.apache.uima.ruta.parser.RutaParser;
import org.apache.uima.ruta.seed.RutaAnnotationSeeder;
import org.apache.uima.ruta.verbalize.RutaVerbalizer;
import org.apache.uima.ruta.visitor.CreatedByVisitor;
import org.apache.uima.ruta.visitor.DebugInfoCollectorVisitor;
import org.apache.uima.ruta.visitor.InferenceCrowd;
import org.apache.uima.ruta.visitor.RutaInferenceVisitor;
import org.apache.uima.ruta.visitor.StatisticsVisitor;
import org.apache.uima.ruta.visitor.TimeProfilerVisitor;
import org.apache.uima.util.InvalidXMLException;

public class MyRutaEngine extends JCasAnnotator_ImplBase {

  public static final String SCRIPT_FILE_EXTENSION = ".ruta";

  public static final String SOURCE_DOCUMENT_INFORMATION = "org.apache.uima.examples.SourceDocumentInformation";

  public static final String BASIC_TYPE = "org.apache.uima.ruta.type.RutaBasic";

  public static final String OPTIONAL_TYPE = "org.apache.uima.ruta.type.RutaOptional";

  public static final String FRAME_TYPE = "org.apache.uima.ruta.type.RutaFrame";

  /**
   * Load script in Java notation, with "{@code .}" as package separator and no extension. File
   * needs to be located in the path specified below with ending {@code .ruta}.
   */
  public static final String PARAM_MAIN_SCRIPT = "mainScript";

  @ConfigurationParameter(name = PARAM_MAIN_SCRIPT, mandatory = false)
  private String mainScipt;

  /**
   * This parameter specifies the encoding of the rule files. Its default value is "UTF-8".
   */
  public static final String PARAM_SCRIPT_ENCODING = "scriptEncoding";

  @ConfigurationParameter(name = PARAM_SCRIPT_ENCODING, mandatory = false, defaultValue = "UTF-8")
  private String scriptEncoding;

  /**
   * The parameter scriptPaths refers to a list of String values, which specify the possible
   * locations of script files. The given locations are absolute paths. A typical value for this
   * parameter is, for example, "C:/Ruta/MyProject/script/". If the parameter mainScript is set to
   * org.apache.uima.Main, then the absolute path of the script file has to be
   * "C:/Ruta/MyProject/script/org/apache/uima/Main.ruta". This parameter can contain multiple
   * values, as the main script can refer to multiple projects similar to a class path in Java.
   */
  public static final String PARAM_SCRIPT_PATHS = "scriptPaths";

  @ConfigurationParameter(name = PARAM_SCRIPT_PATHS, mandatory = false)
  private String[] scriptPaths;

  /**
   * This parameter specifies the possible locations for descriptors like analysis engines or type
   * systems, similar to the parameter scriptPaths for the script files. A typical value for this
   * parameter is for example "C:/Ruta/MyProject/descriptor/". The relative values of the parameter
   * additionalEngines are resolved to these absolute locations. This parameter can contain multiple
   * values, as the main script can refer to multiple projects similar to a class path in Java.
   */
  public static final String PARAM_DESCRIPTOR_PATHS = "descriptorPaths";

  @ConfigurationParameter(name = PARAM_DESCRIPTOR_PATHS, mandatory = false, defaultValue = {})
  private String[] descriptorPaths;

  /**
   * This parameter specifies the possible locations of additional resources like word lists or CSV
   * tables. The string values have to contain absolute locations, for example,
   * "C:/Ruta/MyProject/resources/".
   */
  public static final String PARAM_RESOURCE_PATHS = "resourcePaths";

  @ConfigurationParameter(name = PARAM_RESOURCE_PATHS, mandatory = false, defaultValue = {})
  private String[] resourcePaths;

  /**
   * The parameter additionalScripts is defined as a list of string values and contains script
   * files, which are additionally loaded by the analysis engine. These script files are specified
   * by their complete namespace, exactly like the value of the parameter mainScript and can be
   * refered to by language elements, e.g., by executing the containing rules. An exemplary value of
   * this parameter is "org.apache.uima.SecondaryScript". In this example, the main script could
   * import this script file by the declaration "SCRIPT org.apache.uima.SecondaryScript;" and then
   * could execute it with the rule "Document{-> CALL(SecondaryScript)};".
   */
  public static final String PARAM_ADDITIONAL_SCRIPTS = "additionalScripts";

  @ConfigurationParameter(name = PARAM_ADDITIONAL_SCRIPTS, mandatory = false, defaultValue = {})
  private String[] additionalScripts;

  /**
   * This parameter contains a list of additional analysis engines, which can be executed by the
   * UIMA Ruta rules. The single values are given by the name of the analysis engine with their
   * complete namespace and have to be located relative to one value of the parameter
   * descriptorPaths, the location where the analysis engine searches for the descriptor file. An
   * example for one value of the parameter is "utils.HtmlAnnotator", which points to the descriptor
   * "HtmlAnnotator.xml" in the folder "utils".
   */
  public static final String PARAM_ADDITIONAL_ENGINES = "additionalEngines";

  @ConfigurationParameter(name = PARAM_ADDITIONAL_ENGINES, mandatory = false, defaultValue = {})
  private String[] additionalEngines;

  /**
   * List of additional uimaFIT analysis engines, which are loaded without descriptor.
   */
  public static final String PARAM_ADDITIONAL_UIMAFIT_ENGINES = "additionalUimafitEngines";

  @ConfigurationParameter(name = PARAM_ADDITIONAL_UIMAFIT_ENGINES, mandatory = false, defaultValue = {})
  private String[] additionalUimafitEngines;

  /**
   * The parameter "additionalEngineLoaders" specifies a list of optional implementations of the
   * interface "org.apache.uima.ruta.extensions.IEngineLoader", which can be used to
   * application-specific configurations of additional analysis engines.
   */
  public static final String PARAM_ADDITIONAL_ENGINE_LOADERS = "additionalEngineLoaders";

  @ConfigurationParameter(name = PARAM_ADDITIONAL_ENGINE_LOADERS, mandatory = false, defaultValue = {})
  private String[] additionalEngineLoaders;

  /**
   * This parameter specifies optional extensions of the UIMA Ruta language. The elements of the
   * string list have to implement the interface "org.apache.uima.ruta.extensions.IRutaExtension".
   * With these extensions, application-specific conditions and actions can be added to the set of
   * provided ones.
   */
  public static final String PARAM_ADDITIONAL_EXTENSIONS = "additionalExtensions";

  @ConfigurationParameter(name = PARAM_ADDITIONAL_EXTENSIONS, mandatory = false, defaultValue = {})
  private String[] additionalExtensions;

  /**
   * This boolean parameter indicates whether the script or resource files should be reloaded when
   * processing a CAS. The default value is set to false. In this case, the script files are loaded
   * when the analysis engine is initialized. If script files or resource files are extended, e.g.,
   * a dictionary is filled yet when a collection of documents are processed, then the parameter is
   * needed to be set to true in order to include the changes.
   */
  public static final String PARAM_RELOAD_SCRIPT = "reloadScript";

  @ConfigurationParameter(name = PARAM_RELOAD_SCRIPT, mandatory = false, defaultValue = "false")
  private Boolean reloadScript;

  /**
   * This list of string values refers to implementations of the interface
   * "org.apache.uima.ruta.seed.RutaAnnotationSeeder", which can be used to automatically add
   * annotations to the CAS. The default value of the parameter is a single seeder, namely
   * "org.apache.uima.ruta.seed.DefaultSeeder" that adds annotations for token classes like CW,
   * MARKUP or SEMICOLON. Remember that additional annotations can also be added with an additional
   * engine that is executed by a UIMA Ruta rule.
   */
  public static final String PARAM_SEEDERS = "seeders";

  @ConfigurationParameter(name = PARAM_SEEDERS, mandatory = false, defaultValue = { "org.apache.uima.ruta.seed.DefaultSeeder" })
  private String[] seeders;

  /**
   * This parameter specifies a list of types, which are filtered by default when executing a script
   * file. Using the default values of this parameter, whitespaces, line breaks and markup elements
   * are not visible to Ruta rules. The visibility of annotations and, therefore, the covered text
   * can be changed using the actions FILTERTYPE and RETAINTYPE.
   */
  public static final String PARAM_DEFAULT_FILTERED_TYPES = "defaultFilteredTypes";

  @ConfigurationParameter(name = PARAM_DEFAULT_FILTERED_TYPES, mandatory = false, defaultValue = {
      "org.apache.uima.ruta.type.SPACE", "org.apache.uima.ruta.type.NBSP",
      "org.apache.uima.ruta.type.BREAK", "org.apache.uima.ruta.type.MARKUP" })
  private String[] defaultFilteredTypes;

  /**
   * This parameter specifies whether the inference annotations created by the analysis engine
   * should be removed after processing the CAS. The default value is set to true.
   */
  public static final String PARAM_REMOVE_BASICS = "removeBasics";

  @ConfigurationParameter(name = PARAM_REMOVE_BASICS, mandatory = false, defaultValue = "true")
  private Boolean removeBasics;

  /**
   * If this parameter is set to true, then the Ruta rules are not forced to start to match with the
   * first rule element. Rather, the rule element referring to the most rare type is chosen. This
   * option can be utilized to optimize the performance. Please mind that the matching result can
   * vary in some cases when greedy rule elements are applied. The default value is set to false.
   */
  public static final String PARAM_DYNAMIC_ANCHORING = "dynamicAnchoring";

  @ConfigurationParameter(name = PARAM_DYNAMIC_ANCHORING, mandatory = false, defaultValue = "false")
  private Boolean dynamicAnchoring;

  /**
   * This parameter specifies whether the memory consumption should be reduced. This parameter
   * should be set to true for very large CAS documents (e.g., > 500k tokens), but it also reduces
   * the performance. The default value is set to false.
   */
  public static final String PARAM_LOW_MEMORY_PROFILE = "lowMemoryProfile";

  @ConfigurationParameter(name = PARAM_LOW_MEMORY_PROFILE, mandatory = false, defaultValue = "false")
  private Boolean lowMemoryProfile;

  /**
   * This parameter specifies whether a different inference strategy for composed rule elements
   * should be applied. This option is only necessary when the composed rule element is expected to
   * match very often, e.g., a rule element like (ANY ANY)+. The default value of this parameter is
   * set to false.
   */
  public static final String PARAM_SIMPLE_GREEDY_FOR_COMPOSED = "simpleGreedyForComposed";

  @ConfigurationParameter(name = PARAM_SIMPLE_GREEDY_FOR_COMPOSED, mandatory = false, defaultValue = "false")
  private Boolean simpleGreedyForComposed;

  /**
   * If this parameter is set to true, then start positions already matched by the same rule element
   * will be ignored. This situation occurs mostly for rules that start with a quantifier. The
   * following rule, for example, matches only once, if this parameter is set to true: {@code ANY+;}
   */
  public static final String PARAM_GREEDY_RULE_ELEMENT = "greedyRuleElement";

  @ConfigurationParameter(name = PARAM_GREEDY_RULE_ELEMENT, mandatory = false, defaultValue = "false")
  private Boolean greedyRuleElement = false;

  /**
   * If this parameter is set to true, then start positions already matched by the rule will be
   * ignored and only positions not part of an match will be considered.
   */
  public static final String PARAM_GREEDY_RULE = "greedyRule";

  @ConfigurationParameter(name = PARAM_GREEDY_RULE, mandatory = false, defaultValue = "false")
  private Boolean greedyRule = false;

  /**
   * If this parameter is set to true, then additional information about the execution of a rule
   * script is added to the CAS. The actual information is specified by the following parameters.
   * The default value of this parameter is set to false.
   */
  public static final String PARAM_DEBUG = "debug";

  @ConfigurationParameter(name = PARAM_DEBUG, mandatory = false, defaultValue = "false")
  private Boolean debug;

  /**
   * This parameter specifies whether the match information (covered text) of the rules should be
   * stored in the CAS. The default value of this parameter is set to false.
   */
  public static final String PARAM_DEBUG_WITH_MATCHES = "debugWithMatches";

  @ConfigurationParameter(name = PARAM_DEBUG_WITH_MATCHES, mandatory = false, defaultValue = "false")
  private Boolean debugWithMatches;

  /**
   * This parameter specifies a list of rule-ids that enumerate the rule for which debug information
   * should be created. No specific ids are given by default.
   */
  public static final String PARAM_DEBUG_ONLY_FOR = "debugOnlyFor";

  @ConfigurationParameter(name = PARAM_DEBUG_ONLY_FOR, mandatory = false, defaultValue = {})
  private String[] debugOnlyFor;

  /**
   * If this parameter is set to true, then additional information about the runtime of applied
   * rules is added to the CAS. The default value of this parameter is set to false.
   */
  public static final String PARAM_PROFILE = "profile";

  @ConfigurationParameter(name = PARAM_PROFILE, mandatory = false, defaultValue = "false")
  private Boolean profile;

  /**
   * If this parameter is set to true, then additional information about the runtime of UIMA Ruta
   * language elements like conditions and actions is added to the CAS. The default value of this
   * parameter is set to false.
   */
  public static final String PARAM_STATISTICS = "statistics";

  @ConfigurationParameter(name = PARAM_STATISTICS, mandatory = false, defaultValue = "false")
  private Boolean statistics;

  /**
   * If this parameter is set to true, then additional information about what annotation was created
   * by which rule is added to the CAS. The default value of this parameter is set to false.
   */
  public static final String PARAM_CREATED_BY = "createdBy";

  @ConfigurationParameter(name = PARAM_CREATED_BY, mandatory = false, defaultValue = "false")
  private Boolean createdBy;

  /**
   * If this parameter is set to true, then only types in declared type systems are available by
   * their short name.
   */
  public static final String PARAM_STRICT_IMPORTS = "strictImports";

  @ConfigurationParameter(name = PARAM_STRICT_IMPORTS, mandatory = false, defaultValue = "false")
  private Boolean strictImports = false;

  private UimaContext context;

  private RutaModule script;

  private RutaExternalFactory factory;

  private RutaEngineLoader engineLoader;

  private String mainScript;

  private RutaVerbalizer verbalizer;

  private boolean initialized = false;

  private List<Type> seedTypes;

  private TypeSystem lastTypeSystem;

  private ResourceManager resourceManager = null;

  @Override
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);
    if (aContext == null && context != null) {
      aContext = context;
    }
    if (aContext != null) {
      seeders = (String[]) aContext.getConfigParameterValue(PARAM_SEEDERS);
      removeBasics = (Boolean) aContext.getConfigParameterValue(PARAM_REMOVE_BASICS);
      scriptPaths = (String[]) aContext.getConfigParameterValue(PARAM_SCRIPT_PATHS);
      descriptorPaths = (String[]) aContext.getConfigParameterValue(PARAM_DESCRIPTOR_PATHS);
      mainScript = (String) aContext.getConfigParameterValue(PARAM_MAIN_SCRIPT);
      additionalScripts = (String[]) aContext.getConfigParameterValue(PARAM_ADDITIONAL_SCRIPTS);
      additionalEngines = (String[]) aContext.getConfigParameterValue(PARAM_ADDITIONAL_ENGINES);
      additionalUimafitEngines = (String[]) aContext
              .getConfigParameterValue(PARAM_ADDITIONAL_UIMAFIT_ENGINES);
      additionalExtensions = (String[]) aContext
              .getConfigParameterValue(PARAM_ADDITIONAL_EXTENSIONS);
      additionalEngineLoaders = (String[]) aContext
              .getConfigParameterValue(PARAM_ADDITIONAL_ENGINE_LOADERS);

      debug = (Boolean) aContext.getConfigParameterValue(PARAM_DEBUG);
      debugOnlyFor = (String[]) aContext.getConfigParameterValue(PARAM_DEBUG_ONLY_FOR);
      profile = (Boolean) aContext.getConfigParameterValue(PARAM_PROFILE);
      statistics = (Boolean) aContext.getConfigParameterValue(PARAM_STATISTICS);
      createdBy = (Boolean) aContext.getConfigParameterValue(PARAM_CREATED_BY);
      debugWithMatches = (Boolean) aContext.getConfigParameterValue(PARAM_DEBUG_WITH_MATCHES);

      resourcePaths = (String[]) aContext.getConfigParameterValue(PARAM_RESOURCE_PATHS);
      scriptEncoding = (String) aContext.getConfigParameterValue(PARAM_SCRIPT_ENCODING);
      defaultFilteredTypes = (String[]) aContext
              .getConfigParameterValue(PARAM_DEFAULT_FILTERED_TYPES);
      dynamicAnchoring = (Boolean) aContext.getConfigParameterValue(PARAM_DYNAMIC_ANCHORING);
      reloadScript = (Boolean) aContext.getConfigParameterValue(PARAM_RELOAD_SCRIPT);
      lowMemoryProfile = (Boolean) aContext.getConfigParameterValue(PARAM_LOW_MEMORY_PROFILE);
      simpleGreedyForComposed = (Boolean) aContext
              .getConfigParameterValue(PARAM_SIMPLE_GREEDY_FOR_COMPOSED);
      greedyRuleElement = (Boolean) aContext.getConfigParameterValue(PARAM_GREEDY_RULE_ELEMENT);
      greedyRule = (Boolean) aContext.getConfigParameterValue(PARAM_GREEDY_RULE);

      resourcePaths = resourcePaths == null ? new String[0] : resourcePaths;
      removeBasics = removeBasics == null ? false : removeBasics;
      debug = debug == null ? false : debug;
      debugOnlyFor = debugOnlyFor == null ? new String[0] : debugOnlyFor;
      profile = profile == null ? false : profile;
      statistics = statistics == null ? false : statistics;
      createdBy = createdBy == null ? false : createdBy;
      debugWithMatches = debugWithMatches == null ? true : debugWithMatches;

      scriptEncoding = scriptEncoding == null ? "UTF-8" : scriptEncoding;
      defaultFilteredTypes = defaultFilteredTypes == null ? new String[0] : defaultFilteredTypes;
      dynamicAnchoring = dynamicAnchoring == null ? false : dynamicAnchoring;
      reloadScript = reloadScript == null ? false : reloadScript;
      lowMemoryProfile = lowMemoryProfile == null ? false : lowMemoryProfile;
      simpleGreedyForComposed = simpleGreedyForComposed == null ? false : simpleGreedyForComposed;
      greedyRuleElement = greedyRuleElement == null ? false : greedyRuleElement;
      greedyRule = greedyRule == null ? false : greedyRule;

      this.context = aContext;

      factory = new RutaExternalFactory();
      engineLoader = new RutaEngineLoader();
      verbalizer = new RutaVerbalizer();

      resourceManager = UIMAFramework.newDefaultResourceManager();
      String dataPath = "";
      if (descriptorPaths != null) {
        for (String path : descriptorPaths) {
          dataPath += path + File.pathSeparator;
        }
        try {
          resourceManager.setDataPath(dataPath);
        } catch (MalformedURLException e) {
          throw new ResourceInitializationException(e);
        }
      }
      if (!factory.isInitialized()) {
        initializeExtensionWithClassPath();
      }
      if (!engineLoader.isInitialized()) {
        initializeEngineLoaderWithClassPath();
      }
      if (!reloadScript) {
        try {
          initializeScript(CAS.NAME_DEFAULT_SOFA);
        } catch (AnalysisEngineProcessException e) {
          throw new ResourceInitializationException(e);
        }
      }
    }
  }

  @Override
  public void process(JCas jcas) throws AnalysisEngineProcessException {
    CAS cas = jcas.getCas();
    if (reloadScript || (!initialized && !cas.getViewName().equals(CAS.NAME_DEFAULT_SOFA))) {
      initializeScript(cas.getViewName());
    } else {
      resetEnvironments(cas);
    }
    boolean typeSystemChanged = lastTypeSystem != cas.getTypeSystem();
    if (!initialized || reloadScript || typeSystemChanged) {
      initializeTypes(script, cas);
      initialized = true;
      lastTypeSystem = cas.getTypeSystem();
    }
    InferenceCrowd crowd = initializeCrowd();
    RutaStream stream = initializeStream(cas, crowd);
    stream.setDynamicAnchoring(dynamicAnchoring);
    stream.setGreedyRuleElement(greedyRuleElement);
    stream.setGreedyRule(greedyRule);
    try {
      script.apply(stream, crowd);
    } catch (Throwable e) {
      throw new AnalysisEngineProcessException(AnalysisEngineProcessException.ANNOTATOR_EXCEPTION,
              new Object[] {}, e);
    }
    crowd.finished(stream);

    if (removeBasics) {
      List<AnnotationFS> toRemove = new ArrayList<AnnotationFS>();
      Type basicType = cas.getTypeSystem().getType(BASIC_TYPE);
      AnnotationIndex<AnnotationFS> basicIndex = cas.getAnnotationIndex(basicType);
      for (AnnotationFS fs : basicIndex) {
        toRemove.add(fs);
      }
      for (Type seedType : seedTypes) {
        AnnotationIndex<AnnotationFS> seedIndex = cas.getAnnotationIndex(seedType);
        for (AnnotationFS fs : seedIndex) {
          toRemove.add(fs);
        }
      }
      for (AnnotationFS annotationFS : toRemove) {
        cas.removeFsFromIndexes(annotationFS);
      }
    }
  }

  private void resetEnvironments(CAS cas) {
    resetEnvironment(script, cas);
    Collection<RutaModule> scripts = script.getScripts().values();
    for (RutaModule module : scripts) {
      resetEnvironment(module, cas);
    }
  }

  private void resetEnvironment(RutaModule module, CAS cas) {
    RutaBlock block = module.getBlock(null);
    block.getEnvironment().reset(cas);
    Collection<RutaBlock> blocks = module.getBlocks().values();
    for (RutaBlock each : blocks) {
      each.getEnvironment().reset(cas);
    }
  }

  private void initializeTypes(RutaModule script, CAS cas) {
    // TODO find a better solution for telling everyone about the types!
    RutaBlock mainRootBlock = script.getBlock(null);
    mainRootBlock.getEnvironment().initializeTypes(cas, strictImports);
    Collection<RutaModule> values = script.getScripts().values();
    for (RutaModule eachModule : values) {
      relinkEnvironments(eachModule, mainRootBlock, new ArrayList<RutaModule>());
      // initializeTypes(eachModule, cas);
    }
  }

  private void relinkEnvironments(RutaModule script, RutaBlock mainRootBlock,
          Collection<RutaModule> processed) {
    if (!processed.contains(script)) {
      processed.add(script);
      RutaBlock block = script.getBlock(null);
      block.setParent(mainRootBlock);
      Collection<RutaModule> innerScripts = script.getScripts().values();
      for (RutaModule module : innerScripts) {
        relinkEnvironments(module, mainRootBlock, processed);
      }
    }
  }

  private void initializeExtensionWithClassPath() {
    if (additionalExtensions == null) {
      return;
    }
    for (String each : additionalExtensions) {
      try {
        Class<?> forName = Class.forName(each);
        if (IRutaExtension.class.isAssignableFrom(forName)) {
          IRutaExtension extension = (IRutaExtension) forName.newInstance();
          verbalizer.addExternalVerbalizers(extension);
          for (String name : extension.getKnownExtensions()) {
            factory.addExtension(name, extension);
          }
        }
      } catch (Exception e) {
        // System.out.println("EXTENSION ERROR: " + each);
      }
    }
  }

  private void initializeEngineLoaderWithClassPath() {
    if (additionalEngineLoaders == null) {
      return;
    }
    for (String each : additionalEngineLoaders) {
      try {
        Class<?> forName = Class.forName(each);
        if (IEngineLoader.class.isAssignableFrom(forName)) {
          IEngineLoader loader = (IEngineLoader) forName.newInstance();
          for (String name : loader.getKnownEngines()) {
            engineLoader.addLoader(name, loader);
          }
        }
      } catch (Exception e) {
        // System.out.println("LOADER ERROR: " + each);
      }
    }
  }

  private InferenceCrowd initializeCrowd() {
    List<RutaInferenceVisitor> visitors = new ArrayList<RutaInferenceVisitor>();
    if (debug) {
      visitors.add(new DebugInfoCollectorVisitor(debug, debugWithMatches, Arrays
              .asList(debugOnlyFor), verbalizer));
    }
    if (profile) {
      visitors.add(new TimeProfilerVisitor());
    }
    if (statistics) {
      visitors.add(new StatisticsVisitor(verbalizer));
    }
    if (createdBy) {
      visitors.add(new CreatedByVisitor(verbalizer));
    }
    return new InferenceCrowd(visitors);
  }

  private RutaStream initializeStream(CAS cas, InferenceCrowd crowd)
          throws AnalysisEngineProcessException {
    Collection<Type> filterTypes = new ArrayList<Type>();
    TypeSystem typeSystem = cas.getTypeSystem();
    for (String each : defaultFilteredTypes) {
      Type type = typeSystem.getType(each);
      if (type != null) {
        filterTypes.add(type);
      }
    }
    FilterManager filter = new FilterManager(filterTypes, cas);
    Type basicType = typeSystem.getType(BASIC_TYPE);
    seedTypes = seedAnnotations(cas);
    RutaStream stream = new RutaStream(cas, basicType, filter, lowMemoryProfile,
            simpleGreedyForComposed, crowd);

    stream.initalizeBasics();
    return stream;
  }

  private List<Type> seedAnnotations(CAS cas) throws AnalysisEngineProcessException {
    List<Type> result = new ArrayList<Type>();
    if (seeders != null) {
      for (String seederClass : seeders) {
        Class<?> loadClass = null;
        try {
          loadClass = Class.forName(seederClass);
        } catch (ClassNotFoundException e) {
          throw new AnalysisEngineProcessException(e);
        }
        Object newInstance = null;
        try {
          newInstance = loadClass.newInstance();
        } catch (Exception e) {
          throw new AnalysisEngineProcessException(e);
        }
        try {
          RutaAnnotationSeeder seeder = (RutaAnnotationSeeder) newInstance;
          result.add(seeder.seed(cas.getDocumentText(), cas));
        } catch (Exception e) {
          throw new AnalysisEngineProcessException(e);
        }
      }
    }
    return result;
  }

  private void initializeScript(String viewName) throws AnalysisEngineProcessException {
    if (mainScript == null) {
      return;
    }
    String scriptLocation = locate(mainScript, scriptPaths, SCRIPT_FILE_EXTENSION);
    if (scriptLocation == null) {
      try {
        String mainScriptPath = mainScript.replaceAll("\\.", "/") + SCRIPT_FILE_EXTENSION;
        script = loadScriptIS(mainScriptPath);
      } catch (IOException e) {
        throw new AnalysisEngineProcessException(new FileNotFoundException("Script [" + mainScript
                + "] cannot be found at [" + collectionToString(scriptPaths)
                + "] or classpath with extension .ruta"));
      } catch (RecognitionException e) {
        throw new AnalysisEngineProcessException(new FileNotFoundException("Script [" + mainScript
                + "] cannot be found at [" + collectionToString(scriptPaths)
                + "] or classpath  with extension .ruta"));
      }
    } else {
      try {
        script = loadScript(scriptLocation);
      } catch (Exception e) {
        throw new AnalysisEngineProcessException(e);
      }
    }

    Map<String, RutaModule> additionalScriptsMap = new HashMap<String, RutaModule>();
    Map<String, AnalysisEngine> additionalEnginesMap = new HashMap<String, AnalysisEngine>();

    if (additionalUimafitEngines != null) {
      for (String eachUimafitEngine : additionalUimafitEngines) {
        AnalysisEngine eachEngine = null;
        try {
          @SuppressWarnings("unchecked")
          // Class clazz = this.getClass().getClassLoader().loadClass(eachUimafitEngine) ;
          Class<? extends AnalysisComponent> uimafitClass = (Class<? extends AnalysisComponent>) Class
                  .forName(eachUimafitEngine);
          eachEngine = AnalysisEngineFactory.createEngine(uimafitClass);
        } catch (ClassNotFoundException e) {
          throw new AnalysisEngineProcessException(e);
        } catch (ResourceInitializationException e) {
          throw new AnalysisEngineProcessException(e);
        }
        try {
          additionalEnginesMap.put(eachUimafitEngine, eachEngine);
          String[] eachEngineLocationPartArray = eachUimafitEngine.split("\\.");
          if (eachEngineLocationPartArray.length > 1) {
            String shortEachEngineLocation = eachEngineLocationPartArray[eachEngineLocationPartArray.length - 1];
            additionalEnginesMap.put(shortEachEngineLocation, eachEngine);
          }
        } catch (Exception e) {
          throw new AnalysisEngineProcessException(e);
        }
      }
    }
    if (additionalEngines != null) {
      for (String eachEngineLocation : additionalEngines) {
        AnalysisEngine eachEngine;
        String location = locate(eachEngineLocation, descriptorPaths, ".xml");
        if (location == null) {
          String locationIS = locateIS(eachEngineLocation, descriptorPaths, ".xml");
          try {
            eachEngine = engineLoader.loadEngineIS(locationIS, viewName);
          } catch (InvalidXMLException e) {
            throw new AnalysisEngineProcessException(new FileNotFoundException("Engine at ["
                    + eachEngineLocation + "] cannot be found in ["
                    + collectionToString(descriptorPaths)
                    + "] with extension .xml (from mainScript=" + mainScript + " in "
                    + collectionToString(scriptPaths)));
          } catch (ResourceInitializationException e) {
            throw new AnalysisEngineProcessException(new FileNotFoundException("Engine at ["
                    + eachEngineLocation + "] cannot be found in ["
                    + collectionToString(descriptorPaths)
                    + "] with extension .xml (from mainScript=" + mainScript + " in "
                    + collectionToString(scriptPaths)));
          } catch (IOException e) {
            throw new AnalysisEngineProcessException(new FileNotFoundException("Engine at ["
                    + eachEngineLocation + "] cannot be found in ["
                    + collectionToString(descriptorPaths)
                    + "] with extension .xml (from mainScript=" + mainScript + " in "
                    + collectionToString(scriptPaths)));
          } catch (ResourceConfigurationException e) {
            throw new AnalysisEngineProcessException(e);
          } catch (URISyntaxException e) {
            throw new AnalysisEngineProcessException(e);
          }
        } else {
          try {
            eachEngine = engineLoader.loadEngine(location, viewName);
          } catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
          }
        }
        try {
          additionalEnginesMap.put(eachEngineLocation, eachEngine);
          String[] eachEngineLocationPartArray = eachEngineLocation.split("\\.");
          if (eachEngineLocationPartArray.length > 1) {
            String shortEachEngineLocation = eachEngineLocationPartArray[eachEngineLocationPartArray.length - 1];
            additionalEnginesMap.put(shortEachEngineLocation, eachEngine);
          }
        } catch (Exception e) {
          throw new AnalysisEngineProcessException(e);
        }
      }
    }

    if (additionalScripts != null) {
      for (String add : additionalScripts) {
        recursiveLoadScript(add, additionalScriptsMap, additionalEnginesMap, viewName);
      }
    }

    for (RutaModule each : additionalScriptsMap.values()) {
      each.setScriptDependencies(additionalScriptsMap);
    }
    script.setScriptDependencies(additionalScriptsMap);

    for (RutaModule each : additionalScriptsMap.values()) {
      each.setEngineDependencies(additionalEnginesMap);
    }
    script.setEngineDependencies(additionalEnginesMap);
  }

  public static void addSourceDocumentInformation(CAS cas, File each) {
    Type sdiType = cas.getTypeSystem()
            .getType("org.apache.uima.examples.SourceDocumentInformation");
    if (sdiType != null) {
      if (cas.getAnnotationIndex(sdiType).size() == 0) {
        AnnotationFS sdi = cas.createAnnotation(sdiType, cas.getDocumentAnnotation().getBegin(),
                cas.getDocumentAnnotation().getEnd());
        Feature uriFeature = sdiType.getFeatureByBaseName("uri");
        sdi.setStringValue(uriFeature, each.toURI().getPath());
        cas.addFsToIndexes(sdi);
      }
    }
  }

  public static void removeSourceDocumentInformation(CAS cas) {
    Type sdiType = cas.getTypeSystem()
            .getType("org.apache.uima.examples.SourceDocumentInformation");
    if (sdiType != null) {
      AnnotationIndex<AnnotationFS> annotationIndex = cas.getAnnotationIndex(sdiType);
      List<AnnotationFS> toRemove = new ArrayList<AnnotationFS>();
      for (AnnotationFS annotationFS : annotationIndex) {
        toRemove.add(annotationFS);
      }
      for (AnnotationFS annotationFS : toRemove) {
        cas.removeFsFromIndexes(annotationFS);
      }
    }
  }

  public static String locate(String name, String[] paths, String suffix) {
    return locate(name, paths, suffix, true);
  }

  public static String locateIS(String name, String[] paths, String suffix) {
    return locateIS(name, paths, suffix, true);
  }

  public static String locate(String name, String[] paths, String suffix, boolean mustExist) {
    if (name == null || paths == null) {
      return null;
    }
    name = name.replaceAll("[.]", "/");
    for (String each : paths) {
      File file = new File(each, name + suffix);
      if (!mustExist || file.exists()) {
        return file.getAbsolutePath();
      }
    }
    return null;
  }

  public static String locateIS(String name, String[] paths, String suffix, boolean mustExist) {
    if (name == null) {
      return null;
    }
    name = name.replaceAll("[.]", "/");
    return name + suffix;
  }

  private void recursiveLoadScript(String toLoad, Map<String, RutaModule> additionalScripts,
          Map<String, AnalysisEngine> additionalEngines, String viewName)
          throws AnalysisEngineProcessException {
    String location = locate(toLoad, scriptPaths, SCRIPT_FILE_EXTENSION);
    RutaModule eachScript = null;
    if (location == null) {
      try {
        String scriptPath = toLoad.replaceAll("\\.", "/") + SCRIPT_FILE_EXTENSION;
        eachScript = loadScriptIS(scriptPath);
      } catch (IOException e) {
        throw new AnalysisEngineProcessException(new FileNotFoundException("Script [" + toLoad
                + "] cannot be found at [" + collectionToString(scriptPaths)
                + "] with extension .ruta"));
      } catch (RecognitionException e) {
        throw new AnalysisEngineProcessException(new FileNotFoundException("Script [" + toLoad
                + "] cannot be found at [" + collectionToString(scriptPaths)
                + "] with extension .ruta"));
      }
    } else {
      try {
        eachScript = loadScript(location);
      } catch (IOException e) {
        throw new AnalysisEngineProcessException(new FileNotFoundException("Script [" + toLoad
                + "] cannot be found at [" + collectionToString(scriptPaths)
                + "] with extension .ruta"));
      } catch (RecognitionException e) {
        throw new AnalysisEngineProcessException(new FileNotFoundException("Script [" + toLoad
                + "] cannot be found at [" + collectionToString(scriptPaths)
                + "] with extension .ruta"));
      }
    }
    additionalScripts.put(toLoad, eachScript);

    for (String add : eachScript.getScripts().keySet()) {
      if (!additionalScripts.containsKey(add)) {
        recursiveLoadScript(add, additionalScripts, additionalEngines, viewName);
      }
    }

    Set<String> engineKeySet = eachScript.getEngines().keySet();
    for (String eachEngineLocation : engineKeySet) {
      if (!additionalEngines.containsKey(eachEngineLocation)) {
        String engineLocation = locate(eachEngineLocation, descriptorPaths, ".xml");
        if (engineLocation == null) {
          String engineLocationIS = locateIS(eachEngineLocation, descriptorPaths, ".xml");
          try {
            AnalysisEngine eachEngine = engineLoader.loadEngineIS(engineLocationIS, viewName);
            additionalEngines.put(eachEngineLocation, eachEngine);
          } catch (Exception e) {
            // uimaFit engine?
            try {
              @SuppressWarnings("unchecked")
              Class<? extends AnalysisComponent> uimafitClass = (Class<? extends AnalysisComponent>) Class
                      .forName(eachEngineLocation);
              AnalysisEngine eachEngine = AnalysisEngineFactory.createEngine(uimafitClass);
              additionalEngines.put(eachEngineLocation, eachEngine);
            } catch (Exception e1) {
              throw new AnalysisEngineProcessException(e1);
            }
          }
        } else {
          try {
            AnalysisEngine eachEngine = engineLoader.loadEngine(engineLocation, viewName);
            additionalEngines.put(eachEngineLocation, eachEngine);
          } catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
          }
        }
      }
    }
  }

  private RutaModule loadScript(String scriptLocation) throws IOException, RecognitionException {
    File scriptFile = new File(scriptLocation);
    CharStream st = new ANTLRFileStream(scriptLocation, scriptEncoding);
    RutaLexer lexer = new RutaLexer(st);
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    RutaParser parser = new RutaParser(tokens);
    parser.setExternalFactory(factory);
    parser.setResourcePaths(resourcePaths);
    parser.setResourceManager(resourceManager);
    String name = scriptFile.getName();
    int lastIndexOf = name.lastIndexOf(SCRIPT_FILE_EXTENSION);
    if (lastIndexOf != -1) {
      name = name.substring(0, lastIndexOf);
    }
    RutaModule script = parser.file_input(name);
    return script;
  }

  private RutaModule loadScriptIS(String scriptLocation) throws IOException, RecognitionException {
    InputStream scriptInputStream = getClass().getClassLoader().getResourceAsStream(scriptLocation);
    if (scriptInputStream == null) {
      throw new FileNotFoundException("No script found in location [" + scriptLocation + "]");
    }
    CharStream st = new ANTLRInputStream(scriptInputStream, scriptEncoding);
    RutaLexer lexer = new RutaLexer(st);
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    RutaParser parser = new RutaParser(tokens);
    parser.setExternalFactory(factory);
    parser.setResourcePaths(resourcePaths);
    String name = scriptLocation;
    if (scriptLocation.indexOf("/") != -1) {
      String[] split = scriptLocation.split("[/]");
      name = split[split.length - 1];
    }
    int lastIndexOf = name.lastIndexOf(SCRIPT_FILE_EXTENSION);
    if (lastIndexOf != -1) {
      name = name.substring(0, lastIndexOf);
    }
    RutaModule script = parser.file_input(name);
    return script;
  }

  public RutaExternalFactory getFactory() {
    return factory;
  }

  public RutaEngineLoader getEngineLoader() {
    return engineLoader;
  }

  private String collectionToString(Collection<?> collection) {
    StringBuilder collectionSB = new StringBuilder();
    collectionSB.append("{");
    for (Object element : collection) {
      collectionSB.append("[").append(element.toString()).append("]");
    }
    collectionSB.append("}");
    return collectionSB.toString();
  }

  private String collectionToString(Object[] collection) {
    if (collection == null) {
      return "";
    } else {
      return collectionToString(Arrays.asList(collection));
    }
  }

  @Override
  public void batchProcessComplete() throws AnalysisEngineProcessException {
    super.batchProcessComplete();
    Collection<AnalysisEngine> values = script.getEngines().values();
    for (AnalysisEngine each : values) {
      each.batchProcessComplete();
    }
  }

  @Override
  public void collectionProcessComplete() throws AnalysisEngineProcessException {
    super.collectionProcessComplete();
    Collection<AnalysisEngine> values = script.getEngines().values();
    for (AnalysisEngine each : values) {
      each.collectionProcessComplete();
    }
  }

}

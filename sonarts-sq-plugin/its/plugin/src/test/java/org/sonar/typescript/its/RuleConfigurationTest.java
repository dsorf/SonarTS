/*
 * SonarTS
 * Copyright (C) 2017-2018 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.typescript.its;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.zip.ZipEntry;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.batch.rule.internal.ActiveRulesBuilder;
import org.sonar.api.batch.rule.internal.NewActiveRule;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.Configuration;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.rule.RuleKey;
import org.sonar.plugin.typescript.ExternalProcessStreamConsumer;
import org.sonar.plugin.typescript.ExternalTypescriptSensor;
import org.sonar.plugin.typescript.TypeScriptLanguage;
import org.sonar.plugin.typescript.TypeScriptRules;
import org.sonar.plugin.typescript.TypeScriptRulesDefinition;
import org.sonar.plugin.typescript.executable.ExecutableBundle;
import org.sonar.plugin.typescript.executable.ExecutableBundleFactory;
import org.sonar.plugin.typescript.executable.SonarTSCoreBundle;
import org.sonar.plugin.typescript.executable.Zip;
import org.sonar.plugin.typescript.rules.TypeScriptRule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(Parameterized.class)
public class RuleConfigurationTest {

  private static final Path RULE_LINT_FILES = Paths.get("../../../sonarts-core/tests/rules");
  private static final Pattern ERROR_COMMENT = Pattern.compile("//.*\\^+[^<>]*");

  @ClassRule
  public static TemporaryFolder temp = new TemporaryFolder();

  private static File baseDir;
  private SensorContextTester testContext;
  private static ExternalProcessStreamConsumer errorConsumer;
  private static ExecutableBundleFactory executableBundleFactory;
  private static FileLinesContextFactory fileLinesContextFactory;
  private static NoSonarFilter noSonarFilter;

  @Parameter
  public String ruleKey;

  @Parameter(1)
  public String lintFile;

  @Parameters(name = "{0}:{1}")
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][]{
      {"S3776", "cognitiveComplexityRule"},
      {"S4275", "noAccessorFieldMismatchRule"},
    });
  }

  @BeforeClass
  public static void setUp() throws Exception {
    baseDir = temp.newFolder();
    Tests.runNPMInstall(baseDir, "typescript", "--no-save");

    errorConsumer = new ExternalProcessStreamConsumer();
    errorConsumer.start();
    executableBundleFactory = RuleConfigurationTest::createAndDeploy;
    fileLinesContextFactory = (inputFile) -> mock(FileLinesContext.class);
    noSonarFilter = mock(NoSonarFilter.class);
  }

  private static ExecutableBundle createAndDeploy(File deployDestination, Configuration configuration) {
    try {
      File file = Tests.PLUGIN_LOCATION.getFile();
      JarFile jar = new JarFile(file);
      ZipEntry entry = jar.getEntry("sonarts-bundle.zip");
      InputStream inputStream = jar.getInputStream(entry);
      File workDir = temp.newFolder();
      Zip.extract(inputStream, workDir);
      return new SonarTSCoreBundle(new File(workDir, "sonarts-bundle"), configuration);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to extract bundle", e);
    }
  }

  @Test
  public void test() throws Exception {
    testContext = SensorContextTester.create(baseDir);
    testContext.fileSystem().setWorkDir(temp.newFolder().toPath());

    List<Integer> expectedLines = prepareTestFile(lintFile);
    CheckFactory checkFactory = getCheckFactory(ruleKey);
    ExternalTypescriptSensor sensor = new ExternalTypescriptSensor(executableBundleFactory, noSonarFilter, fileLinesContextFactory, checkFactory, errorConsumer);
    sensor.execute(testContext);

    assertThat(testContext.allIssues().stream().allMatch(i -> i.ruleKey().rule().equals(ruleKey))).isTrue();
    List<Integer> actualLines = testContext.allIssues().stream().map(i -> i.primaryLocation().textRange().start().line()).collect(Collectors.toList());
    assertThat(actualLines).isEqualTo(expectedLines);
  }

  private List<Integer> prepareTestFile(String tslintRule) throws IOException {
    Files.write(baseDir.toPath().resolve("tsconfig.json"), "{ \"include\": [\"**/*\"] }".getBytes(StandardCharsets.UTF_8));
    List<String> lines = Files.readAllLines(lintFile(tslintRule));
    List<Integer> expectedLines = IntStream.range(0, lines.size())
      .filter(i -> ERROR_COMMENT.matcher(lines.get(i)).matches())
      .boxed()
      .collect(Collectors.toList());
    testFile(lintFileName(tslintRule), lines.stream().collect(Collectors.joining("\n")));
    return expectedLines;
  }

  private Path lintFile(String tslintRule) {
    return RULE_LINT_FILES.resolve(tslintRule).resolve(lintFileName(tslintRule));
  }

  private String lintFileName(String tsLintRule) {
    return tsLintRule + ".lint.ts";
  }

  private void testFile(String filename, String content) throws IOException {
    File filePath = new File(baseDir, filename);
    Files.write(filePath.toPath(), content.getBytes(StandardCharsets.UTF_8));
    InputFile inputFile = TestInputFileBuilder.create("module", baseDir, filePath)
      .setLanguage(TypeScriptLanguage.KEY)
      .setContents(content)
      .build();
    testContext.fileSystem().add(inputFile);
  }

  private CheckFactory getCheckFactory(String activeRule) {
    List<Class<? extends TypeScriptRule>> ruleClasses = TypeScriptRules.getRuleClasses();
    List<String> allKeys = ruleClasses.stream().map(ruleClass -> ((org.sonar.check.Rule) ruleClass.getAnnotations()[0]).key()).collect(Collectors.toList());
    ActiveRulesBuilder rulesBuilder = new ActiveRulesBuilder();
    allKeys.forEach(key -> {
      NewActiveRule newActiveRule = rulesBuilder.create(RuleKey.of(TypeScriptRulesDefinition.REPOSITORY_KEY, key));
      if (activeRule.equals(key)) {
        newActiveRule.activate();
      }
    });
    ActiveRules activeRules = rulesBuilder.build();
    CheckFactory checkFactory = new CheckFactory(activeRules);
    Checks<TypeScriptRule> checks = checkFactory.create(TypeScriptRulesDefinition.REPOSITORY_KEY);
    checks.addAnnotatedChecks(ruleClasses);
    return checkFactory;
  }
}

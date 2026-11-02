package com.ecoguard.api.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "com.ecoguard", importOptions = ImportOption.DoNotIncludeTests.class)
class HexagonalArchitectureTest {
  @ArchTest
  static final ArchRule domainStaysFreeOfFrameworks =
      noClasses()
          .that()
          .resideInAPackage("com.ecoguard.domain..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "org.springframework..",
              "org.postgresql..",
              "com.fasterxml..",
              "com.ecoguard.application..",
              "com.ecoguard.ingestion..",
              "com.ecoguard.api..",
              "com.ecoguard.agent..");

  @ArchTest
  static final ArchRule applicationStaysInsideTheHexagon =
      noClasses()
          .that()
          .resideInAPackage("com.ecoguard.application..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "org.springframework..",
              "com.ecoguard.ingestion..",
              "com.ecoguard.api..",
              "com.ecoguard.agent..");

  @ArchTest
  static final ArchRule httpDoesNotCallCollectors =
      noClasses()
          .that()
          .resideInAPackage("com.ecoguard.api..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("com.ecoguard.ingestion..");

  @ArchTest
  static final ArchRule collectorsDoNotCallHttp =
      noClasses()
          .that()
          .resideInAPackage("com.ecoguard.ingestion..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("com.ecoguard.api..");

  @ArchTest
  static final ArchRule agentDoesNotOwnDelivery =
      noClasses()
          .that()
          .resideInAPackage("com.ecoguard.agent..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("com.ecoguard.api..", "com.ecoguard.ingestion..");
}

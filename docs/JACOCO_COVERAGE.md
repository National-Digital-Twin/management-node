# JaCoCo Code Coverage Setup

**Repository:** `management-node`  
**Description:** `Provides APIs to be accessed by Consumer and Producer Federators for the purpose of dynamic configuration management `  
**SPDX-License-Identifier:** `Apache-2.0 AND OGL-UK-3.0 `

---
## Overview

This document describes the JaCoCo code coverage setup for the Management Node application. JaCoCo has been configured to measure code coverage and ensure that it meets the specified thresholds.

## Current Configuration

JaCoCo has been configured in the `pom.xml` file with the following settings:

1. **Coverage Thresholds**: Currently set to 50% for:
   - Instructions
   - Branches
   - Lines
   - Methods
   - Classes

2. **Excluded Packages/Classes**:
   - DTOs (`**/dto/**`)
   - Entity classes (`**/entity/**`)
   - Configuration classes (`**/config/**`)
   - Exception classes (`**/exception/**`)
   - Main application class (`**/ManagementNodeApplication.java`)

3. **Build Configuration**:
   - Tests will run even if they fail (`testFailureIgnore=true` in maven-surefire-plugin)
   - Coverage checks will not fail the build if thresholds aren't met (`haltOnFailure=false`)

## Current Coverage Levels

As of the latest build, the coverage levels are:
- Branches: 0%
- Lines: 22%
- Methods: 33%

These are below the current thresholds of 50%, and significantly below the target of 80%.

## Running the Coverage Report

To generate the JaCoCo coverage report, run:

```bash
./mvnw clean verify
```

The report will be generated in the `target/site/jacoco` directory. Open `target/site/jacoco/index.html` in a web browser to view the detailed coverage report.

## Recommendations for Improving Coverage

To reach the target of 80% code coverage:

1. **Fix Failing Tests**: 
   - Address the NullPointerException in `ConsumerAllowedDataProviderServiceImplTest`
   - Ensure all existing tests pass

2. **Add More Tests**:
   - Focus on adding tests for uncovered branches
   - Increase method coverage by testing all public methods
   - Prioritize testing business logic and service implementations

3. **Gradual Threshold Increase**:
   - Once coverage improves, gradually increase thresholds in the JaCoCo configuration
   - Aim for incremental improvements: 50% → 60% → 70% → 80%

4. **Consider Additional Exclusions**:
   - If certain classes are not practical to test, consider adding them to the exclusions
   - Document the rationale for any exclusions

## Final Goal

The final goal is to achieve 80% code coverage across all metrics:
- 80% instruction coverage
- 80% branch coverage
- 80% line coverage
- 80% method coverage
- 80% class coverage

Once this goal is achieved, update the JaCoCo configuration to:
1. Set all thresholds to 80%
2. Set `haltOnFailure` to `true` to enforce the coverage requirements

## Best Practices

1. **Write Tests First**: Follow Test-Driven Development (TDD) principles
2. **Focus on Quality**: Aim for meaningful tests that verify behavior, not just increase coverage
3. **Regular Monitoring**: Check coverage reports regularly to identify areas needing improvement
4. **Integration with CI/CD**: Include coverage checks in your CI/CD pipeline
5. **Documentation**: Keep this document updated with changes to coverage configuration
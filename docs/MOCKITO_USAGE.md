# Mockito Testing Tool Usage Guide
**Repository:** `management-node`  
**Description:** `Provides APIs to be accessed by Consumer and Producer Federators for the purpose of dynamic configuration management `  
**SPDX-License-Identifier:** `Apache-2.0 AND OGL-UK-3.0 `

---
## Overview

Mockito is a popular mocking framework for Java that allows you to create and configure mock objects. Using Mockito, you can verify that certain methods are called with certain parameters, stub method calls to return specific values, and more.

This guide explains how Mockito has been integrated into the project and provides examples of how to use it for testing.

## Dependencies Added

The following dependencies have been added to the project's `pom.xml`:

```xml
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <version>5.10.0</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-junit-jupiter</artifactId>
    <version>5.10.0</version>
    <scope>test</scope>
</dependency>
```

## Basic Mockito Usage

### Setting Up Mockito in a Test Class

To use Mockito with JUnit 5, add the `@ExtendWith(MockitoExtension.class)` annotation to your test class:

```java
@ExtendWith(MockitoExtension.class)
class MyServiceTest {
    // Test methods
}
```

### Creating Mock Objects

Use the `@Mock` annotation to create mock objects:

```java
@Mock
private DependencyService dependencyService;
```

### Injecting Mocks

Use the `@InjectMocks` annotation to inject mock objects into the class under test:

```java
@InjectMocks
private MyService myService;
```

## Mockito Examples

### Stubbing Method Calls

```java
// Stub a method to return a specific value
when(dependencyService.getData()).thenReturn(expectedData);

// Stub a method with any argument of a specific type
when(dependencyService.processData(any(Data.class))).thenReturn(processedData);

// Stub a method with a specific argument
when(dependencyService.findById("1")).thenReturn(Optional.of(testData));

// Stub a method with a combination of specific and any arguments
when(dependencyService.updateData(eq("1"), any(Data.class))).thenReturn(Optional.of(updatedData));
```

### Verifying Method Calls

```java
// Verify that a method was called exactly once
verify(dependencyService, times(1)).getData();

// Verify that a method was called with a specific argument
verify(dependencyService, times(1)).findById("1");

// Verify that a method was called with a combination of specific and any arguments
verify(dependencyService, times(1)).updateData(eq("1"), any(Data.class));
```

## Advanced Mockito Features

Mockito offers many advanced features not covered in the examples:

1. **Argument Captors**: Capture arguments passed to methods for further verification
2. **Spies**: Create partial mocks that call real methods but can still be verified and stubbed
3. **Verification Modes**: Verify method calls with different modes like `atLeastOnce()`, `atMost(n)`, etc.
4. **Answer Interfaces**: Provide custom answers for stubbed methods
5. **Verification Timeouts**: Verify method calls with timeouts for concurrent code

For more information, refer to the [Mockito documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html).

## Best Practices

1. **Keep Tests Focused**: Each test should verify a single behavior
2. **Use Descriptive Test Names**: Test names should describe what they're testing
3. **Minimize Stubbing**: Only stub methods that are necessary for the test
4. **Verify Important Interactions**: Only verify method calls that are important for the test
5. **Use Argument Matchers Consistently**: If you use an argument matcher for one argument, you must use matchers for all arguments in that method call
6. **Reset Mocks When Necessary**: Use `reset(mock)` when you need to reset a mock's state between tests

## Troubleshooting

### Common Issues

1. **"Invalid use of argument matchers"**: If you use an argument matcher for one argument, you must use matchers for all arguments in that method call
2. **"Wanted but not invoked"**: The method you're verifying was not called with the specified arguments
3. **"Unnecessary stubbing"**: You stubbed a method that was not called during the test

### Solutions

1. Use `any()`, `eq()`, or other matchers consistently for all arguments
2. Check that the method is being called with the expected arguments
3. Remove unnecessary stubbing or add `lenient()` to the stubbing
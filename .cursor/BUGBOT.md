# Bugbot Review Rules

When reviewing code in this repository, always follow these rules:

## 1. Language

- All code reviews must be written **in Russian**.
- Use clear, concise, and professional language.

## 2. Context: Java and Kotlin Codebase

- This repository uses **Java and Kotlin**.
- All reviews must consider:
    - Best practices specific to Java and Kotlin.
    - JVM ecosystem conventions.
    - Idiomatic patterns for both languages.
    - Interoperability issues between Java and Kotlin where applicable.

## 3. General Review Focus

- Verify adherence to **industry best practices** for Java, Kotlin, and the JVM ecosystem.
- Check code **readability** (naming, structure, decomposition, clarity of intent).
- Check code **maintainability** (modularity, separation of concerns, well-defined responsibilities).
- Consider Kotlin-specific idioms (data classes, sealed classes, scope functions, null-safety, immutability).
- Consider Java-specific idioms (streams, try-with-resources, Optional, concurrency tools).

## 4. Resource Management (IO, Connections, Files, Streams)

Pay close attention to proper handling of all resources:

- Ensure that **files**, **database connections**, **network connections**, **sockets**, and other IO resources:
    - Are opened safely with proper error handling.
    - Are correctly **closed** in all execution paths (including exceptions).
    - In Java: prefer **try-with-resources**.
    - In Kotlin: prefer **use{}** or equivalent scoped constructs.
- Flag situations where:
    - Resources can remain open after errors.
    - Missing cleanup blocks (`finally`, `use`, etc.).
    - There is potential for deadlocks or blocked IO caused by unclosed streams or locks.

## 5. Security and Privacy

Identify potential **security vulnerabilities**:

- Detect injection risks (SQL, command, LDAP, etc.) and recommend safer alternatives.
- Check for unsafe or unvalidated input.
- Flag the presence of **hardcoded secrets**.
- Identify:
    - Insecure cryptographic choices.
    - Missing authentication/authorization checks.
    - Leakage of sensitive information via logs/errors.

## 6. Memory Management and Leaks (JVM Specific)

Look for potential **memory leaks** and inefficient memory usage:

- Detect improper use of long-lived collections, caches, static fields, or singletons.
- Identify unintended retention of objects in lambdas, coroutines, or inner classes.
- Identify unnecessary allocations, excessive copying, or inappropriate data structures.

## 7. Performance and Code Quality

Identify **non-optimal code** and opportunities for improvement:

- Look for inefficient algorithms or avoidable nested loops.
- Flag duplicated code and suggest extracting reusable components.
- Identify overly complex functions or classes.

### Pay special attention to data structures:
- Detect **non-optimal data structure choices** that may cause:
    - Excessive memory consumption.
    - Poor time complexity (e.g., O(n²) operations due to wrong container choice).
    - Inefficient lookups, insertions, or deletions.
- Examples of what to watch for:
    - Using `List` where `Set` or `Map` would provide faster access.
    - Using `LinkedList` where `ArrayList` is more appropriate (typical for JVM).
    - Creating large mutable structures where immutable alternatives would be lighter and safer.
    - Using heavy structures like `HashMap` or `ConcurrentHashMap` when simpler or smaller structures would suffice.
- For Kotlin:
    - Prefer idiomatic collections (`mutableListOf`, `mapOf`) and avoid unnecessary copies.
    - Watch for misuse of sequences vs. collections.

### Unused Code
- Highlight functions, classes, imports, variables, or modules that are never referenced.
- Recommend removing or refactoring unused elements to reduce complexity and improve maintainability.

## 8. Review Structure Requirements

The review must be **well-structured**, not a long unformatted text wall.

Use the following structure **in Russian**:

### 1. Краткое резюме
- 1–5 пунктов с общей оценкой: сильные стороны и основные проблемы.

### 2. Критические проблемы
- Ошибки, влияющие на безопасность, корректность, утечки ресурсов.
- Формат: проблема → где → конкретное решение.

### 3. Важные замечания
- Существенные, но не блокирующие проблемы: производительность, архитектура, поддерживаемость.
- Формат: проблема → где → улучшение.

### 4. Незначительные замечания и стиль
- Стиль, именование, форматирование, мелкие улучшения.

### 5. Предложения по улучшениям
- Идеи по улучшению архитектуры, тестирования, логирования, применения Java/Kotlin идиом.

## 9. Format and Level of Detail

- Use bullet points and subheadings; avoid long paragraphs.
- Focus on issues that matter; do not waste time on the trivial.
- Include short snippets only when necessary.
- Explicitly acknowledge areas that are implemented correctly.

## 10. Tone and Approach

- Be constructive, specific, and professional.
- Critique the code, not the author.
- Base recommendations on well-established Java/Kotlin best practices.
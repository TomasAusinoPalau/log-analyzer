# Log Analyzer

Log Analyzer is a command-line tool designed to process and analyze web server log files. It identifies user sessions, calculates various metrics, and generates a detailed report.

---

## Features

- **User Session Analysis**: Detects user sessions based on a 10-minute inactivity threshold.
- **Top User Insights**: Displays top users by page views with session details.
- **Parallel and non-blocking processing**: File reading and report generation are async and non blocking, providing efficiency and scalability.
---

## Table of Contents

1. [Getting Started](#getting-started)
2. [Usage](#usage)
3. [Output Example](#output-example)
4. [Test](#test)
5. [Core Architecture](#core-architecture)
6. [Priorities in the Design Process](#priorities-in-the-design-process)

---

## Getting Started

Follow these instructions to set up and use the Log Analyzer on your local machine.

### Prerequisites

- **Scala 2.13**
- **SBT (Scala Build Tool)**
- **Java 8 or newer**

### Installation
Non installation required, follow this steps:

1. Unzip the compressed folder.
2. Access the log-analyzer directory.
```
cd log-analyzer 
```

## Usage

1. Compile the project:
```
sbt compile
```

2. Run the analyzer with the path to your log files directory:
```
sbt "run /path/to/logs/directory"
```

## Output Example

An example report generated by the Log Analyzer:

Total unique users: 27  
Top users:

| id       | # pages | # sess | longest | shortest |
|----------|---------|--------|---------|----------|
| 71f28176 | 75      | 3      | 35      | 1        |
| 41f58122 | 65      | 4      | 60      | 10       |
| 58122233 | 44      | 2      | 121     | 3        |
| 43122543 | 40      | 4      | 50      | 4        |
| 52123456 | 33      | 3      | 100     | 1        |

## Test

1. Compile the project:
```
sbt compile
```

2. Run all tests of the project:
```
sbt test
```

## Core Architecture

This section outlines the key components of the project, focusing on their responsibilities and how they interact to achieve a modular and scalable design. Each component is designed with reusability and clarity in mind to ensure maintainable and efficient log processing.

### Components

- **Main**:  
  The main class of the project, responsible for orchestrating the overall workflow.

- **LogAnalyzerHelper**:  
  A trait that encapsulates core functionalities, such as reading log files, transforming logs into metrics, and printing user reports. It ensures modularity and reusability.

- **LogEntryHelper**:  
  A trait providing utility method for parsing raw log lines into structured `LogEntry` objects.

- **ScriptExecutionContext**:  
  Provides a custom thread pool for handling asynchronous operations efficiently, avoiding overloading the global execution context.

## Priorities in the Design Process

1. **Simplicity and Clarity**: The codebase was structured to prioritize readability and maintainability, ensuring that it is easy to understand.


2. **Asynchronous Processing**: Given the nature of log processing and potentially large data volumes, the system was designed to handle tasks asynchronously. 
This approach optimizes performance and ensures scalability for larger datasets without blocking the main thread.


3. **Modular Architecture**: Each component was designed with single responsibility principles, allowing individual modules to be tested and reused independently.


4. **Functional Paradigms**: The implementation leverages functional programming principles, such as immutability and higher-order functions, to ensure predictable and reliable behavior.


5. **Robust log parsing**: Regular expression ensures that only logs matching the expected format are processed, reducing the risk of invalid data propagation.


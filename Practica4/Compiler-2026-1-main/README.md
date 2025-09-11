# Compiler - Compilers Laboratory (0817)

This repository contains the source code for a compiler built incrementally throughout the semester for the Compilers Laboratory course at the Faculty of Sciences, UNAM. The project is developed entirely in Java and showcases the implementation of the core stages of the compilation process, from lexical analysis to intermediate code generation.

---

## Course Information

* **Course:** Compilers Laboratory (0817)
* **Institution:** Faculty of Sciences, UNAM
* **Semester:** `2026-1`

---

## Project Status

This project is built incrementally. The current status of the implemented phases is as follows:

* [ ] **Phase 1: Lexical Analysis (Scanner)** - Converts a stream of characters into a sequence of tokens.
* [ ] **Phase 2: Syntactic Analysis (Parser)** - Validates the structure and builds an Abstract Syntax Tree (AST).
* [ ] **Phase 3: Semantic Analysis** - Performs type and scope checking on the AST.
* [ ] **Phase 4: Intermediate Code Generation** - Translates the AST into three-address code.
* [ ] **Phase 5: Final Integration** - Integrates all phases into a functional compiler.

---

## 🛠️ Tech Stack

* **Main Language:** Java
* **Build Tool:** Maven
* **Testing:** JUnit
* **Version Control:** Git

---

## 📂 Project Structure

The source code is organized into packages that reflect the different phases and components of the compiler to maintain a clean and modular design.

```
com.compiler
├── Main.java           // Main entry point that orchestrates the phases
├── lexer/              // Components for the Lexical Analyzer (Scanner)
│   ├── Token.java
│   └── Lexer.java
├── parser/             // Components for the Syntactic Analyzer
│   └── Parser.java
├── ast/                // Classes for the Abstract Syntax Tree nodes
└── ...                 // Packages for future phases (semantic, codegen, etc.)
```

---

## How to Build and Run

### Prerequisites

* OpenJDK (version `24` or newer)
* Maven

### Building the Project

To compile the project and generate the executable `.jar` file, navigate to the project root and run:

```bash
mvn clean install
```

## ✒️ Authors

* **Adrián Martínez** - `adrian-mm-fc`
EASTER_EGG_URLS

# HtmlAnalyzer (Axur Internship Challenge)

## Requirements
- JDK 17
- No external libraries/frameworks
- Compile/run from command line

## How to compile
From the directory containing the source files:

javac HtmlAnalyzer.java

## How to run
After compiling:

java HtmlAnalyzer <url>

Example:

java HtmlAnalyzer http://hiring.axreng.com/internship/example1.html

## Output rules (as required by the challenge)
The program prints only one of:
- A single line with the deepest text found in the HTML
- malformed HTML
- URL connection error

## Notes
- Lines are trimmed (leading indentation and blank lines are ignored).
- Only paired tags are supported (no self-closing tags, no attributes), per the challenge assumptions.

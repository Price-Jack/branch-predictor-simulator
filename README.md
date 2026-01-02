````md
# Branch Predictor Simulator (Java)

A configurable branch predictor simulator that evaluates prediction accuracy from branch traces. Implements multiple classic predictors used in computer architecture coursework and research: **Smith**, **Bimodal**, **Gshare**, and **Hybrid**.

---

## Features
- Simulates multiple branch prediction strategies:
  - **Smith (n-bit counter)**
  - **Bimodal**
  - **Gshare**
  - **Hybrid** (chooser + gshare + bimodal)
- Runs from the command line with a consistent interface
- Prints final prediction statistics and predictor table contents (per typical simulator specs)
- Designed to work with standard branch trace formats

---

## Project Structure
```text
├── src
│   ├── sim.java
│   ├── SmithPredictor.java
│   ├── BimodalPredictor.java
│   ├── GSharePredictor.java
│   └── HybridPredictor.java
├── examples
│   └── mini.trace
├── Makefile
└── README.md
````

---

## Requirements

* Java JDK 8+ (11+ recommended)
* `make` (optional, but recommended)

Check your Java version:

```bash
java -version
```

---

## Build

Compile and generate the `./sim` launcher:

```bash
make
```

Clean build artifacts:

```bash
make clean
```

---

## Run

General format:

```bash
./sim <predictor> <params...> <trace_file>
```

Supported predictors (with parameters):

* Smith:

  ```bash
  ./sim smith <B> <trace_file>
  ```
* Bimodal:

  ```bash
  ./sim bimodal <M2> <trace_file>
  ```
* Gshare:

  ```bash
  ./sim gshare <M1> <N> <trace_file>
  ```
* Hybrid:

  ```bash
  ./sim hybrid <K> <M1> <N> <M2> <trace_file>
  ```

### Parameter Details

* `B`: number of bits for the Smith counter (e.g., 1–8)
* `M2`: number of PC index bits for the Bimodal table (table size = 2^M2)
* `M1`: number of PC index bits for the Gshare table (table size = 2^M1)
* `N`: number of Global History Register bits used by Gshare
* `K`: number of PC index bits for the Hybrid chooser table (chooser size = 2^K)
* `trace_file`: branch trace input file

---

## Quickstart demo (included mini trace)

```bash
make
./sim smith 4 examples/mini.trace
./sim bimodal 10 examples/mini.trace
./sim gshare 14 10 examples/mini.trace
./sim hybrid 8 14 10 10 examples/mini.trace
```

---

## Trace Format

Each line in the trace is:

```text
<hex_branch_pc> <t|n>
```

Where:

* `<hex_branch_pc>` is the branch program counter (hex)
* `t` = taken
* `n` = not taken

Example:

```text
00a3b5fc t
00a3b60c n
```

> Note: Large trace files are often not committed to public repos. This repo includes a tiny demo trace in `examples/mini.trace` for quick validation.

---

## Output

The simulator prints:

* predictor configuration summary (predictor type + parameters)
* total predictions and mispredictions
* misprediction rate (%)
* final predictor table contents (format may vary by predictor)

---

## Notes for a Public Repo

* Avoid committing large or non-redistributable traces. Use `examples/` for small synthetic demos.
* `.gitignore` should exclude Java build artifacts like `*.class`.

---

## Ideas for Future Improvements

* Add automated tests using small synthetic traces (golden expected outputs)
* Add CSV/JSON output mode for plotting and analysis
* Add support for additional predictors (e.g., perceptron, TAGE) or prefetching-style extensions

Keywords: branch prediction, gshare, bimodal, smith counter, hybrid predictor, computer architecture, simulation

```
::contentReference[oaicite:0]{index=0}
```

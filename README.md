# Branch Predictor Simulator (Java)

A command-line branch prediction simulator that evaluates several classic predictors on branch trace files and reports misprediction statistics.

## Implemented Predictors
- Smith n-bit counter predictor
- Bimodal predictor (2^M2 table of saturating counters)
- Gshare predictor (global history XOR with PC index)
- Hybrid predictor (chooser table selects between Bimodal and Gshare)

## Project Structure (Option B: files in repo root)
    .
    ├── sim.java
    ├── BranchPrediction.java
    ├── Makefile
    ├── README.md
    ├── .gitignore
    ├── traces/            (local only; not committed)
    └── validation_runs/   (local only; not committed)

## Requirements
- Java JDK 8+ (11+ recommended)
- make (recommended)

Check your Java version:
    java -version

## Build
Build (recommended):
    make

Clean:
    make clean

Alternative (without make):
    javac sim.java BranchPrediction.java

## Run
After `make`, run the program as `./sim` with one of the following formats:

Smith n-bit counter:
    ./sim smith <B> <tracefile>

Bimodal:
    ./sim bimodal <M2> <tracefile>

Gshare:
    ./sim gshare <M1> <N> <tracefile>

Hybrid:
    ./sim hybrid <K> <M1> <N> <M2> <tracefile>

### Parameter Notes
- B:  number of bits in the Smith counter
- M2: number of PC bits used to index the Bimodal table (table size = 2^M2)
- M1: number of PC bits used to index the Gshare table (table size = 2^M1)
- N:  number of global history bits (typically N <= M1)
- K:  number of PC bits used to index the chooser table (table size = 2^K)
- tracefile: path to the trace file (relative paths supported)

### Examples
    ./sim smith 4 traces/gcc_trace.txt
    ./sim bimodal 10 traces/gcc_trace.txt
    ./sim gshare 14 10 traces/gcc_trace.txt
    ./sim hybrid 8 14 10 10 traces/gcc_trace.txt

## Trace Format
Each line in a trace file is:
    <hex_branch_pc> t|n

Where:
- <hex_branch_pc> is the branch instruction address (hex)
- t means the branch was actually taken
- n means the branch was actually not taken

Example:
    00a3b5fc t
    00a3b60c n

## Output
After running, the simulator prints:
- total number of predictions (branches)
- total number of mispredictions
- misprediction rate (%)
- final contents of the relevant predictor tables (useful for validation/debugging)

## Local Data Folders (Not Committed)
This repo is set up to ignore these folders:
- traces/
- validation_runs/

Place your trace files under `traces/` locally and run commands using those paths.

## Validation Tip
To compare your output against an expected output file:
    diff -i -w my_output.txt expected_output.txt

## Keywords
computer architecture, branch prediction, smith counter, bimodal, gshare, hybrid predictor, saturating counters

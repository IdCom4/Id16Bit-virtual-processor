# Id16Bit Virtual Processor

A custom 16-bit virtual processor designed to execute binary programs with a simple instruction set.
It can be used to recreate a working computer ecosystem, like:
- a kernel
- a file system
- or whatever you may think of

---

## 📖 Overview
The way this processor is implemented is intended to be as **clear and straightforward** as possible to allow anyone to understand how it works and expand upon it.

I didn't put any focus towards optimisation as it was not my goal, but at some point I'll most likely come back to see what I can do to improve it while preserving the code clarity.


### Tools
There is a set of tools to facilitate the processor usage:
- [IdASM compiler](https://github.com/IdCom4/IdASM-compiler) - a dedicated custom assembly compiler to facilitate program writing
- [XMem compiler](https://github.com/IdCom4/Xmem-compiler) - a binary mem formater allowing easy binary file creation 
- [IdASM syntax highlight](https://github.com/IdCom4/IdASM-syntax-highlight-plugin-intellij) - intellij syntax highlight
- [XMem syntax highlight](https://github.com/IdCom4/XMem-syntax-highlight-intellij-plugin) - intellij syntax highlight

---

## 📂 Binary File Format

### Memory map

This processor, as all processors do, only reads binary.

The way you can inject binary in the processor is via a **memory map** file,
\
that describes a list of memory section that are available to the processor.


````json
[
  {
    "name": "ROM",
    "size": 4095,
    "readonly": true,
    "persistent": false,
    "stateFile": "ROM.mem"
  }
]
````

You can specify for each memory section:
- a name
- a size (amount of 16 bit addresses available)
- if this memory can be written to or only read
- if the state of the memory is to be saved between executions
- the state file that holds the memory content at startup, and to which to save when execution stop

> ⚠️ The state file must be in binary format, including a header:\
> - the first byte must be the byte encoding
> - the next 4 bytes must be the amount of value contained in the file
>   - amount of values = bytes / byte encoding

### Binary format

When the processor reads the binary loaded in its memory, \
it starts at address **0x19 (25)**, and will read 3 addresses to get the opcode, and it's 2 parameters, like so:

| ... | 0x18  | 0x19   | 0x21 | 0x22 | 0x23  | ... |
|------|-------|--------|------|------|-------|------|
| ... | 0x... | 0x8080 | 0x0002 | 0x0001 | 0x... | ... |

In this case, it would read:
- opcode = 0x8080
- param0 = 0x0002
- param1 = 0x0001

(which means a MOVE operation of the value 2 to the register 1)

It will then start reading the next instruction at the address 0x19 + 0x03 = 0x1c, and so on

Each memory value is 2 bytes (16 bits), in big-endian format.

---

## Instruction format

An instruction always comes in 3 16-bit parts stored at 3 consecutive addresses:

| OPCODE | PARAM0 | PARAM1 |
|--------|--------|--------|

The opcode is structured as follows: `instruction | flags`

And the params are just numbers, that depending on the instruction and the flags mean either:
- an address
- or a value

Here is the list of opcode flags and their description:

| FLAG MASK | FLAG DESCRIPTION                       |
|-----------|----------------------------------------|
| 0x8000    | use param0 as an immediate value       |
| 0x4000    | use param1 as an immediate value       |
| 0x2000    | use param0 as a dynamic memory address |
| 0x1000    | use param1 as a dynamic memory address |
| 0x0800    | use param0 as a pointer                |
| 0x0400    | use param1 as a pointer                |

And the instruction list with their opcode is listed below

---

## ⚙️ Instruction Set
The processor supports the following operations:

> ⚠️ The description of what is the exact source and destination \
> also depends on the flags.
> 
> By default, a param value is considered a static address, \
> meaning an address of one of the static memory components, like a register \
> But this behavior can be controlled with the opcode flags
> 
> ex: \
> `0x01 0x01 0x02` means ADD the content of register R1 and register R2 \
> `(0x01 | 0x8000) 0x01 0x02` means ADD the value 0x01 and the content of register R2

| Mnemonic | Opcode (hex) | Description                                                        |
|----------|--------------|--------------------------------------------------------------------|
| ADD      | 0x01         | Add `param0` and `param1`                                          |
| SUB      | 0x02         | Substract `param0` of `param1`                                     |
| DIV      | 0x03         | Divide `param0` by `param1`                                        |
| MUL      | 0x04         | Multiply `param0` by `param1`                                      |
| MOD      | 0x05         | Modulo `param0` by `param1`                                        |
| AND      | 0x06         | Bitwise AND `param0` & `param1`                                    |
| OR       | 0x07         | Bitwise OR `param0` \| `param1`                                    |
| XOR      | 0x08         | Bitwise XOR `param0` ^ `param1`                                    |
| LSHFT    | 0x09         | BITWISE left shift `param0` by `param1` amount of bits             |
| RSHFT    | 0x0a         | BITWISE right shift `param0` by `param1` amount of bits            |
| JMPE     | 0x10         | If `param0` is equal to `param1`, jump to address in R3            |
| JMPL     | 0x11         | If `param0` is less than `param1`, jump to address in R3           |
| JMPLE    | 0x12         | If `param0` is less or equal to `param1`, jump to address in R3    |
| JMPG     | 0x13         | If `param0` is greater than `param1`, jump to address in R3        |
| JMPGE    | 0x14         | If `param0` is greater or equal to `param1`, jump to address in R3 |
| MOVE     | 0x20         | Move `param0` to `param1`                                          |
| INTR     | 0x21         | Send an interrupt with `param0` as interrupt code                  |

---

## 🧮 Registers and Static Addresses
Here is the list of all static addresses:

| Name             | Static Address (hex) | Description                                                                                                                                                                 |
|------------------|----------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| R0               | 0x00                 | General purpose register 0                                                                                                                                                  |
| R1               | 0x01                 | General purpose register 1                                                                                                                                                  |
| R2               | 0x02                 | General purpose register 2                                                                                                                                                  |
| R3               | 0x03                 | General purpose register 3, but it use as source for the target address when jump condition is true                                                                         |
| ACCU0            | 0x04                 | Contains the result of any computation                                                                                                                                      |
| ACCU1            | 0x05                 | Contains the secondary result of any computation, like the remainder of a division                                                                                          |
| FLAGS            | 0x06                 | Contains flags representing some data about the computation result (see below)                                                                                              |
| SPTR             | 0x07                 | Contains the stack pointer                                                                                                                                                  |
| EXPTR            | 0x08                 | Contains the execution pointer                                                                                                                                              |
| MEM_EXTENSION    | 0x09                 | Contains the memory extension, it's value is used as the 16 high bits when addressing memory, to get 32 bits addresses                                                      |
| INTERRUPT_CODE   | 0x0a                 | Contains the code if an interrupt occurs                                                                                                                                    |
| STACK            | 0x0b                 | The stack                                                                                                                                                                   |
| IN               | 0x0c                 | Processor's input                                                                                                                                                           |
| OUT              | 0x0d                 | Processor's output                                                                                                                                                          |
| MEM_INTERRUPT    | 0x0e                 | The address at which the processor jumps if an interrupt occurs (this memory address should contain a jump to another location where the code for the handling actually is) |

> ⚠️ Here are the flags bits and their meaning: 
> 
> FLAGS: (low bits) \
> 0 0 0 0 0 0 0 0 \
> a b c d e f g h
> 
>         a. input0 == input1
>         b. input0 < input1
>         c. output0 == 0
>         d. output0 < 0
>         e. output0 > 0
>         f. output1 == 0
>         g. output1 < 0
>         h. output1 > 0

---

## ▶️ Usage
The **Id16Bit** supports several options:

- `--mmap=<mmap-file-path>` – the memmap file path
- `--delay=<value>` - the delay in milliseconds between instructions
- `--logs` – turns on the logs, detailing the execution steps and values
- `--help` - display help

You can run it from a configuration, or using the 2 helper scripts:
- `./build.sh` - builds the jar file (required only once or after code changes)
- `./run.sh` - run the application

````shell
$> ./build.sh
$> ./run.sh -h
````

---

## 🧪 Examples

The repo already contains:
- a memmap file (`./mmap.json`)
- and a binary mem file (`ROM.mem`)

This binary file contains the code for a small fibonacci program.

````shell
$> ./build.sh
$> ./run.sh --mmap=./mmap.json
````

---

## ✈️ Roadmap

- add flow control (code already implemented, missing interface and input handling)
  - pause execution
  - step by step execution
  - resume execution
- show current data state
  - see registers content
  - see dynamic memory content

---

## 📜 License
e.g. MIT
PROGRAM ANALYSIS - ASSIGNEMENT 4
@Author = Gursimran Singh
@RollNum = 2014041

The submitted .zip file contains two Eclipse Keplar projects
1. Assign_4_A
2. Assign_4_B

1. Assign_4_A
The first part of the Assignment - Intra Taint Analysis adapted for function calls. At any function call if the argument is tainted, the return value is assumed to be tainted.

To Run:
Either provide the testfile qualified name in the commandline arguments OR in Driver.java put the testfile names in the TESTFILES String array filed member.
Run Driver.java to begin Analysis.

2. Assign_4_B
The second part of Assignment - Inter Taint Analysis.
All class function summaries are calculated before analysis in reverse topological order. The main is analyzed using the function summaries.

To Run:
Provide a single testfile name either in commandline args OR in Driver.java put the testfile name in the TESTFILES String array filed member.
Run Driver.java to begin Analysis.

BONUS:
1. Attempted the assignment individually.
2. Inter Taint Analysis works for more than one parameter for methods.
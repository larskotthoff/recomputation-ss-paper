Recomputation Summer School Paper
======================

Repository for the paper produced by students at the recomputation summer school https://blogs.cs.st-andrews.ac.uk/emcsr2014/

The first draft of the paper itself has now been submitted to arxiv 
[in this pdf](https://github.com/larskotthoff/recomputation-ss-paper/blob/master/SummerSchoolPaper/Preprints/arxiv1.pdf?raw=true)

To build the paper you will need at least R and LaTeX, and other things such as make.

The paper source lives in directory SummerSchoolPaper.  
To clone the repository and build the paper try this:

    git clone https://github.com/larskotthoff/recomputation-ss-paper.git
    cd recomputation-ss-paper/SummerSchoolPaper
    make

Other forms of building the pdf may well work.

The make script uses latexmk which is not in all default TeX installations.  Also various R packages are necessary, and can be installed 
from within R.



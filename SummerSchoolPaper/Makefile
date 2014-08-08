# Makefile for EMCSR-2014 paper
# Started by Ian Gent 8 Aug 2014
#
# Requires R, pdflatex, mklatex

.SUFFIXES: .tex .pdf .Rnw .Snw

PDFS = paper.pdf

TEXS = paper.tex ack.tex group2.tex obstacles.tex paper-concordance.tex recomputation.tex discussion.tex group3.tex  school.tex group1.tex intro.tex

paper.pdf: $(TEXS)

all: $(PDFS)

.Snw.tex:
		R CMD Sweave $*.Snw

.Rnw.tex:
		R CMD Sweave $*.Rnw

.tex.pdf: 
		latexmk -pdf $*.tex


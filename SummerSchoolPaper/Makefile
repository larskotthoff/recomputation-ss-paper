# Makefile for EMCSR-2014 paper
# Started by Ian Gent 8 Aug 2014
#
# Requires R, pdflatex

.SUFFIXES: .pdf .Snw

PDFS = paper.pdf

all: $(PDFS)

.Snw.pdf: 
		R CMD Sweave --pdf $<


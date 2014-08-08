# Makefile for EMCSR-2014 paper
# Started by Ian Gent 8 Aug 2014
#
# Requires R, pdflatex, mklatex

.SUFFIXES: .tex .pdf .Rnw .Snw

PDFS = paper.pdf

SWEAVEFILES = ${shell find . -mindepth 1 -maxdepth 1 -name \*.Snw}
SWEAVETEXFILES = $(SWEAVE:.Snw=.tex)

TEXFILES = ${shell find . -mindepth 1 -maxdepth 1 -name \*.tex -not -name \*.Snw}

TEXS = $(SWEAVETEXFILES) $(TEXFILES)

# temporary files
CLEAN = $(SWEAVETEX) *.aux *.bbl *.blg *-concordance.tex *.fdb_latexmk *.log *.out *.synctex. gz

paper.pdf: $(TEXS)

all: $(PDFS)

.Snw.tex:
		R CMD Sweave $*.Snw

.Rnw.tex:
		R CMD Sweave $*.Rnw

.tex.pdf: 
		latexmk -pdf $*.tex

clean:
		rm -rf $(CLEAN)

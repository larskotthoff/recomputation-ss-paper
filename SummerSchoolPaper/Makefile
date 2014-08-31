# Makefile for EMCSR-2014 paper
# Started by Ian Gent 8 Aug 2014
#
# Requires R, pdflatex, mklatex

.SUFFIXES: .tex .pdf .Rnw .Snw

PDFS = paper.pdf

SWEAVEFILES = ${shell find . -mindepth 1 -maxdepth 1 -name \*.Snw}
SWEAVETEXFILES = $(SWEAVEFILES:.Snw=.tex)

TEXFILES = ${shell find . -mindepth 1 -maxdepth 1 -name \*.tex -not -name \*.Snw}
BIBFILES = ${shell find . -mindepth 1 -maxdepth 1 -name \*.bib}

TEXS = $(SWEAVETEXFILES) $(TEXFILES)

# temporary files
CLEAN = $(SWEAVETEXFILES) *.aux *.bbl *.blg *-concordance.tex *.fdb_latexmk *.log *.out *.synctex.gz group1-001.pdf

paper.pdf: $(SWEAVEFILES) $(SWEAVETEXFILES) $(TEXFILES) $(BIBFILES)

all: $(PDFS)

.Snw.tex:
		R CMD Sweave $*.Snw

.Rnw.tex:
		R CMD Sweave $*.Rnw

.tex.pdf: 
		touch paper-concordance.tex
		latexmk -pdf -pdflatex="pdflatex --shell-escape" $*.tex

clean:
		rm -rf $(CLEAN)

report: tex/report.tex
	xelatex $<

session1: tex/session1.tex
	xelatex $<

session2: tex/session2.tex
	xelatex $<

source: tex/source.tex
	xelatex $<

wireshark: tex/wireshark.tex
	xelatex $<

router: tex/router.tex
	xelatex $<

.PHONY: clean

clean: 
	rm -rf *.log *.aux *.xdv *.fls *.fdb_latexmk *.out *.toc *.gz *.bbl *.blg

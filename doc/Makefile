PANDOC_OPTS = --standalone --css=style.css --pdf-engine=weasyprint -V document-css=false -M lang=fr

dossier-lancement.pdf: dossier-lancement.md style.css
	pandoc dossier-lancement.md -o $@ $(PANDOC_OPTS)

.PHONY: dossier
dossier: dossier-lancement.pdf

# Emojis propis

Generador dels 119 emojis de l'app (SVG de 100 × 100 amb animacions CSS). Tots els personatges porten la barretina
calcada del dibuix de referència (`barretina_nova.py`).

```bash
cd disseny/emojis && python3 gen-emojis.py   # escriu emojis.html, la galeria
```

- `gen-emojis.py`: la primera versió de tots els emojis i la galeria.
- `emojis_v2.py` … `emojis_v6.py`: les correccions de cada ronda de valoració, per ordre (cada fitxer substitueix emojis del d'abans).
- La valoració de cada ronda està apuntada a `docs/decisions-pendents.md`.

Encara falta passar-los a l'app.
